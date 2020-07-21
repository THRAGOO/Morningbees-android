package com.jasen.kimjaeseung.morningbees.network

import androidx.media.AudioAttributesCompat
import com.jasen.kimjaeseung.morningbees.beforejoin.model.MeResponse
import com.jasen.kimjaeseung.morningbees.createbee.model.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.createbee.model.RenewalResponse
import com.jasen.kimjaeseung.morningbees.invitebee.model.JoinBeeResponse
import com.jasen.kimjaeseung.morningbees.login.model.SignInRequest
import com.jasen.kimjaeseung.morningbees.login.model.SignInResponse
import com.jasen.kimjaeseung.morningbees.main.model.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.main.model.MissionInfoRequest
import com.jasen.kimjaeseung.morningbees.main.model.MissionInfoResponse
import com.jasen.kimjaeseung.morningbees.missioncreate.model.MissionCreateRequest
import com.jasen.kimjaeseung.morningbees.signup.model.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpRequest
import com.jasen.kimjaeseung.morningbees.signup.model.SignUpResponse
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers
import java.io.File


interface MorningBeesService {

    @GET("/api/auth/valid_nickname")
    fun nameValidationCheck(
        @Query("nickname") nickname: String?
    ): Call<NameValidataionCheckResponse>

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

    @POST("/api/bees")
    fun createBee(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String,
        @Body createBeeRequest : CreateBeeRequest
    ): Call<Void>

    @POST("/api/auth/renewal")
    fun renewal(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String,
        @Header("X-BEES-REFRESH-TOKEN") refreshToken : String
    ):Call<RenewalResponse>

    @GET("/api/auth/me")
    fun me(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String
    ):Call<MeResponse>

    @GET("/api/bees/join")
    fun joinBee(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String
    ): Call<JoinBeeResponse>

    @Headers("Content-Type:application/json")
    @DELETE("/api/bees/withdrawal")
    fun withdrawalBee(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String
    ): Call<Void>

    @GET("/api/my_bee")
    fun beeInfo(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String
    ): Call<BeeInfoResponse>

    @GET("/api/my_bee/mission")
    fun missionInfo(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String,
        @Query("targetDate") targetDate: String
    ): Call<MissionInfoResponse>

    @Multipart
    @POST("/api/missions")
    fun missionCreate(
        @Header ("X-BEES-ACCESS-TOKEN") accessToken : String,
        @Part ("image") image :  File,
        @Part ("beeId") beeId : Int,
        @Part ("description") description : String,
        @Part ("type") type : Int,
        @Part ("difficult") difficult : Int
        //@Body missionCreateRequest: MissionCreateRequest
    ): Call<Void>


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