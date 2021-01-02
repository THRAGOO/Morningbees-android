package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
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
        val item = itemView.findViewById<ImageView>(R.id.item_main_recycler)

        fun bind(itemUrl : URL?, context: Context){
            //val urlString = itemUrl.toString()
            if(itemUrl == null){
                item?.setImageResource(R.drawable.item_main_img)
            }
            else{
                val photoImage = BitmapFactory.decodeStream(itemUrl.openConnection().getInputStream())
                item.setImageBitmap(photoImage)
            }
        }
    }
}



