package com.jasen.kimjaeseung.morningbees.ui.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.invitebee.InviteBeeActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.signin.SignInRequest
import com.jasen.kimjaeseung.morningbees.model.signin.SignInResponse
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.signup.SignUpActivity
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.jasen.kimjaeseung.morningbees.utils.showToast
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class LoginActivity : BaseActivity(), View.OnClickListener,
    LoginContract.View {

    // Properties

    private lateinit var loginPresenter: LoginPresenter

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mOAuthLoginModule: OAuthLogin //naver sign in module

    var translationY = 0.0f

    private val service = MorningBeesService.create()

    private var beeId = 0
    private var beeTitle = ""
    private var parameter = ""

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.signInStatusBarTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginPresenter.takeView(this)
        initGoogleSignIn()
        initNaverSignIn()
        initButtonListeners()

        when {
            intent.hasExtra("RequestLogOut") -> {
                signOut()
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
    }

    override fun onResume() {
        super.onResume()
        initAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.dropView()
    }

    override fun onPause() {
        super.onPause()
        loginBeeImage.animate().translationY(translationY)
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.login_google_sign_in_button -> googleSignIn()
            R.id.login_naver_sign_in_button -> naverSignIn()
        }
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

            RC_GET_TOKEN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    // Init Method

    override fun initPresenter() {
        loginPresenter =
            LoginPresenter()
    }

    private fun initButtonListeners() {
        login_google_sign_in_button.setOnClickListener(this)
        login_naver_sign_in_button.setOnClickListener(this)
    }

    private fun initAnimation() {
        val displayMetrics : DisplayMetrics
        val heightPixel : Int
        val widthPixel : Int
        val density : Float



        if (GlobalApp.prefsDeviceInfo.density == 0f || GlobalApp.prefsDeviceInfo.heightPixel == 0 || GlobalApp.prefsDeviceInfo.widthPixel == 0){
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
        loginBeeImage.layoutParams.width = (((loginBeeImage.layoutParams.width * heightDp * 0.27f) / beeHeight) * density).toInt()

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

    // Init Method for SNS Login

    override fun initNaverSignIn() {
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(
            this
            , this.getString(R.string.naver_oauth_client_id)
            , this.getString(R.string.naver_oauth_client_secret)
            , this.getString(R.string.naver_oauth_client_name)
        )
    }

    override fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.getString(R.string.google_server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        Dlog().d("google sign in")
//        refreshIdToken()
    }

    // SNS Login

    override fun refreshIdToken() {
        Dlog().d("google refresh id token")
        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }
    }

    override fun signOut() {
        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""
        GlobalApp.prefsBeeInfo.beeId = 0

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            Dlog().d("Google Sign Out")
        }

        mOAuthLoginModule.logout(this)
        Dlog().d("Naver Sign Out")
    }

    override fun naverSignIn() {
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
            mOAuthLoginModule.startOauthLoginActivity(this, @SuppressLint("HandlerLeak")
            object : OAuthLoginHandler() {
                override fun run(success: Boolean) {
                    if (success) {
                        val accessToken = mOAuthLoginModule.getAccessToken(this@LoginActivity)
                        val refreshToken = mOAuthLoginModule.getRefreshToken(this@LoginActivity)
                        val expiresAt = mOAuthLoginModule.getExpiresAt(this@LoginActivity)
                        val tokenType = mOAuthLoginModule.getTokenType(this@LoginActivity)

                        Dlog().d("naver Login Access Token : $accessToken")
                        Dlog().d("naver Login refresh Token : $refreshToken")
                        Dlog().d("naver Login expiresAt : $expiresAt")
                        Dlog().d("naver Login Token Type : $tokenType")
                        Dlog().i(
                            "naver Login Module State : " + mOAuthLoginModule.getState(this@LoginActivity)
                                .toString()
                        )

                        GlobalApp.prefs.socialAccessToken = accessToken
                        GlobalApp.prefs.provider = getString(R.string.naver)

                        requestSignInApi(
                            SignInRequest(
                                accessToken,
                                getString(R.string.naver)
                            )
                        )
                    } else {
                        val errorCode =
                            mOAuthLoginModule.getLastErrorCode(this@LoginActivity).code
                        val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@LoginActivity)
                        Dlog().d("errorCode:$errorCode, errorDesc:$errorDesc")
                    }
                }
            })
        }
    }

    override fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            RC_SIGN_IN
        )
    }

    override fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
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

    override fun requestSignInApi(signInRequest: SignInRequest) {
        service.signIn(signInRequest).enqueue(object : Callback<SignInResponse> {
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(
                call: Call<SignInResponse>,
                response: Response<SignInResponse>
            ) {
                val i = response.code()
                Dlog().d(response.code().toString())
                when (i) {
                    200 -> {
                        val signInResponse: SignInResponse = response.body()!!
                        when (signInResponse.type) {
                            1 -> {
                                GlobalApp.prefs.accessToken = signInResponse.accessToken
                                GlobalApp.prefs.refreshToken = signInResponse.refreshToken

                                requestMeApi()
                            }

                            0 -> {
                                gotoSignUp(signInRequest)
                            }
                        }
                    }

                    400 -> {
                        showToast { "다시 로그인해주세요." }
                        signOut()
                    }

                    500 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        showToast { message }
                    }
                }
            }
        })
    }

    private fun setAutoLogin() {
        Log.d(TAG, "setAutoLogin: ${GlobalApp.prefs.socialAccessToken}")

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

    private fun getDynamicLink(){
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    parameter = deepLink?.getQueryParameter("beeId").orEmpty()
                    beeTitle = deepLink?.getQueryParameter("beeTitle").orEmpty()
                    beeId = Integer.parseInt(parameter)

                    GlobalApp.prefsBeeInfo.beeId = beeId
                    GlobalApp.prefsBeeInfo.beeTitle = beeTitle

                    startActivity(Intent(this, InviteBeeActivity::class.java))
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    // API Request

    private fun requestJoinBeeApi() {
        val joinBeeRequest =
            JoinBeeRequest(GlobalApp.prefsBeeInfo.beeId, GlobalApp.prefs.userId, "")
        service.joinBee(GlobalApp.prefs.accessToken, joinBeeRequest)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    when (response.code()) {
                        200 -> {
                            gotoMainActivity()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if (errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    signOut()
                                } else
                                    requestJoinBeeApi()
                            } else if (errorResponse.code == 170){
                                signOut()
                            } else {
                                showToast { errorResponse.message }
                                finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }
            })
    }

    private fun requestMeApi() {
        var alreadyJoin: Boolean?
        service.me(GlobalApp.prefs.accessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse = response.body()
                            alreadyJoin = meResponse?.alreadyJoin

                            if (alreadyJoin == true) {
                                Log.d(TAG, "already bee join")
                                GlobalApp.prefsBeeInfo.beeId = meResponse!!.beeId
                                gotoMainActivity()
                            } else {
                                Log.d(TAG, "not already bee join")
                                Log.d(TAG, "Global beeId: ${GlobalApp.prefsBeeInfo.beeId}")
                                Log.d(TAG, "beeId: ${meResponse!!.beeId}")

                                if (GlobalApp.prefsBeeInfo.beeId == 0)
                                    gotoBeforeJoin()
                                else {
                                    GlobalApp.prefs.userId = meResponse.userId
                                    requestJoinBeeApi()
                                }
                            }
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if (errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    signOut()
                                } else
                                    requestMeApi()
                            }

                            if (errorResponse.code == 170){
                                Log.d(TAG, "170 여기 ")
                                signOut()
                            } else {
                                showToast { errorResponse.message }
                                finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }

                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }
            })
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

    private fun gotoSignUp(signInRequest: SignInRequest) {
        startActivity(
            Intent(this, SignUpActivity::class.java)
                .putExtra("socialAccessToken", signInRequest.socialAccessToken)
                .putExtra("provider", signInRequest.provider)
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