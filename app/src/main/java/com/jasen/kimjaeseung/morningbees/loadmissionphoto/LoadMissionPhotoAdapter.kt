package com.jasen.kimjaeseung.morningbees.loadmissionphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.missioninfo.Mission

class LoadMissionPhotoAdapter(
    private val missionList: List<Mission>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return missionList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LoadMissionPhotoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mission_photo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as LoadMissionPhotoViewHolder, position)
    }

    private fun bindDefaultView(holder: LoadMissionPhotoViewHolder, position: Int){
        holder.bind(missionList[position])
    }
}