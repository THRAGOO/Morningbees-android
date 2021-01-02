package com.jasen.kimjaeseung.morningbees.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_setting.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    var nickName : String = ""
    private lateinit var accessToken: String
    private val service = MorningBeesService.create()
    var beeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initButtonListener()
        nickName = intent.getStringExtra("nickName")
        accessToken = intent.getStringExtra("accessToken")
        beeId = intent.getIntExtra("beeId", 0)
        beeInfoServer()
    }

    private fun beeInfoServer(){
        service.beeInfo(accessToken, beeId).enqueue(object : Callback<BeeInfoResponse>{
            override fun onFailure(call: Call<BeeInfoResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(
                call: Call<BeeInfoResponse>,
                response: Response<BeeInfoResponse>
            ) {
                when (response.code()){
                    200 -> {
                        val beeInfoResponse : BeeInfoResponse? = response.body()
                        if(beeInfoResponse?.isManager == true){
                            val morningBeesAccessToken = beeInfoResponse.accessToken
                            val missionTime = beeInfoResponse.missionTitle
                            val totalPay = beeInfoResponse.totalPay
                            if(missionTime != null && totalPay != null)
                                updateUI(missionTime, totalPay)
                            wrap_bee_withdrawal_layout.visibility = View.VISIBLE
                            wrap_bee_dismantle_layout.visibility = View.INVISIBLE
                        }
                        else {
                            val missionTime = beeInfoResponse?.missionTitle
                            val totalPay = beeInfoResponse?.totalPay
                            if(missionTime != null && totalPay != null)
                                updateUI(missionTime, totalPay)

                            setting_royaljelly_btn.visibility = View.GONE
                            setting_mission_time_btn.visibility = View.GONE
                            wrap_bee_withdrawal_layout.visibility = View.INVISIBLE
                            wrap_bee_dismantle_layout.visibility = View.VISIBLE
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

    private fun updateUI(missionTime : String, totalPay : Int){
        my_nickname_text.text = nickName
        setting_mission_time_txt.text = missionTime
        setting_royaljelly_txt.text = totalPay.toString()
    }

    private fun setRoyalJelly(){

    }

    private fun withdrawServer(){
        service.beeWithdrawal(accessToken).enqueue(object : Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()){
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

    private fun gotoSignIn(){
        startActivity(
            Intent(this, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun dismantleServer(){

    }

    private fun logout(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGOUT)
        finish()
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.go_main_btn -> finish()
            //R.id.setting_mission_time_btn -> setMissionTime()
            R.id.setting_royaljelly_btn -> setRoyalJelly()
            R.id.logout_button -> logout()
            R.id.bee_withdrawal_button -> withdrawServer()
            R.id.bee_dismantle_button -> withdrawServer() // 수정 필요
        }
    }

    private fun initButtonListener(){
        go_main_btn.setOnClickListener(this)
        setting_mission_time_btn.setOnClickListener(this)
        setting_royaljelly_btn.setOnClickListener(this)
        total_bee_button.setOnClickListener(this)
        logout_button.setOnClickListener(this)
        bee_dismantle_button.setOnClickListener(this)
        bee_withdrawal_button.setOnClickListener(this)
    }

    companion object {
       private const val REQUEST_LOGOUT = 1004
    }
}