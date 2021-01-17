package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jasen.kimjaeseung.morningbees.R
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
            .centerCrop()
            .apply(
                RequestOptions.bitmapTransform(
                    RoundedCorners(30)
                )
            )
            .into(holder.item)
        //holder.bind(urlList[position], context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainRecyclerViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission_participate, parent, false))
    }

    private class MainRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        //val item = itemView.findViewById<ImageView>(R.id.item_main_recycler)
        val item = itemView.item_mission_participate_image

//        fun bind(itemUrl : URL?, context: Context){
//            //val urlString = itemUrl.toString()
//            if(itemUrl == null){
//                item?.setImageResource(R.drawable.item_main_img)
//            }
//            else{
//                val photoImage = BitmapFactory.decodeStream(itemUrl.openConnection().getInputStream())
//                item.setImageBitmap(photoImage)
//            }
//        }
    }
}



