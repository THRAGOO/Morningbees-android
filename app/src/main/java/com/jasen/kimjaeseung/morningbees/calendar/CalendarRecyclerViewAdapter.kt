package com.jasen.kimjaeseung.morningbees.calendar

import android.graphics.Color
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.item_calendar.view.*
import java.text.SimpleDateFormat


import java.util.*

class CalendarRecyclerViewAdapter(val calendarActivity: CalendarDialog) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val baseCalendar = BaseCalendar()

    interface ItemClickListener{
        fun onClick(view: View, position: Int)
    }

    private lateinit var itemClickListener : ItemClickListener

    init {
        baseCalendar.initBaseCalendar {
            refreshView(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)
        val layoutParams : ViewGroup.LayoutParams = view.layoutParams
        layoutParams.height = parent.height / 6
        layoutParams.width = parent.width

        Log.d(TAG, "height: ${layoutParams.height}")
        Log.d(TAG, "width: ${layoutParams.width}")

        view.layoutParams = layoutParams

        return CalendarRecycelrViewHolder(view)
        /*
        return CalendarRecycelrViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar, parent, false))
        */
    }

    override fun getItemCount(): Int {
        return BaseCalendar.LOW_OF_CALLENDAR * BaseCalendar.DAYS_OF_WEEK
    }

    private fun bindDefaultView(holder : CalendarRecycelrViewHolder, position : Int){
        holder.bind(baseCalendar.data[position].toString())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as CalendarRecycelrViewHolder, position)

        var mPosition = baseCalendar.data[position].toString()

        if(mPosition.toInt() in 1..9) mPosition = "0$mPosition"

        val mTargetDate = (calendarActivity._targetDate + mPosition).toInt()

        if(calendarActivity.todayDate.toInt() +1 < mTargetDate){
            holder.itemView.calendar_date.setTextColor(Color.parseColor("#cccccc"))
        }
        else{
            if(position % BaseCalendar.DAYS_OF_WEEK == 0){
                holder.itemView.calendar_date.setTextColor(Color.parseColor("#f03e3e")) // sunday
            }
            else if (position % BaseCalendar.DAYS_OF_WEEK == 6){
                holder.itemView.calendar_date.setTextColor(Color.parseColor("#2269FF")) // saturday
            }
            else{
                holder.itemView.calendar_date.setTextColor(Color.parseColor("#676d6e"))
            }
        }

        if(position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate){
            holder.itemView.calendar_date.alpha = 0f
        }
        else {
            if(calendarActivity.todayDate.toInt() +1 < mTargetDate) {
                holder.itemView.calendar_date.alpha = 1f
                holder.itemView.calendar_date.text = baseCalendar.data[position].toString()
                holder.itemView.calendar_date.isClickable = false
            }
            else {
                holder.itemView.calendar_date.alpha = 1f
                holder.itemView.calendar_date.text = baseCalendar.data[position].toString()
                holder.itemView.setOnClickListener{
                    val data = baseCalendar.data[position]
                    itemClickListener.onClick(it, data)
                    Log.d(TAG, "data: $data")
                }
            }
        }
    }
    
    fun changeToPrevMonth(){
        baseCalendar.changeToPrevMonth {
            refreshView(it)
        }
    }

    fun changeToNextMonth(){
        baseCalendar.changeToNextMonth {
            refreshView(it)
        }
    }

    private fun refreshView(calendar: Calendar){
        notifyDataSetChanged()
        calendarActivity.refreshCurrentMonth(calendar)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener){
        this.itemClickListener = itemClickListener
    }

    class CalendarRecycelrViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val item = itemView.findViewById<TextView>(R.id.calendar_date)

        fun bind(date : String){
            item.text = date
        }
    }

    companion object {
        const val TAG = "CalendarAdapter"
    }

}

