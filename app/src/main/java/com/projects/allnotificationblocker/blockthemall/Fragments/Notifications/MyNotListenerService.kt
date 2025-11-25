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
import com.projects.allnotificationblocker.blockthemall.Utilities.BackgroundAppController
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.CHANNEL_ID
import com.projects.allnotificationblocker.blockthemall.Utilities.NotificationServiceGuard
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.loadRulesManager
import com.projects.allnotificationblocker.blockthemall.data.db.entities.NotificationInfo.Companion.prepareIntent
import timber.log.*

class MyNotListenerService: NotificationListenerService() {
    var rulesManager: RulesManager? = null
    var action: String? = ""
    private var activeNotification = mutableListOf<StatusBarNotification>()
    private var rulesChangeReceiver: BroadcastReceiver? = null

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
        const val ACTION_RULES_CHANGED = "com.projects.allnotificationblocker.blockthemall.RULES_CHANGED"
        private const val NOTIFICATION_ID = 1
        var isServiceRunning = false
        const val TAG = "NService"
        @Volatile
        private var serviceInstance: MyNotListenerService? = null
        
        fun getInstance(): MyNotListenerService? = serviceInstance
        
        fun startService(context: Context, action: Actions = Actions.Enable) {
            if (serviceInstance != null) {
                Timber.tag(TAG).d("Service is already running")
                // Even if running, trigger immediate cancellation
                serviceInstance?.cancelAllBlockedNotifications()
                return
            }
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
        
        fun triggerImmediateCancellation(context: Context) {
            // Try direct call first if service is running
            serviceInstance?.cancelAllBlockedNotifications()
            // Also send broadcast as backup
            try {
                val intent = Intent(ACTION_RULES_CHANGED)
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
                Timber.tag(TAG).d("Sent immediate cancellation broadcast")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error sending immediate cancellation broadcast")
            }
        }

        fun stopService(context: Context) {
            if (serviceInstance == null)
                return
            Timber.tag(TAG).d("Stoping NotificationListenerService")
            val intent = Intent(context, MyNotListenerService::class.java)
            context.stopService(intent)
            serviceInstance = null
            isServiceRunning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("onCreate")
        isServiceRunning = true
        serviceInstance = this
        try {
            registerRulesChangeReceiver()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error registering rules change receiver")
        }
    }
    
    private fun registerRulesChangeReceiver() {
        try {
            rulesChangeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Timber.tag(TAG).d("Rules changed broadcast received - reloading and cancelling notifications immediately")
                    // Cancel immediately without delay
                    try {
                        cancelAllBlockedNotifications()
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Error in broadcast receiver onReceive")
                    }
                }
            }
            val filter = IntentFilter(ACTION_RULES_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(rulesChangeReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(rulesChangeReceiver, filter)
            }
            Timber.tag(TAG).d("Rules change receiver registered successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in registerRulesChangeReceiver - continuing without broadcast receiver")
            rulesChangeReceiver = null
        }
    }
    
    fun cancelAllBlockedNotifications() {
        try {
            // Reload rules to get latest state
            rulesManager = loadRulesManager()
            if (rulesManager == null) {
                Timber.tag(TAG).w("RulesManager is null, cannot cancel notifications")
                return
            }
            
            // Get all active notifications - check if service is connected first
            val activeNotifications = try {
                getActiveNotifications()
            } catch (e: SecurityException) {
                Timber.tag(TAG).w("SecurityException getting active notifications: %s", e.message)
                return
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error getting active notifications")
                return
            }
            
            if (activeNotifications == null || activeNotifications.isEmpty()) {
                Timber.tag(TAG).d("No active notifications to check")
                return
            }
            
            var cancelledCount = 0
            for (sbn in activeNotifications) {
                try {
                    // Skip system UI and our own app
                    val packageName = sbn.packageName ?: continue
                    if (packageName == "com.android.systemui" || packageName == this.packageName) {
                        continue
                    }
                    
                    // Check if this notification should be blocked
                    val isAllowed = rulesManager?.isAllowed(packageName) ?: true
                    if (!isAllowed) {
                        try {
                            cancelNotification(sbn.key)
                            cancelledCount++
                            Timber.tag(TAG).d("Cancelled notification from: %s", packageName)
                            BackgroundAppController.closeApp(applicationContext, packageName)
                        } catch (e: SecurityException) {
                            Timber.tag(TAG).w("SecurityException cancelling notification from: %s", packageName)
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Error cancelling notification from: %s", packageName)
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error processing notification")
                }
            }
            Timber.tag(TAG).d("Cancelled %d blocked notifications", cancelledCount)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in cancelAllBlockedNotifications")
        } 
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
        
        // Clear service instance
        serviceInstance = null
        isServiceRunning = false
        
        // Unregister broadcast receiver
        rulesChangeReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error unregistering rules change receiver")
            }
            rulesChangeReceiver = null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.tag(TAG).w("Notification listener disconnected, attempting to rebind.")
        isServiceRunning = false
        serviceInstance = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requestRebind(ComponentName(this, MyNotListenerService::class.java))
            }
        } catch (t: Throwable) {
            Timber.tag(TAG).e(t, "Failed to request rebind")
        }
        Handler(Looper.getMainLooper()).postDelayed({
            NotificationServiceGuard.ensureServiceRunning(applicationContext)
        }, 1000L)
    }


    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotification = getActiveNotifications()?.toMutableList() ?: mutableListOf()
            // Cancel any blocked notifications immediately when service connects
            try {
                cancelAllBlockedNotifications()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error cancelling notifications in onListenerConnected")
            }
        } catch (ex: Exception) {
            Timber.tag(TAG).e(ex, "Error in onListenerConnected: %s", ex.message)
            // Don't throw - just log the error to prevent service crash
        }

    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == "com.android.systemui") {
                return
            }
            if (sbn.packageName == packageName) {
                return
            }
            val sourcePackage = sbn.packageName ?: return
            rulesManager = loadRulesManager()
            if (rulesManager == null) {
                Timber.tag(TAG).w("RulesManager is null, allowing notification")
                return
            }
            val isAllowed = rulesManager?.isAllowed(sourcePackage) ?: true
            val intent = prepareIntent(sbn, false, !isAllowed)
            NotificationsFragment.onReceive(intent)
            if (!isAllowed) {
                try {
                    cancelNotification(sbn.key)
                    BackgroundAppController.closeApp(applicationContext, sourcePackage)
                } catch (e: SecurityException) {
                    Timber.tag(TAG).w("SecurityException cancelling notification")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error cancelling notification")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in onNotificationPosted")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Timber.tag(TAG).d("<<<<<******* Notification Removed ******>>>>>")
    }


}
