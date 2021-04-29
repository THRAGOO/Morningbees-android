package com.jasen.kimjaeseung.morningbees.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.signin_sns_button.view.*

//class SignInSnsButton @JvmOverloads constructor(
//    context: Context, attrs : AttributeSet? = null, defStyleAttr: Int = 0
//) : LinearLayout(context, attrs, defStyleAttr) {
//    init {
////        val inflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
////        val view = inflater.inflate(R.layout.signin_sns_button, this, false)
////        addView(view)
//        context.theme.obtainStyledAttributes(attrs, R.styleable.SignI)
//    }
//}

class SignInSnsButton @JvmOverloads constructor (
    context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        val inflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.signin_sns_button, this, false)
        addView(view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.signInSNSButton,
            0, 0
        ).apply {
            try{
                val symbol = getResourceId(R.styleable.signInSNSButton_signInSnsSymbol, 0)
                val background = getResourceId(R.styleable.signInSNSButton_backgroundSnsButton, 0)
                val text = getString(R.styleable.signInSNSButton_signInSnsText)
                val textColor = getColor(R.styleable.signInSNSButton_signInSnsTextColor, 0)

                signInSnsSymbol.setImageResource(symbol)
                backgroundSnsButton.setBackgroundResource(background)
                signInSnsText.text = text
                signInSnsText.setTextColor(textColor)
            } finally {
                recycle()
            }
        }
    }
}