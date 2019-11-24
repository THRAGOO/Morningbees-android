package com.jasen.kimjaeseung.morningbees.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.MorningBeesService
import com.jasen.kimjaeseung.morningbees.ResponseDTO
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*Api 통신할 때 AsyncTask 말고 Retrofit library 이용*/

class SignUpActivity : AppCompatActivity() {
    var check : Boolean = false
    private lateinit var mNickname:String
    private lateinit var retrofit : Retrofit
    private lateinit var server: MorningBeesService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jasen.kimjaeseung.morningbees.R.layout.activity_signup)

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://8ee441a8-3537-4ffa-837e-435fd5240a0a.mock.pstmn.io")
            .build()

        server = retrofit.create(MorningBeesService::class.java)

        attachButtonEvent()
    }

    private fun attachButtonEvent(){
        signup_nickname_check_button.setOnClickListener {
            mNickname = signup_nickname_text.text.toString()

            server.getRequest(mNickname).enqueue(object: Callback<ResponseDTO> {
                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "실패", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    println(response.body().toString())
                    //data parsing - ex) this.nickname = nickname
                    Toast.makeText(this@SignUpActivity, "성공", Toast.LENGTH_SHORT).show()
                    check = true
                }
            })
        }

        signup_start_button.setOnClickListener {
            when {
                !check -> {
                    Toast.makeText(this@SignUpActivity, "중복 체크를 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                check -> {
                    server.putRequest(mNickname).enqueue(object: Callback<ResponseDTO>{
                        override fun onFailure(call: Call<ResponseDTO>, t: Throwable) { }

                        override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                            // saveNickname
                        }
                    })
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
    }
}
