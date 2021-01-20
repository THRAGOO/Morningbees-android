package com.jasen.kimjaeseung.morningbees.model.signup

data class SignUpRequest (
    val socialAccessToken : String,
    val provider : String,
    val nickname : String
){}