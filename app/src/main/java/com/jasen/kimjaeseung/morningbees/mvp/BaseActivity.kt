package com.jasen.kimjaeseung.morningbees.mvp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPresenter()
    }

    abstract fun initPresenter()

}
