package com.jasen.kimjaeseung.morningbees.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.ErrorCode
import com.jasen.kimjaeseung.morningbees.common.Output
import com.jasen.kimjaeseung.morningbees.data.MorningBessRepository
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.MainResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainViewModel : ViewModel() {
    private val parentJob = Job()
    private val coroutineContext : CoroutineContext get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    private val mRepository: MorningBessRepository by lazy {
        MorningBessRepository()
    }

    val mainMissionsLiveData = MutableLiveData<JsonArray?>()
    val mainBeeInfoLiveData = MutableLiveData<JsonObject>()

    private var mTargetDate = ""

    // scope 개념 다시 공부
    fun requestApi(targetDate : String) {
        mTargetDate = targetDate
        scope.launch {
            requestMeApi()
            requestMainApi()
        }
    }

    private fun requestMeApi(){
        scope.launch {
            when (val meResponse = mRepository.requestMeApi()){
                is Output.Success -> {
                    GlobalApp.prefsBeeInfo.beeId = meResponse.output.beeId
                    GlobalApp.prefsBeeInfo.myNickname = meResponse.output.nickname
                    GlobalApp.prefsBeeInfo.myEmail = meResponse.output.email
                }

                is Output.Error -> showGenericError(meResponse.error, ME_API)
                is Output.NetworkError -> showNetworkError()
            }
        }
    }

    private fun requestMainApi(){
        scope.launch {
            when (val mainResponse = mRepository.requestMainApi(mTargetDate)) {
                is Output.Success -> showSuccess(mainResponse.output)
                is Output.NetworkError -> showNetworkError()
                is Output.Error -> showGenericError(mainResponse.error, MAIN_API)
            }
        }
    }

    private fun showNetworkError(){
        // error page 호출
    }

    // suspend 등 코루틴 용어 수정 필요
    private fun requestRenewalTokenApi(typeOfApi : Int){
        scope.launch {
            when (val renewalResponse = mRepository.requestRenewalApi()){
                is Output.Success -> {
                    when (typeOfApi) {
                        ME_API -> requestMeApi()
                        MAIN_API -> requestMainApi()
                    }
                }

                is Output.NetworkError -> showNetworkError()
                is Output.Error -> showGenericError(renewalResponse.error, RENEWAL_API)
            }
        }
    }

    private fun showGenericError(errorResponse: ErrorResponse?, typeOfApi : Int){
        errorResponse?.let {
            when (it.code){
                ErrorCode.expiredToken.errorCode -> {
                    requestRenewalTokenApi(typeOfApi)
                }

                ErrorCode.badAccess.errorCode -> {

                }
            }
        }

    }

    private fun showSuccess(mainResponse: MainResponse){
        mainMissionsLiveData.postValue(mainResponse.missions)
        mainBeeInfoLiveData.postValue(mainResponse.beeInfo)

        // beeInfo 객체를 직렬화해서 객체 만들까 생각 중
        GlobalApp.prefsBeeInfo.beeManagerNickname = mainResponse.beeInfo.get("manager").asJsonObject.get("nickname").asString
    }

    fun checkAccessToken(){
        scope.launch {
            mRepository.requestRenewalApi()
        }
    }

    companion object {
        const val ME_API = 1
        const val MAIN_API = 2
        const val RENEWAL_API = 3
    }
}