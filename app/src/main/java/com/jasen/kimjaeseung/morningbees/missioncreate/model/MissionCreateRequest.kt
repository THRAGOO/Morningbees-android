package com.jasen.kimjaeseung.morningbees.missioncreate.model

import android.provider.ContactsContract
import java.io.File

data class MissionCreateRequest(
    val image : ByteArray,
    val beeId : Int,
    val description : String,
    val type : Int,
    val difficulty : Int) {}