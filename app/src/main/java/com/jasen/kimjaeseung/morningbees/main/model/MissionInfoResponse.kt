package com.jasen.kimjaeseung.morningbees.main.model

data class MissionInfoResponse(
    val image_url : String?,
    val desc : String?,
    val nickname : String?,
    val agreeCount : Int?,
    val disagreeCount : Int?
) {}