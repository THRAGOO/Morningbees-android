package com.jasen.kimjaeseung.morningbees.model.beeinfo

data class BeeInfoResponse(
    val accessToken : String?,
    val isManager : Boolean?,
    val title : String?,
    val missionTitle : String?,
    val totalPay : Int?,
    val todayUser : String?
)