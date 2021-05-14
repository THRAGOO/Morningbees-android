package com.jasen.kimjaeseung.morningbees.ui.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.architecture.BaseActivity
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.databinding.ActivitySigninBinding
import com.jasen.kimjaeseung.morningbees.invitebee.InviteBeeActivity
import com.jasen.kimjaeseung.morningbees.manager.GoogleLoginManager
import com.jasen.kimjaeseung.morningbees.manager.NaverLoginManager
import com.jasen.kimjaeseung.morningbees.ui.signup.SignUpActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity

class SignInActivity : BaseActivity(){

    // Properties

    private val binding by binding<ActivitySigninBinding>(R.layout.activity_signin)
    private val signInViewModel: SignInViewModel by viewModels()

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.signInStatusBarTheme)
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = signInViewModel

        signInViewModel.checkToken()

        getDynamicLink()
        observeLiveData()
    }

    private fun observeLiveData() {
        signInViewModel.signUpActivityChangeEvent.observe(this, Observer {
            gotoSignUpActivity()
        })

        signInViewModel.beforeJoinActivityChangeEvent.observe(this, Observer {
            gotoBeforeJoin()
        })

        signInViewModel.mainActivityChangeEvent.observe(this, Observer {
            gotoMainActivity()
        })

        signInViewModel.naverLoginActivityChangeEvent.observe(this, Observer {
            signInViewModel.mOAuthLoginModule.startOauthLoginActivity(this, NaverLoginManager)
        })

        signInViewModel.googleLoginActivityChangeEvent.observe(this, Observer {
            val signInIntent = signInViewModel.mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        initAnimation()
        Log.d(TAG, "onWindowFocusChanged")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> {
                GoogleLoginManager.getTask(data)
                signInViewModel.signInWithGoogle()
            }
        }
    }

    // Init Method

    private fun initAnimation() {
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.verticalBias = 0.55f
        binding.loginBeeImage.layoutParams = layoutParams
    }

    // Get Dynamic Link

    private fun getDynamicLink() {

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                pendingDynamicLinkData?.let {
                    val deepLink = pendingDynamicLinkData.link

                    val beeIdInLink = deepLink?.getQueryParameter("beeId").orEmpty()
                    val beeTitleInLink = deepLink?.getQueryParameter("beeTitle").orEmpty()

                    GlobalApp.prefsBeeInfo.beeId = Integer.parseInt(beeIdInLink)
                    GlobalApp.prefsBeeInfo.beeTitle = beeTitleInLink

                    startActivity(Intent(this, InviteBeeActivity::class.java))
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    // Change Activity

    private fun gotoMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun gotoSignUpActivity() {
        startActivity(
            Intent(this, SignUpActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun gotoBeforeJoin() {
        startActivity(
            Intent(this, BeforeJoinActivity::class.java)
        )
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}