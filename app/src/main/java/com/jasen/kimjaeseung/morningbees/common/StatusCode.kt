package com.jasen.kimjaeseung.morningbees.common

enum class StatusCode (val code : Int) {
    OK(200),
    BadRequest(400),
    InternalServerError(500)
}