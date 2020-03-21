package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.login.model.SignInRequest
import com.jasen.kimjaeseung.morningbees.login.model.SignInResponse
import com.jasen.kimjaeseung.morningbees.signup.model.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpRequest
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers


interface MorningBeesService {
    @GET("/api/auth/valid_nickname")
    fun nameValidationCheck(@Query("nickname") nickname: String?): Call<NameValidataionCheckResponse>
    /*
    fun nameValidationCheck(
        @QueryMap nickname: Map<String, String>
    ):Call<NameValidataionCheckResponse>
    */

    @Headers("Content-Type:application/json")
    @POST("/api/auth/sign_in")
    fun signIn(
        @Body signInRequest: SignInRequest
    ): Call<SignInResponse>

    @POST("/api/auth/sign_up")
    fun signUp(
        @Body signUpRequest: SignUpRequest
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