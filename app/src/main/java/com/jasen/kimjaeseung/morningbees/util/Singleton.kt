package com.jasen.kimjaeseung.morningbees.util

import com.jasen.kimjaeseung.morningbees.model.renewal.RenewalResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Singleton private constructor() {
//class TokenManager private constructor(var accessToken: String, var refreshToken: String) {


    companion object {
        @Volatile
        private var instance: Singleton? = null
        private val service = MorningBeesService.create()
        var mAccessToken: String = ""
        var mRefreshToken: String = ""

        fun getInstance(): Singleton =
            instance ?: synchronized(this) {
                instance ?: Singleton().also {
                    instance = it
                }
            }

        fun setToken(accessToken: String, refreshToken: String) {
            mAccessToken = accessToken
            mRefreshToken = refreshToken
        }

        fun getRefreshToken(): String {
            if (instance == null)
                return ""
            else
                return mRefreshToken
        }

        fun getAccessToken(): String {
            if (instance == null)
                return ""
            else
                return mAccessToken
        }

        fun requestRenewal(_accessToken: String, _refreshToken: String): String {
            service.renewal(_accessToken, _refreshToken)
                .enqueue(object : Callback<RenewalResponse> {
                    override fun onFailure(call: Call<RenewalResponse>, t: Throwable) {
                        Dlog().d(t.toString())
                    }

                    override fun onResponse(
                        call: Call<RenewalResponse>,
                        response: Response<RenewalResponse>
                    ) {
                        when(response.code()){
                            200 -> {
                                val renewalResponse = response.body()
                                mAccessToken = renewalResponse!!.accessToken
                            }

                            400 -> {
                                // login 화면으로 이동해, 다시 로그인
                            }

                            500 -> {
                                // 400 과 동일
                            }
                        }

                    }
                })
            return mAccessToken
        }
    }
}

