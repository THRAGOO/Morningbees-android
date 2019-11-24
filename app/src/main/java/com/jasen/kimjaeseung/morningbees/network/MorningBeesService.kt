package com.jasen.kimjaeseung.morningbees.network

import com.jasen.kimjaeseung.morningbees.data.NameValidataionCheckResponse
import com.jasen.kimjaeseung.morningbees.data.SignInRequest
import com.jasen.kimjaeseung.morningbees.data.SignUpResponse
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET


//data class ResponseDTO(var result:String? = null)

interface MorningBeesService {
    @GET("/api/v1/auth/{nickname}/valid")
    fun nameValidationCheck(@Path("nickname") nickname: String?): Call<NameValidataionCheckResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth//sign_in")
    fun signIn(
        @Body signInRequest: SignInRequest
    ): Call<String>

    @FormUrlEncoded
    @POST("/api/v1/auth/sign_up")
    fun signUp(
        @Field("socialAccessToken") socialAccessToken: String,
        @Field("provider") provider: String,
        @Field("nickname") nickname: String
    ): Call<SignUpResponse>



//    @GET("/nickname")
//    fun getRequest(@Query("nickname") nickname: String): Call<ResponseDTO>
//
//    @FormUrlEncoded
//    @PUT("/nickname")
//    fun putRequest(@Field("nickname")nickname:String):Call<ResponseDTO>

    /*
    @GET("/bees/{nickname}")
    fun getParamRequest(@Path("nickname")nickname:String):Call<ResponseDTO>

    //FormData
    //UrlEncoded
    @FormUrlEncoded
    @POST("/bees")
    fun postRequest(@Field("id")id:String,
                    @Field("password")password:String):Call<ResponseDTO>

    @DELETE("/bees/{id}")
    fun deleteRequest(@Path("id")id:String):Call<ResponseDTO>*/
}