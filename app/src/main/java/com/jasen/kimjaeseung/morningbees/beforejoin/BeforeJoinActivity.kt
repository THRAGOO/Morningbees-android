package com.jasen.kimjaeseung.morningbees.beforejoin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import kotlinx.android.synthetic.main.activity_beforejoin.*

class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var refreshToken : String
    lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)

        initButtonListeners()
        onStart()
    }

    override fun onStart() {
        super.onStart()

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
           R.id.beecreate_button -> gotoBeeCreate()
        }
    }

    private fun initButtonListeners(){
        beecreate_button.setOnClickListener(this)
    }

    private fun gotoBeeCreate(){
        val nextIntent: Intent = Intent(this, CreateStep1Activity::class.java)

        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        startActivity(nextIntent)
    }

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val RC_SIGN_IN = 9001
        private const val RC_GET_TOKEN = 9002
    }
}