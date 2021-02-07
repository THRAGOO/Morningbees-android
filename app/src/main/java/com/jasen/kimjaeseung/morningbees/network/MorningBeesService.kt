package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.createbee.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.model.renewal.RenewalResponse
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.signin.SignInRequest
import com.jasen.kimjaeseung.morningbees.model.signin.SignInResponse
import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMemberResponse
import com.jasen.kimjaeseung.morningbees.model.beepenalty.BeePenaltyResponse
import com.jasen.kimjaeseung.morningbees.model.main.MainResponse
import com.jasen.kimjaeseung.morningbees.model.missioninfo.Mission
import com.jasen.kimjaeseung.morningbees.model.missioninfo.MissionInfoResponse
import com.jasen.kimjaeseung.morningbees.model.namevalidationcheck.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface MorningBeesService {
    @GET("/api/auth/valid_nickname")
    fun nameValidationCheck(
        @Query("nickname") nickname: String?
    ): Call<NameValidataionCheckResponse>

    @Headers("Content-Type:application/json")
    @POST("/api/auth/sign_in")
    fun signIn(
        @Body signInRequest: SignInRequest
    ): Call<SignInResponse>

    @POST("/api/auth/sign_up")
    fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Call<SignUpResponse>

    @POST("/api/bees")
    fun createBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body createBeeRequest: CreateBeeRequest
    ): Call<Void>

    @POST("/api/auth/renewal")
    fun renewal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Header("X-BEES-REFRESH-TOKEN") refreshToken: String
    ): Call<RenewalResponse>

    @GET("/api/auth/me")
    fun me(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Call<MeResponse>

    @POST("/api/bees/join")
    fun joinBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body joinBeeRequest: JoinBeeRequest
    ): Call<Void>

    @Headers("Content-Type:application/json")
    @DELETE("/api/bees/withdrawal")
    fun beeWithdrawal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Call<Void>

    @GET("/api/missions")
    fun missionInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate : String,
        @Query("beeId") beeId: Int
    ): Call<List<Mission>>

    @Multipart
    @POST("/api/missions")
    fun missionCreate(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Part image: MultipartBody.Part,
        @Part("beeId") beeId: Int,
        @Part("description") description: String,
        @Part("type") type: Int,
        @Part("difficulty") difficulty: Int,
        @Part("targetDate") targetDate: String
    ): Call<Void>

    @GET("/api/main")
    fun main(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate: String,
        @Query("beeId") beeId: Int
    ): Call<MainResponse>

    @GET("/api/bees/{id}")
    fun beeInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Call<BeeInfoResponse>

    @GET("/api/bees/{id}/members")
    fun beeMember(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Call<BeeMemberResponse>

    @GET("/api/bee_penalties/{beeId}")
    fun beePenalty(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "beeId", encoded = true) beeId: Int,
        @Query("status") status: Int
    ): Call<BeePenaltyResponse>

    companion object {
        private val interceptor = HttpLoggingInterceptor()
            .apply { level = HttpLoggingInterceptor.Level.BODY }
            .apply {
            }

        private val client =
            OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl("https://api-morningbees.thragoo.com")
            .build()


        fun create(): MorningBeesService {
            return retrofit.create(MorningBeesService::class.java)
        }
    }
}