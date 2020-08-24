package com.jasen.kimjaeseung.morningbees.missioncreate.model

import android.provider.ContactsContract
import retrofit2.http.Multipart
import java.io.File

data class MissionCreateRequest(
    val image : File,
    val beeId : Int,
    val description : String,
    val type : Int,
    val difficulty : Int) {}