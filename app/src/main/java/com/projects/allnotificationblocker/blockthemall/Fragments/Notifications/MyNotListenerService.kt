package com.projects.allnotificationblocker.blockthemall.Fragments.Notifications

import android.app.*
import android.content.*
import android.content.pm.*
import android.os.*
import android.service.notification.*
import androidx.annotation.RequiresApi
import androidx.core.app.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.CHANNEL_ID
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.loadRulesManager
import com.projects.allnotificationblocker.blockthemall.data.db.entities.NotificationInfo.Companion.prepareIntent
import timber.log.*

class MyNotListenerService: NotificationListenerService() {
    var rulesManager: RulesManager? = null
    var action: String? = ""
    private var activeNotification = mutableListOf<StatusBarNotification>()

    enum class Actions(val value: String) {
        Enable("enable"), DeleteAll("delete_all")
    }

    fun toAction(action: String?): Actions? {
        return when (action) {
            Actions.Enable.value -> Actions.Enable
            Actions.DeleteAll.value -> Actions.DeleteAll
            else -> null
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        var isServiceRunning = false
        const val TAG = "NService"
        fun                 startService(context: Context, action: Actions = Actions.Enable) {
            if (isServiceRunning) {
                Timber.tag(TAG).d("Service is already running")
                return
            }
            isServiceRunning = true
            Timber.tag(TAG).d("Starting NotificationListenerService")
            val notificationsServiceIntent = Intent(
                context,
                MyNotListenerService::class.java
            )
            notificationsServiceIntent.setAction(action.value)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(notificationsServiceIntent)
            } else {
                context.startService(notificationsServiceIntent)
            }
        }

        fun stopService(context: Context) {
            if (!isServiceRunning)
                return
            Timber.tag(TAG).d("Stoping NotificationListenerService")
            val intent = Intent(context, MyNotListenerService::class.java)
            context.stopService(intent)
            isServiceRunning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        if (intent != null) {
            action = intent.getStringExtra("action")
            Timber.tag(TAG).d("action: %s", action)
        } else {
            action = ""
        }

        for (sbn in activeNotification) {
            Timber.tag(TAG).d("sbn ID = %s", sbn.id)
            Timber.tag(TAG).d("SBN PackageName = %s", sbn.packageName)
            if (sbn.packageName == packageName) {
                continue
            }
            cancelNotification(sbn.key)
            if (toAction(action) == Actions.DeleteAll) {
            } else {
                //       val intent1 = prepareIntent(sbn, true, false)
                //       sendBroadcast(intent1)
            }
        }
        Timber.tag(TAG).d("removed notification  = %s", activeNotification.size)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_is_running))
            .setSmallIcon(R.drawable.ic_notifications)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).d("NotificationListenerService is destroyed")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }


    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotification = getActiveNotifications()?.toMutableList() ?: mutableListOf()
        } catch (ex: Exception) {
            Timber.tag(TAG).d("Error: %s", ex.message)
            throw ex
        }

    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == "com.android.systemui") {
            return
        }
        if (sbn.packageName == packageName) {
            return
        }
        rulesManager = loadRulesManager()
        NotificationsFragment.onReceive(prepareIntent(sbn, true, true))
        val extras = sbn.notification.extras
        val b = extras.get(Notification.EXTRA_MESSAGES) as Array<*>?
        if (b != null) {
            var content = ""
            for (tmp in b) {
                val msgBundle = tmp as Bundle
                content = msgBundle.getString("text") + "\n"
            }

            Timber.tag(TAG).d("content: %s", content)
        }
        val isAllowed = rulesManager!!.isAllowed(sbn.packageName)
        if (!isAllowed) {
            cancelNotification(sbn.key)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Timber.tag(TAG).d("<<<<<******* Notification Removed ******>>>>>")
    }


}
