package com.jasen.kimjaeseung.morningbees.loadmissionphoto

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

class LoadMissionPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val profileMissionPhoto = itemView.itemProfileMissionPhoto
    private val nicknameMissionPhoto = itemView.itemNicknameMissionPhoto
    private val imageMissionPhoto = itemView.itemMissionPhotoImage

    fun bind(mission : Mission){
//        setProfileImage(mission.imageUrl)
        nicknameMissionPhoto.text = mission.nickname
        setMissionImage(mission.imageUrl)
    }

//    private fun setProfileImage(imageUrl : String){
//        Glide.with(itemView.context)
//            .load(imageUrl)
//            .centerCrop()
//            .into(profileMissionPhoto)
//    }

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
}