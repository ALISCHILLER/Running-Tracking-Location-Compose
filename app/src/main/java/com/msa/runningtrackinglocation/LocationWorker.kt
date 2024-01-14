package com.msa.runningtrackinglocation

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.msa.runningtrackinglocation.util.Constant.WEATHER_RESPONSE
import com.msa.runningtrackinglocation.util.createForegroundInfo
import com.msa.runningtrackinglocation.util.getNotificationBuilder
import com.msa.runningtrackinglocation.util.showPiNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.catch
import timber.log.Timber


@HiltWorker
class LocationWorker @AssistedInject constructor(@Assisted val context:Context,
    @Assisted private val workerParameters: WorkerParameters
):CoroutineWorker(context,workerParameters){
    private val notificationBuilder = getNotificationBuilder()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PiLocationWorkerProviderEntryPoint {
        fun piLocationManager():PiLocationManager
    }


    override suspend fun doWork(): Result {
        Log.d("start", "doWork" )

        val entryPoint = EntryPointAccessors.fromApplication(context, PiLocationWorkerProviderEntryPoint::class.java)
        val piLocationManager = entryPoint.piLocationManager()

        val foregroundInfo =createForegroundInfo(notificationBuilder)
        setForeground(foregroundInfo)


        piLocationManager.locationUpdates(5).catch {
            Timber.e(it)
            Log.d("LocationWorker", "doWork: $it")
        }.collect {
            Log.d("LocationWorker", "doWork: ${it.latitude} ${ it.longitude}")
            Timber.d("Received the location: ${it.latitude} ${ it.longitude}")
            val message=" ${it.latitude} ,  ${it.longitude}"
            val messagelist = listOf( it.latitude.toString() , it.longitude.toString())
            context.showPiNotification(message.toString(), notificationBuilder)

            setProgressAsync(Data.Builder().putStringArray(WEATHER_RESPONSE, messagelist.toTypedArray()
            ).build())
            if (isStopped) {
                return@collect
            }
        }
        Timber.d("STOPPED")
        return Result.success()
    }


}