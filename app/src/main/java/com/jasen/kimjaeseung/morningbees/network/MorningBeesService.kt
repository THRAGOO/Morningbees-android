package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.data.*
import com.jasen.kimjaeseung.morningbees.login.model.SignInResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*


interface MorningBeesService {
    @GET("/api/auth/valid_nickname")
    fun nameValidationCheck(@Query("nickname") nickname: String?): Call<NameValidataionCheckResponse>

    @Headers("Content-Type:application/json")
    @POST("/api/auth/sign_in")
    fun signIn(
        @QueryMap socialAccessToken : Map<String,String>,
        @QueryMap nickname : Map<String,String>
    ): Call<SignInResponse>

    @FormUrlEncoded
    @POST("/api/auth/sign_up")
    fun signUp(
        @Field("socialAccessToken") socialAccessToken: String,
        @Field("provider") provider: String,
        @Field("nickname") nickname: String
    ): Call<SignUpResponse>

    companion object{
        fun create(): MorningBeesService{
            val interceptor = HttpLoggingInterceptor()
                .apply { level=HttpLoggingInterceptor.Level.BODY }
            val client =
                OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl("https://api-morningbees.thragoo.com")
                .build()

            return retrofit.create(MorningBeesService::class.java)
        }
    }
}