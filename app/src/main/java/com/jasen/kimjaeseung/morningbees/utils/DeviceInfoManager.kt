package com.jasen.kimjaeseung.morningbees.utils

import android.content.Context

class DeviceInfoManager (context: Context){
    private val fileName = "prefsDeviceInfo"
    private val mDisplayMetrics = "displayMetrics"

    private val mHeightPixel = "heightPixel"
    private val mWidthPixel = "widthPixel"
    private val mDensity = "density"

    private val prefsDeviceInfo = context.getSharedPreferences(fileName, 0)

    var heightPixel: Int
        get() = prefsDeviceInfo.getInt(mHeightPixel, 0)
        set(value) = prefsDeviceInfo.edit().putInt(mHeightPixel, value).apply()

    var widthPixel: Int
        get() = prefsDeviceInfo.getInt(mWidthPixel, 0)
        set(value) = prefsDeviceInfo.edit().putInt(mWidthPixel, value).apply()

    var density: Float
        get() = prefsDeviceInfo.getFloat(mDensity, 0f)
        set(value) = prefsDeviceInfo.edit().putFloat(mDensity, value).apply()
}