package com.jasen.kimjaeseung.morningbees.util

import java.text.DecimalFormat

fun Int.getPriceAnnotation(): String {
    return DecimalFormat("###,###").format(this)
}