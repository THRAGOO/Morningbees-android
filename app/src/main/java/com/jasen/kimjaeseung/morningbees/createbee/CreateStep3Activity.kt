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
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.jasen.kimjaeseung.morningbees.utils.showToast
import kotlinx.android.synthetic.main.activity_create_step3.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response


class CreateStep3Activity : AppCompatActivity(), View.OnClickListener {

    // Properties

    private var jellyCount: Int = 0
    private var beeTitle = ""
    var startTime = 0
    var endTime = 0
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val service = MorningBeesService.create()

    // Life Cycle

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

    // Callback Method

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, CreateStep2Activity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.gotoMainFromStep3Button -> requestCreateBeeApi()
            R.id.gotoStep2FromStep3Button -> onBackPressed()

            R.id.jelly2Button -> {
                jellyCount = 2
                jelly2Button.isSelected = !jelly2Button.isSelected
                initButtonVisible()

                if (jelly2Button.isSelected) {
                    jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly2Text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly2Text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                }

                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }

            R.id.jelly3Button -> {
                jellyCount = 3
                jelly3Button.isSelected = !jelly3Button.isSelected
                initButtonVisible()

                if (jelly3Button.isSelected) {
                    jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly3Text.visibility = View.VISIBLE
                } else {
                    jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly3Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly4Button -> {
                jellyCount = 4
                jelly4Button.isSelected = !jelly4Button.isSelected
                initButtonVisible()

                if (jelly4Button.isSelected) {
                    jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly4Text.visibility = View.VISIBLE
                } else {
                    jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly4Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly5Button -> {
                jellyCount = 5
                jelly5Button.isSelected = !jelly5Button.isSelected
                initButtonVisible()

                if (jelly5Button.isSelected) {
                    jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly5Text.visibility = View.VISIBLE
                } else {
                    jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly5Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly6Button -> {
                jellyCount = 6
                jelly6Button.isSelected = !jelly6Button.isSelected
                initButtonVisible()

                if (jelly6Button.isSelected) {
                    jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly6Text.visibility = View.VISIBLE
                } else {
                    jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly6Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly7Button -> {
                jellyCount = 7
                jelly7Button.isSelected = !jelly7Button.isSelected
                initButtonVisible()

                if (jelly7Button.isSelected) {
                    jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly7Text.visibility = View.VISIBLE
                } else {
                    jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly7Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly8Buton -> {
                jellyCount = 8
                jelly8Buton.isSelected = !jelly8Buton.isSelected
                initButtonVisible()

                if (jelly8Buton.isSelected) {
                    jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly8Text.visibility = View.VISIBLE
                } else {
                    jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly8Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly9Button -> {
                jellyCount = 9
                jelly9Button.isSelected = !jelly9Button.isSelected
                initButtonVisible()

                if (jelly9Button.isSelected) {
                    jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly9Text.visibility = View.VISIBLE
                } else {
                    jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly9Text.visibility = View.INVISIBLE
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
            R.id.jelly10Button -> {
                jellyCount = 10
                jelly10Button.isSelected = !jelly10Button.isSelected
                initButtonVisible()

                if (jelly10Button.isSelected) {
                    jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_selected))
                    gotoMainFromStep3Button.isEnabled = true
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#222222"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                    jelly10Text.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    jelly10Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                    gotoMainFromStep3Button.isEnabled = false
                    gotoMainFromStep3Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoMainFromStep3Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                    jelly10Text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                }

                jelly2Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly3Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly4Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly5Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly6Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly7Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly8Buton.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
                jelly9Button.setImageDrawable(getDrawable(R.drawable.jelly_button_notselected))
            }
        }
    }

    // Init Method

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

        jelly2Text.textSize = (width / 30).toFloat()
        jelly3Text.textSize = (width / 30).toFloat()
        jelly4Text.textSize = (width / 30).toFloat()
        jelly5Text.textSize = (width / 30).toFloat()
        jelly6Text.textSize = (width / 30).toFloat()
        jelly7Text.textSize = (width / 30).toFloat()
        jelly8Text.textSize = (width / 30).toFloat()
        jelly9Text.textSize = (width / 30).toFloat()
        jelly10Text.textSize = (width / 30).toFloat()

        gotoMainFromStep3Button.layoutParams.width = displayMetrics.widthPixels
        gotoMainFromStep3Button.layoutParams.height = (heightPixel * 0.07f).toInt()
        gotoMainFromStep3Button.textSize = (width / 25).toFloat()
    }

    private fun initButtonVisible() {
        jelly2Text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        jelly3Text.visibility = View.INVISIBLE
        jelly4Text.visibility = View.INVISIBLE
        jelly5Text.visibility = View.INVISIBLE
        jelly6Text.visibility = View.INVISIBLE
        jelly7Text.visibility = View.INVISIBLE
        jelly8Text.visibility = View.INVISIBLE
        jelly9Text.visibility = View.INVISIBLE
        jelly10Text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private fun initButtonListeners() {
        gotoMainFromStep3Button.setOnClickListener(this)
        gotoStep2FromStep3Button.setOnClickListener(this)
        jelly2Button.setOnClickListener(this)
        jelly3Button.setOnClickListener(this)
        jelly4Button.setOnClickListener(this)
        jelly5Button.setOnClickListener(this)
        jelly6Button.setOnClickListener(this)
        jelly7Button.setOnClickListener(this)
        jelly8Buton.setOnClickListener(this)
        jelly9Button.setOnClickListener(this)
        jelly10Button.setOnClickListener(this)
    }

    // API Request

    private fun requestCreateBeeApi() {
        val pay: Int = (jellyCount * 1000)
        val createBeeRequest =
            CreateBeeRequest(
                beeTitle,
                startTime,
                endTime,
                pay,
                " "
            )
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

    // Change Activity

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun gotoLogOut() {
        startActivity(
            Intent(this, SignInActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }
}

