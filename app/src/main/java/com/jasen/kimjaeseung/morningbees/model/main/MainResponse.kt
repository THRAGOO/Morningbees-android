package com.jasen.kimjaeseung.morningbees.model.main

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.net.URL


data class MainResponse(
    var missions: JsonArray?,
    var beeInfo: JsonObject?
)

data class Missions (
    val missionId : Int,
    var imageUrl : URL,
    var nickname : String,
    val type : Int,
    val difficulty : Int,
    var createdAt : String,
    val agreeCount : Int,
    val disagreeCount : Int
)

data class TodayQuestioner (
    var nickname : String
)

data class NextQuestioner (
    var nickname: String
)

data class BeeInfos (
    val totalPenalty : Int,
    val memberCounts : Int,
    val todayQuestioner: TodayQuestioner,
    var todayDifficulty : Int?,
    val startTime : Int?,
    val endTime : Int?,
    var title : String,
    var nextQuestioner : NextQuestioner
)
