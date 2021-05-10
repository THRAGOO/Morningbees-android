package com.jasen.kimjaeseung.morningbees.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class ErrorResponse(
    var message: String,
    var code: Int
)

data class BeeInfoResponse(
    val manager : Boolean,
    val nickname : String,
    val startTime : Array<String>,
    val endTime : Array<String>,
    val pay : Int
)

data class BeeMemberResponse (
    var members: JsonArray?
)

data class MainResponse(
    var missions: JsonArray?,
    var beeInfo: JsonObject
)

data class BeeInfo (
    val totalPenalty : Int,
    val memberCounts : Int,
    val todayQuestioner : JsonObject,
    val todayDifficulty : Int,
    val startTime : Int, // notion에는 int로 되어있음
    val endTime : Int,
    val title : String,
    val nextQuestioner : JsonObject
)

data class BeePenaltyResponse (
    var penaltyHistories: JsonArray?,
    var penalties: JsonArray?
)

data class MeResponse(
    val nickname : String,
    val alreadyJoin : Boolean?,
    val beeId : Int,
    val userId : Int,
    val email : String
)

data class MissionInfoResponse(
    var missions: JsonArray?
)

data class RenewalResponse(
    val accessToken : String
)

data class SignInResponse (
    val accessToken : String,
    val refreshToken : String,
    val type : Int
)

data class SignUpResponse (
    var accessToken : String = "",
    var refreshToken : String = ""
)

data class ValidNicknameResponse(
    var isValid : Boolean
)