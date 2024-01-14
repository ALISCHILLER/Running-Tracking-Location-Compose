package com.msa.runningtrackinglocation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class RunningApp :  Application(),  Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun onCreate() {
        super.onCreate()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            val channel = NotificationChannel(
//                "running_channel",
//                "Running Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
//                    as NotificationManager
//
//            notificationManager.createNotificationChannel(channel)
//        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()


}