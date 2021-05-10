package com.jasen.kimjaeseung.morningbees.common

import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import java.lang.Exception

//<out T : Any>에서 out은 상속관계에 있는 클래스들의 관계가 Invariance일 때, Covariance로 만들어주는 키워드입니다.

// 네트워크 상태 노출
// 데이터와 데이터 상태를 모두 캡슐화

sealed class Output<out T : Any> {
    data class Success<out T : Any>(val output : T) : Output<T>()
//    data class Error(val exception: Exception) : Output<Nothing>()
    data class Error(val code: Int? = null, val error: ErrorResponse? = null) : Output<Nothing>()
    object NetworkError: Output<Nothing>()
//    object InProgress: Output<Nothing>()
}