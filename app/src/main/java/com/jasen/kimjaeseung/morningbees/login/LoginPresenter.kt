package com.jasen.kimjaeseung.morningbees.login

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState

class LoginPresenter : LoginContract.Presenter {
    private var loginView: LoginContract.View? = null

    override fun takeView(view: LoginContract.View) {
        loginView = view
    }


    override fun dropView() {
        loginView = null
    }

}