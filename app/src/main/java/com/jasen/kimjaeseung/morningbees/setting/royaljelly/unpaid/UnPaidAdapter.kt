package com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.utils.getPriceAnnotation
import kotlinx.android.synthetic.main.item_royaljelly_unpaid.view.*

class UnPaidAdapter(
    private val penaltyList: MutableList<Penalty>,
    private val listener: OnItemSelectedInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemSelectedInterface {
        fun onItemSelected(v: View, position: Int)
    }

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

    private fun bindDefaultView(holder: UnPaidViewHolder, position: Int) {
        val item = penaltyList[position]

        holder.itemView.itemUnPaidNickname.text = item.nickname
        holder.itemView.itemUnPaidMoney.text = item.penalty.getPriceAnnotation()

        if (GlobalApp.prefsBeeInfo.myNickname == GlobalApp.prefsBeeInfo.beeManagerNickname){
            Log.d(TAG, "onItemSelected 호출")
            holder.itemView.setOnClickListener(object : View.OnClickListener{
                override fun onClick(v: View) {
                    listener.onItemSelected(v, position)
                }
            })
        }
    }

    private class UnPaidViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    companion object {
        const val TAG = "UnPaidAdapter"
    }
}