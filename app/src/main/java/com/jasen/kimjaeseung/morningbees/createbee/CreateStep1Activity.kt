package com.jasen.kimjaeseung.morningbees.createbee

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import kotlinx.android.synthetic.main.activity_create_step1.*

class CreateStep1Activity : AppCompatActivity(), View.OnClickListener {

    // Properties

    var beeTitle = ""

    // Life Cycle for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_step1)

        initButtonListeners()
        initEditTextListeners()
        initTextView()

        beeTitle = GlobalApp.prefsBeeInfo.beeTitle
        create_beename_text.setText(beeTitle)

        beeNameTextInputLayer.setEndIconDrawable(R.drawable.icon_delete_all)
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.gotoSignInFromCreateStep1Button -> onBackPressed()
            R.id.gotoStep2FromStep1Button -> gotoStep2()
        }
    }

    override fun onBackPressed() {
        startActivity(
            Intent(this, BeforeJoinActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    // Init Method

    private fun initButtonListeners() {
        gotoSignInFromCreateStep1Button.setOnClickListener(this)
        gotoStep2FromStep1Button.setOnClickListener(this)
    }

    private fun initTextView(){
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val heightPixel = displayMetrics.heightPixels
        val widthPixel = displayMetrics.widthPixels
        val heightDp = heightPixel / displayMetrics.density

        createStep1Text1.textSize = (width / 15).toFloat()
        createStep1Text2.textSize = (width / 15).toFloat()
        createStep1Text3.textSize = (width / 30).toFloat()

        gotoStep2FromStep1Button.layoutParams.width = displayMetrics.widthPixels
        gotoStep2FromStep1Button.layoutParams.height = (heightPixel * 0.07f).toInt()
        gotoStep2FromStep1Button.textSize = (width / 25).toFloat()
    }

    private fun initEditTextListeners() {
        create_beename_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable) {
                if (create_beename_text.length() > 10) {
                    createStep1BeeNameText.text = "2~10자 이내로 입력해주세요"
                    createStep1BeeNameText.setTextColor(
                        ContextCompat.getColor(
                            this@CreateStep1Activity,
                            R.color.tooLongText
                        )
                    )
                    gotoStep2FromStep1Button.isEnabled = false
                    gotoStep2FromStep1Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoStep2FromStep1Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                } else if (create_beename_text.length() == 1) {
                    createStep1BeeNameText.text = "글자 수가 너무 짧아요."
                    createStep1BeeNameText.setTextColor(
                        ContextCompat.getColor(
                            this@CreateStep1Activity,
                            R.color.tooShortText
                        )
                    )

                    gotoStep2FromStep1Button.isEnabled = false
                    gotoStep2FromStep1Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoStep2FromStep1Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                } else if (create_beename_text.length() == 0) {
                    createStep1BeeNameText.text = ""
                    gotoStep2FromStep1Button.isEnabled = false
                    gotoStep2FromStep1Button.setTextColor(Color.parseColor("#aaaaaa"))
                    gotoStep2FromStep1Button.background =
                        applicationContext.getDrawable(R.color.deactive_button)
                } else {
                    createStep1BeeNameText.text = ""
                    gotoStep2FromStep1Button.isEnabled = true
                    gotoStep2FromStep1Button.setTextColor(Color.parseColor("#222222"))
                    gotoStep2FromStep1Button.background =
                        applicationContext.getDrawable(R.color.active_button)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                beeTitle = create_beename_text.text.toString()
            }
        })
    }

    // Change Acivity

    private fun gotoStep2() {
        GlobalApp.prefsBeeInfo.beeTitle = beeTitle
        startActivity(
            Intent(this, CreateStep2Activity::class.java)
        )
    }

    // Companion

    companion object {
        private const val TAG = "CreateStep1Activity"
    }
}