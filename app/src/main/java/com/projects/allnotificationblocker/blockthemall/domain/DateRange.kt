package com.projects.allnotificationblocker.blockthemall.domain

import android.content.*
import com.projects.allnotificationblocker.blockthemall.*
import java.text.*
import java.util.*

/**
 * Represents a range of dates.
 *
 * @property startDay The day of the month for the start date (1-31).
 * @property startMonth The month of the year for the start date (1-12, where January = 1).
 * @property startYear The year for the start date.
 * @property endDay The day of the month for the end date (1-31).
 * @property endMonth The month of the year for the end date (1-12, where January = 1).
 * @property endYear The year for the end date.
 */
data class DateRange(
    val startDay: Int,
    val startMonth: Int,
    val startYear: Int,
    val endDay: Int,
    val endMonth: Int,
    val endYear: Int,
) {
    fun isBetween(date: Date): Boolean {
        val startCalendar = Calendar.getInstance().apply {
            set(startYear, startMonth - 1, startDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(endYear, endMonth - 1, endDay, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val currentCalendar = Calendar.getInstance().apply {
            time = date
        }

        return currentCalendar.time in startCalendar.time..endCalendar.time
    }

    fun getStartDate(): Date {
        val fromCalendar = Calendar.getInstance().apply {
            set(startYear, startMonth - 1, startDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return fromCalendar.time
    }

    fun getEndDate(): Date {
        val toCalendar = Calendar.getInstance().apply {
            set(endYear, endMonth - 1, endDay, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return toCalendar.time
    }

    fun getDuration(currentDate: Date, context: Context): String {

        val curT = getDayMonthYear(currentDate)
        val (curDay, curMonth, currentYear) = curT
        val startT = Triple(startDay, startMonth, startYear)
        val endT = Triple(endDay, endMonth, endYear)
        if (startT == endT) {
            if (startT == curT)
                return context.getString(R.string.today)
            else if (startT == curT.copy(first = curDay + 1)) {
                return context.getString(R.string.tomorrow)
            }
        }


        val startYearDisplay = if (startYear == currentYear) {
            ""
        } else {
            "$startYear "
        }
        val endYearDisplay = if (endYear == currentYear) {
            ""
        } else {
            "$endYear "
        }

        var startDayDisplay: String
        var endDayDisplay: String
        var startMonthDisplay: String
        var endMonthDisplay: String
        if (startMonth == curMonth) {
            startDayDisplay =
                if (startDay == curDay) {
                    context.getString(R.string.today)
                } else if (startDay == curDay + 1) {
                    context.getString(R.string.tomorrow)
                } else {
                    "$startDay"
                }
            endDayDisplay = if (endDay == curDay + 1) {
                context.getString(R.string.tomorrow)
            } else {
                "$endDay"
            }
            startMonthDisplay = ""
            if (startMonth == endMonth) {
                endMonthDisplay = ""
            } else {
                endMonthDisplay = DateFormatSymbols().shortMonths[endMonth - 1]
            }
        } else {
            startDayDisplay = "$startDay"
            startMonthDisplay = DateFormatSymbols().shortMonths[startMonth - 1]
            endDayDisplay = "$endDay"
            endMonthDisplay = DateFormatSymbols().shortMonths[endMonth - 1]
        }

        return if (startYear == endYear && currentYear == startYear) {
            "$startMonthDisplay $startDayDisplay ~ $endMonthDisplay $endDayDisplay"
        } else if (startYear == endYear) {
            "$startYearDisplay $startMonthDisplay $startDayDisplay ~ $endMonthDisplay $endDayDisplay"
        } else {
            "$startYearDisplay $startMonthDisplay $startDayDisplay ~ $endYearDisplay $endMonthDisplay $endDayDisplay"
        }
    }

    fun isBefore(date: Date): Boolean {
        val (day, month, year) = getDayMonthYear(date)
        return if (endYear < year) {
            true
        } else if (endYear == year && endMonth < month) {
            true
        } else if (endYear == year && endMonth == month && endDay < day) {
            true
        } else {
            false
        }
    }

    /**
     * Extracts the day, month, and year from a given Date.
     *
     * This function takes a [Date] object as input and returns a [Triple]
     * containing the day of the month, the month (1-indexed), and the year.
     * @param date The [Date] object from which to extract the day, month, and year.
     * @return A [Triple] where:
     *         - `first` is the day of the month (1-31).
     *         - `second` is the month of the year (1-12, where 1 is January).
     *         - `third` is the year (e.g., 2023).
     */
    companion object {
        fun getDayMonthYear(date: Date): Triple<Int, Int, Int> {
            val calendar = Calendar.getInstance().apply {
                time = date
            }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based in Calendar
            val year = calendar.get(Calendar.YEAR)
            return Triple(day, month, year)
        }

        fun getTomorrowDate(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = calendar.time
            val (day, month, year) = getDayMonthYear(tomorrow)
            return DateRange(day, month, year, day, month, year)
        }

        fun getTodayDate() : DateRange {
            val calendar = Calendar.getInstance()
            val today = calendar.time
            val (day, month, year) = getDayMonthYear(today)
            return DateRange(day, month, year, day, month, year)
        }
    }
}