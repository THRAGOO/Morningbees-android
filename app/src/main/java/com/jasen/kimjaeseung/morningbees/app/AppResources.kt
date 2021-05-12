package com.jasen.kimjaeseung.morningbees.app

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes

object AppResources {
    // 앱 리소스 관련 요소들을 하나의 파일에서 관리 가능
    @JvmStatic
    fun getContext() : Context {
        return MorningBeesApp.applicationContext()
    }

    /*
    * static 변수의 get/set 함수를 자동으로 만들라는 의미
    * JvmStatic 어노테이션 없으면 AppResource.INSTANCE.getContext() 접근 가능!
    * 어노테이션 없으면 AppResource.getContext() 하면 에러남 (근데 런타임에 나는듯?)
    */

    @JvmStatic
    fun getResources() : Resources {
        return getContext().resources
    }

    @JvmStatic
    fun getStringResId(@StringRes resId : Int): String {
        return getResources().getString(resId)
    }
}