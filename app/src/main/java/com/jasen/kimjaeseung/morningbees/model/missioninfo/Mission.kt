package com.jasen.kimjaeseung.morningbees.model.missioninfo

data class Mission (
    var missionid : Int,
    var missionTitle : String,
    var imageUrl : String,
    var nickname : String,
    var type : Int,
    var difficulty : Int,
    var createdAt : String,
    var agreeCount : Int,
    var disagreeCount : Int
)