package com.projects.allnotificationblocker.blockthemall.Utilities.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.projects.allnotificationblocker.blockthemall.Utilities.BlockingAudioCoordinator
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants
import com.projects.allnotificationblocker.blockthemall.Utilities.NotificationServiceGuard
import com.projects.allnotificationblocker.blockthemall.Utilities.Util
import com.projects.allnotificationblocker.blockthemall.data.db.converter.ScheduleConverters
import java.util.Date

class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getStringExtra(RuleScheduler.EXTRA_ALARM_ID) ?: return
        val scheduleJson = intent.getStringExtra(RuleScheduler.EXTRA_SCHEDULE) ?: return
        val schedule = ScheduleConverters().fromJson(scheduleJson) ?: return
        val triggerAt = intent.getLongExtra(
            RuleScheduler.EXTRA_TRIGGER_AT,
            System.currentTimeMillis()
        )

        val rulesManager = Util.loadRulesManager() ?: return
        val rule = rulesManager.rules.firstOrNull { it.alarmId == alarmId } ?: return

        if (rule.schedule != schedule) {
            // Schedule changed since this alarm was created; resync with current values.
            RuleScheduler.schedule(context, rule)
            return
        }

        when (action) {
            RuleScheduler.ACTION_ACTIVATE_RULE -> {
                rule.isEnabled = true
                rule.status = Constants.RULES_STATUS_ACTIVE
                RuleScheduler.scheduleNextStart(context, rule, triggerAt + 60_000L)
            }

            RuleScheduler.ACTION_DEACTIVATE_RULE -> {
                rule.isEnabled = false
                rule.status = if (rule.schedule.isOutDated(Date(triggerAt))) {
                    Constants.RULES_STATUS_EXPIRED
                } else {
                    Constants.RULES_STATUS_INACTIVE
                }
                RuleScheduler.scheduleNextEnd(context, rule, triggerAt + 60_000L)
            }

            else -> return
        }

        Util.saveRulesManager(rulesManager)
        BlockingAudioCoordinator.syncWithRules(context, rulesManager)
        NotificationServiceGuard.ensureServiceRunning(context)
    }
}




