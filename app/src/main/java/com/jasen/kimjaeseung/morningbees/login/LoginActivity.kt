package com.jasen.kimjaeseung.morningbees.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.activity_login.*

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.beforejoin.model.MeResponse
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.login.model.SignInRequest
import com.jasen.kimjaeseung.morningbees.login.model.SignInResponse
import com.jasen.kimjaeseung.morningbees.main.MainActivity

import com.jasen.kimjaeseung.morningbees.main.SignUpActivity
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

class LoginActivity : BaseActivity(), View.OnClickListener, LoginContract.View {
    private lateinit var loginPresenter: LoginPresenter

    private lateinit var mGoogleSignInClient: GoogleSignInClient    //google sign in client
    private lateinit var mOAuthLoginModule: OAuthLogin  //naver sign in module

    private lateinit var mAccessToken : String
    private lateinit var mRefreshToken : String
    private lateinit var provider : String
    val service =  MorningBeesService.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginPresenter.takeView(this)

        initButtonListeners()
        initGoogleSignIn()
        initNaverSignIn()

        if(intent.hasExtra("refreshTokenExpiration")){
            signOut()
            showToast { "signOut" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginPresenter.dropView()
    }

    override fun initPresenter() {
        loginPresenter = LoginPresenter()
    }

    private fun initButtonListeners() {
        // Button Listeners
        login_google_sign_in_button.setOnClickListener(this)
        login_google_sign_out_button.setOnClickListener(this)
        login_naver_sign_in_button.setOnClickListener(this)
        withdraw_bee_button.setOnClickListener(this)
        //login_goto_signup.setOnClickListener(this)
        //login_goto_beecreate.setOnClickListener(this)
    }


    override fun initNaverSignIn() {
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(
            this, this.getString(R.string.naver_oauth_client_id)
            , this.getString(R.string.naver_oauth_client_secret)
            , this.getString(R.string.naver_oauth_client_name)
        )
    }

    override fun initGoogleSignIn() {
        // configure Google Sign-in and the GoogleSignInClient object
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.getString(R.string.google_server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        Dlog().d("google sign in")

        refreshIdToken()

        // check existing user
//        val account = GoogleSignIn.gestLastSignedInAccount(this)
//        if (account!=null) {
//            Log.d(TAG,"already ${account.displayName}")
//
//        }
        //updateUI(acoount)
    }

    override fun refreshIdToken() {
        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
        // already has a valid token this method may complete immediately.
        //
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently and get a valid
        // ID token. Cross-device single sign on will occur in this branch.

        Dlog().d("google refresh id token")

        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }
    }

    override fun signOut() {
        //google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            Dlog().d("Google Sign Out")
        }
        //naver sign out
        mOAuthLoginModule.logout(this)
        Dlog().d("Naver Sign Out")
    }

    override fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun naverSignIn() {
        if (mOAuthLoginModule.getState(this) == OAuthLoginState.OK) {
            Dlog().d("Status don't need Naver Login")
            //네이버 access token으로 앱 로그인
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
                            "naver Login Module State : " + mOAuthLoginModule.getState(this@LoginActivity).toString()
                        )

                        provider = getString(R.string.naver)

                        signInMorningbeesServer(SignInRequest(accessToken,getString(R.string.naver)))

                    } else {
                        val errorCode =
                            mOAuthLoginModule.getLastErrorCode(this@LoginActivity).getCode()
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

            Dlog().d("Id Token : " + idToken)

            //모닝비즈서버에 엑세스토큰,프로바이더 전송
            signInMorningbeesServer(SignInRequest(idToken.toString(),getString(R.string.google)))

            //updateUI(account)
        } catch (e: ApiException) {
            Dlog().w("handleSignInResult:error" + e)
            //updateUI(null)
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.login_google_sign_in_button -> googleSignIn()
            R.id.login_google_sign_out_button -> signOut()
            R.id.login_naver_sign_in_button -> naverSignIn()
            R.id.withdraw_bee_button -> withdrawBee(mAccessToken, mRefreshToken)
            //R.id.login_goto_signup -> gotoSignUp()
            //R.id.login_goto_beecreate -> meServer()
        }
    }

    override fun signInMorningbeesServer(signInRequest: SignInRequest) {
        service.signIn(signInRequest).enqueue(object : Callback<SignInResponse>{
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                showToast { getString(R.string.network_error_message) }
                Dlog().d(t.toString())
            }

            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                val i = response.code()
                Dlog().d(response.code().toString())
                when(i){
                    200 ->{
                        val signInResponse : SignInResponse = response.body()!!
                        if (signInResponse.type == 1){    //SignIn process
                            //bee check이후 bee 생성 or main

                            val accessToken = signInResponse.accessToken
                            val refreshToken = signInResponse.refreshToken

                            mAccessToken = accessToken
                            mRefreshToken = refreshToken

                            meServer(accessToken, refreshToken)

                        }else{  //SignUp process
                            gotoSignUp(signInRequest)
                        }
                    }
                    400 ->{ //Bad Request
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val timestamp = jsonObject.getString("timestamp")
                        val status = jsonObject.getString("status")
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")

                        showToast { message }
                    }
                    500 ->{ //internal server error

                    }
                }
            }
        })
    }

    private fun withdrawBee(accessToken: String, refreshToken: String){

        service.withdrawalBee(accessToken)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    val i = response.code()

                    when (i) {
                        200 ->{
                            Log.d(TAG,"bee withdrawal success")
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                        }

                        500 -> {

                        }
                    }

                }
            })
    }

    private fun meServer(accessToken : String, refreshToken : String){
        var alreadyJoin : Boolean
        var nickname : String = ""

        service.me(accessToken)
            .enqueue(object : Callback<MeResponse>{
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val i = response.code()

                    when(i){
                        200 ->{
                            val meResponse : MeResponse = response.body()!!
                            alreadyJoin = meResponse.alreadyJoin
                            nickname = meResponse.nickname

                            Log.d(TAG, meResponse.alreadyJoin.toString())

                            if(alreadyJoin){
                                nickname = meResponse.nickname
                                Log.d(TAG,"already bee join")

                                //bee 이미 생성, 메인이동
                                gotoMainActivty()
                            }
                            else {
                                Log.d(TAG, "not already bee join")
                                gotoBeeCreate(accessToken, refreshToken)
                            }
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                        }
                    }
                }

                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }
            })

    }

    private fun gotoMainActivty(){
        val nextIntent = Intent(this, MainActivity::class.java)

        startActivity(nextIntent)
    }


    private fun gotoSignUp(signInRequest: SignInRequest) {
        val nextIntent = Intent(this, SignUpActivity::class.java)

        nextIntent.putExtra("socialAccessToken", signInRequest.socialAccessToken)
        nextIntent.putExtra("provider", signInRequest.provider)

        startActivity(nextIntent)
    }

    private fun gotoBeeCreate(accessToken: String, refreshToken: String){
        val nextIntent = Intent(this, BeforeJoinActivity::class.java)

        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        startActivity(nextIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            RC_SIGN_IN -> {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    // Google Sign In was successful
                    val account = task.getResult(ApiException::class.java)
                    Dlog().d(account!!.displayName!!)

                    handleSignInResult(task)

                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Dlog().w("Google sign in failed" + e)
                }
            }
            RC_GET_TOKEN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
        private const val RC_GET_TOKEN = 9002
    }
}