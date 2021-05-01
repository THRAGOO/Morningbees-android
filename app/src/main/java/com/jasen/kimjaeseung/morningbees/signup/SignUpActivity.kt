package com.jasen.kimjaeseung.morningbees.signup

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.ui.signin.LoginActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest
import com.jasen.kimjaeseung.morningbees.mvp.BaseActivity
import com.jasen.kimjaeseung.morningbees.utils.showToast
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.regex.Pattern

class SignUpActivity : BaseActivity(), SignUpContract.View, View.OnClickListener {

    // Properties

    private lateinit var signUpPresenter: SignUpPresenter

    private var socialAccessToken: String = ""
    private var provider: String = ""

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate callback")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)

        socialAccessToken = intent.getStringExtra("socialAccessToken").toString()
        provider = intent.getStringExtra("provider").toString()

        initButtonListeners()
        signUpPresenter.takeView(this)
    }

    override fun onStart() {
        Log.d(TAG, "onStart callback")
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        signUpPresenter.dropView()
    }

    // Init Method

    override fun initPresenter() {
        signUpPresenter = SignUpPresenter(this)
    }

    private fun initButtonListeners() {
        nicknameCheckSignUpButton.setOnClickListener(this)
        gotoCreateBeeFromSignUpButton.setOnClickListener(this)
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.nicknameCheckSignUpButton -> nicknameCheck()
            R.id.gotoCreateBeeFromSignUpButton -> signUpStart()
        }
    }

    private fun nicknameCheck() {
        nicknameCheckSignUpButton.setOnClickListener {
            val usrNickname = nicknameSignUpText.text.toString()

            closeKeyboard()
            val filteredNickname = nicknameFilter(usrNickname)

            if (filteredNickname != "")
                signUpPresenter.nameValidMorningbeesServer(filteredNickname)
        }
    }

    override fun nicknameValidCheck(i: Int) {
        if (i == 1) {
            gotoCreateBeeFromSignUpButton.isEnabled = true
            gotoCreateBeeFromSignUpButton.background = applicationContext.getDrawable(R.color.active_button)
            gotoCreateBeeFromSignUpButton.setTextColor(Color.parseColor("#222222"))
        } else {
            gotoCreateBeeFromSignUpButton.isEnabled = false
            gotoCreateBeeFromSignUpButton.background = applicationContext.getDrawable(R.color.deactive_button)
            gotoCreateBeeFromSignUpButton.setTextColor(Color.parseColor("#aaaaaa"))
        }
    }

    private fun signUpStart() {
        closeKeyboard()
        when {
            !signUpPresenter.validCheck -> {
                Log.d(TAG, "need validcheck")
                showToast { getString(R.string.no_nickname_duplicate_check) }
            }

            signUpPresenter.validCheck -> {
                signUpPresenter.signUpMorningbeesServer(
                    SignUpRequest(socialAccessToken, provider, signUpPresenter.mNickname)
                )
            }
        }
    }

    override fun showToastView(toastMessage: () -> String) {
        Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()
    }

    private fun nicknameFilter(source: String): String {
        when (source.length) {
            in 2..10 -> {
                signUpNickNameText.text = ""
                val ps: Pattern =
                    Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$")

                when {
                    ps.matcher(source).matches() -> return source
                    else -> {
                        showToast { getString(R.string.nickname_restriction) }
                        return ""
                    }
                }
            }
            else -> {
                signUpNickNameText.text = "2~10자 이내로 입력해 주세요."
                return ""
            }
        }
    }

    override fun gotoBeforeJoin() {
        startActivity(
            Intent(this, BeforeJoinActivity::class.java)
        )
    }

    override fun gotoMain() {
        startActivity(
            Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    override fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gotoLogOut()
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}