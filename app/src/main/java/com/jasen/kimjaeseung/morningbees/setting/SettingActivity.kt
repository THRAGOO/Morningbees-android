package com.jasen.kimjaeseung.morningbees.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.setting.beemember.formanager.BeeMemberForManagerActivity
import com.jasen.kimjaeseung.morningbees.setting.beemember.formember.BeeMemberForMemberActivity
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_setting.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    var nickName: String = ""
    private lateinit var accessToken: String
    private val service = MorningBeesService.create()
    var beeId: Int = -1
    private var managerNickname = ""
    private var myNickname = ""
    private var beeTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initButtonListener()

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        beeTitle = GlobalApp.prefsBeeInfo.beeTitle

        myNickname = intent.getStringExtra("myNickname")

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
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        showToast { message }
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
        setting_mission_time_txt.text =
            "${beeInfoResponse.startTime[0]}시-${beeInfoResponse.endTime[0]}시"
        setting_royaljelly_txt.text = "${beeInfoResponse.totalPay}원"
        if (beeInfoResponse.manager) {
            managerNickname = beeInfoResponse.nickname
            wrap_bee_withdrawal_layout.visibility = View.INVISIBLE
            wrap_bee_dismantle_layout.visibility = View.VISIBLE
            setting_royaljelly_btn.visibility = View.VISIBLE
            setting_mission_time_button.visibility = View.VISIBLE
        } else {
            wrap_bee_withdrawal_layout.visibility = View.VISIBLE
            wrap_bee_dismantle_layout.visibility = View.INVISIBLE
            setting_royaljelly_btn.visibility = View.INVISIBLE
            setting_mission_time_button.visibility = View.INVISIBLE
        }
    }

    private fun setRoyalJelly() {

    }

    private fun setMissionTime() {

    }

    private fun withdrawServer() {
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
                        gotoSignIn()
                    }
                    400 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        showToast { message }
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

    private fun gotoSignIn() {
        startActivity(
            Intent(this, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun dismantleServer() {

    }

    private fun gotoTotalBeeMember() {
        if (myNickname == managerNickname) {
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
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", REQUEST_LOGOUT)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.go_main_btn -> finish()
            R.id.setting_mission_time_button -> setMissionTime()
            R.id.setting_royaljelly_btn -> setRoyalJelly()
            R.id.logout_button -> gotoLogOut()
            R.id.bee_withdrawal_button -> withdrawServer()
            R.id.bee_dismantle_button -> withdrawServer() // 수정 필요
            R.id.total_bee_member_button -> gotoTotalBeeMember()
        }
    }

    private fun initButtonListener() {
        go_main_btn.setOnClickListener(this)
        setting_mission_time_button.setOnClickListener(this)
        setting_royaljelly_btn.setOnClickListener(this)
        total_bee_member_button.setOnClickListener(this)
        logout_button.setOnClickListener(this)
        bee_dismantle_button.setOnClickListener(this)
        bee_withdrawal_button.setOnClickListener(this)
    }

    companion object {
        private const val REQUEST_LOGOUT = 1004
        private const val TAG = "SettingActivity"
    }
}