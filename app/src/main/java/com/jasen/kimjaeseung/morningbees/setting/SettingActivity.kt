package com.jasen.kimjaeseung.morningbees.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.beemember.formanager.BeeMemberForManagerActivity
import com.jasen.kimjaeseung.morningbees.setting.beemember.formember.BeeMemberForMemberActivity
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.RoyalJellyActivity
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.getPriceAnnotation
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_setting.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var accessToken: String
    private val service = MorningBeesService.create()
    var beeId: Int = -1
    private var beeTitle = ""
    private var pay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initButtonListener()

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        beeTitle = GlobalApp.prefsBeeInfo.beeTitle

        requestBeeInfoApi()
    }

    private fun requestBeeInfoApi() {
        service.beeInfo(accessToken, beeId).enqueue(object : Callback<BeeInfoResponse> {
            override fun onFailure(call: Call<BeeInfoResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(
                call: Call<BeeInfoResponse>,
                response: Response<BeeInfoResponse>
            ) {
                when (response.code()) {
                    200 -> {
                        val beeInfoResponse: BeeInfoResponse? = response.body()
                        if (beeInfoResponse != null) {
                            setLayoutToSetting(beeInfoResponse)
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
                                showToast { "다시 로그인해주세요." }
                                gotoLogOut()
                            } else
                                requestBeeInfoApi()
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

    private fun setLayoutToSetting(beeInfoResponse: BeeInfoResponse) {
        my_nickname_text.text = beeInfoResponse.nickname
        myEmailText.text = GlobalApp.prefsBeeInfo.myEmail
        missionTimeInSetting.text =
            "${beeInfoResponse.startTime[0]}시-${beeInfoResponse.endTime[0]}시"
        pay = beeInfoResponse.pay
        royalJellyInSetting.text = "${pay.getPriceAnnotation()}원"
        if (beeInfoResponse.manager) {
            wrap_bee_withdrawal_layout.visibility = View.INVISIBLE
        } else {
            wrap_bee_withdrawal_layout.visibility = View.VISIBLE
        }
    }

    private fun requestBeeWithdrawalApi() {
        GlobalApp.prefsBeeInfo.beeId = 0
        GlobalApp.prefsBeeInfo.beeTitle = ""
        GlobalApp.prefsBeeInfo.startTime = 0
        GlobalApp.prefsBeeInfo.endTime = 0

        service.beeWithdrawal(accessToken).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> {
                        gotoLogOut()
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
                                showToast { "다시 로그인해주세요." }
                                gotoLogOut()
                            } else
                                requestBeeWithdrawalApi()
                        } else {
                            showToast { errorResponse.message }
                            finish()
                        }
                    }

                    500 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        gotoLogOut()
                    }
                }
            }
        })

    }

    private fun gotoTotalBeeMember() {
        if (GlobalApp.prefsBeeInfo.myNickname == GlobalApp.prefsBeeInfo.beeManagerNickname) {
            startActivity(
                Intent(this, BeeMemberForManagerActivity::class.java)
            )
        } else {
            startActivity(
                Intent(this, BeeMemberForMemberActivity::class.java)
            )
        }
    }

    private fun gotoLogOut() {
        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""

        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", REQUEST_LOGOUT)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun gotoRoyalJelly(){
        startActivity(
            Intent(this, RoyalJellyActivity::class.java)
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.go_main_btn -> finish()
            R.id.goToRoyalJellyButton -> gotoRoyalJelly()
            R.id.logout_button -> gotoLogOut()
            R.id.wrap_bee_withdrawal_layout -> requestBeeWithdrawalApi()
            R.id.total_bee_member_button -> gotoTotalBeeMember()
        }
    }

    private fun initButtonListener() {
        go_main_btn.setOnClickListener(this)
        goToRoyalJellyButton.setOnClickListener(this)
        total_bee_member_button.setOnClickListener(this)
        logout_button.setOnClickListener(this)
        wrap_bee_withdrawal_layout.setOnClickListener(this)
    }

    companion object {
        private const val REQUEST_LOGOUT = 1004
        private const val TAG = "SettingActivity"
    }
}