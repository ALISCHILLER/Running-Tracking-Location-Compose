package com.msa.runningtrackinglocation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.msa.runningtrackinglocation.util.Constant.WEATHER_RESPONSE
import com.msa.runningtrackinglocation.util.createForegroundInfo
import com.msa.runningtrackinglocation.util.getNotificationBuilder
import com.msa.runningtrackinglocation.util.isNetworkOrGPSEnabled
import com.msa.runningtrackinglocation.util.showPiNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.Executor


@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    private val notificationBuilder = getNotificationBuilder()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PiLocationWorkerProviderEntryPoint {
        fun piLocationManager(): PiLocationManager
    }

    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 1L
    }

    override suspend fun doWork(): Result {
        try {
            if (!context.isNetworkOrGPSEnabled()) {
                Log.d("LocationWorker", "isNetworkOrGPSEnabled")
            }
            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)
            delay(delayDuration)
            setProgress(lastUpdate)
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                PiLocationWorkerProviderEntryPoint::class.java
            )
            val piLocationManager = entryPoint.piLocationManager()
            val foregroundInfo = createForegroundInfo(notificationBuilder)
            setForeground(foregroundInfo)
            piLocationManager.locationUpdates(5).catch {
                Timber.e(it)
                Log.d("LocationWorker", "doWork: $it")
            }.collect {
                Log.d("LocationWorker", "doWork: ${it.latitude} ${it.longitude}")
                Timber.d("Received the location: ${it.latitude} ${it.longitude}")
                val message = " ${it.latitude} ,  ${it.longitude}"
                val messagelist = listOf(it.latitude.toString(), it.longitude.toString())
                context.showPiNotification(message.toString(), notificationBuilder)

                setProgressAsync(
                    Data.Builder().putStringArray(
                        WEATHER_RESPONSE, messagelist.toTypedArray()
                    ).build()
                )
                if (isStopped) {
                    return@collect
                }
            }
        } catch (e: SecurityException) {
            // Handle the SecurityException when accessing location services
            Log.e("LocationWorker", "SecurityException: ${e.message}")
            // Optionally, you can trigger an appropriate action or retry mechanism
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e("LocationWorker", "Exception: ${e.message}")
            // Optionally, you can trigger an appropriate action or retry mechanism
        }
        Timber.d("STOPPED")
        return Result.success()


    }

}