package com.jasen.kimjaeseung.morningbees.setting.royaljelly.total

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.util.getPriceAnnotation
import kotlinx.android.synthetic.main.item_royaljelly_total.view.*
import java.text.DecimalFormat

class TotalAdapter(
    private val totalList: MutableList<Penalty>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun getItemCount(): Int {
        return totalList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TotalViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_royaljelly_total, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as TotalViewHolder, position)    }

    private fun bindDefaultView(holder: TotalViewHolder, position: Int) {
        val item = totalList[position]

        holder.itemView.itemTotalNickname.text = item.nickname
        holder.itemView.itemTotalMoney.text = item.penalty.getPriceAnnotation()
    }

    private class TotalViewHolder(view: View) : RecyclerView.ViewHolder(view){
    }
}