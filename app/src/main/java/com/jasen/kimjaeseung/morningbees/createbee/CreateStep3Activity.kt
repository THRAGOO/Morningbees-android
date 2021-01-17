package com.jasen.kimjaeseung.morningbees.createbee

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.createbee.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreateStep3Activity : AppCompatActivity(), View.OnClickListener {
    private var jellyCnt: Int = 0

    //intent variables
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, CreateStep2Activity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.create_step3_next_button -> createBeeServer()
            R.id.go_back_step2_button -> onBackPressed()

            R.id.jelly_2 -> {
                jellyCnt = 2
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
                jellyCnt = 3
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
                jellyCnt = 4
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
                jellyCnt = 5
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
                jellyCnt = 6
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
                jellyCnt = 7
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
                jellyCnt = 8
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
                jellyCnt = 9
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
                jellyCnt = 10
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

    private fun createBeeServer() {
        val pay: Int = (jellyCnt * 1000)
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

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    companion object {
        private const val TAG = "CreateStep3Activity"
    }
}

