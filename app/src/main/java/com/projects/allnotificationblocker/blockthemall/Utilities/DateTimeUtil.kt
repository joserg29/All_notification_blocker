package com.projects.allnotificationblocker.blockthemall.Utilities

import android.text.format.DateFormat
import timber.log.*
import java.text.*
import java.util.*

object DateTimeUtil {
    fun LongToDateString(d: Long): String {
        return DateFormat.format(
            "yyyy-MM-dd HH:mm:ss",
            Date(d)
        ).toString()
    }

    fun DateStringToLong(dateString: String): Long {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = sdf.parse(dateString)

            return date!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0L
    }

    fun toReadableFormat(ts: String): String? {
        val longTs = DateStringToLong(ts)
        val DATE_FORMAT_NOW = "E, dd MMM yyyy hh:mm aa"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(longTs)
    }

    fun toReadableFormat1(ts: String): String? {
        val longTs = DateStringToLong(ts)
        val DATE_FORMAT_NOW = "hh:mm aa"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(longTs)
    }

    fun toHrMinAmPm(ts: String): String? {
        val longTs = DateStringToLong(ts)
        val DATE_FORMAT_NOW = "hh:mm aa"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(longTs)
    }

    fun toHrMinSecAmPm(ts: String): String? {
        val longTs = DateStringToLong(ts)
        val DATE_FORMAT_NOW = "hh:mm:ss aa"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(longTs)
    }

    fun isDateBetweenDates(date: String, date1: String, date2: String): Boolean {
        val isDateAfterDate1 = isDate1AfterDate2(date, date1)
        val isDateBeforeDate2 = !isDate1AfterDate2(date, date2)

        return isDateAfterDate1 && isDateBeforeDate2
    }

    fun isTimeBetweenTimes(date: String, date1: String, date2: String): Boolean {
        val isDateAfterDate1 = isTime1AfterTime2(date, date1)
        val isDateBeforeDate2 = !isTime1AfterTime2(date, date2)

        return isDateAfterDate1 && isDateBeforeDate2
    }

    val currentDateTime: String
        get() {
            val DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss"
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
            return sdf.format(cal.getTime())
        }

    val currentDate: String
        get() {
            val DATE_FORMAT_NOW = "yyyy-MM-dd"
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
            return sdf.format(cal.getTime())
        }

    val currentTime: String
        get() {
            val DATE_FORMAT_NOW = "HH:mm:ss"
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
            return sdf.format(cal.getTime())
        }

    fun getTimeHrMinSec(d: Long?): String? {
        val DATE_FORMAT_NOW = "HH:mm:ss"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(d)
    }


    fun convertDateTimeToTodayTime(timestamp: String): String {
        val d = DateStringToLong(timestamp)
        val currentDate: String = currentDate
        val time = getTimeHrMinSec(d)
        return currentDate + " " + time
    }


    fun Date.getDateString(): String {
        val DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(this.time) + ":00"
    }

    fun getDateStringAmPm(d: Date): String? {
        val DATE_FORMAT_NOW = "hh:mm aa"
        val sdf = SimpleDateFormat(DATE_FORMAT_NOW)
        return sdf.format(d.time)
    }

    fun hoursAndMinutes(date: Date): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        cal.time = date
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        return Pair(hours, minutes)
    }

    val monthYearFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    fun isDate1AfterDate2(firstTime: String, secondTime: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val dFirstTime = sdf.parse(firstTime)
            val dSecondTime = sdf.parse(secondTime)


            return (dFirstTime!!.time >= dSecondTime!!.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    fun rangeDateToString(from: Date, to: Date): String {
        val fromCalendar = Calendar.getInstance().apply { time = from }
        val toCalendar = Calendar.getInstance().apply { time = to }
        val now = Calendar.getInstance()

        val fromYear = fromCalendar.get(Calendar.YEAR)
        val toYear = toCalendar.get(Calendar.YEAR)

        val fromMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(from)
        val toMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(to)

        var fromDay = fromCalendar.get(Calendar.DAY_OF_MONTH).toString()
        var toDay = toCalendar.get(Calendar.DAY_OF_MONTH).toString()
        //if from day is today or tommorow
        if (fromCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            fromDay = "Today"
        } else if (fromCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1) {
            fromDay = "Tomorrow"
        }
        if (toCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1) {
            toDay = "Tomorrow"
        }

        return if (fromYear == toYear) {
            if (fromMonth == toMonth) {
                "$fromYear $fromMonth: $fromDay ~ $toDay"
            } else {
                "$fromYear: $fromMonth $fromDay ~ $toMonth $toDay"
            }
        } else {
            "$fromYear $fromMonth $fromDay ~ $toYear $toMonth $toDay"
        }
    }

    fun getTimeDiffInSeconds(firstTime: String, secondTime: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val dFirstTime = sdf.parse(firstTime)

            val dSecondTime = sdf.parse(secondTime)

            val diff = (dSecondTime!!.time - dFirstTime!!.time) / 1000
            Timber.tag("AppInfo").d("getTimeDiffInSeconds: %d", diff)

            return diff
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return 0
    }

    fun isTime1AfterTime2(time1: String, time2: String): Boolean {
        val time1InMs = DateStringToLong(time1)
        val time2InMs = DateStringToLong(time2)


        val timeFormat = SimpleDateFormat("HH:mm:ss")
        try {
            val firstTimeParsed = timeFormat.parse(getTimeHrMinSec(time1InMs))
            val secondTimeParsed = timeFormat.parse(getTimeHrMinSec(time2InMs))
            return (firstTimeParsed!!.time >= secondTimeParsed!!.time)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }


    fun getDuration(from: String, to: String): String {
        var ret = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val startDate = sdf.parse(from)
            val endDate = sdf.parse(to)
            //milliseconds
            var different = endDate!!.time - startDate!!.time

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            val elapsedDays = different / daysInMilli
            different = different % daysInMilli

            val elapsedHours = different / hoursInMilli
            different = different % hoursInMilli

            val elapsedMinutes = different / minutesInMilli
            different = different % minutesInMilli
            different / secondsInMilli
            if (elapsedDays > 0) {
                ret += String.format("%d days ", elapsedDays)
            }
            if (elapsedHours > 0) {
                ret += String.format("%d hours ", elapsedHours)
            }
            if (elapsedMinutes > 0) {
                ret += String.format("%d minutes ", elapsedMinutes)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ret
    }
}
