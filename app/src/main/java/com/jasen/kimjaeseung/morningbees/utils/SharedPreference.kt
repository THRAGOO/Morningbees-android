package com.jasen.kimjaeseung.morningbees.utils

import android.content.Context
import androidx.preference.PreferenceManager
//import android.preference.PreferenceManager
import com.jasen.kimjaeseung.morningbees.model.RenewalResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SharedPreference(val context: Context) {
//    private val pref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//    private val service = MorningBeesService.create()

    private val fileName = "prefs"
    private val mAccessToken = "AccessToken"
    private val mRefreshToken = "RefreshToken"
    private val mUserId = "UserId"
    private val mProvider = "Provider"
    private val mSocialAccessToken = "SocialAccessToken"

    var accessToken: String
        get() = prefs.getString(mAccessToken, "").toString()
        set(value) = prefs.edit().putString(mAccessToken, value).apply()

    var refreshToken: String
        get() = prefs.getString(mRefreshToken, "").toString()
        set(value) = prefs.edit().putString(mRefreshToken, value).apply()

    var userId: Int
        get() = prefs.getInt(mUserId, 0)
        set(value) = prefs.edit().putInt(mUserId, value).apply()

    var provider: String
        get() = prefs.getString(mProvider, "").toString()
        set(value) = prefs.edit().putString(mProvider, value).apply()

    var socialAccessToken: String
        get() = prefs.getString(mSocialAccessToken, "").toString()
        set(value) = prefs.edit().putString(mSocialAccessToken, value).apply()
}