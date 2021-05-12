package com.jasen.kimjaeseung.morningbees.ui.signin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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
import com.jasen.kimjaeseung.morningbees.model.SignInRequest
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.signup.SignUpActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.data.OAuthLoginState

class SignInActivity : BaseActivity(), View.OnClickListener {

    // Properties
//
//    private lateinit var signInPresenter: SignInPresenter
//
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mOAuthLoginModule: OAuthLogin //naver sign in module

    var translationY = 0.0f

    private val binding by binding<ActivitySigninBinding>(R.layout.activity_signin)
    private val signInViewModel: SignInViewModel by viewModels()

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.signInStatusBarTheme)
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = signInViewModel

        // check token -> 있으면 자동로그인지, 없으면 signin 화면 유지

//        signInPresenter.takeView(this)
//        initGoogleSignIn()
//        initNaverSignIn()
//        initButtonListeners()

        when {
            intent.hasExtra("RequestLogOut") -> {

            }
            intent.hasExtra("RequestSignIn") -> {
                // 자동 로그인 & sign out 둘 다 X
            }
            intent.hasExtra("RequestJoin") -> {
                refreshIdToken()
                setAutoLogin()
            }
            else -> {
                refreshIdToken()
                setAutoLogin()
            }
        }
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
    }

//    override fun onResume() {
//        super.onResume()
//        initAnimation()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
////        signInPresenter.dropView()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        loginBeeImage.animate().translationY(translationY)
//    }

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
            }

            RC_GET_TOKEN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    // Init Method

//    override fun initPresenter() {
//        signInPresenter =
//            SignInPresenter()
//    }

//    private fun initButtonListeners() {
//        login_google_sign_in_button.setOnClickListener(this)
//        login_naver_sign_in_button.setOnClickListener(this)
//    }

    private fun initAnimation() {
        val displayMetrics: DisplayMetrics
        val heightPixel: Int
        val widthPixel: Int
        val density: Float

        if (GlobalApp.prefsDeviceInfo.density == 0f || GlobalApp.prefsDeviceInfo.heightPixel == 0 || GlobalApp.prefsDeviceInfo.widthPixel == 0) {
            displayMetrics = DisplayMetrics()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display!!.getRealMetrics(displayMetrics)
            } else {
                windowManager.defaultDisplay.getMetrics(displayMetrics)
            }

            GlobalApp.prefsDeviceInfo.heightPixel = displayMetrics.heightPixels
            GlobalApp.prefsDeviceInfo.widthPixel = displayMetrics.widthPixels
            GlobalApp.prefsDeviceInfo.density = displayMetrics.density
        }

        heightPixel = GlobalApp.prefsDeviceInfo.heightPixel
        widthPixel = GlobalApp.prefsDeviceInfo.widthPixel
        density = GlobalApp.prefsDeviceInfo.density

        val widthDp = widthPixel / density
        val heightDp = heightPixel / density

        val wDp = widthDp * 0.55f
        val hDp = heightPixel * 3f

        val width =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, wDp, resources.displayMetrics)
                .toInt()

        val height =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, hDp, resources.displayMetrics)
                .toInt()

        val beeHeight = loginBeeImage.layoutParams.height
        loginBeeImage.layoutParams.width =
            (((loginBeeImage.layoutParams.width * heightDp * 0.27f) / beeHeight) * density).toInt()

        signInLogoTitle.layoutParams.width = width

        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = (heightPixel * 0.15f).toInt()
        lp.bottomMargin = 13
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
        signInText.layoutParams = lp

        loginWhiteBackground.post {
            translationY =
                loginWhiteBackground.height.toFloat() - (login_naver_sign_in_button.height.toFloat() * 1.7f)
        }
        loginBeeImage.animate().translationY(-translationY).duration = 500
    }


    // SNS Login

//    fun refreshIdToken() {
//        Dlog().d("google refresh id token")
//        mGoogleSignInClient.silentSignIn()
//            .addOnCompleteListener(
//                this
//            ) { task -> handleSignInResult(task) }
//    }

