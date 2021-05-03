package com.jasen.kimjaeseung.morningbees.model

import okhttp3.MultipartBody

data class BeeInfoRequest (
    val beeId : Int
)

data class MainRequest (
    val targetDate: String
)

data class SignInRequest(
    val socialAccessToken : String,
    val provider : String
)

data class BeePenaltyRequest(
    val beeId : Int,
    val status : Int
)

data class CreateBeeRequest (
    val title : String,
    val startTime : Int,
    val endTime : Int,
    val pay : Int,
    val description : String
)

data class JoinBeeRequest (
    var beeId : Int,
    var userId : Int,
    var title : String?
)

data class MissionCreateRequest(
    val image: MultipartBody.Part,
    val beeId: Int,
    val description: String,
    val type: Int,
    val difficulty: Int,
    val targetDate: String
)

data class MissionInfoRequest (
    val targetDate : String,
    val beeId : Int
)

data class PaidRequest (
    var penalties : List<Paid>
)

data class SignUpRequest (
    val socialAccessToken : String,
    val provider : String,
    val nickname : String
)

data class ValidNicknameRequest(
    var nickname : String
)