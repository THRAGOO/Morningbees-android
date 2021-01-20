package com.jasen.kimjaeseung.morningbees.model.missioninfo

import java.util.*

data class MissionInfoRequest(
    val targetDate : Date,
    val beeId : Int
)