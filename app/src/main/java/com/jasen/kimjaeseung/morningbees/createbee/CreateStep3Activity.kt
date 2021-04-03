package com.jasen.kimjaeseung.morningbees.createbee

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.createbee.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step2.*
import kotlinx.android.synthetic.main.activity_create_step3.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response


class CreateStep3Activity : AppCompatActivity(), View.OnClickListener {
    private var jellyCount: Int = 0
    private var beeTitle = ""
    var startTime = 0
    var endTime = 0
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val service = MorningBeesService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step3)
        initButtonListeners()
        accessToken = GlobalApp.prefs.accessToken
        refreshToken = GlobalApp.prefs.refreshToken

        beeTitle = GlobalApp.prefsBeeInfo.beeTitle

        startTime = GlobalApp.prefsBeeInfo.startTime
        endTime = GlobalApp.prefsBeeInfo.endTime
        initTextView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, CreateStep2Activity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
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

        createStep3Text1.textSize = (width / 15).toFloat()
        createStep3Text2.textSize = (width / 15).toFloat()
        createStep3Description.textSize = (width / 30).toFloat()
        createStep3Text3.textSize = (width / 30).toFloat()

        jelly_2_text.textSize = (width / 30).toFloat()
        jelly_3_text.textSize = (width / 30).toFloat()
        jelly_4_text.textSize = (width / 30).toFloat()
        jelly_5_text.textSize = (width / 30).toFloat()
        jelly_6_text.textSize = (width / 30).toFloat()
        jelly_7_text.textSize = (width / 30).toFloat()
        jelly_8_text.textSize = (width / 30).toFloat()
        jelly_9_text.textSize = (width / 30).toFloat()
        jelly_10_text.textSize = (width / 30).toFloat()

        create_step3_next_button.layoutParams.width = displayMetrics.widthPixels
        create_step3_next_button.layoutParams.height = (heightPixel * 0.07f).toInt()
        create_step3_next_button.textSize = (width / 25).toFloat()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.create_step3_next_button -> requestCreateBeeApi()
            R.id.go_back_step2_button -> onBackPressed()

            R.id.jelly_2 -> {
                jellyCount = 2
                jelly_2.isSelected = !jelly_2.isSelected
                initButtonVisible()

                if (jelly_2.isSelected) {
                    jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_2_text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    jelly_2.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
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
                jellyCount = 3
                jelly_3.isSelected = !jelly_3.isSelected
                initButtonVisible()

                if (jelly_3.isSelected) {
                    jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_3_text.visibility = View.VISIBLE
                } else {
                    jelly_3.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_3_text.visibility = View.INVISIBLE
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
                jellyCount = 4
                jelly_4.isSelected = !jelly_4.isSelected
                initButtonVisible()

                if (jelly_4.isSelected) {
                    jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_4_text.visibility = View.VISIBLE
                } else {
                    jelly_4.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_4_text.visibility = View.INVISIBLE
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
                jellyCount = 5
                jelly_5.isSelected = !jelly_5.isSelected
                initButtonVisible()

                if (jelly_5.isSelected) {
                    jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_5_text.visibility = View.VISIBLE
                } else {
                    jelly_5.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_5_text.visibility = View.INVISIBLE
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
                jellyCount = 6
                jelly_6.isSelected = !jelly_6.isSelected
                initButtonVisible()

                if (jelly_6.isSelected) {
                    jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_6_text.visibility = View.VISIBLE
                } else {
                    jelly_6.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_6_text.visibility = View.INVISIBLE
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
                jellyCount = 7
                jelly_7.isSelected = !jelly_7.isSelected
                initButtonVisible()

                if (jelly_7.isSelected) {
                    jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_7_text.visibility = View.VISIBLE
                } else {
                    jelly_7.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_7_text.visibility = View.INVISIBLE
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
                jellyCount = 8
                jelly_8.isSelected = !jelly_8.isSelected
                initButtonVisible()

                if (jelly_8.isSelected) {
                    jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_8_text.visibility = View.VISIBLE
                } else {
                    jelly_8.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_8_text.visibility = View.INVISIBLE
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
                jellyCount = 9
                jelly_9.isSelected = !jelly_9.isSelected
                initButtonVisible()

                if (jelly_9.isSelected) {
                    jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_9_text.visibility = View.VISIBLE
                } else {
                    jelly_9.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly_9_text.visibility = View.INVISIBLE
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
                jellyCount = 10
                jelly_10.isSelected = !jelly_10.isSelected
                initButtonVisible()

                if (jelly_10.isSelected) {
                    jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    create_step3_next_button.isEnabled = true
                    create_step3_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly_10_text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    jelly_10.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    create_step3_next_button.isEnabled = false
                    create_step3_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step3_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
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

    private fun initButtonVisible() {
        jelly_2_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        jelly_3_text.visibility = View.INVISIBLE
        jelly_4_text.visibility = View.INVISIBLE
        jelly_5_text.visibility = View.INVISIBLE
        jelly_6_text.visibility = View.INVISIBLE
        jelly_7_text.visibility = View.INVISIBLE
        jelly_8_text.visibility = View.INVISIBLE
        jelly_9_text.visibility = View.INVISIBLE
        jelly_10_text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private fun initButtonListeners() {
        create_step3_next_button.setOnClickListener(this)
        go_back_step2_button.setOnClickListener(this)
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

    private fun requestCreateBeeApi() {
        val pay: Int = (jellyCount * 1000)
        val createBeeRequest = CreateBeeRequest(beeTitle, startTime, endTime, pay, " ")
        service.createBee(accessToken, createBeeRequest)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {

                    when (response.code()) {
                        201 -> {
                            gotoMain()
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
                                    requestCreateBeeApi()
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

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }
}

