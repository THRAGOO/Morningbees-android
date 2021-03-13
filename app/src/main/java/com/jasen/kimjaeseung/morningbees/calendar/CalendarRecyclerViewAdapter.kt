package com.jasen.kimjaeseung.morningbees.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.item_calendar.view.*
import java.util.*

class CalendarRecyclerViewAdapter(private val calendarDialog: CalendarDialog) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val baseCalendar = BaseCalendar()

    init {
        baseCalendar.initBaseCalendar {
            refreshView(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)
        val rowOfCalendar = countRowsOfCalendar()
        val layoutParams = view.layoutParams

        layoutParams.height = parent.height / rowOfCalendar
        layoutParams.width = parent.width

        view.layoutParams = layoutParams

        return CalendarRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return BaseCalendar.LOW_OF_CALENDAR * BaseCalendar.DAYS_OF_WEEK
    }

    private fun bindDefaultView(holder: CalendarRecyclerViewHolder, position: Int) {
        holder.bind(baseCalendar.data[position].toString())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as CalendarRecyclerViewHolder, position)

        var mPosition = baseCalendar.data[position].toString()
        if (mPosition.toInt() in 1..9) mPosition = "0$mPosition"
        val mTargetDate = (calendarDialog.targetDate + mPosition).toInt()

        if (calendarDialog.todayDate.toInt() + 1 < mTargetDate) {
            holder.itemView.itemCalendarDateText.setTextColor(Color.parseColor("#cccccc"))
        } else {
            when {
                position % BaseCalendar.DAYS_OF_WEEK == 0 -> {
                    holder.itemView.itemCalendarDateText.setTextColor(Color.parseColor("#f03e3e"))
                }
                position % BaseCalendar.DAYS_OF_WEEK == 6 -> {
                    holder.itemView.itemCalendarDateText.setTextColor(Color.parseColor("#2269FF"))
                }
                else -> {
                    holder.itemView.itemCalendarDateText.setTextColor(Color.parseColor("#777777"))
                }
            }
        }

        if (position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
            holder.itemView.itemCalendarDateText.alpha = 0f
        } else {
            if (calendarDialog.todayDate.toInt() + 1 < mTargetDate) {
                holder.itemView.itemCalendarDateText.alpha = 1f
                holder.itemView.itemCalendarDateText.text = baseCalendar.data[position].toString()
                holder.itemView.itemCalendarDateText.isClickable = false
            } else {
                holder.itemView.itemCalendarDateText.alpha = 1f
                holder.itemView.itemCalendarDateText.text = baseCalendar.data[position].toString()
            }
        }
    }

    private fun countRowsOfCalendar(): Int {
        val daysInWeek = 7
        val startDay = baseCalendar.prevMonthTailOffset
        val daysInMonth = baseCalendar.currentMonthMaxDate

        var numRows = (startDay + daysInMonth) / daysInWeek
        if ((startDay + daysInMonth) % daysInWeek != 0)
            numRows += 1

        return numRows
    }

    fun getDate(position: Int): Int {
        return baseCalendar.data[position]
    }

    fun changeToPrevMonth() {
        baseCalendar.changeToPrevMonth {
            refreshView(it)
        }
    }

    fun changeToNextMonth() {
        baseCalendar.changeToNextMonth {
            refreshView(it)
        }
    }

    private fun refreshView(calendar: Calendar) {
        notifyDataSetChanged()
        calendarDialog.refreshCurrentMonth(calendar)
    }

    class CalendarRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemText = itemView.findViewById<TextView>(R.id.itemCalendarDateText)

        fun bind(date: String) {
            itemText.text = date
        }
    }

    companion object {
        const val TAG = "CalendarAdapter"
    }

}