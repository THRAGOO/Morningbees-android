package com.jasen.kimjaeseung.morningbees.manager

import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.AppResources
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState

object NaverLoginManager : OAuthLoginHandler() {

    private val appContext = AppResources.getContext()

    // apply()는 함수를 호출하는 객체를 이어지는 블록의 리시버로 전달하고, 객체 자체를 반환함
    // 리시버란, 바로 이어지는 블록 내에서 메서드 및 속성에 바로 접근할 수 있도록 할 객체
    // 따라서, 특정 객체를 생성하면서 함께 호출해야하는 초기화 코드가 있는 경우 사용함

    private val naverLoginInstance = OAuthLogin.getInstance().apply {
        init (
            appContext,
            AppResources.getStringResId(R.string.naver_oauth_client_id),
            AppResources.getStringResId(R.string.naver_oauth_client_secret),
            AppResources.getStringResId(R.string.naver_oauth_client_name)
        )
    }

    fun getNaverLoginInstance() : OAuthLogin {
        return naverLoginInstance
    }

    fun haveNeedNaverLogin() : Boolean {
        return when(naverLoginInstance.getState(appContext)){
            OAuthLoginState.OK -> false // don't have need to login
            else -> true // have need to login
        }
    }

    fun naverLogout(){
        naverLoginInstance?.logout(
            appContext
        )
    }

    override fun run(success: Boolean) {
        if (success) {
            val accessToken = naverLoginInstance.getAccessToken(
                appContext
            )
            val refreshToken = naverLoginInstance.getRefreshToken(
                appContext
            )
            val expiresAt = naverLoginInstance.getExpiresAt(
                appContext
            )
            val tokenType = naverLoginInstance.getTokenType(
                appContext
            )

            GlobalApp.prefs.accessToken = accessToken
            GlobalApp.prefs.provider = "naver"

        } else {
            val errorCode = naverLoginInstance.getLastErrorCode(
                appContext
            ).code
            val errorDesc = naverLoginInstance.getLastErrorDesc(
                appContext
            )
            Dlog().d("errorCode:$errorCode, errorDesc:$errorDesc")
        }
    }
}