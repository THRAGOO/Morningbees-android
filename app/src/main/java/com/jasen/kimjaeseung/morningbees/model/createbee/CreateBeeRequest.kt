package com.jasen.kimjaeseung.morningbees.model.createbee

data class CreateBeeRequest (
    val title : String,
    val startTime : Int,
    val endTime : Int,
    val pay : Int,
    val description : String
){}