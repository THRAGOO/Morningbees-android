package com.jasen.kimjaeseung.morningbees.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Button Listeners
        login_google_sign_in_button.setOnClickListener(this)
        login_google_sign_out_button.setOnClickListener(this)

        initGoogleSignIn()
    }

    private fun initGoogleSignIn() {

        // configure Google Sign-in and the GoogleSignInClient object
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // check existing user
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account!=null) Log.d(TAG,"already ${account.displayName}")
        //updateUI(acoount)

    }

    private fun googleSignIn(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun googleSignOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this){
            Log.d(TAG,"Sign Out")
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.login_google_sign_in_button -> googleSignIn()
            R.id.login_google_sign_out_button -> googleSignOut()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Google Sign In was successful
                val account = task.getResult(ApiException::class.java)

                Log.d(TAG,account!!.displayName)


            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}