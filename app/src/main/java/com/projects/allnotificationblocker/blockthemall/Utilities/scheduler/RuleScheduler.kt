package com.projects.allnotificationblocker.blockthemall.Utilities.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.projects.allnotificationblocker.blockthemall.data.db.converter.ScheduleConverters
import com.projects.allnotificationblocker.blockthemall.data.db.entities.Rule
import com.projects.allnotificationblocker.blockthemall.domain.Schedule
import com.projects.allnotificationblocker.blockthemall.domain.TimeRange
import java.util.Calendar
import java.util.Date

object RuleScheduler {
    private const val ACTION_PREFIX =
        "com.projects.allnotificationblocker.blockthemall.scheduler.ACTION_"
    const val ACTION_ACTIVATE_RULE = "${ACTION_PREFIX}ACTIVATE_RULE"
    const val ACTION_DEACTIVATE_RULE = "${ACTION_PREFIX}DEACTIVATE_RULE"

    private const val EXTRA_PREFIX =
        "com.projects.allnotificationblocker.blockthemall.scheduler.EXTRA_"
    const val EXTRA_ALARM_ID = "${EXTRA_PREFIX}ALARM_ID"
    const val EXTRA_PACKAGE = "${EXTRA_PREFIX}PACKAGE"
    const val EXTRA_RULE_TYPE = "${EXTRA_PREFIX}RULE_TYPE"
    const val EXTRA_SCHEDULE = "${EXTRA_PREFIX}SCHEDULE"
    const val EXTRA_TRIGGER_AT = "${EXTRA_PREFIX}TRIGGER_AT"

    fun schedule(context: Context, rule: Rule) {
        if (!rule.isCustom || rule.schedule.timeRange == null) return
        scheduleNextStart(context, rule, System.currentTimeMillis())
        scheduleNextEnd(context, rule, System.currentTimeMillis())
    }

    fun rescheduleAll(context: Context, rules: List<Rule>) {
        rules.filter { it.isCustom && it.schedule.timeRange != null }
            .forEach { schedule(context, it) }
    }

    fun cancel(context: Context, rule: Rule) {
        cancelEvent(context, rule, isStart = true)
        cancelEvent(context, rule, isStart = false)
    }

    fun scheduleNextStart(
        context: Context,
        rule: Rule,
        referenceMillis: Long,
    ) {
        if (!rule.isCustom || rule.schedule.timeRange == null) return
        cancelEvent(context, rule, isStart = true)
        val triggerCal = findNextTrigger(rule.schedule, isStart = true, referenceMillis) ?: return
        registerAlarm(context, rule, ACTION_ACTIVATE_RULE, triggerCal.timeInMillis, true)
    }

    fun scheduleNextEnd(
        context: Context,
        rule: Rule,
        referenceMillis: Long,
    ) {
        if (!rule.isCustom || rule.schedule.timeRange == null) return
        cancelEvent(context, rule, isStart = false)
        val triggerCal = findNextTrigger(rule.schedule, isStart = false, referenceMillis) ?: return
        registerAlarm(context, rule, ACTION_DEACTIVATE_RULE, triggerCal.timeInMillis, false)
    }

    private fun registerAlarm(
        context: Context,
        rule: Rule,
        action: String,
        triggerAtMillis: Long,
        isStart: Boolean,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            this.action = action
            data = buildDataUri(rule, isStart)
            putExtra(EXTRA_ALARM_ID, rule.alarmId)
            putExtra(EXTRA_PACKAGE, rule.packageName)
            putExtra(EXTRA_RULE_TYPE, rule.ruleType)
            putExtra(EXTRA_SCHEDULE, ScheduleConverters().toJson(rule.schedule))
            putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(rule, isStart),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelEvent(context: Context, rule: Rule, isStart: Boolean) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = if (isStart) ACTION_ACTIVATE_RULE else ACTION_DEACTIVATE_RULE
            data = buildDataUri(rule, isStart)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(rule, isStart),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }

    private fun buildDataUri(rule: Rule, isStart: Boolean): Uri {
        val type = if (isStart) "start" else "end"
        return Uri.parse("blockthemall://${rule.alarmId}/$type")
    }

    private fun requestCode(rule: Rule, isStart: Boolean): Int {
        val base = rule.alarmId.hashCode() and 0x0fffffff
        return if (isStart) base else base + 1
    }

    private fun findNextTrigger(
        schedule: Schedule,
        isStart: Boolean,
        referenceMillis: Long,
    ): Calendar? {
        val range = schedule.timeRange ?: return null
        if (range.isInvalid()) {
            return null
        }
        schedule.dateRange?.let {
            if (it.isBefore(Date(referenceMillis))) {
                return null
            }
        }
        val windows = buildCandidateWindows(schedule, range, referenceMillis)
        val comparator: (Window) -> Calendar = { if (isStart) it.start else it.end }
        return windows
            .map(comparator)
            .firstOrNull { it.timeInMillis > referenceMillis }
    }

    private fun buildCandidateWindows(
        schedule: Schedule,
        range: TimeRange,
        referenceMillis: Long,
    ): List<Window> {
        val result = mutableListOf<Window>()
        val referenceCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
        val startOfDay = (referenceCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -1) // cover currently active overnight windows
        }
        val endDate = schedule.dateRange?.getEndDate()
        val searchHorizon = 62 // ~2 months to cover weekday combinations
        for (offset in 0..searchHorizon) {
            val candidate = (startOfDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            if (endDate != null && candidate.time.after(endDate)) {
                break
            }
            if (!isDayEnabled(schedule, candidate.time)) {
                continue
            }
            result.add(createWindow(candidate, range))
        }
        return result.sortedBy { it.start.timeInMillis }
    }

    private fun isDayEnabled(schedule: Schedule, date: Date): Boolean {
        val inDateRange = schedule.dateRange?.isBetween(date) ?: true
        if (!inDateRange) return false
        val weekdayMatch = schedule.weekDays?.isActive(date) ?: true
        return weekdayMatch
    }

    private fun createWindow(day: Calendar, range: TimeRange): Window {
        val start = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, range.startHour)
            set(Calendar.MINUTE, range.startMin)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (day.clone() as Calendar).apply {
            if (range.spansMidnight()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            set(Calendar.HOUR_OF_DAY, range.endHour)
            set(Calendar.MINUTE, range.endMin)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= start.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return Window(start, end)
    }

    private data class Window(val start: Calendar, val end: Calendar)
}

