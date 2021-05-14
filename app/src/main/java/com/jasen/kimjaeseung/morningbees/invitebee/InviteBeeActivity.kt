package com.jasen.kimjaeseung.morningbees.invitebee

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.MeResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.jasen.kimjaeseung.morningbees.utils.showToast

import kotlinx.android.synthetic.main.activity_invite_bee.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class InviteBeeActivity : AppCompatActivity(), View.OnClickListener{

    // Properties

    private val service = MorningBeesService.create()
    private var accessToken = ""
    private var userId = 0
    private var beeId = 0
    private var beeTitle = ""

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_bee)
        initButtonListener()
        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        beeTitle = GlobalApp.prefsBeeInfo.beeTitle
        beeNameText.text = ("${beeTitle}에 참여하여")
        initTextView()
    }

    // Callback Method

    override fun onClick(v: View) {
        when(v.id){
            R.id.acceptInviteBeeButton -> getAccessToken()
            R.id.denyInviteBeeButton -> clickCloseInviteButton()
        }
    }

    // Init Method

    private fun getAccessToken(){
        Log.d(TAG, "accessToken: ${GlobalApp.prefs.accessToken}")
        Log.d(TAG, "accessToken: ${accessToken}")
        if(GlobalApp.prefs.accessToken == ""){
            startActivity(
                Intent(this, SignInActivity::class.java)
                    .putExtra("RequestJoin", "")
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            requestMeApi()
        }
    }

    private fun initButtonListener(){
        acceptInviteBeeButton.setOnClickListener(this)
        denyInviteBeeButton.setOnClickListener(this)
    }

    private fun initTextView(){
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val heightPixel = displayMetrics.heightPixels
        val widthPixel = displayMetrics.widthPixels
        val heightDp = heightPixel / displayMetrics.density

        inviteBeeText1.textSize = (width / 17).toFloat()
        beeNameText.textSize = (width / 28).toFloat()
        inviteBeeText2.textSize = (width / 28).toFloat()

        val resizeHeight = inviteBeeImage.layoutParams.height
        inviteBeeImage.layoutParams.width = (((inviteBeeImage.layoutParams.width * heightDp * 0.35f) / resizeHeight) * displayMetrics.density).toInt()
        inviteBeeImage.layoutParams.height = inviteBeeImage.layoutParams.width

        acceptInviteBeeButton.layoutParams.width = (widthPixel * 0.6f).toInt()
        acceptInviteBeeButton.layoutParams.height = (heightPixel * 0.06f).toInt()
        acceptInviteBeeButton.textSize = (width / 30).toFloat()
    }

    // API Request

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
                            requestJoinBeApi()
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

    private fun requestJoinBeApi(){
        val joinBeeRequest =
            JoinBeeRequest(
                beeId,
                userId,
                beeTitle
            )
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

    // Change Activity

    private fun gotoLogOut(){
        startActivity(
            Intent(this, SignInActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }

    private fun gotoSignIn(){
        startActivity(
            Intent(this, SignInActivity::class.java)
                .putExtra("RequestSignIn", REQUEST_SIGN_IN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun gotoMain(){
        startActivity(Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }

    private fun gotoBeforeJoin(){
        startActivity(Intent(this, BeforeJoinActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun clickCloseInviteButton(){
        GlobalApp.prefsBeeInfo.beeId = 0
        if(accessToken == ""){
//            startActivity(
//                Intent(this, SignInActivity::class.java)
//                    .putExtra("RequestSignIn", "")
//                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            )
            gotoSignIn()
        } else {
            gotoBeforeJoin()
        }
    }

    companion object{
        const val TAG = "InviteBeeActivity"
        private const val REQUEST_SIGN_IN = 1007
    }
}


