package com.jasen.kimjaeseung.morningbees.utils

import java.text.DecimalFormat

fun Int.getPriceAnnotation(): String {
    return DecimalFormat("###,###").format(this)
}