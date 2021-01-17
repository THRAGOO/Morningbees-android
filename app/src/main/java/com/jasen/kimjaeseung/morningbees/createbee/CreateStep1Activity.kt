package com.jasen.kimjaeseung.morningbees.createbee

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import kotlinx.android.synthetic.main.activity_create_step1.*


class CreateStep1Activity : AppCompatActivity(), View.OnClickListener {
    var beeTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step1)

        delete_beename_text_button.visibility = View.INVISIBLE
        initButtonListeners()
        initEditTextListeners()

        beeTitle = GlobalApp.prefsBeeInfo.beeTitle
        create_beename_text.setText(beeTitle)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode == REQUEST_TEST){
//            if(resultCode == Activity.RESULT_OK){
//                if (intent.hasExtra("beename")) {
//                    create_beename_text.setText(intent.getStringExtra("beename"))
//                }
//                beeTitle = create_beename_text.text.toString()
//            }
//        }
//    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.go_back_start_button -> onBackPressed()
            R.id.create_step1_next_button -> gotoStep2()
            R.id.delete_beename_text_button -> beeNameTextDelete()
        }
    }

    private fun initButtonListeners() {
        go_back_start_button.setOnClickListener(this)
        create_step1_next_button.setOnClickListener(this)
        delete_beename_text_button.setOnClickListener(this)
    }

    private fun initEditTextListeners() {
        create_beename_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable) {
                // Text가 바뀌고 동작할 코드
                if (create_beename_text.length() == 0) {
                    delete_beename_text_button.visibility = View.INVISIBLE
                } else
                    delete_beename_text_button.visibility = View.VISIBLE

                if (create_beename_text.length() < 2 || create_beename_text.length() > 10) {
                    beename_textview.text = "2~10자 이내로 입력해주세요"
                    create_step1_next_button.isEnabled = false
                    create_step1_next_button.setTextColor(Color.parseColor("#aaaaaa"))
                    create_step1_next_button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                } else {
                    beename_textview.text = ""
                    create_step1_next_button.isEnabled = true
                    create_step1_next_button.setTextColor(Color.parseColor("#222222"))
                    create_step1_next_button.background =
                        applicationContext.getDrawable(R.color.active_button)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                beeTitle = create_beename_text.text.toString()
            }
        })
    }

    private fun beeNameTextDelete() {
        create_beename_text.text = null
        create_step1_next_button.isEnabled = false
        create_step1_next_button.background =
            applicationContext.getDrawable(R.color.deactive_button)
    }


    private fun gotoStep2() {
        GlobalApp.prefsBeeInfo.beeTitle = beeTitle
        startActivity(
            Intent(this, CreateStep2Activity::class.java)
        )
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, BeforeJoinActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    companion object {
        private const val TAG = "CreateStep1Activity"
    }
}