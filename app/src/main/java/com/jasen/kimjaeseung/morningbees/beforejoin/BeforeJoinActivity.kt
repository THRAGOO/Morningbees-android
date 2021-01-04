package com.jasen.kimjaeseung.morningbees.beforejoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import kotlinx.android.synthetic.main.activity_beforejoin.*

class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var refreshToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)
        initButtonListeners()
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
        startActivityForResult(
            Intent(this, CreateStep1Activity::class.java), REQUEST_TEST
        )
    }

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val REQUEST_TEST = 0
    }
}