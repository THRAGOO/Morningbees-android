package com.jasen.kimjaeseung.morningbees

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET

data class ResponseDTO(var result:String? = null)

interface MorningBeesService {

    @GET("/name"    )
    fun getRequest(@Query("name") nickname: String): Call<ResponseDTO>

    @GET("30")
    fun read(@Path("no") no: String): Call<JsonObject>

    @FormUrlEncoded
    @PUT("/name")
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