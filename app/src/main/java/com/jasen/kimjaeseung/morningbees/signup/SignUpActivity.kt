package com.jasen.kimjaeseung.morningbees.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_signup.*
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest
import java.util.regex.Pattern

class SignUpActivity : BaseActivity(), SignUpContract.View, View.OnClickListener {
    private lateinit var signupPresenter : SignUpPresenter
    private var beeId : Int = 0
    private var socialAccessToken : String = ""
    private var provider: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate callback")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)

        socialAccessToken = intent.getStringExtra("socialAccessToken")
        provider = intent.getStringExtra("provider")
        beeId = intent.getIntExtra("beeId", 0)

        initButtonListeners()
        signupPresenter.takeView(this)
    }

    override fun onStart() {
        Log.d(TAG, "onStart callback")
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        signupPresenter.dropView()
    }

    override fun initPresenter(){
        signupPresenter = SignUpPresenter(this)
    }

    private fun initButtonListeners(){
        signup_nickname_check_button.setOnClickListener(this)
        signup_start_button.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.signup_nickname_check_button -> nicknameCheck()
            R.id.signup_start_button -> signUpStart()
        }
    }

    private fun nicknameCheck(){
        signup_nickname_check_button.setOnClickListener{
            val usrNickname = signup_nickname_text.text.toString()

            closeKeyboard()
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

    private fun signUpStart(){
        closeKeyboard()
        when{
            !signupPresenter.validCheck -> {
                Log.d(TAG, "need validcheck")
                showToast { getString(R.string.no_nickname_duplicate_check) }
            }

            signupPresenter.validCheck -> {
                if (socialAccessToken == "" || provider == "") {
                    Log.d(TAG, "need intent value from signInActivity")
                    showToast{getString(R.string.social_login_recheck)}
                } else {
                    signupPresenter.signUpMorningbeesServer(
                        SignUpRequest(socialAccessToken, provider, signupPresenter.mNickname), beeId
                    )
                }
            }
        }
    }

    override fun showToastView(toastMessage: () -> String) {
        Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()
    }

    private fun nicknameFilter(source: String): String {
        when (source.length) {
            in 2..10 -> {
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

    override fun gotoBeforeJoin(accessToken: String, refreshToken: String){
        startActivity(
            Intent(this,BeforeJoinActivity::class.java)
        )
    }

    override fun gotoMain(accessToken: String, refreshToken: String){
        startActivity(
            Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun closeKeyboard() {
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