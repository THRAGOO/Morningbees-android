package com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import kotlinx.android.synthetic.main.item_royaljelly_unpaid.view.*
import java.text.DecimalFormat

class UnPaidAdapter(
    private val penaltyList: MutableList<Penalty>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UnPaidViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_royaljelly_unpaid, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return penaltyList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as UnPaidViewHolder, position)
    }

    private fun Int.getPriceAnnotation(): String {
        return DecimalFormat("###,###").format(this)
    }

    private fun bindDefaultView(holder: UnPaidViewHolder, position: Int) {
        val item = penaltyList[position]

        holder.itemView.itemUnPaidNickname.text = item.nickname
        holder.itemView.itemUnPaidMoney.text = item.penalty.getPriceAnnotation()
    }

    private class UnPaidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

}