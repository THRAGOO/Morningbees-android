package com.jasen.kimjaeseung.morningbees.utils

import java.text.SimpleDateFormat
import java.time.LocalDate

fun LocalDate.toString(type: String): String {
    return SimpleDateFormat(type).format(this)
}

