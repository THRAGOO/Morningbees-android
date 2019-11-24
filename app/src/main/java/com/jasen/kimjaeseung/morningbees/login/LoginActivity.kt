package com.jasen.kimjaeseung.morningbees.login

import android.annotation.SuppressLint
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

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.jasen.kimjaeseung.morningbees.data.SignInRequest
import com.jasen.kimjaeseung.morningbees.data.SignUpResponse
import com.jasen.kimjaeseung.morningbees.main.SignUpActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mGoogleSignInClient: GoogleSignInClient    //google sign in client
    private lateinit var mOAuthLoginModule: OAuthLogin  //naver sign in module
    val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://f553233b-4d4c-4d45-bf1e-d3999008d933.mock.pstmn.io")
        .build()

    val service = retrofit.create(MorningBeesService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initButtonListeners()

        initGoogleSignIn()
        initNaverSignIn()
    }

    private fun initButtonListeners() {
        // Button Listeners
        login_google_sign_in_button.setOnClickListener(this)
        login_google_sign_out_button.setOnClickListener(this)
        login_naver_sign_in_button.setOnClickListener(this)
        login_goto_signup.setOnClickListener(this)
    }


    private fun initNaverSignIn() {
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(
            this, getString(R.string.naver_oauth_client_id)
            , getString(R.string.naver_oauth_client_secret)
            , getString(R.string.naver_oauth_client_name)
        )
    }

    private fun initGoogleSignIn() {
        // configure Google Sign-in and the GoogleSignInClient object
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        refreshIdToken()

        // check existing user
//        val account = GoogleSignIn.getLastSignedInAccount(this)
//        if (account!=null) {
//            Log.d(TAG,"already ${account.displayName}")
//
//        }
        //updateUI(acoount)

    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        //google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            Log.d(TAG, "Google Sign Out")
        }

        //naver sign out
        mOAuthLoginModule.logout(this)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account!!.idToken

            Log.d(TAG, "Id Token : " + idToken)

            //모닝비즈서버에 엑세스토큰,프로바이더 전송
//            signInMorningbeesServer(idToken.toString(), "google")
            signUpMorningbeesServer(idToken.toString(), "google", "nick")

            //updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "handleSignInResult:error", e)
            //updateUI(null)
        }

    }

    private fun refreshIdToken() {
        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
        // already has a valid token this method may complete immediately.
        //
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently and get a valid
        // ID token. Cross-device single sign on will occur in this branch.
        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }
    }

    private fun naverSignIn() {
        if (mOAuthLoginModule.getState(this) == OAuthLoginState.OK) {
            Log.d(TAG, "Status don't need Naver Login")
            //네이버 access token으로 앱 로그인
        } else {
            Log.d(TAG, "Status need login")
            mOAuthLoginModule.startOauthLoginActivity(this, @SuppressLint("HandlerLeak")
            object : OAuthLoginHandler() {
                override fun run(success: Boolean) {
                    if (success) {
                        val accessToken = mOAuthLoginModule.getAccessToken(this@LoginActivity)
                        val refreshToken = mOAuthLoginModule.getRefreshToken(this@LoginActivity)
                        val expiresAt = mOAuthLoginModule.getExpiresAt(this@LoginActivity)
                        val tokenType = mOAuthLoginModule.getTokenType(this@LoginActivity)
                        Log.i(TAG, "naver Login Access Token : $accessToken")
                        Log.i(TAG, "naver Login refresh Token : $refreshToken")
                        Log.i(TAG, "naver Login expiresAt : $expiresAt")
                        Log.i(TAG, "naver Login Token Type : $tokenType")
                        Log.i(
                            TAG,
                            "naver Login Module State : " + mOAuthLoginModule.getState(this@LoginActivity).toString()
                        )

                        //모닝비즈서버에 signin요청 및 액티비티 이동
//                        val jsonObject = JSONObject()
//                        jsonObject.put("socialAccessToken",accessToken)
//                        jsonObject.put("provider","naver")
//                        signInMorningbeesServer(jsonObject.toString())
                        signInMorningbeesServer(SignInRequest(accessToken,"naver"))

                    } else {
                        val errorCode =
                            mOAuthLoginModule.getLastErrorCode(this@LoginActivity).getCode()
                        val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@LoginActivity)
                        Log.d(TAG, "errorCode:$errorCode, errorDesc:$errorDesc")

                    }
                }
            })
        }
    }

    private fun signInMorningbeesServer(signInRequest: SignInRequest) {
        service.signIn(signInRequest).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, t.toString())
            }

            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                Log.d(TAG, response.body().toString())
                //json parser
            }
        })
    }

    private fun signUpMorningbeesServer(
        socialAccessToken: String,
        provider: String,
        nickname: String
    ) {
        service.signUp(socialAccessToken, provider, nickname)
            .enqueue(object : Callback<SignUpResponse> {
                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Log.d(TAG, t.toString())
                }

                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>
                ) {
                    Log.d(TAG, response.body().toString())
                }
            })
    }

    private fun gotoSignUp() {
        val nextIntent = Intent(this, SignUpActivity::class.java)
        startActivity(nextIntent)
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.login_google_sign_in_button -> googleSignIn()
            R.id.login_google_sign_out_button -> signOut()
            R.id.login_naver_sign_in_button -> naverSignIn()
            R.id.login_goto_signup -> gotoSignUp()
        }
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

                    Log.d(TAG, account!!.displayName)

                    handleSignInResult(task)


                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
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