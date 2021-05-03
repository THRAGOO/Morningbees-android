package com.jasen.kimjaeseung.morningbees.model

data class MeResponse(
    val nickname : String,
    val alreadyJoin : Boolean?,
    val beeId : Int,
    val userId : Int,
    val email : String
)