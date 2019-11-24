package com.jasen.kimjaeseung.morningbees.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.data.NameValidataionCheckResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*Api 통신할 때 AsyncTask 말고 Retrofit library 이용*/

class SignUpActivity : AppCompatActivity() {
//    var check : Boolean = false

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://f553233b-4d4c-4d45-bf1e-d3999008d933.mock.pstmn.io")
        .build()

    val service = retrofit.create(MorningBeesService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        nameCheckValidation()
//        attachButtonEvent()
    }

    private fun nameCheckValidation() {
        val tempName = "bees"

        service.nameValidationCheck(tempName).enqueue(object : Callback<NameValidataionCheckResponse> {
            override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                Log.d(TAG,t.toString())
            }

            override fun onResponse(call: Call<NameValidataionCheckResponse>, response: Response<NameValidataionCheckResponse>) {
                Log.d(TAG,response.body().toString())
            }
        })

    }

    //    private fun attachButtonEvent(){
//        val mNickname:String = signup_nickname_text.text.toString()
//
//        signup_nickname_check_button.setOnClickListener {
//            server.getRequest(/*mNickname*/"bees").enqueue(object: Callback<ResponseDTO> {
//                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                }
//
//                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
//                    println(response.body().toString())
//                    //data parsing - ex) this.nickname = nickname
//                    check = true
//                }
//            })
//        }
//
//        signup_start_button.setOnClickListener {
//            when {
//                !check -> {
//                    Toast.makeText(this@SignUpActivity, "중복체크를 확인해주세요", Toast.LENGTH_SHORT).show()
//                }
//                check -> {
//                    server.putRequest(/*mNickname*/"bees").enqueue(object: Callback<ResponseDTO>{
//                        override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
//                            // saveNickname
//                        }
//                    })
//                    startActivity(Intent(this, MainActivity::class.java))
//                }
//            }
//        }
//    }
    companion object {
        private const val TAG = "SignUpActivity"
    }
}
