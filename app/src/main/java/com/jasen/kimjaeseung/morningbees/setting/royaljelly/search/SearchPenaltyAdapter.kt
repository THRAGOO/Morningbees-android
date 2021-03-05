package com.jasen.kimjaeseung.morningbees.setting.royaljelly.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMember
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import kotlinx.android.synthetic.main.item_search_penalty_list.view.*

class SearchPenaltyAdapter(
    private val beeList: MutableList<Penalty>,
    private val listener: OnItemSelectedInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemSelectedInterface {
        fun onItemSelected(v: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchPenaltyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_penalty_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return beeList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as SearchPenaltyViewHolder, position)
    }

    private fun bindDefaultView(holder: RecyclerView.ViewHolder, position: Int) {
        val item = beeList[position]

        holder.itemView.itemSearchPenaltyNickname.text = item.nickname

        Glide.with(holder.itemView.context)
            .load(R.drawable.default_profile_image)
            .centerCrop()
            .into(holder.itemView.itemSearchPenaltyProfile)

        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View) {
                listener.onItemSelected(v, position)
            }
        })
    }

    private class SearchPenaltyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}