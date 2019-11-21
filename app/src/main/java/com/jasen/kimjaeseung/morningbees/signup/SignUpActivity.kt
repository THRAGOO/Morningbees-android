package com.jasen.kimjaeseung.morningbees.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.MorningBeesService
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.ResponseDTO
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*Api 통신할 때 AsyncTask 말고 Retrofit library 이용*/

class SignUpActivity : AppCompatActivity() {
    var check : Boolean = false

    val retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create())
        .baseUrl("https://8ee441a8-3537-4ffa-837e-435fd5240a0a.mock.pstmn.io/api/v1/auth/sign_up")
        .build()

    val server = retrofit.create(MorningBeesService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        attachButtonEvent()
    }

    private fun attachButtonEvent(){
        val mNickname:String = signup_nickname_text.text.toString()

        signup_nickname_check_button.setOnClickListener {
            server.getRequest(/*mNickname*/"bees").enqueue(object: Callback<ResponseDTO> {
                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    println(response.body().toString())
                    //data parsing - ex) this.nickname = nickname
                    check = true
                }
            })
        }

        signup_start_button.setOnClickListener {
            when {
                !check -> {
                    Toast.makeText(this@SignUpActivity, "중복체크를 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                check -> {
                    server.putRequest(/*mNickname*/"bees").enqueue(object: Callback<ResponseDTO>{
                        override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

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
