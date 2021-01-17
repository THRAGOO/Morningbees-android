package com.jasen.kimjaeseung.morningbees.beforejoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import kotlinx.android.synthetic.main.activity_beforejoin.*

class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var refreshToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)
        initButtonListeners()
        initBeeInfo()
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

    private fun gotoBeeCreate() {
        startActivity(
            Intent(this, CreateStep1Activity::class.java)
        )
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val REQUEST_TEST = 0
    }
}