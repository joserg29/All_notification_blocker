package com.projects.allnotificationblocker.blockthemall.Utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationServiceRestarterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Best effort: whenever the system sends any of the subscribed broadcasts,
        // make sure the notification listener is running.
        NotificationServiceGuard.ensureServiceRunning(context.applicationContext)
    }
}

