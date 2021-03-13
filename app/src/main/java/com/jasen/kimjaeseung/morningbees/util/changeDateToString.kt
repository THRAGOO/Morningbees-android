package com.jasen.kimjaeseung.morningbees.util

import java.text.SimpleDateFormat
import java.util.*

fun Date.toString(type: String): String {
    return SimpleDateFormat(type).format(this)
}