package com.jasen.kimjaeseung.morningbees

import java.lang.Exception

//<out T : Any>에서 out은 상속관계에 있는 클래스들의 관계가 Invariance일 때, Covariance로 만들어주는 키워드입니다.

sealed class Output<out T : Any> {
    data class Success<out T : Any>(val output : T) : Output<T>()
    data class Error(val exception: Exception) : Output<Nothing>()
}