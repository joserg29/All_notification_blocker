package com.projects.allnotificationblocker.blockthemall.domain

import java.text.*
import java.util.*

data class TimeRange(
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int,
) {

    private fun toMinutes(hour: Int, minute: Int) = hour * 60 + minute

    private fun Calendar.currentMinutes(): Int =
        get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)

    fun startMinutes(): Int = toMinutes(startHour, startMin)
    fun endMinutes(): Int = toMinutes(endHour, endMin)

    fun spansMidnight(): Boolean = isComplete() && endMinutes() <= startMinutes()

    fun isBetween(date: Date): Boolean {
        if (!isComplete()) return false

        val calendar = Calendar.getInstance().apply { time = date }
        val currentMinutes = calendar.currentMinutes()

        val start = startMinutes()
        val end = endMinutes()

        return if (spansMidnight()) {
            currentMinutes >= start || currentMinutes < end
        } else {
            currentMinutes in start..end
        }
    }

    fun isComplete(): Boolean {
        return startHour != -1 && startMin != -1 && endHour != -1 && endMin != -1
    }

    fun isOutDated(date: Date = Date()): Boolean {
        if (!isComplete()) return true

        val calendar = Calendar.getInstance().apply { time = date }
        val currentMinutes = calendar.currentMinutes()
        val start = startMinutes()
        val end = endMinutes()

        return if (spansMidnight()) {
            currentMinutes > end && currentMinutes < start
        } else {
            currentMinutes > end
        }
    }

    fun isInvalid(): Boolean {
        if (!isComplete()) {
            return true
        }
        val hoursValid = listOf(startHour, endHour).all { it in 0..23 }
        val minutesValid = listOf(startMin, endMin).all { it in 0..59 }
        if (!hoursValid || !minutesValid) {
            return true
        }
        // Disallow zero-length ranges
        if (startHour == endHour && startMin == endMin) {
            return true
        }
        return false
    }

    fun getTimeDuration(): String {
        val startTotalMinutes = startMinutes()
        val endTotalMinutes = endMinutes()
        var diffMinutes = endTotalMinutes - startTotalMinutes
        if (diffMinutes <= 0) diffMinutes += 24 * 60 // Handle overnight ranges
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getStartString(): String? {
        if (startHour == -1 || startMin == -1) {
            return null
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMin)
        }
        return SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(calendar.time)
    }

    fun getEndString(): String? {
        if (endHour == -1 || endMin == -1) {
            return null
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMin)
        }
        return SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(calendar.time)
    }



}