package com.jasen.kimjaeseung.morningbees.calendar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.util.toString
import kotlinx.android.synthetic.main.item_calendar.view.*
import kotlinx.android.synthetic.main.popup_calendar.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarDialog : DialogFragment() {
    lateinit var calendarAdapter: CalendarRecyclerViewAdapter
    lateinit var mDialogFragment: OnMyDialogResult
    var hyphenTargetDate: String = ""
    var targetDate: String = ""
    var todayDate: String = ""

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.calendar_width)
        val height = resources.getDimensionPixelSize(R.dimen.calendar_height)
        dialog?.window?.setLayout(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.popup_calendar, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.popup_calendar)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarAdapter = CalendarRecyclerViewAdapter(this)
        calendarRecyclerView.adapter = calendarAdapter
        calendarRecyclerView.layoutManager =
            GridLayoutManager(activity!!.applicationContext, BaseCalendar.DAYS_OF_WEEK)

        calendarRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                val position = rv.getChildAdapterPosition(child!!)

                when (e.action){
                    MotionEvent.ACTION_DOWN -> {
                        Log.d(TAG, "action_down: position: $position")
                        child.itemCalendarDateText?.background = activity?.applicationContext?.getDrawable(R.drawable.background_item_calendar_grey)
                    }

                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "action_up: position: $position")
                        child.itemCalendarDateText?.background = activity?.applicationContext?.getDrawable(R.drawable.background_item_calendar_yellow)
                        val date = calendarAdapter.getDate(position)
                        finishFragment(date)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        goPrevMonthButton.setOnClickListener {
            calendarAdapter.changeToPrevMonth()
        }

        goNextMonthButton.setOnClickListener {
            calendarAdapter.changeToNextMonth()
        }

        val curDate = Date()
        val simpleDate = SimpleDateFormat("yyyyMMdd").format(curDate)
        todayDate = simpleDate
    }

    private fun finishFragment(position: Int) {
        if (position in 1..9) {
            hyphenTargetDate += "-0$position"
            targetDate += "0$position"
        } else {
            hyphenTargetDate += "-$position"
            targetDate += "$position"
        }

        mDialogFragment.finish(hyphenTargetDate, targetDate)
        dialog!!.dismiss()
    }

    fun refreshCurrentMonth(calendar: Calendar) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        currentMonthText.text = sdf.format(calendar.time)
        this.hyphenTargetDate = calendar.time.toString("yyyy-MM")
        this.targetDate = calendar.time.toString("yyyyMM")
    }

    fun setDialogResult(dialogResult: OnMyDialogResult) {
        mDialogFragment = dialogResult
    }

    interface OnMyDialogResult {
        fun finish(str: String, _str: String)
    }

    companion object {
        const val TAG = "CalendarActivity"
    }
}