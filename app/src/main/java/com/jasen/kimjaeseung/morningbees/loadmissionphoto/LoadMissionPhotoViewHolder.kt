package com.jasen.kimjaeseung.morningbees.loadmissionphoto

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.missioninfo.Mission
import kotlinx.android.synthetic.main.item_mission_photo.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LoadMissionPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val profileMissionPhoto = itemView.itemProfileMissionPhoto
    private val nicknameMissionPhoto = itemView.itemNicknameMissionPhoto
    private val imageMissionPhoto = itemView.itemMissionPhotoImage
    private val afterTimeMissionPhoto = itemView.itemAfterTimeMissionPhoto

    fun bind(mission : Mission){
//        setProfileImage(mission.imageUrl)
        nicknameMissionPhoto.text = mission.nickname
        setMissionImage(mission.imageUrl)
        afterTimeMissionPhoto.text = setAfterTimeText(mission.createdAt)
    }

    private fun setAfterTimeText(missionDate : String) : String{
        var missionDate = missionDate
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(missionDate)

        val curTime = System.currentTimeMillis()
        val regTime = date.time
        var diffTime = (curTime - regTime) / 1000

        if (diffTime < SEC){
            missionDate = diffTime.toString() + "초 전"
        } else {
            diffTime /= SEC
            if(diffTime < MIN){
                missionDate = diffTime.toString() + "분 전"
            } else {
                diffTime /= MIN
                if (diffTime < HOUR){
                    missionDate = diffTime.toString() + "시간 전"
                } else {
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    missionDate = format.format(date)
                }
            }
        }
        return missionDate
    }

    private fun setMissionImage(imageUrl: String){
        Glide.with(itemView.context)
            .load(imageUrl)
            .apply(RequestOptions().override(327, 407))
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(30)
                )
            )
            .error(R.drawable.not_upload_mission_img_view)
            .into(imageMissionPhoto)
    }

    companion object {
        const val TAG = "LoadMissionPhotoViewHolder"
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 7
        const val MONTH = 12
    }
}