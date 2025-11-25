package com.projects.allnotificationblocker.blockthemall.Utilities

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.MyNotListenerService
import timber.log.Timber

object NotificationServiceGuard {
    fun ensureServiceRunning(context: Context) {
        val appContext = context.applicationContext
        val hasAccess = NotificationManagerCompat.getEnabledListenerPackages(appContext)
            .contains(appContext.packageName)
        if (!hasAccess) {
            Timber.tag("NotificationServiceGuard")
                .w("Notification listener access not granted, cannot ensure service.")
            return
        }
        if (!MyNotListenerService.isServiceRunning) {
            Timber.tag("NotificationServiceGuard").d("Notification service not running, starting it.")
            MyNotListenerService.startService(appContext, MyNotListenerService.Actions.Enable)
        }
    }
}

