package com.jasen.kimjaeseung.morningbees.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.mvp.BasePresenter
import com.jasen.kimjaeseung.morningbees.mvp.BaseView

interface LoginContract {
    interface View : BaseView {
        fun initNaverSignIn()
        fun initGoogleSignIn()
        fun refreshIdToken()
        fun signOut()
        fun googleSignIn()
        fun naverSignIn()
        fun handleSignInResult(completedTask: Task<GoogleSignInAccount>)
        fun signInMorningbeesServer(socialAccessToken:HashMap<String,String>,provider:HashMap<String,String>)
    }

    interface Presenter : BasePresenter<View> {


    }
}