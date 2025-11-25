package com.projects.allnotificationblocker.blockthemall.Utilities.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.projects.allnotificationblocker.blockthemall.Utilities.NotificationServiceGuard
import com.projects.allnotificationblocker.blockthemall.Utilities.Util

class ScheduleBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val rulesManager = Util.loadRulesManager()
            rulesManager?.let {
                RuleScheduler.rescheduleAll(context, it.rules)
            }
            NotificationServiceGuard.ensureServiceRunning(context)
        }
    }
}




