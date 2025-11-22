package com.projects.allnotificationblocker.blockthemall.domain

import android.content.*
import android.content.pm.*
import androidx.room.*
import androidx.room.Entity
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Utilities.*

@Entity(tableName = "app_infos_table")
class AppInfo: Comparable<AppInfo?> {
    @PrimaryKey(autoGenerate = true) var pkey: Int = 0

    var appName: String = ""
    var packageName: String = ""
    var isEnabled: Boolean = false

    override operator fun compareTo(appInfo: AppInfo?): Int {
        if (appInfo == null) {
            // If the other object is null, consider this object greater
            return 1
        }

        if (this.isEnabled != appInfo.isEnabled) {
            return if (this.isEnabled) -1 else 1
        }

        val s1 = Constants.socialMediaApps.contains(packageName)
        val s2 = Constants.socialMediaApps.contains(appInfo.packageName)
        if (s1 != s2) {
            // Sort social media apps after non-social media apps
            return if (s1) -1 else 1
        }

        // If both are in the same category, sort by name
        return this.appName.compareTo(appInfo.appName)
    }

    companion object {
        fun getAppInfo(context: Context, packageName: String): AppInfo? {
            if (MyApplication.Companion.USE_MY_APPLICATION) {
                return MyApplication.Companion.getAppInfo(packageName)
            }
            var packageInfo: PackageInfo? = null
            try {
                if (context.packageManager.getLaunchIntentForPackage(packageName) != null) {
                    packageInfo = context.packageManager.getPackageInfo(packageName, 0)
                    val newInfo = AppInfo()
                    newInfo.appName =
                        packageInfo.applicationInfo!!.loadLabel(context.packageManager)
                            .toString()
                    newInfo.packageName = packageInfo.packageName
                    return newInfo
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return null

        }
    }
}