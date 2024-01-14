package com.msa.runningtrackinglocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.msa.runningtrackinglocation.util.checkLocationPermission
import com.msa.runningtrackinglocation.util.isNetworkOrGPSEnabled
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class PiLocationException (message:String):Exception()


@Singleton
class PiLocationManager @Inject constructor(@ApplicationContext val context: Context) {


    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun locationUpdates(intervalInMillis: Long): Flow<Location> {
        return callbackFlow {
            if (!context.checkLocationPermission()) {
                throw PiLocationException(context.getString(R.string.missing_location_permission))
            }
            if (!context.isNetworkOrGPSEnabled()) {
                throw PiLocationException(context.getString(R.string.network_or_gps_is_not_available))
            }

            // make the request
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalInMillis).setWaitForAccurateLocation(false).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let {location: Location ->
                        launch {
                            Timber.d("Sending the location")
                            send(location)
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            awaitClose {
                Log.d("fusedLocationClient", "PiLocationManager:")
                Timber.d("Producer coroutine is above to close")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

        }

    }

}