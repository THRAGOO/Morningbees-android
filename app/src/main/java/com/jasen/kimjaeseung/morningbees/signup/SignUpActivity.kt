package com.jasen.kimjaeseung.morningbees.main

import android.annotation.SuppressLint
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
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpRequest
import java.util.regex.Pattern


class SignUpActivity : BaseActivity(), SignUpContract.View {
    private lateinit var signupPresenter : SignUpPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate callback")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        signupPresenter.takeView(this)
    }

    override fun onStart() {
        Log.d(TAG, "onStart callback")
        super.onStart()
        attachButtonEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        signupPresenter.dropView()
    }

    override fun initPresenter(){
        signupPresenter = SignUpPresenter(this)
    }

    @SuppressLint("ResourceType")
    private fun attachButtonEvent() {
        signup_nickname_check_button.setOnClickListener{
            val usrNickname = signup_nickname_text.text.toString()

            CloseKeyboard()
            val filteredNickname = nicknameFilter(usrNickname)

            if(filteredNickname != "")
                signupPresenter.nameValidMorningbeesServer(filteredNickname)
        }

        signup_start_button.setOnClickListener{
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

    override fun gotoMain() {
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }

   override fun showToastView(toastMessage: () -> String) {
       Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()
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