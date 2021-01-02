package com.jasen.kimjaeseung.morningbees.calendar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jasen.kimjaeseung.morningbees.R
import kotlinx.android.synthetic.main.popup_calendar.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarDialog : DialogFragment(), View.OnClickListener{
    lateinit var calendarAdapter : CalendarRecyclerViewAdapter
    lateinit var mDialogFragment: OnMyDialogResult
    var targetDate : String = ""
    var todayDate : String = ""
    var _targetDate : String = ""

    override fun onResume() {
        super.onResume()

        val width = resources.getDimensionPixelSize(R.dimen.calendar_width)
        val height = resources.getDimensionPixelSize(R.dimen.calendar_height)
        dialog!!.window!!.setLayout(width, height)
    }

    override fun onCreateView( //view가 생성되는 동안 실행
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.popup_calendar, container, false)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.popup_calendar)

        dialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // view가 생성되고 난 뒤 실행
        super.onViewCreated(view, savedInstanceState)

        calendarAdapter = CalendarRecyclerViewAdapter(this)
        calendar_recyclerview.adapter = calendarAdapter
        calendar_recyclerview.layoutManager = GridLayoutManager(activity!!.applicationContext, BaseCalendar.DAYS_OF_WEEK)

        calendarAdapter.setItemClickListener(object : CalendarRecyclerViewAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int) {

                if (position in 1..9){
                    targetDate += "-0$position"
                    _targetDate += "0$position"
                }
                else{
                    targetDate += "-$position"
                    _targetDate += "$position"
                }

                mDialogFragment.finish(targetDate, _targetDate)
                dialog!!.dismiss()
            }
        })
        tv_prev_month.setOnClickListener{
            calendarAdapter.changeToPrevMonth()
        }

        tv_next_month.setOnClickListener{
            calendarAdapter.changeToNextMonth()
        }

        val curDate = Date()
        val simpleDate = SimpleDateFormat("yyyyMMdd").format(curDate)
        todayDate = simpleDate
    }

    override fun onClick(p0: View?) {

    }

    fun refreshCurrentMonth(calendar: Calendar){
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        tv_current_month.text = sdf.format(calendar.time)

        val _tarDate = SimpleDateFormat("yyyy-MM", Locale.KOREAN)
        targetDate = _tarDate.format(calendar.time)

        val _tDate = SimpleDateFormat("yyyyMM", Locale.KOREAN)
        _targetDate = _tDate.format(calendar.time)
    }

    fun setDialogResult(dialogResult : OnMyDialogResult){
        mDialogFragment = dialogResult
    }

    interface OnMyDialogResult{
        fun finish(str : String, _str : String)
    }

    companion object{
        const val TAG = "CalendarActivity"
    }
}