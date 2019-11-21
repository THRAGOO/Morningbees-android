package com.jasen.kimjaeseung.morningbees

import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET

data class ResponseDTO(var result:String? = null)

interface MorningBeesService {

    @GET("/nickname")
    fun getRequest(@Query("nickname") nickname: String): Call<ResponseDTO>

    @FormUrlEncoded
    @PUT("/nickname")
    fun putRequest(@Field("nickname")nickname:String):Call<ResponseDTO>

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