//    override fun signOut() {
//        GlobalApp.prefs.socialAccessToken = ""
//        GlobalApp.prefs.accessToken = ""
//        GlobalApp.prefs.refreshToken = ""
//        GlobalApp.prefs.provider = ""
//        GlobalApp.prefsBeeInfo.beeId = 0
//
//        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
//            Dlog().d("Google Sign Out")
//        }
//
//        mOAuthLoginModule.logout(this)
//        Dlog().d("Naver Sign Out")
//    }

    fun naverSignIn() {
        if (mOAuthLoginModule.getState(this) == OAuthLoginState.OK) {
            Dlog().d("Status don't need Naver Login")
            // 네이버 access token 으로 앱 로그인

            GlobalApp.prefs.socialAccessToken = mOAuthLoginModule.getAccessToken(this)
            GlobalApp.prefs.refreshToken = mOAuthLoginModule.getRefreshToken(this)
            GlobalApp.prefs.provider = getString(R.string.naver)

            requestSignInApi(
                SignInRequest(
                    GlobalApp.prefs.socialAccessToken,
                    getString(R.string.naver)
                )
            )
        } else {
            Dlog().d("Status need login")
            mOAuthLoginModule.startOauthLoginActivity(
                this,
                NaverLoginManager
            )
//                @SuppressLint("HandlerLeak")
//            object : OAuthLoginHandler() {
//                override fun run(success: Boolean) {
//                    if (success) {
//                        val accessToken = mOAuthLoginModule.getAccessToken(this@SignInActivity)
//                        val refreshToken = mOAuthLoginModule.getRefreshToken(this@SignInActivity)
//                        val expiresAt = mOAuthLoginModule.getExpiresAt(this@SignInActivity)
//                        val tokenType = mOAuthLoginModule.getTokenType(this@SignInActivity)
//
//                        Dlog().d("naver Login Access Token : $accessToken")
//                        Dlog().d("naver Login refresh Token : $refreshToken")
//                        Dlog().d("naver Login expiresAt : $expiresAt")
//                        Dlog().d("naver Login Token Type : $tokenType")
//                        Dlog().i(
//                            "naver Login Module State : " + mOAuthLoginModule.getState(this@SignInActivity)
//                                .toString()
//                        )
//
//                        GlobalApp.prefs.socialAccessToken = accessToken
//                        GlobalApp.prefs.provider = getString(R.string.naver)
//
//                        requestSignInApi(
//                            SignInRequest(
//                                accessToken,
//                                getString(R.string.naver)
//                            )
//                        )
//                    } else {
//                        val errorCode =
//                            mOAuthLoginModule.getLastErrorCode(this@SignInActivity).code
//                        val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@SignInActivity)
//                        Dlog().d("errorCode:$errorCode, errorDesc:$errorDesc")
//                    }
//                }
//            })
        }
    }

    fun googleSignIn() {
        val signInIntent = GoogleLoginManager.getGoogleSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account!!.idToken

            GlobalApp.prefs.socialAccessToken = idToken.toString()
            GlobalApp.prefs.provider = getString(R.string.google)

            requestSignInApi(
                SignInRequest(
                    idToken.toString(),
                    getString(R.string.google)
                )
            )
        } catch (e: ApiException) {
            Dlog().w("handleSignInResult:error $e")
        }
    }

    private fun setAutoLogin() {
        Log.d(TAG, "setAutoLogin: ${GlobalApp.prefs.socialAccessToken}")

        // Enum 클래스로 상태 판단한다던데
        if (GlobalApp.prefs.socialAccessToken != "") {
            requestSignInApi(
                SignInRequest(
                    GlobalApp.prefs.socialAccessToken,
                    GlobalApp.prefs.provider
                )
            )
        }
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
//                    beeId = Integer.parseInt(parameter)

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
                .putExtra("socialAccessToken", GlobalApp.prefs.socialAccessToken)
                .putExtra("provider", GlobalApp.prefs.provider)
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
        private const val RC_GET_TOKEN = 9002
        private const val REQUEST_LOGOUT = 1004
    }
}