package com.jasen.kimjaeseung.morningbees.manager

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.AppResources
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.SignInRequest
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.data.OAuthLoginState

object GoogleLoginManager {

    private val appContext = AppResources.getContext()

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(AppResources.getStringResId(R.string.google_server_client_id))
        .requestEmail()
        .build()


    private val googleLoginInstance = GoogleSignIn.getClient(appContext, gso)

    fun getTask(data : Intent?){
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            // Google Sign In was successful
            val account = task.getResult(ApiException::class.java)!!
            Dlog().d(account.displayName!!)
            handleSignInResult(task)
        } catch (e: ApiException) {
            Dlog().w("Google sign in failed: " + e)
        }
    }

    fun refreshIdToken() {
        googleLoginInstance.silentSignIn()
            .addOnCompleteListener{ task -> handleSignInResult(task)}
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account!!.idToken

            GlobalApp.prefs.socialAccessToken = idToken.toString()
            GlobalApp.prefs.provider = AppResources.getStringResId(R.string.google)

        } catch (e: ApiException) {
            Dlog().w("handleSignInResult:error $e")
        }
    }

    fun getGoogleLoginInstance() : GoogleSignInClient {
        return googleLoginInstance
    }

    fun googleLogout() {
        googleLoginInstance.signOut().addOnCompleteListener {
            Dlog().d("Google Sign Out")
        }
    }

    fun getGoogleSignInIntent() : Intent {
        return googleLoginInstance.signInIntent
    }
}