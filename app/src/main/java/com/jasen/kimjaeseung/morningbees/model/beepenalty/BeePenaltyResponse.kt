package com.jasen.kimjaeseung.morningbees.model.beepenalty

import com.google.gson.JsonArray

data class BeePenaltyResponse (
    var penaltyHistories: JsonArray?,
    var penalties: JsonArray?
)