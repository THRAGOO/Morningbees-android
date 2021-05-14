package com.jasen.kimjaeseung.morningbees.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.ErrorCode
import com.jasen.kimjaeseung.morningbees.common.Output
import com.jasen.kimjaeseung.morningbees.common.TokenStatusCode
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

    private lateinit var _mGoogleSignInClient: GoogleSignInClient
    val mGoogleSignInClient = _mGoogleSignInClient

    private lateinit var _mOAuthLoginModule: OAuthLogin
    val mOAuthLoginModule = _mOAuthLoginModule

    private var invitedBeeId = GlobalApp.prefsBeeInfo.beeId

    private val mRepository: MorningBessRepository by lazy {
        MorningBessRepository()
    }

    private val _mainActivityChangeEvent = MutableLiveData<Unit>()
    val mainActivityChangeEvent: LiveData<Unit> = _mainActivityChangeEvent

    private val _signUpActivityChangeEvent = MutableLiveData<Unit>()
    val signUpActivityChangeEvent: LiveData<Unit> = _signUpActivityChangeEvent

    private val _beforeJoinActivityChangeEvent = MutableLiveData<Unit>()
    val beforeJoinActivityChangeEvent: LiveData<Unit> = _beforeJoinActivityChangeEvent

    private val _naverLoginActivityChangeEvent = MutableLiveData<Unit>()
    val naverLoginActivityChangeEvent: LiveData<Unit> = _naverLoginActivityChangeEvent

    private val _googleLoginActivityChangeEvent = MutableLiveData<Unit>()
    val googleLoginActivityChangeEvent: LiveData<Unit> = _googleLoginActivityChangeEvent

    private fun initSignInWithNaver() {
        _mOAuthLoginModule = NaverLoginManager.getNaverLoginInstance()
    }

    private fun initSignInWithGoogle() {
        _mGoogleSignInClient = GoogleLoginManager.getGoogleLoginInstance()
    }

    fun signInWithNaver() {
        initSignInWithNaver()

        if (NaverLoginManager.haveNeedNaverLogin()) { // 네이버 로그인이 되어있는지 확인
            _naverLoginActivityChangeEvent.value = Unit
        } else
            requestSignInApi()
    }

    fun signInWithGoogle() {
        initSignInWithGoogle()

        if (GoogleLoginManager.haveNeedGoogleLogin()) {
            _googleLoginActivityChangeEvent.value = Unit
        } else
            requestSignInApi()
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
                            requestMeApi()
                        }
                    }
                }

                is Output.Error -> showGenericError(signInResponse.error, SIGN_IN_API)

                is Output.NetworkError -> showNetworkError()
            }
        }
    }

    fun checkToken() {
        when (GlobalApp.prefs.socialAccessToken) {
            TokenStatusCode.HaveNotToken.token -> {
                initSignInWithGoogle()
                initSignInWithNaver()
            }

            else -> { // 토큰이 존재하면 자동 로그인 수행
                requestSignInApi()
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
                ErrorCode.ExpiredToken.errorCode -> {
                    requestRenewalTokenApi(typeOfApi)
                }

                ErrorCode.BadAccess.errorCode -> {

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