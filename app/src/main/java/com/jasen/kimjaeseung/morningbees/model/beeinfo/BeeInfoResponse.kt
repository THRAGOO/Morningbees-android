package com.jasen.kimjaeseung.morningbees.model.beeinfo

data class BeeInfoResponse(
    val manager : Boolean,
    val nickname : String,
    val startTime : Array<String>,
    val endTime : Array<String>,
    val pay : Int
)