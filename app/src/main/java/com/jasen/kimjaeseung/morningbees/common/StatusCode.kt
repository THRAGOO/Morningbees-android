package com.jasen.kimjaeseung.morningbees.common

//enum class StatusCode (val code : Int) {
//    OK(200),
//    BadRequest(400),
//    InternalServerError(500),
//    Unknown(0);
//
//    val statusCode: StatusCode get() = StatusCode.values().find {it.code == code} ?: StatusCode.Unknown
//}


enum class ResponseCode (val code : Int) {
    success(200),
    created(201)
}

enum class ErrorCode(val errorCode : Int) {
    expiredToken(101),
    badAccess(110);
}

enum class ErrorDescription(val message: String){
    unhandledError("에상치 못한 에러가 발생했습니다."),
    badRequest("서버에 대한 잘못된 요청입니다."),
    internalServerError("예상치 못한 서버 내부 오류가 발생했습니다."),
    foundNull("해당 값을 찾아오지 못했습니다."),

    noToken("토큰이 존재하지 않습니다."),
    unexpectedTokenData("예상치 못한 토큰 데이터입니다.")
}

