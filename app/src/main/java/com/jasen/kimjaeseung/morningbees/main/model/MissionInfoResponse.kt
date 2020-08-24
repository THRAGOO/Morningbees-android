package com.jasen.kimjaeseung.morningbees.main.model

import retrofit2.http.Url

class MissionInfoResponse{
    class missions{
        lateinit var image_url : Url
        lateinit var desc : String
        lateinit var nickname : String
        val agreeCount : Int = 0
        val disagreeCount : Int = 0
    }
}