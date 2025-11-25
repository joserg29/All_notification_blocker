package com.projects.allnotificationblocker.blockthemall.Application

import android.app.*
import android.content.*
import android.os.*
import androidx.multidex.*
import com.github.tamir7.contacts.*
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.pixplicity.easyprefs.library.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.BuildConfig
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.CHANNEL_ID
import com.projects.allnotificationblocker.blockthemall.Utilities.scheduler.RuleScheduler
import com.projects.allnotificationblocker.blockthemall.data.repo.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*
import timber.log.Timber.*
import java.util.Arrays

class MyApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        context = this
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        MobileAds.initialize(this) { initializationStatus ->
//            loadInterstitialAd()

            val config = RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("7FB649F9058272F029AFC3E4C086E0BB"))
            MobileAds.setRequestConfiguration(config.build())
        }
        Contacts.initialize(this)
        rulesViewModel = RulesViewModel(this)
        notificationRepo = NotificationsRepo(context)
        createNotificationChannel(this)
        Prefs.Builder()
            .setContext(this)
            .setMode(MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        Util.loadRulesManager()?.let {
            RuleScheduler.rescheduleAll(this, it.rules)
        }

        if (STORE_NOTIFICATIONS) {
            Prefs.putString(Constants.PARAM_NOTIFICATIONS_MANAGER, "")
        }
    }


    companion object {
        const val DEBUG_BLOCKING: Boolean = false
        const val USE_MY_APPLICATION: Boolean = true
        const val STORE_NOTIFICATIONS: Boolean = true
        const val ENABLE_ANIMATION: Boolean = true
        var lastShownAd = 0L
        lateinit var context: Context//todo
        lateinit var rulesViewModel: RulesViewModel
        lateinit var notificationRepo: NotificationsRepo

        private val intentsMap: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        private val iconsMap: MutableMap<String, Int?> = HashMap<String, Int?>()
        private val appsMap: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        val appInfos: MutableList<AppInfo> = ArrayList<AppInfo>()

        fun addIntent(key: String?, intent: PendingIntent?) {
            intentsMap.put(key, intent)
        }

        fun getIntent(key: String?): PendingIntent? {
            return intentsMap[key] as PendingIntent?
        }

        fun addIcon(d: Int?, s: String) {
            iconsMap.put(s, d)
        }

        fun getIcon(s: String): Int? {
            return iconsMap[s]
        }

        fun getAppInfo(packageName: String?): AppInfo? {
            return appsMap[packageName] as AppInfo?
        }

        fun addAppInfo(packageName: String?, appInfo: AppInfo) {
            appInfos.add(appInfo)
            appsMap.put(packageName, appInfo)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val description = context.getString(R.string.channel_description)
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
        channel.description = description
        val notificationManager =
            checkNotNull(context.getSystemService<NotificationManager>(NotificationManager::class.java))
        notificationManager.createNotificationChannel(channel)
    }

}
