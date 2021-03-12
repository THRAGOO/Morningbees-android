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
    private val mSelectedPartPayment = "selectedPartPayment"
    private val mUnPaidPenalty = "unPaidPenalty"
    private val mPaidPenalty = "paidPenalty"
    private val mSelectedUserId = "selectedUserId"
    private val mMyEmail = "myEmail"

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

    var selectedPartPayment: Int
        get() = prefsBeeInfo.getInt(mSelectedPartPayment, 0)
        set(value) = prefsBeeInfo.edit().putInt(mSelectedPartPayment, value).apply()

    var selectedUserId: Long
        get() = prefsBeeInfo.getLong(mSelectedUserId, 0)
        set(value) = prefsBeeInfo.edit().putLong(mSelectedUserId, value).apply()

    var unPaidPenalty: Int
        get() = prefsBeeInfo.getInt(mUnPaidPenalty, 0)
        set(value) = prefsBeeInfo.edit().putInt(mUnPaidPenalty, value).apply()

    var paidPenalty: Int
        get() = prefsBeeInfo.getInt(mPaidPenalty, 0)
        set(value) = prefsBeeInfo.edit().putInt(mPaidPenalty, value).apply()

    var myNickname: String
        get() = prefsBeeInfo.getString(mMyNickname, "")
        set(value) = prefsBeeInfo.edit().putString(mMyNickname, value).apply()

    var myEmail: String
        get() = prefsBeeInfo.getString(mMyEmail, "")
        set(value) = prefsBeeInfo.edit().putString(mMyEmail, value).apply()
}