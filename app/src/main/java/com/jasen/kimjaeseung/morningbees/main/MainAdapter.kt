package com.jasen.kimjaeseung.morningbees.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.missionurl.MissionUrl
import kotlinx.android.synthetic.main.item_load_more_mission_button.view.*
import kotlinx.android.synthetic.main.item_mission_participate_button.view.*
import kotlinx.android.synthetic.main.item_mission_participate_image.view.*

class MainAdapter(
    private var urlList: MutableList<MissionUrl?> = mutableListOf(),
    private val listener: OnItemClick
) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return urlList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = urlList[position]
        when (item?.type) {
            MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE -> {
                (holder as ImageViewHolder)
                Glide.with(holder.itemView.context)
                    .load(urlList[position]?.imageUrl)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(50)
                        )
                    )
                    .error(R.drawable.mission_participate_button)
                    .into(holder.participateMissionItem)
            }

            MissionUrl.MISSION_PARTICIPATE_BUTTON_TYPE -> {
                (holder as ParticipateButtonViewHolder)
                holder.participateMissionButtonItem.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        listener.clickMissionParticipate()
                    }
                })
            }

            MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE -> {
                (holder as LoadMoreButtonViewHolder)
                holder.loadMoreButtonItem.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        listener.clickLoadMoreMission()
                    }
                })
            }

            MissionUrl.NO_MISSION_IMAGE_TYPE -> {
                (holder as ImageViewHolder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View?
        return when (viewType) {
            MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mission_participate_image, parent, false)
                ImageViewHolder(view)
            }

            MissionUrl.MISSION_PARTICIPATE_BUTTON_TYPE -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mission_participate_button, parent, false)
                ParticipateButtonViewHolder(view)
            }

            MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_load_more_mission_button, parent, false)
                LoadMoreButtonViewHolder(view)
            }

            MissionUrl.NO_MISSION_IMAGE_TYPE -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mission_participate_image, parent, false)
                ImageViewHolder(view)
            }
            else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return urlList[position]!!.type
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val participateMissionItem = itemView.itemMissionParticipateImage
    }

    inner class LoadMoreButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loadMoreButtonItem = itemView.missionLoadMoreButton
    }

    inner class ParticipateButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val participateMissionButtonItem = itemView.itemMissionParticipateButton
    }

    companion object {
        const val TAG = "MainAdapter"
    }
}



