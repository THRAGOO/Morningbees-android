package com.jasen.kimjaeseung.morningbees.utils

import android.content.Context
import android.widget.Toast

inline fun Context.showToast(text : () -> String){
    Toast.makeText(this, text(), Toast.LENGTH_SHORT).show()
}