package com.jasen.kimjaeseung.morningbees.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.net.URL

data class MainResponse(
    var missions: JsonArray?,
    var beeInfo: JsonObject?
)