package com.jasen.kimjaeseung.morningbees.createbee

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import kotlinx.android.synthetic.main.activity_create_step2.*


class CreateStep2Activity : AppCompatActivity(), View.OnClickListener {

    // Properties

    var startTime = 0
    var endTime = 0
    var count = 0
    private var checkNextButtonText: Boolean = false
    var missionTimeArr: Array<Boolean> = arrayOf(false, false, false, false, false, false)

    // Life Cycle for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step2)
        initButtonListeners()
        buttonPressed(GlobalApp.prefsBeeInfo.startTime)
        buttonPressed(GlobalApp.prefsBeeInfo.endTime)

        clock6Button.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                initImageView()
                clock6Button.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        initTextView()
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clock6Button -> buttonPressed(6)
            R.id.clock7Button -> buttonPressed(7)
            R.id.clock8Button -> buttonPressed(8)
            R.id.clock9Button -> buttonPressed(9)
            R.id.clock10Button -> buttonPressed(10)
            R.id.gotoStep3FromStep2Button -> gotoStep3()
            R.id.gotoStep1FromStep2Button -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, CreateStep1Activity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    // Init Method

    private fun initButtonListeners() {
        clock6Button.setOnClickListener(this)
        clock7Button.setOnClickListener(this)
        clock8Button.setOnClickListener(this)
        clock9Button.setOnClickListener(this)
        clock10Button.setOnClickListener(this)
        gotoStep3FromStep2Button.setOnClickListener(this)
        gotoStep1FromStep2Button.setOnClickListener(this)
    }

    private fun initImageView(){
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.width = clock6Button.width
        lp.height = clock6Button.height
        lp.gravity = Gravity.CENTER
        text6Clock.layoutParams = lp

        lp.width = clock7Button.width
        lp.height = clock7Button.height
        lp.gravity = Gravity.CENTER
        text7Clock.layoutParams = lp

        lp.width = clock8Button.width
        lp.height = clock8Button.height
        lp.gravity = Gravity.CENTER
        text8Clock.layoutParams = lp

        lp.width = clock9Button.width
        lp.height = clock9Button.height
        lp.gravity = Gravity.CENTER
        text9Clock.layoutParams = lp

        lp.width = clock10Button.width
        lp.height = clock10Button.height
        lp.gravity = Gravity.CENTER
        text10Clock.layoutParams = lp
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

        createBeeStep2Text1.textSize = (width / 15).toFloat()
        createBeeStep2Text2.textSize = (width / 15).toFloat()
        createBeeTimeText.textSize = (width / 30).toFloat()

        text6Clock.textSize = (width / 25).toFloat()
        text7Clock.textSize = (width / 25).toFloat()
        text8Clock.textSize = (width / 25).toFloat()
        text9Clock.textSize = (width / 25).toFloat()
        text10Clock.textSize = (width / 25).toFloat()

        gotoStep3FromStep2Button.layoutParams.width = displayMetrics.widthPixels
        gotoStep3FromStep2Button.layoutParams.height = (heightPixel * 0.07f).toInt()
        gotoStep3FromStep2Button.textSize = (width / 25).toFloat()
    }

    // Click Event

    private fun buttonPressed(pressedButton: Int) {
        buttonColorChange(pressedButton)

        if (count == 1) {
            if (!checkNextButtonText) {
                gotoStep3FromStep2Button.isEnabled = false
                gotoStep3FromStep2Button.setTextColor(Color.parseColor("#aaaaaa"))
                gotoStep3FromStep2Button.background =
                    applicationContext.getDrawable(R.color.deactive_button)
                gotoStep3FromStep2Button.text = "미션 종료시간도 선택해주세요 "
            } else {
                gotoStep3FromStep2Button.isEnabled = false
                gotoStep3FromStep2Button.setTextColor(Color.parseColor("#aaaaaa"))
                gotoStep3FromStep2Button.background =
                    applicationContext.getDrawable(R.color.deactive_button)
                gotoStep3FromStep2Button.text = "미션 시작 시간도 선택해주세요 "
            }

        } else if (count == 2) {
            endTime = pressedButton
            gotoStep3FromStep2Button.text = "다음 2/3"
            gotoStep3FromStep2Button.isEnabled = true
            gotoStep3FromStep2Button.setTextColor(Color.parseColor("#222222"))
            gotoStep3FromStep2Button.background =
                applicationContext.getDrawable(R.color.active_button)
        } else if (count == 0) {
            gotoStep3FromStep2Button.text = "다음 2/3"
            gotoStep3FromStep2Button.isEnabled = false
            gotoStep3FromStep2Button.setTextColor(Color.parseColor("#aaaaaa"))
            gotoStep3FromStep2Button.background =
                applicationContext.getDrawable(R.color.deactive_button)
        }
    }

    private fun missionTimeCheck() {
        for (i in 0..4) {
            if (missionTimeArr[i]) {
                if (startTime == 0) {
                    startTime = i + 6
                } else
                    endTime = i + 6
            }
        }
    }

    // View Design

    private fun buttonColorChange(pressedButton: Int) {
        if (pressedButton == 6 && !clock6Button.isSelected && count != 2) {
            clock6Button.isSelected = true
            missionTimeArr[0] = true
            count++
        } else if (pressedButton == 6 && clock6Button.isSelected) {
            clock6Button.isSelected = false
            missionTimeArr[0] = false
            count--
        } else if (pressedButton == 7 && !clock7Button.isSelected && count != 2) {
            clock7Button.isSelected = true
            missionTimeArr[1] = true
            count++
        } else if (pressedButton == 7 && clock7Button.isSelected) {
            clock7Button.isSelected = false
            missionTimeArr[1] = false
            count--
        } else if (pressedButton == 8 && !clock8Button.isSelected && count != 2) {
            clock8Button.isSelected = true
            missionTimeArr[2] = true
            count++
        } else if (pressedButton == 8 && clock8Button.isSelected) {
            clock8Button.isSelected = false
            missionTimeArr[2] = false
            count--
        } else if (pressedButton == 9 && !clock9Button.isSelected && count != 2) {
            clock9Button.isSelected = true
            missionTimeArr[3] = true
            count++
        } else if (pressedButton == 9 && clock9Button.isSelected) {
            clock9Button.isSelected = false
            missionTimeArr[3] = false
            count--
        } else if (pressedButton == 10 && !clock10Button.isSelected && count != 2) {
            clock10Button.isSelected = true
            missionTimeArr[4] = true
            checkNextButtonText = true
            count++
        } else if (pressedButton == 10 && clock10Button.isSelected) {
            clock10Button.isSelected = false
            missionTimeArr[4] = false
            checkNextButtonText = false
            count--
        }
    }

    // Change Activity

    private fun gotoStep3() {
        missionTimeCheck()
        GlobalApp.prefsBeeInfo.startTime = startTime
        GlobalApp.prefsBeeInfo.endTime = endTime
        startActivity(
            Intent(this, CreateStep3Activity::class.java)
        )
    }

    // Companion

    companion object {
        const val TAG = "CreateStep2Activity"
    }
}