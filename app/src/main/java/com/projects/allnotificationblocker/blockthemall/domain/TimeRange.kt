package com.projects.allnotificationblocker.blockthemall.domain

import java.text.*
import java.util.*

data class TimeRange(
    val startHour: Int,
    val startMin: Int,
    val endHour: Int,
    val endMin: Int,
) {
    fun isBetween(date: Date): Boolean {
        val calendar = Calendar.getInstance().apply {
            time = date
        }

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMin
        val endMinutes = endHour * 60 + endMin

        return currentMinutes in startMinutes..endMinutes
    }

    fun isComplete(): Boolean {
        return startHour != -1 && startMin != -1 && endHour != -1 && endMin != -1
    }
    fun isOutDated(date: Date=Date()): Boolean {
        val calendar = Calendar.getInstance().apply {
            time = date
        }
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val endMinutes = endHour * 60 + endMin

        return currentMinutes > endMinutes
    }

    fun isInvalid(): Boolean {
        if (!isComplete()) {
            return true
        }
        if (startHour > endHour) {
            return true
        }
        if (startHour == endHour && startMin > endMin) {
            return true
        }
        return false
    }

    fun getTimeDuration(): String {
        val startTotalMinutes = startHour * 60 + startMin
        val endTotalMinutes = endHour * 60 + endMin
        var diffMinutes = endTotalMinutes - startTotalMinutes
        if (diffMinutes < 0) diffMinutes += 24 * 60 // Handle overnight ranges
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