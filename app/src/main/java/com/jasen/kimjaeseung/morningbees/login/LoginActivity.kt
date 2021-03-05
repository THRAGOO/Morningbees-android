package com.jasen.kimjaeseung.morningbees.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.activity_login.*

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.signin.SignInRequest
import com.jasen.kimjaeseung.morningbees.model.signin.SignInResponse
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest

import com.jasen.kimjaeseung.morningbees.signup.SignUpActivity
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast

import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import kotlin.math.roundToInt

class LoginActivity : BaseActivity(), View.OnClickListener, LoginContract.View {

    // MARK:~ Properties

    private lateinit var loginPresenter: LoginPresenter

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mOAuthLoginModule: OAuthLogin //naver sign in module

    private lateinit var provider: String
//    private var beeId: Int = 0

    private val service = MorningBeesService.create()

    // MARK:~ Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginPresenter.takeView(this)

        initAnimation()
        initButtonListeners()
        initGoogleSignIn()
        initNaverSignIn()
        initImage()

        when {
            intent.hasExtra("RequestLogOut") -> {
                signOut()
            }
            intent.hasExtra("RequestSignIn") -> {
                // 자동 로그인 & sign out 둘 다 X
            }
            else -> {
                setAutoLogin()
            }
        }
    }

    private fun setAutoLogin(){
        Log.d(TAG, "setAutoLogin: ${GlobalApp.prefs.socialAccessToken}")
        if (GlobalApp.prefs.socialAccessToken != ""){
            requestSignInApi(SignInRequest(GlobalApp.prefs.socialAccessToken, GlobalApp.prefs.provider))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.dropView()
    }

    // MARK:~ MVP Init

    override fun initPresenter() {
        loginPresenter = LoginPresenter()
    }

    // MARK:~ Button Listener

    private fun initButtonListeners() {
        login_google_sign_in_button.setOnClickListener(this)
        login_naver_sign_in_button.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.login_google_sign_in_button -> googleSignIn()
            R.id.login_naver_sign_in_button -> naverSignIn()
        }
    }

    // MARK:~ View Design

    private fun initAnimation() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Log.d(TAG, "heightPixels: ${displayMetrics.heightPixels}")
        val translationY = displayMetrics.heightPixels * 0.27f
        loginBeeImage.animate().translationY(-translationY).duration = 500

        val size = (380 * displayMetrics.density).roundToInt()
        val backgroundParam = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        backgroundParam.topMargin = size

        loginWhiteBackground.layoutParams = backgroundParam
    }

    private fun initImage(){
//        loginBeeImage.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
//            override fun onGlobalLayout() {
//                val displayMetrics = DisplayMetrics()
//                windowManager.defaultDisplay.getMetrics(displayMetrics)
//                val params = loginBeeImage.layoutParams
//                params.width = displayMetrics.widthPixels
//                params.height = displayMetrics.heightPixels
//                loginBeeImage.layoutParams = params
//
//                Log.d(TAG, "displayMetrics.widthPixels: ${displayMetrics.widthPixels}")
//                Log.d(TAG, "displayMetrics.heightPixels: ${displayMetrics.heightPixels}")
//
//                Log.d(TAG, "loginBeeImage.width: ${loginBeeImage.width}")
//                Log.d(TAG, "loginBeeImage.height: ${loginBeeImage.height}")
//
//                loginBeeImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
    }

    // MARK:~ SNS Login Init

    override fun initNaverSignIn() {
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(
            this, this.getString(R.string.naver_oauth_client_id)
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
        refreshIdToken()
    }

    override fun refreshIdToken() {
        Dlog().d("google refresh id token")
        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }
    }

    // MARK:~ Sign Out

    override fun signOut() {
        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            Dlog().d("Google Sign Out")
        }

        mOAuthLoginModule.logout(this)
        Dlog().d("Naver Sign Out")
    }

    // MARK:~ SNS Login

    override fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun naverSignIn() {
        if (mOAuthLoginModule.getState(this) == OAuthLoginState.OK) {
            Dlog().d("Status don't need Naver Login")
            // 네이버 access token 으로 앱 로그인

            GlobalApp.prefs.socialAccessToken = mOAuthLoginModule.getAccessToken(this)
//            GlobalApp.prefs.accessToken = mOAuthLoginModule.getAccessToken(this)
            GlobalApp.prefs.refreshToken = mOAuthLoginModule.getRefreshToken(this)
            GlobalApp.prefs.provider = getString(R.string.naver)

            requestSignInApi(
                SignInRequest(
                    GlobalApp.prefs.socialAccessToken ,
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

    // MARK:~ JoinBee API Request

    private fun requestJoinBeeApi() {
        val joinBeeRequest = JoinBeeRequest(GlobalApp.prefsBeeInfo.beeId,  GlobalApp.prefs.userId , "")
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

                            if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    signOut()
                                } else
                                    requestJoinBeeApi()
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

    // MARK:~ SignIn Api Request

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

    // MARK:~ Me Api Request

    private fun requestMeApi() {
        var alreadyJoin: Boolean?
        service.me(GlobalApp.prefs.accessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse: MeResponse? = response.body()
                            alreadyJoin = meResponse?.alreadyJoin
                            GlobalApp.prefsBeeInfo.beeId = meResponse!!.beeId

                            if (alreadyJoin == true) {
                                Log.d(TAG, "already bee join")
                                gotoMainActivity()
                            } else {
                                Log.d(TAG, "not already bee join")

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

                            if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    signOut()
                                } else
                                    requestMeApi()
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

    // MARK:~ Move Activity

    private fun gotoMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
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