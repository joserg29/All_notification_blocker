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
            // Reload rules to get latest state (critical for Block All)
            rulesManager = loadRulesManager()
            if (rulesManager == null) {
                Timber.tag(TAG).w("RulesManager is null, cannot cancel notifications")
                return
            }
            
            val isBlockAllEnabled = rulesManager?.isBlockAllEnabled ?: false
            Timber.tag(TAG).d("cancelAllBlockedNotifications: isBlockAllEnabled=%s, rules count=%d", 
                isBlockAllEnabled, rulesManager?.rules?.size ?: 0)
            
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
            
            Timber.tag(TAG).d("Checking %d active notifications", activeNotifications.size)
            var cancelledCount = 0
            val packagesToClose = mutableSetOf<String>()
            
            for (sbn in activeNotifications) {
                try {
                    // Skip system UI and our own app
                    val packageName = sbn.packageName ?: continue
                    if (packageName == "com.android.systemui" || packageName == this.packageName) {
                        continue
                    }
                    
                    // Check if this notification should be blocked
                    val isAllowed = rulesManager?.isAllowed(packageName) ?: true
                    Timber.tag(TAG).d("Notification from %s: isAllowed=%s", packageName, isAllowed)
                    
                    if (!isAllowed) {
                        try {
                            // Try multiple cancellation methods
                            cancelNotification(sbn.key)
                            
                            // Also try by tag/id if available
                            if (sbn.tag != null) {
                                try {
                                    cancelNotification(packageName, sbn.tag, sbn.id)
                                } catch (e: Exception) {
                                    // Ignore - may not be supported
                                }
                            }
                            
                            cancelledCount++
                            packagesToClose.add(packageName)
                            Timber.tag(TAG).d("Cancelled notification from: %s (key: %s)", packageName, sbn.key)
                        } catch (e: SecurityException) {
                            Timber.tag(TAG).w("SecurityException cancelling notification from: %s", packageName)
                            packagesToClose.add(packageName) // Still close app even if cancellation fails
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Error cancelling notification from: %s", packageName)
                            packagesToClose.add(packageName) // Still close app even if cancellation fails
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error processing notification")
                }
            }
            
            // Close all background apps for blocked notifications (more aggressive in Block All mode)
            for (packageName in packagesToClose) {
                try {
                    BackgroundAppController.closeApp(applicationContext, packageName, isBlockAllEnabled)
                    Timber.tag(TAG).d("Requested background app closure for: %s (BlockAll=%s)", packageName, isBlockAllEnabled)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error closing background app for: %s", packageName)
                }
            }
            
            Timber.tag(TAG).d("Cancelled %d blocked notifications, closed %d apps", cancelledCount, packagesToClose.size)
            
            // Retry cancellation after a short delay for any notifications that might have been missed
            if (cancelledCount > 0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val retryNotifications = getActiveNotifications()
                        if (retryNotifications != null) {
                            var retryCount = 0
                            for (sbn in retryNotifications) {
                                val packageName = sbn.packageName ?: continue
                                if (packageName == "com.android.systemui" || packageName == this.packageName) {
                                    continue
                                }
                                val isAllowed = rulesManager?.isAllowed(packageName) ?: true
                                if (!isAllowed) {
                                    try {
                                        cancelNotification(sbn.key)
                                        retryCount++
                                    } catch (e: Exception) {
                                        // Ignore retry errors
                                    }
                                }
                            }
                            if (retryCount > 0) {
                                Timber.tag(TAG).d("Retry cancelled %d additional notifications", retryCount)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).d("Retry cancellation error: %s", e.message)
                    }
                }, 200)
            }
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
            
            // Reload rules to get latest state (especially important for Block All)
            rulesManager = loadRulesManager()
            if (rulesManager == null) {
                Timber.tag(TAG).w("RulesManager is null, allowing notification from %s", sourcePackage)
                return
            }
            
            val isAllowed = rulesManager?.isAllowed(sourcePackage) ?: true
            Timber.tag(TAG).d("Notification from %s: isAllowed=%s, isBlockAllEnabled=%s", 
                sourcePackage, isAllowed, rulesManager?.isBlockAllEnabled)
            
            // Always store the notification first
            val intent = prepareIntent(sbn, false, !isAllowed)
            NotificationsFragment.onReceive(intent)
            
            if (!isAllowed) {
                // Block this notification - try multiple cancellation methods
                try {
                    // Method 1: Cancel by key (primary method)
                    cancelNotification(sbn.key)
                    Timber.tag(TAG).d("Cancelled notification from %s using key: %s", sourcePackage, sbn.key)
                    
                    // Method 2: Cancel by tag and ID (backup method)
                    if (sbn.tag != null) {
                        try {
                            cancelNotification(sourcePackage, sbn.tag, sbn.id)
                            Timber.tag(TAG).d("Cancelled notification from %s using tag/id: %s/%d", sourcePackage, sbn.tag, sbn.id)
                        } catch (e: Exception) {
                            Timber.tag(TAG).d("Could not cancel by tag/id (may not be supported): %s", e.message)
                        }
                    }
                    
                    // Method 3: Retry cancellation after a short delay (some notifications take time to appear)
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            cancelNotification(sbn.key)
                            Timber.tag(TAG).d("Retry cancelled notification from %s", sourcePackage)
                        } catch (e: Exception) {
                            Timber.tag(TAG).d("Retry cancellation failed: %s", e.message)
                        }
                    }, 100)
                    
                    // Close the background app immediately (more aggressive in Block All mode)
                    val isBlockAllMode = rulesManager?.isBlockAllEnabled ?: false
                    BackgroundAppController.closeApp(applicationContext, sourcePackage, isBlockAllMode)
                    Timber.tag(TAG).d("Requested background app closure for %s (BlockAll=%s)", sourcePackage, isBlockAllMode)
                    
                } catch (e: SecurityException) {
                    Timber.tag(TAG).w("SecurityException cancelling notification from %s: %s", sourcePackage, e.message)
                    // Still try to close the app even if cancellation fails
                    val isBlockAllMode = rulesManager?.isBlockAllEnabled ?: false
                    BackgroundAppController.closeApp(applicationContext, sourcePackage, isBlockAllMode)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error cancelling notification from %s", sourcePackage)
                    // Still try to close the app even if cancellation fails
                    val isBlockAllMode = rulesManager?.isBlockAllEnabled ?: false
                    BackgroundAppController.closeApp(applicationContext, sourcePackage, isBlockAllMode)
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
