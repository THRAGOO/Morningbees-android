package com.jasen.kimjaeseung.morningbees.model.signin

data class SignInRequest(
    val socialAccessToken : String,
    val provider : String
) {}