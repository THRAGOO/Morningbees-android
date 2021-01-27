package com.jasen.kimjaeseung.morningbees.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest

import com.jasen.kimjaeseung.morningbees.signup.SignUpActivity
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast

import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LoginActivity : BaseActivity(), View.OnClickListener, LoginContract.View {

    // MARK:~ Properties

    private lateinit var loginPresenter: LoginPresenter

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mOAuthLoginModule: OAuthLogin //naver sign in module

    private lateinit var provider: String
    private var beeId: Int = 0
    private var userId: Int = 0

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

        beeId = GlobalApp.prefsBeeInfo.beeId

        if (intent.hasExtra("RequestLogOut")) {
            signOut()
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
        val translationY = displayMetrics.heightPixels * 0.25f

        login_beeimage.animate().translationY(-translationY).duration = 500
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

            GlobalApp.prefs.accessToken = mOAuthLoginModule.getAccessToken(this)
            GlobalApp.prefs.refreshToken = mOAuthLoginModule.getRefreshToken(this)

            requestSignInApi(
                SignInRequest(
                    mOAuthLoginModule.getAccessToken(this),
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
            Dlog().d("Id Token :   $idToken")

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

    private fun requestJoinBeeApi(
        accessToken: String,
        refreshToken: String,
        beeId: Int,
        userId: Int
    ) {
        val joinBeeRequest = JoinBeeRequest(beeId, userId, "")
        service.joinBee(accessToken, joinBeeRequest)
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
                            gotoMainActivity(accessToken, refreshToken)
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
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
                showToast { getString(R.string.network_error_message) }
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
                                val accessToken = signInResponse.accessToken
                                val refreshToken = signInResponse.refreshToken

                                requestMeApi(accessToken, refreshToken)
                            }
                            0 -> {
                                gotoSignUp(signInRequest)
                            }
                        }
                    }
                    400 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")

                        if (code == 101) {
                            val oldAccessToken = GlobalApp.prefs.accessToken
                            GlobalApp.prefs.requestRenewalApi()
                            val renewalAccessToken = GlobalApp.prefs.accessToken

                            if (oldAccessToken == renewalAccessToken) {
                                showToast { "다시 로그인해주세요." }
                                signOut()
                            } else
                                requestSignInApi(SignInRequest(renewalAccessToken, provider))
                        } else {
                            showToast { message }
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

    // MARK:~ Me Api Request

    private fun requestMeApi(accessToken: String, refreshToken: String) {
        var alreadyJoin: Boolean?
        service.me(accessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse: MeResponse? = response.body()
                            alreadyJoin = meResponse?.alreadyJoin
                            GlobalApp.prefsBeeInfo.beeId = meResponse!!.beeId
                            beeId = meResponse.beeId
                            Log.d(TAG, "beeId: $beeId")

                            if (alreadyJoin == true) {
                                Log.d(TAG, "already bee join")
                                gotoMainActivity(accessToken, refreshToken)
                            } else {
                                Log.d(TAG, "not already bee join")

                                if (beeId == 0)
                                    gotoBeforeJoin(accessToken, refreshToken)
                                else {
                                    userId = meResponse!!.userId
                                    requestJoinBeeApi(accessToken, refreshToken, beeId, userId)
                                }
                            }
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if (code == 110) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    signOut()
                                } else
                                    requestMeApi(renewalAccessToken, refreshToken)
                            } else {
                                showToast { message }
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

    private fun gotoMainActivity(accessToken: String, refreshToken: String) {
        GlobalApp.prefs.accessToken = accessToken
        GlobalApp.prefs.refreshToken = refreshToken

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
        )
    }

    private fun gotoBeforeJoin(accessToken: String, refreshToken: String) {
        GlobalApp.prefs.accessToken = accessToken
        GlobalApp.prefs.refreshToken = refreshToken
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