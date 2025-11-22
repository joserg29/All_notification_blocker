package com.projects.allnotificationblocker.blockthemall.domain

import com.projects.allnotificationblocker.blockthemall.Utilities.*

data class AppStat(
    val packageName: String,
    val appName: String = "",
    val notificationBlockedCount: Int,
    val notificationPercentage: Float,
): Comparable<AppStat?> {
    override operator fun compareTo(appInfo: AppStat?): Int {
        if (appInfo == null) {
            // If the other object is null, consider this object greater
            return 1
        }
        val s1 = Constants.socialMediaApps.contains(this.packageName)
        val s2 = Constants.socialMediaApps.contains(appInfo.packageName)
        if (s1 != s2) {
            // Sort social media apps after non-social media apps
            return if (s1) -1 else 1
        }

        // If both are in the same category, sort by name
        return this.appName.compareTo(appInfo.appName)
    }

}