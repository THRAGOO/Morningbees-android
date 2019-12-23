package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.data.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.data.SignUpResponse
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {
    val service = MorningBeesService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        attachButtonEvent()
    }

    private fun attachButtonEvent(){
        signup_nickname_check_button.setOnClickListener {
            nameCheckValidation()
        }

//        signup_start_button.setOnClickListener {
//            when {
//                !check -> {
//                    Toast.makeText(this@SignUpActivity, "중복체크를 확인해주세요", Toast.LENGTH_SHORT).show()
//                }
//                check -> {
//                    signUpMorningbeesServer()
//                }
//            }
//        }
    }

    private fun nameCheckValidation() {
        val tempName = signup_nickname_text.text.toString()

        service.nameValidationCheck(tempName).enqueue(object : Callback<NameValidataionCheckResponse> {
            override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                showToast { getString(R.string.network_error_message) }
            }

            override fun onResponse(call: Call<NameValidataionCheckResponse>, response: Response<NameValidataionCheckResponse>) {
                when (response.code()){
                    200 -> {
                        val nameValidataionCheckResponse : NameValidataionCheckResponse? = response.body()
                        if (nameValidataionCheckResponse!!.isValid){    //valid nickname
                            signUpMorningbeesServer()
                        }else{
                            showToast { getString(R.string.validnickname_duplicate) }
                        }
                    }
                    400 -> {
                        Dlog().d(response.code().toString())
                    }
                    500 -> {
                        Dlog().d(response.code().toString())
                    }
                }
            }
        })
    }

    private fun signUpMorningbeesServer(){
        //Login에서 넘겨준 socialAccessToken, provider과 nickname 같이 post
        service.signUp(
            "socialAccessToken",
            "provider", "nick")
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
                }
        })
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}
