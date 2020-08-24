package com.jasen.kimjaeseung.morningbees.main.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

import java.util.*
import kotlin.collections.ArrayList


data class MainResponse(
    var missions: JsonArray?,
    var beeInfos: JsonObject?
    //var missions: List<Missions>,
    //var beeInfos: BeeInfos
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
