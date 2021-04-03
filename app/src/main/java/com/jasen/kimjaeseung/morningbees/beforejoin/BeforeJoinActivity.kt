package com.jasen.kimjaeseung.morningbees.beforejoin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import kotlinx.android.synthetic.main.activity_beforejoin.*
import kotlinx.android.synthetic.main.activity_login.*

class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)
        initButtonListeners()
        initBeeInfo()
        initImageView()
        initTextView()
    }

    private fun initBeeInfo(){
        GlobalApp.prefsBeeInfo.beeTitle = ""
        GlobalApp.prefsBeeInfo.startTime = 0
        GlobalApp.prefsBeeInfo.endTime = 0
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.accept_beecreate_button -> gotoBeeCreate()
        }
    }

    private fun initButtonListeners() {
        accept_beecreate_button.setOnClickListener(this)
    }

    private fun initImageView(){
    }

    private fun initTextView(){
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        before_joining_textview1.textSize = (width / 17).toFloat()
        before_joining_textview2.textSize = (width / 28).toFloat()
        before_joining_textview3.textSize = (width / 28).toFloat()

        val heightPixel = displayMetrics.heightPixels
        val widthPixel = displayMetrics.widthPixels
        val heightDp = heightPixel / displayMetrics.density
        val resizeHeight = illust_before_joining_ImageView.layoutParams.height
        illust_before_joining_ImageView.layoutParams.width = (((illust_before_joining_ImageView.layoutParams.width * heightDp * 0.35f) / resizeHeight) * displayMetrics.density).toInt()
        illust_before_joining_ImageView.layoutParams.height = illust_before_joining_ImageView.layoutParams.width

        accept_beecreate_button.layoutParams.width = (widthPixel * 0.6f).toInt()
        accept_beecreate_button.layoutParams.height = (heightPixel * 0.06f).toInt()
        accept_beecreate_button.textSize = (width / 30).toFloat()
    }

    private fun gotoBeeCreate() {
        startActivity(
            Intent(this, CreateStep1Activity::class.java)
        )
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

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val REQUEST_SIGN_IN = 1007
    }
}