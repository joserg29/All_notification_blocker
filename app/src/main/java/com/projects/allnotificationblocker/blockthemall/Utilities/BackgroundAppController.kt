package com.projects.allnotificationblocker.blockthemall.Utilities

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Best-effort helper that closes background processes for packages we are blocking.
 * Works within the limits of the KILL_BACKGROUND_PROCESSES permission.
 */
object BackgroundAppController {
    private const val MIN_KILL_INTERVAL_MS = 60_000L
    private const val MIN_KILL_INTERVAL_BLOCK_ALL_MS = 5_000L // More aggressive for Block All
    private val lastKillMap = ConcurrentHashMap<String, Long>()

    fun closeApp(context: Context, packageName: String?, isBlockAllMode: Boolean = false) {
        if (packageName.isNullOrBlank()) return

        val now = System.currentTimeMillis()
        val lastKill = lastKillMap[packageName]
        val minInterval = if (isBlockAllMode) MIN_KILL_INTERVAL_BLOCK_ALL_MS else MIN_KILL_INTERVAL_MS
        
        if (lastKill != null && now - lastKill < minInterval) {
            // Avoid spamming the system with kill requests for the same package.
            Timber.tag("BackgroundAppController").d("Skipping kill for %s (rate limited, last kill %d ms ago)", 
                packageName, now - lastKill)
            return
        }
        lastKillMap[packageName] = now

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return
        try {
            Timber.tag("BackgroundAppController").d("Killing background processes for %s (BlockAll=%s)", 
                packageName, isBlockAllMode)
            
            // Method 1: killBackgroundProcesses (standard method)
            activityManager.killBackgroundProcesses(packageName)
            
            // Method 2: forceStopCompat (more aggressive, if available)
            forceStopCompat(activityManager, packageName)
            
            Timber.tag("BackgroundAppController").d("Successfully requested closure for %s", packageName)
        } catch (securityException: SecurityException) {
            Timber.tag("BackgroundAppController")
                .w(securityException, "Not allowed to kill background process for %s", packageName)
        } catch (t: Throwable) {
            Timber.tag("BackgroundAppController")
                .e(t, "Error closing background app for %s", packageName)
        }
    }

    @Suppress("DiscouragedPrivateApi")
    private fun forceStopCompat(activityManager: ActivityManager, packageName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        try {
            val method = ActivityManager::class.java.getMethod("forceStopPackage", String::class.java)
            method.isAccessible = true
            method.invoke(activityManager, packageName)
            Timber.tag("BackgroundAppController").d("forceStopPackage invoked for %s", packageName)
        } catch (securityException: SecurityException) {
            // Expected on non-system builds; ignore but log for debugging.
            Timber.tag("BackgroundAppController")
                .w(securityException, "forceStopPackage rejected for %s", packageName)
        } catch (ignored: Throwable) {
            // Hidden API might not be accessible; best-effort only.
        }
    }
}

