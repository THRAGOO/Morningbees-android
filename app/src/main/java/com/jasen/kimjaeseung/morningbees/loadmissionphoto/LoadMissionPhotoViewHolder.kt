package com.jasen.kimjaeseung.morningbees.loadmissionphoto

import android.util.TypedValue
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

class LoadMissionPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nicknameMissionPhoto = itemView.itemNicknameMissionPhoto
    private val imageMissionPhoto = itemView.itemMissionPhotoImage
    private val afterTimeMissionPhoto = itemView.itemAfterTimeMissionPhoto

    fun bind(mission : Mission){
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
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, itemView.context.resources.displayMetrics).toInt()

        Glide.with(itemView.context)
            .load(imageUrl)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(px)
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