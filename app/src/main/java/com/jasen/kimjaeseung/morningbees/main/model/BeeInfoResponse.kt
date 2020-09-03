package com.jasen.kimjaeseung.morningbees.main.model

data class BeeInfoResponse(
    val accessToken : String, // if usr is manager
    val isManager : Boolean,
    val title : String,
    val missionTitle : String,
    val totalPay : Int,
    val todayUser : String
)