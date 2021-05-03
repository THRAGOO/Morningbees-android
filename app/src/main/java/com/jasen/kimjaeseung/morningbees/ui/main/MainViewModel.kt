package com.jasen.kimjaeseung.morningbees.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.data.MorningBessRepository
import com.jasen.kimjaeseung.morningbees.model.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.model.MainResponse
import com.jasen.kimjaeseung.morningbees.model.Mission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import kotlin.coroutines.CoroutineContext

class MainViewModel : ViewModel() {
    private val parentJob = Job()
    private val coroutineContext : CoroutineContext get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    private val mRepository: MorningBessRepository by lazy {
        MorningBessRepository()
    }

    val mainMissionsLiveData = MutableLiveData<JsonArray>()
    val mainBeeInfoLiveData = MutableLiveData<JsonObject>()

    fun requestApi(targetDate : String) {
        scope.launch {
            mRepository.requestMeApi()
            val mainResponse = mRepository.requestMainApi(targetDate)

            // LiveData Update
            mainResponse?.let {
                mainMissionsLiveData.postValue(mainResponse.missions)
                mainBeeInfoLiveData.postValue(mainResponse.beeInfo)
            }
        }
    }

    fun checkAccessToken(){
        scope.launch {
            mRepository.checkAccessToken()
        }
    }
}