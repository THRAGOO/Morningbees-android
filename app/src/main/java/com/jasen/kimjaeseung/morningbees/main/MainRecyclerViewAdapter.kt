package com.jasen.kimjaeseung.morningbees.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.item_main_recycler.view.*

class MainRecyclerViewAdapter(
    private val context: Context,
    val items : MutableList<ImageView> = mutableListOf()): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as MainRecyclerViewHolder, position)
    }

    private fun bindDefaultView(holder: MainRecyclerViewHolder, position: Int){
        val item = items[position]
        holder.todayMissionImg = item.item_main_recycler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main_recycler, parent, false))
    }

    private class MainRecyclerViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var todayMissionImg = itemView.findViewById<ImageView>(R.id.today_mission_image)
    }
}



