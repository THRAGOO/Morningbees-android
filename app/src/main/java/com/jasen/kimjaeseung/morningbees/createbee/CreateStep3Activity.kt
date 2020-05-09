package com.jasen.kimjaeseung.morningbees.createbee

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.model.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.createbee.model.RenewalResponse
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreateStep3Activity: AppCompatActivity(), View.OnClickListener {
    private var clickCnt : Int = 0
    private var jellyCnt : Int = 0

    //intent variables
    lateinit var beename : String
    var firstMissionTime : Int = 0
    var lastMissionTime : Int = 0
    private lateinit var accessToken : String
    private lateinit var refreshToken: String

    private val service = MorningBeesService.create()
    private var royalJellyArray : Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0)

    //private val REQUEST_TEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_step3)

        initButtonListeners()


        if(intent.hasExtra("beename")) {
            intent.getStringExtra("beename")?.let{
                beename = intent.getStringExtra("beename") } }
        else
            beename = ""

        if(intent.hasExtra("accessToken")){
            accessToken = intent.getStringExtra("accessToken") }

        if(intent.hasExtra("refreshToken")){
            refreshToken = intent.getStringExtra("refreshToken") }

        if(intent.hasExtra("firstMissionTime")){
            firstMissionTime = intent.getIntExtra("firstMissionTime", 0) }

        if(intent.hasExtra("lastMissionTime")){
            lastMissionTime = intent.getIntExtra("lastMissionTime", 0) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val nextIntent = Intent(this, CreateStep2Activity::class.java)

        nextIntent.putExtra("beename", beename)
        nextIntent.putExtra("firstMissionTime", firstMissionTime)
        nextIntent.putExtra("lastMissionTime", lastMissionTime)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        setResult(Activity.RESULT_OK, nextIntent)
        finish()
    }


    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.create_step3_next_button -> createBeeServer()
            R.id.go_back_step2_button -> onBackPressed()
            R.id.info_step3_button -> showInfo()

            R.id.jelly_2 -> {
                jellyCnt = 2
                jelly_2.isSelected = !jelly_2.isSelected
                initButtonVisible()

                if(jelly_2.isSelected) {
                    jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_2_text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                }
                else {
                    jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_2_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                }

                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }

            R.id.jelly_3 -> {
                jellyCnt = 3
                jelly_3.isSelected = !jelly_3.isSelected
                initButtonVisible()

                if(jelly_3.isSelected) {
                    jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_3_text.isVisible = true
                }
                else {
                    jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_3_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_4 -> {
                jellyCnt = 4
                jelly_4.isSelected = !jelly_4.isSelected
                initButtonVisible()

                if(jelly_4.isSelected) {
                    jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_4_text.isVisible = true
                }
                else {
                    jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_4_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_5 -> {
                jellyCnt = 5
                jelly_5.isSelected = !jelly_5.isSelected
                initButtonVisible()

                if(jelly_5.isSelected) {
                    jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_5_text.isVisible = true
                }
                else {
                    jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_5_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_6 -> {
                jellyCnt = 6
                jelly_6.isSelected = !jelly_6.isSelected
                initButtonVisible()

                if(jelly_6.isSelected) {
                    jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_6_text.isVisible = true
                }
                else {
                    jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_6_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_7 -> {
                jellyCnt = 7
                jelly_7.isSelected = !jelly_7.isSelected
                initButtonVisible()

                if(jelly_7.isSelected) {
                    jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_7_text.isVisible = true
                }
                else {
                    jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_7_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_8 -> {
                jellyCnt = 8
                jelly_8.isSelected = !jelly_8.isSelected
                initButtonVisible()

                if(jelly_8.isSelected) {
                    jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_8_text.isVisible = true
                }
                else {
                    jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_8_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_9 -> {
                jellyCnt = 9
                jelly_9.isSelected = !jelly_9.isSelected
                initButtonVisible()

                if(jelly_9.isSelected) {
                    jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_9_text.isVisible = true
                }
                else {
                    jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_9_text.isVisible = false
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly_10 -> {
                jellyCnt = 10
                jelly_10.isSelected = !jelly_10.isSelected
                initButtonVisible()

                if(jelly_10.isSelected) {
                    jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
                    jelly_10_text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                }
                else {
                    jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    jelly_10_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                }

                jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
        }
    }

    private fun initButtonVisible(){
        jelly_2_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        jelly_3_text.isVisible = false
        jelly_4_text.isVisible = false
        jelly_5_text.isVisible = false
        jelly_6_text.isVisible = false
        jelly_7_text.isVisible = false
        jelly_8_text.isVisible = false
        jelly_9_text.isVisible = false
        jelly_10_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private fun initButtonListeners() {
        create_step3_next_button.setOnClickListener(this)
        go_back_step2_button.setOnClickListener(this)
        info_step3_button.setOnClickListener(this)
        jelly_2.setOnClickListener(this)
        jelly_3.setOnClickListener(this)
        jelly_4.setOnClickListener(this)
        jelly_5.setOnClickListener(this)
        jelly_6.setOnClickListener(this)
        jelly_7.setOnClickListener(this)
        jelly_8.setOnClickListener(this)
        jelly_9.setOnClickListener(this)
        jelly_10.setOnClickListener(this)
    }

    private fun createBeeServer(){
        val mPay : Int = (jellyCnt*1000)
        val createBeeRequest = CreateBeeRequest(beename, firstMissionTime, lastMissionTime, mPay, " ")
        //val mAccessToken = accessToken
        
        service.createBee(accessToken, createBeeRequest)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call : Call<Void>, t:Throwable){
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call : Call<Void>,
                    response: Response<Void>
                ){

                    when (response.code()){
                        201 -> {
                            gotoMain()
                        }

                        400 ->{
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if(code == 101){ // access token 만료 error handling
                                showToast { "token 만료" }
                                renewalServer()
                            }
                            else {
                                showToast { message }
                            }
                        }

                        500 -> { //internal server error
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if(code == 101){ // access token 만료 error handling
                                //showToast { "token 만료" }
                                renewalServer()
                            }
                            else {
                                showToast { message }
                            }
                        }
                    }
                }
            })
    }

    private fun renewalServer(){
        Log.d(TAG, "renewalServer")
        service.renewal(accessToken, refreshToken)
            .enqueue(object : Callback<RenewalResponse>{
                override fun onFailure(call: Call<RenewalResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<RenewalResponse>,
                    response: Response<RenewalResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val renewalResponse = response.body()

                            accessToken = renewalResponse!!.accessToken
                            Log.d(TAG, "renewal accessToken: $accessToken")

                            createBeeServer()
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if(code == 101){
                                //refresh token 만료
                                showToast { message }
                                gotoLogin()

                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if(code == 101){
                                //refresh token 만료
                                showToast { message }
                                gotoLogin()

                            }
                            else
                                showToast { message }
                        }
                    }
                }
            })
    }

    private fun  gotoMain(){
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }


    private fun gotoLogin(){
        val nextIntent = Intent(this, LoginActivity::class.java)
        nextIntent.putExtra("refreshTokenExpiration", "refreshTokenExpiration")
        startActivity(nextIntent)
    }

    private fun showInfo(){

    }

    companion object {
        private const val TAG = "CreateStep3Activity"

    }
}

