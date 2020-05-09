package com.jasen.kimjaeseung.morningbees.createbee

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import kotlinx.android.synthetic.main.activity_create_step1.*


class CreateStep1Activity:AppCompatActivity(), View.OnClickListener {
    var beename : String? = ""
    lateinit var accessToken : String
    lateinit var refreshToken: String

    private val REQUEST_TEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step1)

        delete_beename_text_button.visibility = View.INVISIBLE

        if (intent.hasExtra("accessToken")) {
            accessToken = intent.getStringExtra("accessToken") }

        if (intent.hasExtra("refreshToken")) {
            refreshToken = intent.getStringExtra("refreshToken") }

        initButtonListeners()
        initEditTextListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_TEST){
            if(resultCode == Activity.RESULT_OK){
                if (intent.hasExtra("beename")) {
                    create_beename_text.setText(intent.getStringExtra("beename"))
                }

                if (intent.hasExtra("accessToken")) {
                    accessToken = intent.getStringExtra("accessToken")
                    //만료된 accessToken
                    //accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb3JuaW5nYmVlcyIsIm5pY2tuYW1lIjoiaGlydSIsImV4cCI6MTY3MTk4MTY2MywidG9rZW5UeXBlIjowLCJpYXQiOjE1ODU1ODE2NjMsInVzZXJJZCI6MjF9.8u5Triq7OVqfcwDVwpscteDCQ1k9ptM13W4f49-zT_I"
                }

                if (intent.hasExtra("refreshToken")) {
                    refreshToken = intent.getStringExtra("refreshToken")
                }

                beename = create_beename_text.text.toString()
                Log.d(TAG, "accessToken: $accessToken")
                Log.d(TAG, "refreshToken: $refreshToken")
            }
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.go_back_main_button -> onBackPressed()
            R.id.info_step1_button->showInfo()
            R.id.create_step1_next_button -> gotoStep2()
            R.id.delete_beename_text_button -> beenameTextDelete()
        }
    }

    private fun initButtonListeners(){
        go_back_main_button.setOnClickListener(this)
        info_step1_button.setOnClickListener(this)
        create_step1_next_button.setOnClickListener(this)
        delete_beename_text_button.setOnClickListener(this)
    }

    private fun initEditTextListeners(){
        create_beename_text.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(edit: Editable) {
                // Text가 바뀌고 동작할 코드
                if(create_beename_text.length()==0){
                    delete_beename_text_button.visibility = View.INVISIBLE
                }
                else
                    delete_beename_text_button.visibility = View.VISIBLE

                if(create_beename_text.length() > 10){
                    beename_textview.setText("10자 이내로 입력해주세요")
                    val strColor = "#AAAAAA"
                    beename_textview.setTextColor(Color.parseColor(strColor))
                    create_step1_next_button.isEnabled = false
                    create_step1_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
                    /*
                    beename_textview.setText("사용불가한 이름입니다.")
                    val strColor = "#ffffffff"
                    beename_textview.setTextColor(Color.parseColor(strColor))
                     */
                }
                else{
                    beename_textview.setText("")
                    create_step1_next_button.isEnabled = true
                    create_step1_next_button.background = applicationContext.getDrawable(R.color.active_button)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력이 끝났을 때 -> 다음 넘어가도 됨
                beename = create_beename_text.text.toString()
            }
        })

    }

    private fun beenameTextDelete(){
        create_beename_text.text = null
        create_step1_next_button.isEnabled = false
        create_step1_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
        Log.d(TAG, "create_beename_text: $create_beename_text.text")
    }


    private fun gotoStep2(){
        val nextIntent = Intent(this, CreateStep2Activity::class.java)
        nextIntent.putExtra("beename",beename)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)
        Log.d(TAG, "beename: ${beename}")

        startActivityForResult(nextIntent, REQUEST_TEST)
    }

    override fun onBackPressed(){
        val nextIntent = Intent(this, BeforeJoinActivity::class.java)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        setResult(Activity.RESULT_OK, nextIntent)
        finish()
    }

    private fun showInfo(){

    }

    //Lifecycle 해서 main으로 돌아가면 저장된 데이터 지우

    companion object {
        private const val TAG = "CreateStep1Activity"
    }
}