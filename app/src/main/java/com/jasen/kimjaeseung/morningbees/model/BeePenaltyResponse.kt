package com.jasen.kimjaeseung.morningbees.model

import com.google.gson.JsonArray

data class BeePenaltyResponse (
    var penaltyHistories: JsonArray?,
    var penalties: JsonArray?
)