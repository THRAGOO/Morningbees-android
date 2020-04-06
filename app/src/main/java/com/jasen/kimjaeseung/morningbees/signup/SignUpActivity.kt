package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.signup.SignUpContract
import com.jasen.kimjaeseung.morningbees.signup.SignUpPresenter
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_signup.*
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpRequest
import java.util.regex.Pattern


class SignUpActivity : BaseActivity(), SignUpContract.View, View.OnClickListener {
    private lateinit var signupPresenter : SignUpPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate callback")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)
        initButtonListeners()
        signupPresenter.takeView(this)
    }

    override fun onStart() {
        Log.d(TAG, "onStart callback")
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        //signupPresenter.dropView()
    }

    override fun initPresenter(){
        signupPresenter = SignUpPresenter(this)
    }

    private fun initButtonListeners(){
        signup_nickname_check_button.setOnClickListener(this)
        signup_start_button.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.signup_nickname_check_button -> nicknameCheck()
            R.id.signup_start_button -> signupStart()
        }
    }

    private fun nicknameCheck(){
        signup_nickname_check_button.setOnClickListener{
            val usrNickname = signup_nickname_text.text.toString()

            CloseKeyboard()
            val filteredNickname = nicknameFilter(usrNickname)

            if(filteredNickname != "")
                signupPresenter.nameValidMorningbeesServer(filteredNickname)
        }
    }

    override fun nicknameValidCheck(i: Int) {
        if(i==1){
            signup_start_button.isEnabled = true
            signup_start_button.background = applicationContext.getDrawable(R.color.active_button)
        }
        else {
            signup_start_button.isEnabled = false
            signup_start_button.background = applicationContext.getDrawable(R.color.deactive_button)
        }
    }

    private fun signupStart(){
        CloseKeyboard()

        when{
            !signupPresenter.validCheck -> {
                Log.d(TAG, "need validcheck")
                showToast { getString(R.string.no_nickname_duplicate_check) }
            }

            signupPresenter.validCheck -> {
                val IntentSocialAccessToken: String? = intent.getStringExtra("socialAccessToken")
                val IntentProvider: String? = intent.getStringExtra("provider")

                if (IntentSocialAccessToken == null || IntentProvider == null) {
                    Log.d(TAG, "need intent value from signInActivity")
                    showToast{getString(R.string.social_login_recheck)}

                } else {
                    signupPresenter.signUpMorningbeesServer(
                        SignUpRequest(IntentSocialAccessToken,IntentProvider,signupPresenter.mNickname)
                    )
                    Log.d(TAG, signupPresenter.mNickname)
                }
            }
        }
    }

    override fun showToastView(toastMessage: () -> String) {
        Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()
    }

    private fun nicknameFilter(source: String): String {
        when {
            source.length in 2..10 -> {
                val ps: Pattern =
                    Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$")

                when {
                    ps.matcher(source).matches() -> return source
                    else -> {
                        showToast{getString(R.string.nickname_restriction)}
                        return ""
                    }
                }
            }
            else -> {
                showToast{getString(R.string.nickname_byte_restriction)}
                return ""
            }
        }
    }

    override fun gotoMain(accessToken : String, refreshToken : String) {
        val nextIntent = Intent(this, CreateStep1Activity::class.java)

        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        startActivity(nextIntent)
    }


    private fun CloseKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    companion object {
        private const val TAG = "SignUpActivity"
    }
}