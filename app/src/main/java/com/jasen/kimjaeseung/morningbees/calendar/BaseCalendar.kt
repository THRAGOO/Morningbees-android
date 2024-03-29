package com.jasen.kimjaeseung.morningbees.calendar

import java.util.*

class BaseCalendar {
    companion object {
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    }

    private val calendar = Calendar.getInstance()

    var prevMonthTailOffset = 0
    var nextMonthHeadOffset = 0
    var currentMonthMaxDate = 0

    var data = arrayListOf<Int>()

    init {
        calendar.time = Date()
    }

    //Init calendar

    fun initBaseCalendar(refreshCallback: (Calendar) -> Unit) {
        makeMonthDate(refreshCallback)
    }

    //change to prev month

    fun changeToPrevMonth(refreshCallback: (Calendar) -> Unit) {
        if (calendar.get(Calendar.MONTH) == 0) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        }
        makeMonthDate(refreshCallback)
    }

    //change to next month

    fun changeToNextMonth(refreshCallback: (Calendar) -> Unit) {
        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
            calendar.set(Calendar.MONTH, 0)
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
        }
        makeMonthDate(refreshCallback)
    }

    //make month date

    private fun makeMonthDate(refreshCallback: (Calendar) -> Unit) {
        data.clear()
        calendar.set(Calendar.DATE, 1)
        currentMonthMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        prevMonthTailOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1

        makePreMonthTail(calendar.clone() as Calendar)
        makeCurrentMonth(calendar)

        nextMonthHeadOffset = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevMonthTailOffset + currentMonthMaxDate)
        makeNextMonthHead()

        refreshCallback(calendar)
    }

    //generate data for the last month displayed before the first day of the current calendar

    private fun makePreMonthTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        var maxOffsetDate = maxDate - prevMonthTailOffset

        for (i in 1..prevMonthTailOffset) data.add(++maxOffsetDate)
    }

    //generate data for the current calendar

    private fun makeCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) data.add(i)
    }

    //generate data for the next month displayed before the last day of the current calendar

    private fun makeNextMonthHead() {
        var date = 1
        for (i in 1..nextMonthHeadOffset) data.add(date++)
    }
}