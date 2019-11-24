package com.jasen.kimjaeseung.morningbees.data

data class SignInRequest (
    var socialAccessToken : String,
    var provider : String
)