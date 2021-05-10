package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.model.*
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface MorningBeesService {
    @GET("/api/auth/valid_nickname")
    suspend fun nameValidationCheck(
        @Query("nickname") nickname: String?
    ): Response<ValidNicknameResponse>

    @Headers("Content-Type:application/json")
    @POST("/api/auth/sign_in")
    suspend fun signIn(
        @Body signInRequest: SignInRequest
    ): Response<SignInResponse>

    @POST("/api/auth/sign_up")
    suspend fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Response<SignUpResponse>

    @POST("/api/bees")
    suspend fun createBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body createBeeRequest: CreateBeeRequest
    ): Response<Void>

    @POST("/api/auth/renewal")
    suspend fun renewal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Header("X-BEES-REFRESH-TOKEN") refreshToken: String
    ): Response<RenewalResponse>

    @GET("/api/auth/me")
    suspend fun me(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Response<MeResponse>

    @POST("/api/bees/join")
    suspend fun joinBee(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Body joinBeeRequest: JoinBeeRequest
    ): Response<Void>

    @Headers("Content-Type:application/json")
    @DELETE("/api/bees/withdrawal")
    suspend fun beeWithdrawal(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String
    ): Response<Void>

    @GET("/api/missions")
    suspend fun missionInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate : String,
        @Query("beeId") beeId: Int
    ): Response<List<Mission>>

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
    ): Response<Void>

    @GET("/api/main")
    suspend fun main(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Query("targetDate") targetDate: String,
        @Query("beeId") beeId: Int
    ): Response<MainResponse>

    /*
    .addCallAdapterFactory(CoroutineCallAdapterFactory()) 사용 시,
    return 타입: Response<MainResponse> -> Deferred<Response<MainResponse>>
    함수 선언: suspend fun -> fun
    일 때, API 응답이 올 때까지 기다리게 하려면 main() 호출하는 곳에 main().await() 처럼 뒤에 await() 함수 사용해주면 됨

    그러나, coroutine 속 suspend 함수 사용하면 자동으로 응답 올 때까지 기다림
    */

    @GET("/api/bees/{id}")
    suspend fun beeInfo(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Response<BeeInfoResponse>

    @GET("/api/bees/{id}/members")
    suspend fun beeMember(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "id", encoded = true) beeId: Int
    ): Response<BeeMemberResponse>

    @GET("/api/bee_penalties/{beeId}")
    suspend fun beePenalty(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "beeId", encoded = true) beeId: Int,
        @Query("status") status: Int
    ): Response<BeePenaltyResponse>

    @POST("/api/bee_penalties/paid/{beeId}")
    suspend fun paid(
        @Header("X-BEES-ACCESS-TOKEN") accessToken: String,
        @Path(value = "beeId", encoded = true) beeId: Int,
        @Body paidRequest : PaidRequest
    ): Response<Void>
}