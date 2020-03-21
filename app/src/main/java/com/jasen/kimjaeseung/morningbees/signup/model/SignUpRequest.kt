package com.jasen.kimjaeseung.morningbees.signup.model

data class SignUpRequest (
    val socialAccessToken : String,
    val provider : String,
    val nickname : String
){}