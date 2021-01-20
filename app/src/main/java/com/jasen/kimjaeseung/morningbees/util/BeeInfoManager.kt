package com.jasen.kimjaeseung.morningbees.util

import android.content.Context

class BeeInfoManager(context: Context) {
    private val fileName = "prefsBeeInfo"
    private val mBeeId = "beeId"
    private val mBeeTitle = "beeTitle"
    private val mBeeManagerNickname = "beeManagerNickname"
    private val mMyNickname = "myNickname"
    private val mStartTime = "startTime"
    private val mEndTime = "endTime"

    private val prefsBeeInfo = context.getSharedPreferences(fileName, 0)

    var beeId: Int
        get() = prefsBeeInfo.getInt(mBeeId, 0)
        set(value) = prefsBeeInfo.edit().putInt(mBeeId, value).apply()

    var beeTitle: String
        get() = prefsBeeInfo.getString(mBeeTitle, "")
        set(value) = prefsBeeInfo.edit().putString(mBeeTitle, value).apply()

    var beeManagerNickname: String
        get() = prefsBeeInfo.getString(mBeeManagerNickname, "")
        set(value) = prefsBeeInfo.edit().putString(mBeeManagerNickname, value).apply()

    var startTime: Int
        get() = prefsBeeInfo.getInt(mStartTime, 0)
        set(value) = prefsBeeInfo.edit().putInt(mStartTime, value).apply()

    var endTime: Int
        get() = prefsBeeInfo.getInt(mEndTime, 0)
        set(value) = prefsBeeInfo.edit().putInt(mEndTime, value).apply()
}