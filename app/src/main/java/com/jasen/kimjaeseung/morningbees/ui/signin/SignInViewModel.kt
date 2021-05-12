package com.jasen.kimjaeseung.morningbees.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.ErrorCode
import com.jasen.kimjaeseung.morningbees.common.Output
import com.jasen.kimjaeseung.morningbees.data.MorningBessRepository
import com.jasen.kimjaeseung.morningbees.manager.GoogleLoginManager
import com.jasen.kimjaeseung.morningbees.manager.NaverLoginManager
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.SignInRequest
import com.nhn.android.naverlogin.OAuthLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignInViewModel : ViewModel() {
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mOAuthLoginModule: OAuthLogin

    private var invitedBeeId = 0

    private val mRepository: MorningBessRepository by lazy {
        MorningBessRepository()
    }

    private val _mainActivityChangeEvent = MutableLiveData<Unit>()
    val mainActivityChangeEvent : LiveData<Unit> = _mainActivityChangeEvent

    private val _signUpActivityChangeEvent = MutableLiveData<Unit>()
    val signUpActivityChangeEvent : LiveData<Unit> = _signUpActivityChangeEvent

    private val _beforeJoinActivityChangeEvent = MutableLiveData<Unit>()
    val beforeJoinActivityChangeEvent : LiveData<Unit> = _beforeJoinActivityChangeEvent

    private fun initSignInWithNaver() {
        mOAuthLoginModule = NaverLoginManager.getNaverLoginInstance()
    }

    private fun initSignInWithGoogle() {
        mGoogleSignInClient = GoogleLoginManager.getGoogleLoginInstance()
    }

    fun signInWithNaver() {
        // 토큰 확인!
        initSignInWithNaver()

        if (NaverLoginManager.haveNeedNaverLogin()){

        } else {
            mOAuthLoginModule.startOauthLoginActivity(this, NaverLoginManager)
        }
    }

    fun signInWithGoogle() {
        // 토큰 확인!
        initSignInWithGoogle()



    }

    private fun signOut() {
        GoogleLoginManager.googleLogout()
        NaverLoginManager.naverLogout()

        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""
        GlobalApp.prefsBeeInfo.beeId = 0
    }

    private fun requestSignInApi() {

        val signInRequest = SignInRequest(
            GlobalApp.prefs.socialAccessToken,
            GlobalApp.prefs.provider
        )

        scope.launch {
            when (val signInResponse = mRepository.requestSignInApi(signInRequest)) {
                is Output.Success -> {
                    when (signInResponse.output.type) {
                        SIGN_UP_TYPE -> {
                            _signUpActivityChangeEvent.value = Unit
                        }

                        SIGN_IN_TYPE -> {
                            GlobalApp.prefs.accessToken = signInResponse.output.accessToken
                            GlobalApp.prefs.refreshToken = signInResponse.output.refreshToken

                            // request me api
                        }
                    }
                }

                is Output.Error -> showGenericError(signInResponse.error, SIGN_IN_API)

                is Output.NetworkError -> showNetworkError()
            }
        }
    }

    private fun requestJoinBeeApi() {

        val joinBeeRequest = JoinBeeRequest(
            GlobalApp.prefsBeeInfo.beeId,
            GlobalApp.prefs.userId,
            GlobalApp.prefsBeeInfo.beeTitle
        )

        scope.launch {
            when (val joinBeeResponse = mRepository.requestJoinBeeApi(joinBeeRequest)) {
                is Output.Success -> {
                    _mainActivityChangeEvent.value = Unit
                }

                is Output.Error -> showGenericError(joinBeeResponse.error, JOIN_BEE_API)
                is Output.NetworkError -> showNetworkError()
            }
        }
    }

    private fun requestMeApi() {
        scope.launch {
            when (val meResponse = mRepository.requestMeApi()) {
                is Output.Success -> {
                    if (meResponse.output.alreadyJoin) {
                        GlobalApp.prefsBeeInfo.beeId = meResponse.output.beeId
                        _mainActivityChangeEvent.value = Unit
                    } else {
                        when (invitedBeeId) {
                            0 -> { // enum 클래스 쓰면 더 좋을듯
                                _beforeJoinActivityChangeEvent.value = Unit
                            }
                            else -> {
                                // request join bee API
                                GlobalApp.prefs.userId = meResponse.output.userId

                            }
                        }
                    }
                }

                is Output.Error -> showGenericError(meResponse.error, ME_API)
                is Output.NetworkError -> showNetworkError()
            }
        }
    }

    fun refreshIdToken() {
        GoogleLoginManager.refreshIdToken()
    }

    private fun requestRenewalTokenApi(typeOfApi: Int) {
        scope.launch {
            when (val renewalResponse = mRepository.requestRenewalApi()) {
                is Output.Success -> {
                    GlobalApp.prefs.accessToken = renewalResponse.output.accessToken

                    when (typeOfApi) {
                        SIGN_IN_API -> requestSignInApi()
                        ME_API -> requestMeApi()
                        JOIN_BEE_API -> requestJoinBeeApi()
                    }
                }

                is Output.NetworkError -> showNetworkError()
                is Output.Error -> signOut()
            }
        }
    }

    private fun showGenericError(errorResponse: ErrorResponse?, typeOfApi: Int) {
        errorResponse?.let {
            when (it.code) {
                ErrorCode.expiredToken.errorCode -> {
                    requestRenewalTokenApi(typeOfApi)
                }

                ErrorCode.badAccess.errorCode -> {

                }
            }
        }
    }

    private fun showNetworkError() {
        // error page 호출
    }

    companion object {
        const val SIGN_UP_TYPE = 0
        const val SIGN_IN_TYPE = 1

        const val SIGN_IN_API = 2
        const val RENEWAL_API = 3
        const val JOIN_BEE_API = 4
        const val ME_API = 5
    }
}