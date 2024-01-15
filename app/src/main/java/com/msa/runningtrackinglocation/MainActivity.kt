package com.msa.runningtrackinglocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.msa.runningtrackinglocation.ui.theme.RunningTrackingLocationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var piLocationManager: PiLocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,

                ),
                0
            )
        }
        piLocationManager.setActivity(this)
        setContent {
            RunningTrackingLocationTheme {
                // A surface container using the 'background' color from the theme

                // دریافت ViewModel با استفاده از hiltViewModel
                val viewModel: MainViewModel = hiltViewModel()

                  //  viewModel.StartCoroutineWorker()

                // دریافت وضعیت uiState به عنوان یک State در Compose
                val uiState by viewModel.uiState.collectAsState()

                // دریافت لیست از uiState
                val latList = uiState
                val currentLocation = if (latList.size >= 2) {
                    LatLng(latList[0].toDouble(), latList[1].toDouble())
                } else {
                    LatLng(32.4279, 53.6880) // یا هر مقدار پیش‌فرضی که برای LatLng مد نظر شماست
                }

                // CameraPositionState
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                }



                // DisposableEffect برای به‌روزرسانی CameraPosition
                DisposableEffect(currentLocation) {
                    onDispose {
                        // هنگامی که کامپوننت دارای‌شمارنده از دست می‌رود، می‌توانید مکان جاری را به‌روز کنید.
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                    }
                }

                // متغیر برای نگه‌داری موقعیت مارکر
                var markerPosition by remember { mutableStateOf(currentLocation) }
                // DisposableEffect برای به‌روزرسانی موقعیت مارکر
                DisposableEffect(currentLocation) {
                    markerPosition = currentLocation
                    onDispose {
                        markerPosition = LatLng(0.0, 0.0) // تنظیم موقعیت به مقدار پیش‌فرض
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Button(
                        onClick = {
                            viewModel.StartCoroutineWorker()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Start run")
                    }
                    Button(
                        onClick = {
                            viewModel.StopCoroutineWorker()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Stop run")
                    }


                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        cameraPositionState = cameraPositionState
                    ) {
                        // نشان دادن مارکر مرتبط با مکان فعلی در GoogleMap
                        Marker(
                            state = MarkerState(markerPosition),
                            title = "Current Location",
                            snippet = "${currentLocation.latitude} ,${currentLocation.longitude}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        )
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RunningTrackingLocationTheme {

    }
}
