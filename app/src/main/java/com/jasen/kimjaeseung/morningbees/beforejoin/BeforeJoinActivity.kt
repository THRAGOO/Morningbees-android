package com.jasen.kimjaeseung.morningbees.beforejoin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.ui.signin.LoginActivity
import kotlinx.android.synthetic.main.activity_beforejoin.*

class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {

    // Life Cycle for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)
        initButtonListeners()
        initBeeInfo()
        initTextView()
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.acceptBeeCreateButton -> gotoBeeCreate()
        }
    }

    override fun onBackPressed() {
        GlobalApp.prefs.socialAccessToken = ""
        GlobalApp.prefs.accessToken = ""
        GlobalApp.prefs.refreshToken = ""
        GlobalApp.prefs.provider = ""

        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    // Init Method

    private fun initBeeInfo(){
        GlobalApp.prefsBeeInfo.beeTitle = ""
        GlobalApp.prefsBeeInfo.startTime = 0
        GlobalApp.prefsBeeInfo.endTime = 0
    }

    private fun initButtonListeners() {
        acceptBeeCreateButton.setOnClickListener(this)
    }

    private fun initTextView(){
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        beforeJoinText1.textSize = (width / 17).toFloat()
        beforeJoinText2.textSize = (width / 28).toFloat()
        beforeJoinText3.textSize = (width / 28).toFloat()

        val heightPixel = displayMetrics.heightPixels
        val widthPixel = displayMetrics.widthPixels
        val heightDp = heightPixel / displayMetrics.density
        val resizeHeight = beforeJoiningImage.layoutParams.height
        beforeJoiningImage.layoutParams.width = (((beforeJoiningImage.layoutParams.width * heightDp * 0.35f) / resizeHeight) * displayMetrics.density).toInt()
        beforeJoiningImage.layoutParams.height = beforeJoiningImage.layoutParams.width

        acceptBeeCreateButton.layoutParams.width = (widthPixel * 0.6f).toInt()
        acceptBeeCreateButton.layoutParams.height = (heightPixel * 0.06f).toInt()
        acceptBeeCreateButton.textSize = (width / 30).toFloat()
    }

    // Change Activity

    private fun gotoBeeCreate() {
        startActivity(
            Intent(this, CreateStep1Activity::class.java)
        )
    }

    // Companion

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val REQUEST_SIGN_IN = 1007
    }
}