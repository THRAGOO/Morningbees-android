package com.jasen.kimjaeseung.morningbees.model.joinbee

data class JoinBeeRequest (
    var beeId : Int,
    var userId : Int,
    var title : String?
)
