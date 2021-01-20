package com.jasen.kimjaeseung.morningbees.util

import android.content.Context
import com.jasen.kimjaeseung.morningbees.model.renewal.RenewalResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SharedPreference(context: Context) {
    private val fileName = "prefs"
    private val mAccessToken = "AccessToken"
    private val mRefreshToken = "RefreshToken"
    private val prefs = context.getSharedPreferences(fileName, 0)
    private val service = MorningBeesService.create()

    var accessToken: String
        get() = prefs.getString(mAccessToken, "")
        set(value) = prefs.edit().putString(mAccessToken, value).apply()

    var refreshToken: String
        get() = prefs.getString(mRefreshToken, "")
        set(value) = prefs.edit().putString(mRefreshToken, value).apply()

    fun requestRenewalApi() {
        service.renewal(accessToken, refreshToken).enqueue(object : Callback<RenewalResponse> {
            override fun onFailure(call: Call<RenewalResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(call: Call<RenewalResponse>, response: Response<RenewalResponse>) {
                when(response.code()){
                    200 -> {
                        val renewalResponse = response.body()
                        accessToken = renewalResponse!!.accessToken
                    }

                    400 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        Dlog().d(message)
                    }

                    500 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val message = jsonObject.getString("message")
                        Dlog().d(message)
                    }
                }
            }
        })
    }
}