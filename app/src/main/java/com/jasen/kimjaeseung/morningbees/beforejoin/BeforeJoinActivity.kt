package com.jasen.kimjaeseung.morningbees.beforejoin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import kotlinx.android.synthetic.main.activity_beforejoin.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var refreshToken : String
    lateinit var accessToken: String

    private val service = MorningBeesService.create()

    private val REQUEST_TEST = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)

        if(intent.hasExtra("accessToken")){
            accessToken = intent.getStringExtra("accessToken") }

        if(intent.hasExtra("refreshToken")){
            refreshToken = intent.getStringExtra("refreshToken") }

        initButtonListeners()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
           R.id.accept_beecreate_button -> gotoBeeCreate()
        }
    }

    private fun initButtonListeners(){
        accept_beecreate_button.setOnClickListener(this)
    }

    private fun gotoBeeCreate(){
        val nextIntent: Intent = Intent(this, CreateStep1Activity::class.java)

        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        //startActivity(nextIntent)
        startActivityForResult(nextIntent, REQUEST_TEST)
    }

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val RC_SIGN_IN = 9001
        private const val RC_GET_TOKEN = 9002
    }
}