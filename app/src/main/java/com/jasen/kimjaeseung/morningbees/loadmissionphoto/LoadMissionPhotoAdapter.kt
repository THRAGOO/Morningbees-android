package com.jasen.kimjaeseung.morningbees.loadmissionphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.Mission
import kotlinx.android.synthetic.main.item_mission_photo.view.*

class LoadMissionPhotoAdapter(
    private val missionList: List<Mission>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemImageView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mission_photo, parent, false)

        val imageViewWidth = GlobalApp.prefsDeviceInfo.widthPixel


        itemImageView.itemMissionPhotoImage.layoutParams.width = (imageViewWidth - 50 * GlobalApp.prefsDeviceInfo.density).toInt()
        itemImageView.itemMissionPhotoImage.layoutParams.height = (imageViewWidth / 3) * 4

        return LoadMissionPhotoViewHolder(itemImageView)
    }

    override fun getItemCount(): Int {
        return missionList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as LoadMissionPhotoViewHolder, position)
    }

    private fun bindDefaultView(holder: LoadMissionPhotoViewHolder, position: Int) {
        holder.bind(missionList[position])
    }

    companion object {
        const val TAG = "LoadMissionPhotoAdpater"
    }
}