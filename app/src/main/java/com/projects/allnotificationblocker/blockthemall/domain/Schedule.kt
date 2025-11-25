package com.projects.allnotificationblocker.blockthemall.domain

import android.content.*
import com.projects.allnotificationblocker.blockthemall.*
import java.util.*

data class Schedule(
    var timeRange: TimeRange? = null,
    var dateRange: DateRange? = null,
    var weekDays: WeekDays? = null,
) {
    fun isForever() = dateRange == null
    /**
     * if not out of date range. means will be active again
     */
    fun isOutDated(date: Date): Boolean {
        if (isForever()) {
            return false
        }
        return dateRange!!.isBefore(date)
    }


    fun isActive(date: Date): Boolean {
        val isInWeekDays = weekDays?.isActive(date) != false
        val isInTimeRange = timeRange?.isBetween(date) != false
        val isInDateRange = dateRange?.isBetween(date) != false
        return isInWeekDays && isInTimeRange && isInDateRange
    }

    fun setStartTime(h: Int, m: Int) {
        timeRange = timeRange?.copy(startHour = h, startMin = m) ?: TimeRange(h, m, -1, -1)
    }

    fun setEndTime(h: Int, m: Int) {
        timeRange = timeRange?.copy(endHour = h, endMin = m) ?: TimeRange(-1, -1, h, m)
    }
    /**
     * Retrieves the start time of the schedule.
     *
     * @return A `Pair` where the first value is the hour and the second value is the minute,
     *         or `null` if the start time is not set.
     */
    fun getStartTime(): Pair<Int, Int>? {
        if (timeRange != null && timeRange!!.startMin != -1 && timeRange!!.startHour != -1) {
            return Pair(timeRange!!.startHour, timeRange!!.startMin)
        }
        return null
    }
    /**
     * Retrieves the end time of the schedule.
     *
     * @return A `Pair` where the first value is the hour and the second value is the minute,
     *         or `null` if the end time is not set.
     */
    fun getEndTime(): Pair<Int, Int>? {
        if (timeRange != null && timeRange!!.endMin != -1 && timeRange!!.endHour != -1) {
            return Pair(timeRange!!.endHour, timeRange!!.endMin)
        }
        return null
    }
    /**
     * todo
     */
    fun getDateRangeMillis(start: Long, end: Long) : DateRange{
        val startCal = Calendar.getInstance().apply {
            timeInMillis = start
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = end
        }

        return DateRange(
            startDay = startCal.get(Calendar.DAY_OF_MONTH),
            startMonth = startCal.get(Calendar.MONTH) + 1,
            startYear = startCal.get(Calendar.YEAR),
            endDay = endCal.get(Calendar.DAY_OF_MONTH),
            endMonth = endCal.get(Calendar.MONTH) + 1,
            endYear = endCal.get(Calendar.YEAR)
        )
    }

    fun setWeekDays(dayIndex: Int, bool: Boolean) {
        if (weekDays == null) {
            weekDays = WeekDays()
        }
        when (dayIndex) {
            0 -> weekDays!!.sunday = bool
            1 -> weekDays!!.monday = bool
            2 -> weekDays!!.tuesday = bool
            3 -> weekDays!!.wednesday = bool
            4 -> weekDays!!.thursday = bool
            5 -> weekDays!!.friday = bool
            6 -> weekDays!!.saturday = bool
        }
    }

    fun isValid(date: Date): Boolean {
        if (timeRange == null) {
            return false
        }
        /*if (isOutDated(date)) { todo :)
            return false
        }*/
        if (timeRange!!.isInvalid()) {
            return false
        }
        return true
    }

    fun resString(date: Date, context: Context): String {
        if (!isValid(date)) {
            return context.getString(R.string.invalid_time_range)
        }
        val timeBase = timeRange!!.getStartString() + " - " + timeRange!!.getEndString()
        val time = if (timeRange!!.spansMidnight()) {
            timeBase + " " + context.getString(R.string.spans_midnight_suffix)
        } else {
            timeBase
        }
        if (isForever()) {
            return if (weekDays?.anyWeekDaysSelected() == true) {
                time + "\n" + context.getString(R.string.those_week_days_forever)
            } else {
                time + "\n" + context.getString(R.string.everyday)
            }
        }

        if (weekDays?.anyWeekDaysSelected() == true) {
            return time + "\n" + context.getString(R.string.week_days_in_this_range) + "\n" +
                    dateRange!!.getDuration(date, context)
        } else {
            return time + "\n" + dateRange!!.getDuration(date, context)
        }

    }
}