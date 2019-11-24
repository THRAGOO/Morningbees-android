package com.jasen.kimjaeseung.morningbees.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.data.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.data.SignUpResponse
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*Api 통신할 때 AsyncTask 말고 Retrofit library 이용*/

class SignUpActivity : AppCompatActivity() {
    lateinit var mNickname: String
    var check : Boolean = false

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://f553233b-4d4c-4d45-bf1e-d3999008d933.mock.pstmn.io")
        .build()

    val service = retrofit.create(MorningBeesService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        attachButtonEvent()
    }

    private fun attachButtonEvent(){
        signup_nickname_check_button.setOnClickListener {
            nameCheckValidation()
        }

        signup_start_button.setOnClickListener {
            when {
                !check -> {
                    Toast.makeText(this@SignUpActivity, "중복체크를 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                check -> {
                    signUpMorningbeesServer()
                }
            }
        }
    }

    private fun nameCheckValidation() {
        val tempName = signup_nickname_text.text.toString()

        service.nameValidationCheck(tempName).enqueue(object : Callback<NameValidataionCheckResponse> {
            override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                Log.d(TAG,t.toString())
            }

            override fun onResponse(call: Call<NameValidataionCheckResponse>, response: Response<NameValidataionCheckResponse>) {
                Log.d(TAG, response.body().toString())
                Toast.makeText(this@SignUpActivity, "Available nickname", Toast.LENGTH_SHORT).show()

                mNickname = tempName
                Log.d("닉네임", mNickname)

                check = true
                signup_nickname_check_button.isEnabled = false
                signup_nickname_text.addTextChangedListener(object: TextWatcher{
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int)
                    {
                        signup_nickname_check_button.isEnabled = true
                        check = false
                    }
                })
            }
        })
    }

    private fun signUpMorningbeesServer(){
        //Login에서 넘겨준 socialAccessToken, provider과 nickname 같이 post
        service.signUp(
            "socialAccessToken",
            "provider", mNickname)
            .enqueue(object: Callback<SignUpResponse>{
                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Log.d(TAG, t.toString())
                }

                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>)
                {
                    Log.d(TAG, response.body().toString())
                }
        })
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}
