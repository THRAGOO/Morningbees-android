package com.jasen.kimjaeseung.morningbees.model

import android.provider.ContactsContract
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import java.io.File

data class MissionCreateRequest(
    val image: MultipartBody.Part,
    val beeId: Int,
    val description: String,
    val type: Int,
    val difficulty: Int,
    val targetDate: String
)