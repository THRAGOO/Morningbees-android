package com.jasen.kimjaeseung.morningbees.ui.main

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.ErrorCode
import com.jasen.kimjaeseung.morningbees.common.Output
import com.jasen.kimjaeseung.morningbees.data.MorningBessRepository
import com.jasen.kimjaeseung.morningbees.manager.GoogleLoginManager
import com.jasen.kimjaeseung.morningbees.manager.NaverLoginManager
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.MainResponse
import com.jasen.kimjaeseung.morningbees.setting.SettingActivity
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
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

    private val _mainMissionsLiveData = MutableLiveData<JsonArray?>()
    val mainMissionsLiveData = _mainMissionsLiveData

    private val _mainBeeInfoLiveData = MutableLiveData<JsonObject>()
    val mainBeeInfoLiveData = _mainBeeInfoLiveData

    private val _missionCreateButton =  MutableLiveData<Unit>()
    val missionCreateButton = _missionCreateButton

    private val _settingButton =  MutableLiveData<Unit>()
    val settingButton = _settingButton

    private val _royalButton =  MutableLiveData<Unit>()
    val royalButton = _royalButton

    private val _targetDateButton =  MutableLiveData<Unit>()
    val targetDateButton = _targetDateButton

    private val _signOutEvent =  MutableLiveData<Unit>()
    val signOutEvent = _signOutEvent

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
                is Output.Success -> showMainAPISuccess(mainResponse.output)
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
                is Output.Error -> signOut()
            }
        }
    }

    private fun showGenericError(errorResponse: ErrorResponse?, typeOfApi : Int){
        errorResponse?.let {
            when (it.code){
                ErrorCode.ExpiredToken.errorCode -> {
                    requestRenewalTokenApi(typeOfApi)
                }

                ErrorCode.BadAccess.errorCode -> {

                }
            }
        }
    }

    private fun signOut(){
        NaverLoginManager.naverLogout()
        GoogleLoginManager.googleLogout()

        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""
        GlobalApp.prefsBeeInfo.beeId = 0

        _signOutEvent.value = Unit
    }

    private fun showMainAPISuccess(mainResponse: MainResponse){
        _mainMissionsLiveData.postValue(mainResponse.missions)
        _mainBeeInfoLiveData.postValue(mainResponse.beeInfo)

        updateUI()
        // beeInfo 객체를 직렬화해서 객체 만들까 생각 중
        GlobalApp.prefsBeeInfo.beeManagerNickname = mainResponse.beeInfo.get("manager").asJsonObject.get("nickname").asString
    }

    private fun updateUI() {

    }

    fun checkAccessToken(){
        scope.launch {
            mRepository.requestRenewalApi()
        }
    }

    fun clickMissionCreateButton(){
        _missionCreateButton.value = Unit
    }

    fun clickSettingButton(){
        _settingButton.value = Unit
    }

    fun clickRoyalJellyButton(){
        _royalButton.value = Unit
    }

    fun clickTargetDateButton(){
        _targetDateButton.value = Unit
    }

    companion object {
        const val ME_API = 1
        const val MAIN_API = 2
        const val RENEWAL_API = 3
    }
}