package com.jasen.kimjaeseung.morningbees.invitebee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast

import kotlinx.android.synthetic.main.activity_invite_bee.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class InviteBeeActivity : AppCompatActivity(), View.OnClickListener{
    private val service = MorningBeesService.create()
    private var accessToken = ""
    private var userId = 0
    private var beeId = 0
    private var beeTitle = ""
    private var parameter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_bee)
        getDynamicLink()
        initButtonListener()
        accessToken = GlobalApp.prefs.accessToken
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.accept_invitebee_button -> getAccessToken()
            R.id.close_inviteView_button -> clickCloseInviteButton()
        }
    }

    private fun initButtonListener(){
        accept_invitebee_button.setOnClickListener(this)
        close_inviteView_button.setOnClickListener(this)
    }

    private fun getDynamicLink(){
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    parameter = deepLink?.getQueryParameter("beeId").orEmpty()
                    beeTitle = deepLink?.getQueryParameter("beeTitle").orEmpty()
                    beeId = Integer.parseInt(parameter)

                    GlobalApp.prefsBeeInfo.beeId = beeId
                    GlobalApp.prefsBeeInfo.beeTitle = beeTitle

                    beeNameText.text = ("${beeTitle}에 참여하여")
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    private fun requestMeApi() {
        service.me(accessToken)
            .enqueue(object : Callback<MeResponse>{
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when(response.code()){
                        200 -> {
                            val meResponse : MeResponse? = response.body()
                            userId = meResponse!!.userId
                            joinBeeServer()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if(errorResponse.code == 101 || errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    gotoLogOut()
                                } else
                                    requestMeApi()
                            } else {
                                showToast { errorResponse.message }
                                finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }
            })
    }

    private fun gotoLogOut(){
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }

    private fun gotoSignIn(){
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestSignIn", REQUEST_SIGN_IN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun joinBeeServer(){
        val joinBeeRequest = JoinBeeRequest(beeId, userId, beeTitle)
        service.joinBee(GlobalApp.prefs.accessToken, joinBeeRequest)
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    Dlog().d(response.code().toString())
                    Dlog().d(response.body().toString())
                    Dlog().d(response.headers().toString())
                    Dlog().d(response.errorBody().toString())
                    Dlog().d(response.raw().toString())

                    when(response.code()){
                        200 -> {
                            gotoMain()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if(errorResponse.code == 101 || errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "로그인 해주세요." }
                                    gotoSignIn()
                                } else
                                    requestMeApi()
                            } else if (errorResponse.code == 172) {
                                showToast { errorResponse.message }
                                gotoMain()
                            } else {
                                showToast { errorResponse.message }
                                finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }

                    }
                }
            })
    }

    private fun gotoMain(){
        startActivity(Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun gotoBeforeJoin(){
        startActivity(Intent(this, BeforeJoinActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun getAccessToken(){
        if(accessToken == ""){
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra("RequestJoin", "")
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            requestMeApi()
        }
    }

    private fun clickCloseInviteButton(){
        GlobalApp.prefsBeeInfo.beeId = 0
        if(accessToken == ""){
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra("RequestSignIn", "")
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            gotoBeforeJoin()
        }
    }

    companion object{
        const val TAG = "InviteBeeActivity"
        private const val REQUEST_SIGN_IN = 1007
    }
}


