package com.jasen.kimjaeseung.morningbees.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.data.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.data.SignUpResponse
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {
    val service = MorningBeesService.create()
    var nameValidCheckResponse: NameValidataionCheckResponse? = null
    var validCheck : Boolean = false

    lateinit var mNickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        attachButtonEvent()
    }

    @SuppressLint("ResourceType")
    private fun attachButtonEvent(){
        signup_nickname_check_button.setOnClickListener {
            CloseKeyboard()
            val filterNickname= nicknameFilter(signup_nickname_text.text.toString())

            if(filterNickname != ""){
                nameValidMorningbeesServer(filterNickname)
            }
        }

        signup_start_button.setOnClickListener {
            CloseKeyboard()

            when{
                !validCheck ->
                    showToast{"중복체크를 확인해주세요"}

                validCheck -> {
                    val IntentSocialAccessToken: String? = intent.getStringExtra("socialAccessToken")
                    val IntentProvider:String? = intent.getStringExtra("provider")

                    if(IntentSocialAccessToken == null || IntentProvider == null){
                        showToast{"전달된 intent값이 없습니다."}
                    }
                    else {
                        signUpMorningbeesServer(hashMapOf("socialAccessToken" to IntentSocialAccessToken), hashMapOf("provider" to IntentProvider), hashMapOf("nickname" to mNickname))
                    }
                }
            }
        }
    }

    private fun nicknameFilter(source:String): String {
        when {
            source.length in 2..10 -> {
                val ps: Pattern = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$")

                when {
                    ps.matcher(source).matches() -> return source
                    else -> {
                        showToast { getString(R.string.nickname_restriction) }
                        return ""
                    }
                }
            }
            else -> {
                showToast { getString(R.string.nickname_byte_restriction) }
                return ""
            }
        }
    }

    private fun nameValidMorningbeesServer(tempName: String) {
        service.nameValidationCheck(tempName).enqueue(object : Callback<NameValidataionCheckResponse> {
            override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                showToast { getString(R.string.network_error_message) }
            }

            override fun onResponse(call: Call<NameValidataionCheckResponse>, response: Response<NameValidataionCheckResponse>) {
                Log.d(TAG, response.body().toString())

                when (response.code()) {
                    200 -> {
                        nameValidCheckResponse = response.body()
                        if (nameValidCheckResponse!!.isValid) {    //valid nickname
                            mNickname = tempName
                            validCheck = true
                            showToast { getString(R.string.validnickname_ok) }

                        } else {
                            showToast { getString(R.string.validnickname_duplicate) }
                        }
                    }
                    400 -> {
                        Dlog().d(response.code().toString())
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val timestamp = jsonObject.getString("timestamp")
                        val status = jsonObject.getString("status")
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")

                        showToast { message }
                    }
                    500 -> {//internal server error
                        Dlog().d(response.code().toString())
                    }
                }
            }
        })
    }

    private fun signUpMorningbeesServer(
        socialAccessToken: HashMap<String, String>,
        provider: HashMap<String, String>,
        nickname: HashMap<String, String>)
        {
        //Login에서 넘겨준 socialAccessToken, provider과 nickname 같이 post
        service.signUp(
            socialAccessToken, provider, nickname)
            .enqueue(object: Callback<SignUpResponse>{
                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>)
                {
                    Dlog().d(response.code().toString())
                    Dlog().d(response.body().toString())
                    Dlog().d(response.headers().toString())
                    Dlog().d(response.errorBody().toString())
                    Dlog().d(response.raw().toString())

                    when(response.code()){
                        200 ->{
                            when {
                                nameValidCheckResponse?.isValid == true -> gotoMain()
                                nameValidCheckResponse?.isValid == false -> showToast { "중복된 닉네임입니다" }
                                else -> showToast{"중복확인을 해주세요"}
                            }
                        }
                        400 ->{
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

    private fun gotoMain() {
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }

    private fun CloseKeyboard() {
        val view = this.currentFocus

        if(view != null)
        {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}
