package com.jasen.kimjaeseung.morningbees.createbee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step2.*

class CreateStep2Activity : AppCompatActivity(), View.OnClickListener{
    var firstMissionTime : Int = 0
    var lastMissionTime : Int = 0
    var count = 0
    lateinit var intentBeename : String

    lateinit var accessToken : String
    lateinit var refreshToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step2)

        onStart()
        initButtonListeners()
    }

    override fun onStart() {
        super.onStart()

        if(intent.hasExtra("beename")) {
            intent.getStringExtra("beename")?.let{
                intentBeename = intent.getStringExtra("beename") }
        }
        else
            intentBeename = ""

        if(intent.hasExtra("accessToken")){
            accessToken = intent.getStringExtra("accessToken")
        }

        if(intent.hasExtra("refreshToken")){
            refreshToken = intent.getStringExtra("refreshToken")
        }

        Log.d(TAG, "onStart() intentBeename: $intentBeename")
        Log.d(TAG, "accessToken: $accessToken")
        Log.d(TAG, "refreshToken: $refreshToken")
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.clock_6_button -> buttonPressed(6)
            R.id.clock_7_button -> buttonPressed(7)
            R.id.clock_8_button -> buttonPressed(8)
            R.id.clock_9_button -> buttonPressed(9)
            R.id.clock_10_button -> buttonPressed(10)
            R.id.create_step2_next_button -> gotoStep3()
            R.id.go_back_step1_button -> gotoStep1()
            R.id.info_step2_button ->showInfo()
        }
    }

    private fun initButtonListeners(){
        clock_6_button.setOnClickListener(this)
        clock_7_button.setOnClickListener(this)
        clock_8_button.setOnClickListener(this)
        clock_9_button.setOnClickListener(this)
        clock_10_button.setOnClickListener(this)
        create_step2_next_button.setOnClickListener(this)
        go_back_step1_button.setOnClickListener(this)
    }

    private fun buttonPressed(pressedButton : Int){
        // 더 생각 ㄱ
        buttonColorChange(pressedButton)

        if(count == 1){
            firstMissionTime = pressedButton
            create_step2_next_button.isEnabled = false
            create_step2_next_button.background=applicationContext.getDrawable(R.color.deactive_button)
            create_step2_next_button.text="미션 종료시간도 선택해주세요 "
        }
        else if(count == 2){
            lastMissionTime = pressedButton
            create_step2_next_button.text="다음 2/3"
            create_step2_next_button.isEnabled = true

            if(lastMissionTime < firstMissionTime){
                val tmp = lastMissionTime
                lastMissionTime = firstMissionTime
                firstMissionTime = tmp
            }
            create_step2_next_button.background=applicationContext.getDrawable(R.color.active_button)
        }
        else if(count == 0){
            create_step2_next_button.text="다음 2/3"
            create_step2_next_button.isEnabled = false
            create_step2_next_button.background=applicationContext.getDrawable(R.color.deactive_button)
        }
        else{
            showToast { "미션 수행 시간을 모두 지정하였습니다." }
        }
    }

    private fun buttonColorChange(pressedButton: Int){
        if(pressedButton == 6 && !clock_6_button.isSelected && count != 2){
            clock_6_button.isSelected = true
            count++
        }
        else if(pressedButton == 6 && clock_6_button.isSelected){
            clock_6_button.isSelected = false
            count--
        }

        else if(pressedButton == 7 && !clock_7_button.isSelected && count != 2){
            clock_7_button.isSelected = true
            count++
        }
        else if(pressedButton == 7 && clock_7_button.isSelected){
            clock_7_button.isSelected = false
            count--
        }

        else if(pressedButton == 8 && !clock_8_button.isSelected && count != 2){
            clock_8_button.isSelected = true
            count++
        }
        else if(pressedButton == 8 && clock_8_button.isSelected){
            clock_8_button.isSelected = false
            count--
        }

        else if(pressedButton == 9 && !clock_9_button.isSelected && count != 2){
            clock_9_button.isSelected = true
            count++
        }
        else if(pressedButton == 9 && clock_9_button.isSelected){
            clock_9_button.isSelected=false
            count--
        }

        else if(pressedButton == 10 && !clock_10_button.isSelected && count != 2){
            clock_10_button.isSelected = true
            count++
        }
        else if(pressedButton == 10 && clock_10_button.isSelected){
            clock_10_button.isSelected = false
            count--
        }
    }

    private fun gotoStep3(){
        val nextIntent = Intent(this, CreateStep3Activity::class.java)

        nextIntent.putExtra("beename", intentBeename)
        nextIntent.putExtra("firstMissionTime", firstMissionTime)
        nextIntent.putExtra("lastMissionTime", lastMissionTime)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)
        startActivity(nextIntent)
    }

    private fun gotoStep1(){
        val nextIntent = Intent(this, CreateStep1Activity::class.java)
        nextIntent.putExtra("beename", intentBeename)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)
        Log.d(TAG,"gotoStep1 beename:$intentBeename")
       startActivity(nextIntent)
    }

    private fun showInfo(){

    }

    companion object {
        private const val TAG = "CreateStep2Activity"
    }

}