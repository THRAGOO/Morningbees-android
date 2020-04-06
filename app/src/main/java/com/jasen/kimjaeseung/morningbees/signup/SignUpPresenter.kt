package com.jasen.kimjaeseung.morningbees.signup

import android.content.Context
import android.content.res.Resources
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.View
import android.widget.Toast
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.signup.model.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpRequest
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpResponse

import com.jasen.kimjaeseung.morningbees.util.Dlog

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpPresenter(context: Context) : SignUpContract.Presenter{
    //만약에 activity를 라이프사이클에 맞춰서 구현했으면 presenter에도 라이프사이클 해줘야함

    private var signupView : SignUpContract.View ?= null
    private val service = MorningBeesService.create()
    var nameValidCheckResponse: NameValidataionCheckResponse? = null
    var signUpResponse : SignUpResponse? = null

    lateinit var mNickname: String
    var validCheck: Boolean = false
    var mContext : Context = context


    override fun takeView(view: SignUpContract.View){
        signupView = view
    }

    override fun dropView() {
        signupView = null
    }

    override fun nameValidMorningbeesServer(tempName: String){
        service.nameValidationCheck(tempName)
            .enqueue(object : Callback<NameValidataionCheckResponse> {
                override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                    signupView!!.showToastView { mContext.resources.getString(R.string.network_error_message) }
                }

                override fun onResponse(
                    call: Call<NameValidataionCheckResponse>,
                    response: Response<NameValidataionCheckResponse>
                ) {
                    Log.d(TAG, response.body().toString())

                    when (response.code()) {
                        200 -> {
                            nameValidCheckResponse = response.body()
                            if (nameValidCheckResponse!!.isValid) {    //valid nickname
                                mNickname = tempName
                                validCheck = true

                                signupView!!.showToastView { mContext.resources.getString(R.string.validnickname_ok) }
                                signupView!!.nicknameValidCheck(1)
                                Log.d(TAG, "validnickname ok")

                            } else {
                                validCheck = false
                                signupView!!.showToastView { mContext.resources.getString(R.string.validnickname_duplicate) }
                                signupView!!.nicknameValidCheck(0)
                                Log.d(TAG, "validnickname duplicate")
                            }
                        }
                        400 -> {
                            Dlog().d(response.code().toString())
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            signupView!!.showToastView{message}
                        }
                        500 -> {//internal server error
                            Dlog().d(response.code().toString())
                        }
                    }
                }
            })
    }

    override fun signUpMorningbeesServer(
        signUpRequest: SignUpRequest
    ){
        //Login에서 넘겨준 socialAccessToken, provider과 nickname 같이 post
        service.signUp(
            signUpRequest
        )
            .enqueue(object : Callback<SignUpResponse> {
                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>
                ) {
                    Dlog().d(response.code().toString())
                    Dlog().d(response.body().toString())
                    Dlog().d(response.headers().toString())
                    Dlog().d(response.errorBody().toString())
                    Dlog().d(response.raw().toString())

                    when (response.code()) {
                        200 -> {
                             val signUpResponse = response.body()

                            val accessToken : String = signUpResponse!!.accessToken
                            val refreshToken : String  = signUpResponse!!.refreshToken

                            signupView!!.gotoMain(accessToken, refreshToken)
                        }
                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            signupView!!.showToastView{message}
                        }
                        500 -> { //internal server error

                        }
                    }
                }
            })
    }

    companion object {
        private const val TAG = "SignUpPresenter"
    }

}