package com.jasen.kimjaeseung.morningbees.beforejoin.model

data class MeResponse(
    val nickname : String,
    val alreadyJoin : Boolean,
    val beeId : Int
)