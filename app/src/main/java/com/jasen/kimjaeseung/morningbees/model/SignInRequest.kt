package com.jasen.kimjaeseung.morningbees.model

data class SignInRequest(
    val socialAccessToken : String,
    val provider : String
) {}