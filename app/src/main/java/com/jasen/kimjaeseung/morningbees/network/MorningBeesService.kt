package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMemberResponse
import com.jasen.kimjaeseung.morningbees.model.beepenalty.BeePenaltyResponse
import com.jasen.kimjaeseung.morningbees.model.createbee.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.model.main.MainResponse
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.missioninfo.Mission
import com.jasen.kimjaeseung.morningbees.model.namevalidationcheck.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.model.paid.PaidRequest
import com.jasen.kimjaeseung.morningbees.model.renewal.RenewalResponse
import com.jasen.kimjaeseung.morningbees.model.signin.SignInRequest
import com.jasen.kimjaeseung.morningbees.model.signin.SignInResponse
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface MorningBeesService {
    @GET("/api/auth/valid_nickname")
    suspend fun nameValidationCheck(
        @Query("nickname") nickname: String?
    ): Call<NameValidataionCheckResponse>

    @Headers("Content-Type:application/json")
    @POST("/api/auth/sign_in")
    suspend fun signIn(
        @Body signInRequest: SignInRequest
    ): Call<SignInResponse>

    @POST("/api/auth/sign_up")
    suspend fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Call<SignUpResponse>

    @POST("/api/bees")
    suspend fun createBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body createBeeRequest: CreateBeeRequest
    ): Call<Void>

    @POST("/api/auth/renewal")
    suspend fun renewal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Header("X-BEES-REFRESH-TOKEN") refreshToken: String
    ): Call<RenewalResponse>

    @GET("/api/auth/me")
    suspend fun me(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Call<MeResponse>

    @POST("/api/bees/join")
    suspend fun joinBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body joinBeeRequest: JoinBeeRequest
    ): Call<Void>

    @Headers("Content-Type:application/json")
    @DELETE("/api/bees/withdrawal")
    suspend fun beeWithdrawal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Call<Void>

    @GET("/api/missions")
    suspend fun missionInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate : String,
        @Query("beeId") beeId: Int
    ): Call<List<Mission>>

    @Multipart
    @POST("/api/missions")
    suspend fun missionCreate(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Part image: MultipartBody.Part,
        @Part("beeId") beeId: Int,
        @Part("description") description: String,
        @Part("type") type: Int,
        @Part("difficulty") difficulty: Int,
        @Part("targetDate") targetDate: String
    ): Call<Void>

    @GET("/api/main")
    suspend fun main(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate: String,
        @Query("beeId") beeId: Int
    ): Call<MainResponse>

    @GET("/api/bees/{id}")
    suspend fun beeInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Call<BeeInfoResponse>

    @GET("/api/bees/{id}/members")
    suspend fun beeMember(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Call<BeeMemberResponse>

    @GET("/api/bee_penalties/{beeId}")
    suspend fun beePenalty(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "beeId", encoded = true) beeId: Int,
        @Query("status") status: Int
    ): Call<BeePenaltyResponse>

    @POST("/api/bee_penalties/paid/{beeId}")
    suspend fun paid(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "beeId", encoded = true) beeId: Int,
        @Body paidRequest : PaidRequest
    ): Call<Void>

    companion object {

    }
}