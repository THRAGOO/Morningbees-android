package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_mission_participate.view.*

class MissionParticipateAdapter(
    private val urlList : MutableList<String?> = mutableListOf(), private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return urlList.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as MainRecyclerViewHolder, position)
    }

    private fun bindDefaultView(holder: MainRecyclerViewHolder, position: Int){
        Glide.with(holder.itemView.context)
            .load(urlList[position])
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(30)
                )
            )
            .error(R.drawable.not_upload_mission_img_view)
            .into(holder.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainRecyclerViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission_participate, parent, false))
    }

    private class MainRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        //val item = itemView.findViewById<ImageView>(R.id.item_main_recycler)
        val item = itemView.item_mission_participate_image
    }
}



