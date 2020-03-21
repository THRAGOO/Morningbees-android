package com.jasen.kimjaeseung.morningbees.login.model

data class SignInRequest(
    val socialAccessToken : String,
    val provider : String
) {}