package com.jasen.kimjaeseung.morningbees.createbee

import android.app.Activity
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

    private var checkNextButtonText : Boolean = false

    var missionTimeArr : Array<Boolean> = arrayOf(false, false, false, false, false, false)

    lateinit var intentBeename : String
    lateinit var accessToken : String
    lateinit var refreshToken: String

    private val REQUEST_TEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step2)
        initButtonListeners()

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
            R.id.go_back_step1_button -> onBackPressed()
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
            if(!checkNextButtonText){
                create_step2_next_button.isEnabled = false
                create_step2_next_button.background=applicationContext.getDrawable(R.color.deactive_button)
                create_step2_next_button.text="미션 종료시간도 선택해주세요 "
            }
            else{
                create_step2_next_button.isEnabled = false
                create_step2_next_button.background=applicationContext.getDrawable(R.color.deactive_button)
                create_step2_next_button.text="미션 시작 시간도 선택해주세요 "
            }

        }
        else if(count == 2){
            lastMissionTime = pressedButton
            create_step2_next_button.text="다음 2/3"
            create_step2_next_button.isEnabled = true
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
            missionTimeArr[0] = true
            count++
        }
        else if(pressedButton == 6 && clock_6_button.isSelected){
            clock_6_button.isSelected = false
            missionTimeArr[0] = false
            count--
        }

        else if(pressedButton == 7 && !clock_7_button.isSelected && count != 2){
            clock_7_button.isSelected = true
            missionTimeArr[1] = true
            count++
        }
        else if(pressedButton == 7 && clock_7_button.isSelected){
            clock_7_button.isSelected = false
            missionTimeArr[1] = false
            count--
        }

        else if(pressedButton == 8 && !clock_8_button.isSelected && count != 2){
            clock_8_button.isSelected = true
            missionTimeArr[2] = true
            count++
        }
        else if(pressedButton == 8 && clock_8_button.isSelected){
            clock_8_button.isSelected = false
            missionTimeArr[2] = false
            count--
        }

        else if(pressedButton == 9 && !clock_9_button.isSelected && count != 2){
            clock_9_button.isSelected = true
            missionTimeArr[3] = true
            count++
        }
        else if(pressedButton == 9 && clock_9_button.isSelected){
            clock_9_button.isSelected=false
            missionTimeArr[3] = false
            count--
        }

        else if(pressedButton == 10 && !clock_10_button.isSelected && count != 2){
            clock_10_button.isSelected = true
            missionTimeArr[4] = true
            checkNextButtonText = true
            count++
        }
        else if(pressedButton == 10 && clock_10_button.isSelected){
            clock_10_button.isSelected = false
            missionTimeArr[4] = false
            checkNextButtonText = false
            count--
        }
    }

    private fun missionTimeCheck(){
        for(i in 0..4){
            if(missionTimeArr[i]){
                if(firstMissionTime == 0){
                    firstMissionTime = i + 6
                }
                else
                    lastMissionTime = i + 6
            }
        }
    }

    private fun gotoStep3(){
        val nextIntent = Intent(this, CreateStep3Activity::class.java)
        missionTimeCheck()
        Log.d(TAG, "firstMissionTime: $firstMissionTime , lastMissionTime: $lastMissionTime")

        nextIntent.putExtra("beename", intentBeename)
        nextIntent.putExtra("firstMissionTime", firstMissionTime)
        nextIntent.putExtra("lastMissionTime", lastMissionTime)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        startActivityForResult(nextIntent, REQUEST_TEST)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val nextIntent = Intent(this, CreateStep1Activity::class.java)
        nextIntent.putExtra("beename", intentBeename)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)
        Log.d(TAG,"gotoStep1 beename:$intentBeename")

        setResult(Activity.RESULT_OK, nextIntent)
        finish()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_TEST){
            if(resultCode == Activity.RESULT_OK){
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

                if(intent.hasExtra("firstMissionTime")){
                    firstMissionTime = intent.getIntExtra("firstMissionTime", 0)
                    buttonPressed(firstMissionTime)
                    Log.d(TAG, "firstMissionTime: $firstMissionTime")
                }

                if(intent.hasExtra("lastMissionTime")){
                    lastMissionTime = intent.getIntExtra("lastMissionTime", 0)
                    buttonPressed(lastMissionTime)
                    Log.d(TAG, "lastMissionTime: $lastMissionTime")
                }

                Log.d(TAG, "onStart() intentBeename: $intentBeename")
                Log.d(TAG, "accessToken: $accessToken")
                Log.d(TAG, "refreshToken: $refreshToken")
            }
        }
    }

    private fun showInfo(){

    }

    companion object {
        private const val TAG = "CreateStep2Activity"
    }

}