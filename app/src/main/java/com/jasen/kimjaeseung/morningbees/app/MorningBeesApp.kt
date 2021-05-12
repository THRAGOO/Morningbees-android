package com.jasen.kimjaeseung.morningbees.app

import android.app.Application
import android.content.Context

class MorningBeesApp : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance : MorningBeesApp? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}