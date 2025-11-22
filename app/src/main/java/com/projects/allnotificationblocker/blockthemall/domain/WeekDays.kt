package com.projects.allnotificationblocker.blockthemall.domain

import java.util.Calendar
import java.util.Date

data class WeekDays(
    var sunday: Boolean=false,
    var monday: Boolean=false,
    var tuesday: Boolean=false,
    var wednesday: Boolean=false,
    var thursday: Boolean=false,
    var friday: Boolean=false,
    var saturday: Boolean=false,
){
    fun isActive(date: Date): Boolean {
        val calendar = Calendar.getInstance().apply {
            time = date
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> sunday
            Calendar.MONDAY -> monday
            Calendar.TUESDAY -> tuesday
            Calendar.WEDNESDAY -> wednesday
            Calendar.THURSDAY -> thursday
            Calendar.FRIDAY -> friday
            Calendar.SATURDAY -> saturday
            else -> false
        }
    }

    fun getNextDateRange(date: Date): DateRange? {
        if (!anyWeekDaysSelected()) {
            return null
        }

        val calendar = Calendar.getInstance().apply {
            time = date
            // Normalize to the beginning of the day to avoid issues with time components
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var startDate: Date? = null
        var endDate: Date? = null

        // Find the start of the next active range
        // Iterate a reasonable amount of time (e.g., 14 days) to find the start.
        // This prevents an infinite loop if, for some reason, no active days are found ahead,
        // although anyWeekDaysSelected() should prevent this if used correctly.
        for (i in 0..13) { // Check current day and next 13 days
            if (isActive(calendar.time)) {
                startDate = calendar.time
                break
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // If no start date is found within the search window, return null
        if (startDate == null) {
            return null
        }

        // Now that we have a start date, find the end of this contiguous active range
        val endCalendar = Calendar.getInstance().apply {
            time = startDate
        }
        endDate = startDate // Initialize endDate with startDate

        for (i in 0..<6){
            endCalendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
            if (isActive(endCalendar.time)) {
                endDate = endCalendar.time // Update endDate if the next day is active
            }
        }

        // Construct and return the DateRange
        val startCal = Calendar.getInstance().apply { time = startDate }
        val endCal = Calendar.getInstance().apply { time = endDate!! } // endDate will not be null here

        return DateRange(
            startDay = startCal.get(Calendar.DAY_OF_MONTH),
            startMonth = startCal.get(Calendar.MONTH) + 1, // Calendar.MONTH is 0-indexed
            startYear = startCal.get(Calendar.YEAR),
            endDay = endCal.get(Calendar.DAY_OF_MONTH),
            endMonth = endCal.get(Calendar.MONTH) + 1, // Calendar.MONTH is 0-indexed
            endYear = endCal.get(Calendar.YEAR)
        )
    }
    fun anyWeekDaysSelected(): Boolean = this.sunday || this.monday || this.tuesday || this.wednesday || this.thursday || this.friday || this.saturday
}