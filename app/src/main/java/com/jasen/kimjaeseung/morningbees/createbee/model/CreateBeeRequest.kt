package com.jasen.kimjaeseung.morningbees.createbee.model

data class CreateBeeRequest (
    val title : String,
    val startTime : Int,
    val endTime : Int,
    val pay : Int,
    val description : String
){}