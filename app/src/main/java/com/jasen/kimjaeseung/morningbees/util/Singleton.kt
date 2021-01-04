package com.jasen.kimjaeseung.morningbees.util

class Singleton private constructor(val accessToken : String, val refreshToken: String){
    companion object {
        @Volatile private var instance : Singleton? = null
        @Volatile private var accessToken : String? = null
        @Volatile private var refreshToken : String? = null


        fun getInstance(_accessToken: String, _refreshToken : String): Singleton =
            instance ?: synchronized(this) {
                instance ?: Singleton(_accessToken, _refreshToken).also {
                    instance = it
                    accessToken = _accessToken
                    refreshToken = _refreshToken
                }
            }

        fun getRefreshToken() : String {
            if (instance == null)
                return ""
            else
                return instance!!.refreshToken
        }

        fun getAccessToken() : String {
            if (instance == null)
                return ""
            else
                return instance!!.accessToken
        }
    }
}

