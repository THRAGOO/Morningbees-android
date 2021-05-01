package com.jasen.kimjaeseung.morningbees.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// object 키워드 사용함으로써 싱글톤 구현

object NetworkModule {
    private val BASE_URL = "https://api-morningbees.thragoo.com"

    private val interceptor = HttpLoggingInterceptor()
        .apply { level = HttpLoggingInterceptor.Level.BODY }
        .apply {
        }

    private val client =
        OkHttpClient.Builder().addInterceptor(interceptor).build()

    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
    }

    val morningBeesService: MorningBeesService by lazy {
        getInstance().create(MorningBeesService::class.java)
    }
}