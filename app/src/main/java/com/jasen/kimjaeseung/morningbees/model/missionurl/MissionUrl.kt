package com.jasen.kimjaeseung.morningbees.model.missionurl

data class MissionUrl (
    var type : Int,
    val imageUrl : String?,
    val isMyImageUrl : Boolean?
){
    companion object {
        const val MISSION_PARTICIPATE_BUTTON_TYPE = 0
        const val MISSION_PARTICIPATE_IMAGE_TYPE = 1
        const val LOAD_MORE_MISSION_BUTTON_TYPE = 2
        const val NO_MISSION_IMAGE_TYPE = 3
    }
}