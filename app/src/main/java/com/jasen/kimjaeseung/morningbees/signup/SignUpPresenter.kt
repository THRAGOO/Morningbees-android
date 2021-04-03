package com.jasen.kimjaeseung.morningbees.signup

import android.content.Context
import android.util.Log
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.namevalidationcheck.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class SignUpPresenter(context: Context) : SignUpContract.Presenter {
    private var signUpView: SignUpContract.View? = null
    private val service = MorningBeesService.create()
    var nameValidCheckResponse: NameValidataionCheckResponse? = null
    var signUpResponse: SignUpResponse? = null

    lateinit var mNickname: String
    var validCheck: Boolean = false
    var mContext: Context = context

    private var userId = 0

    override fun takeView(view: SignUpContract.View) {
        signUpView = view
    }

    override fun dropView() {
        signUpView = null
    }

    override fun nameValidMorningbeesServer(nickName: String) {
        val nickName = nickName
        service.nameValidationCheck(nickName)
            .enqueue(object : Callback<NameValidataionCheckResponse> {
                override fun onFailure(call: Call<NameValidataionCheckResponse>, t: Throwable) {
                    signUpView!!.showToastView { mContext.resources.getString(R.string.network_error_message) }
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
                                mNickname = nickName
                                validCheck = true

                                signUpView!!.showToastView { mContext.resources.getString(R.string.validnickname_ok) }
                                signUpView!!.nicknameValidCheck(1)

                            } else {
                                validCheck = false
                                signUpView!!.showToastView { mContext.resources.getString(R.string.validnickname_duplicate) }
                                signUpView!!.nicknameValidCheck(0)
                            }
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if (errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    signUpView!!.showToastView { "다시 로그인해주세요." }
                                    signUpView!!.gotoLogOut()
                                } else
                                    nameValidMorningbeesServer(nickName)
                            } else {
                                signUpView!!.showToastView { errorResponse.message }
                            }
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
    ) {
        val signUpRequest = signUpRequest
        service.signUp(signUpRequest)
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
                            signUpResponse = response.body()
                            GlobalApp.prefs.accessToken = signUpResponse!!.accessToken
                            GlobalApp.prefs.refreshToken = signUpResponse!!.refreshToken

                            Log.d(TAG, "signUpMorningbeesServer beeId: ${GlobalApp.prefsBeeInfo.beeId}")
                            if (GlobalApp.prefsBeeInfo.beeId == 0) {
                                signUpView!!.gotoBeforeJoin()
                            } else {
                                meServer()
                            }
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    signUpView!!.showToastView { "다시 로그인해주세요." }
                                    signUpView!!.gotoLogOut()
                                } else
                                    signUpMorningbeesServer(signUpRequest)
                            } else {
                                signUpView!!.showToastView { errorResponse.message }
                            }
                        }

                        500 -> { //internal server error
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            signUpView!!.showToastView { message }
                        }
                    }
                }
            })
    }

    private fun meServer() {
        service.me(GlobalApp.prefs.accessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse: MeResponse? = response.body()
                            userId = meResponse!!.userId
                            Log.d(TAG, "meServer beeId: ${GlobalApp.prefsBeeInfo.beeId}")
                            joinBeeServer(userId)
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    signUpView!!.showToastView { "다시 로그인해주세요." }
                                    signUpView!!.gotoLogOut()
                                } else
                                    meServer()
                            } else {
                                signUpView!!.showToastView { errorResponse.message }
                                signUpView!!.finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            signUpView!!.showToastView { message }
                        }
                    }
                }
            })
    }

    private fun joinBeeServer(userId: Int) {
        val userId = userId
        val joinBeeRequest = JoinBeeRequest(GlobalApp.prefsBeeInfo.beeId, userId, "")
//        service.joinBee(accessToken, beeId, userId)
        service.joinBee(GlobalApp.prefs.accessToken, joinBeeRequest)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    when (response.code()) {
                        200 -> {
                            signUpView!!.gotoMain()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    signUpView!!.showToastView { "다시 로그인해주세요." }
                                    signUpView!!.gotoLogOut()
                                } else
                                    joinBeeServer(userId)
                            } else {
                                signUpView!!.showToastView { errorResponse.message }
                                signUpView!!.finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            signUpView!!.showToastView { message }
                        }
                    }
                }
            })
    }

    companion object {
        private const val TAG = "SignUpPresenter"
    }

}