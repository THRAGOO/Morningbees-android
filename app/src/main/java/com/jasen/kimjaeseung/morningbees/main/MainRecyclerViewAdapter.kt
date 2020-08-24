package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.graphics.BitmapFactory
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.item_main_recycler.view.*
import java.net.URL

class MainRecyclerViewAdapter(
    private val urlList : MutableList<URL?> = mutableListOf(), private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return urlList.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as MainRecyclerViewHolder, position)
    }

    private fun bindDefaultView(holder: MainRecyclerViewHolder, position: Int){
        holder.bind(urlList[position], context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainRecyclerViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_recycler, parent, false))
    }

    private class MainRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val photo = itemView.findViewById<ImageView>(R.id.item_main_recycler)

        fun bind(itemUrl : URL?, context: Context){
            //val urlString = itemUrl.toString()
            if(itemUrl == null){
                photo?.setImageResource(R.drawable.not_upload_mission_recycler_view)
            }
            else{
                val photoImage = BitmapFactory.decodeStream(itemUrl!!.openConnection().getInputStream())
                photo.setImageBitmap(photoImage)
            }
        }
    }
}



