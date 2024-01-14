package com.msa.runningtrackinglocation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.msa.runningtrackinglocation.util.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private var _uiState = MutableStateFlow(listOf<String>())
    val uiState: StateFlow<List<String>> = _uiState

    val workManager = WorkManager.getInstance(context)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<LocationWorker>()
        .setConstraints(constraints).addTag(Constant.LOCATION_COROUTINE_WORKER)
        .build()


    fun StartCoroutineWorker() {
        workManager.enqueue(workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever {
            val response = it.progress.getStringArray(Constant.WEATHER_RESPONSE)
            response?.let { strResponse ->
                Log.d("MainViewModel", "StartCoroutineWorker: $strResponse.")
                _uiState.value = strResponse.toList()
            }
        }
    }


    fun getLoction() {
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever {
            val response = it.progress.getStringArray(Constant.WEATHER_RESPONSE)
            response?.let { strResponse ->
                Log.d("MainViewModel", "StartCoroutineWorker: $strResponse.")
                _uiState.value = strResponse.toList()
            }
        }
    }

    fun StopCoroutineWorker() {
        workManager.cancelAllWorkByTag(Constant.LOCATION_COROUTINE_WORKER)
    }
}