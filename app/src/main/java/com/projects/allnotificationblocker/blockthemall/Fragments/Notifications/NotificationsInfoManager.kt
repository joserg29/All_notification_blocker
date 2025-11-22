package com.projects.allnotificationblocker.blockthemall.Fragments.Notifications

import android.content.*
import android.util.*
import com.google.gson.*
import com.google.gson.annotations.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication.Companion.getAppInfo
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.AppInfo.Companion.getAppInfo
import timber.log.*

class NotificationsInfoManager {
    @Expose
    val notifications: MutableList<NotificationInfo> = mutableListOf()

    private fun shouldSkipAppNotification(
        context: Context,
        myNotification: NotificationInfo,
    ): Boolean {
        Log.d("APP", "shouldSkipAppNotification")
        myNotification.logNotification()
        val app = if (MyApplication.USE_MY_APPLICATION) {
            getAppInfo(myNotification.packageName)
        } else {
            getAppInfo(context, myNotification.packageName)
        }

        if (app == null) {
            Log.d("APP", ">>>>>> Checking if app is not monitored (app == null): true => return")

            return true
        } else {
            Log.d("APP", ">>>>>> Checking if app is not monitored (app == null): false")
        }


        if (isNotificationExistByTitleTextTimestamp(myNotification)) {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification already exist: true")

            return true
        } else {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification already exist: false")
        }


        if (myNotification.text == "null") {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification text == null: true")

            return true
        } else {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification text == null: false")
        }

        if (myNotification.text!!.isEmpty() && myNotification.title!!.isEmpty()) {
            Timber.tag("AppInfo")
                .d(">>>>>> Checking if notification is from title and text are empty = true")

            return true
        } else {
            Timber.tag("AppInfo")
                .d(">>>>>> Checking if notification is from title and text are empty = true")
        }


        if (myNotification.isWhatsApp) {
            return filterWhatsApp(myNotification)
        }

        if (myNotification.isFacebookMessenger) {
            return filterFacebookMessenger(myNotification)
        }

        if (!MyApplication.DEBUG_BLOCKING) {
            if (app.appName == context.getString(R.string.app_name)) {
                Timber.tag("AppInfo")
                    .d(">>>>>> Checking if notification from the BlockThemAll app: true")

                return true
            } else {
                Timber.tag("AppInfo")
                    .d(">>>>>> Checking if notification from the BlockThemAll app: false")
            }
        }


        if (app.appName == context.getString(R.string.system_ui)) {
            //tag("AppInfo").d("[Notification Skipped], Reason: System UI");
            Timber.tag("AppInfo").d(">>>>>> Checking if notification from the System UI: true ")

            return true
        } else {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification from the System UI: false ")
        }


        if (app.appName == context.getString(R.string.android_system)) {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification from the Android System: true")
            return true
        } else {
            Timber.tag("AppInfo")
                .d(">>>>>> Checking if notification from the Android System: false ")
        }


        if (myNotification.text == context.getString(R.string.dialing)
            || myNotification.text == context.getString(R.string.ongoing_call)
            || myNotification.text == context.getString(R.string.incoming_call)
        ) {
            Timber.tag("AppInfo")
                .d(">>>>>> Checking if notification from the Phone (Dialing/Incoming/Outgoing): true")

            return true
        } else {
            Timber.tag("AppInfo")
                .d(">>>>>> Checking if notification from the Phone (Dialing/Incoming/Outgoing): false")
        }


        if (!myNotification.hasTitle()) {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification doesn't have title: true")

            return true
        } else {
            Timber.tag("AppInfo").d(">>>>>> Checking if notification doesn't have title: false")
        }


        return false
    }

    private fun filterWhatsApp(myNotification: NotificationInfo): Boolean {
        Timber.tag("AppInfo").d("processWhatsAppNotification")
        var skip_notification = myNotification.text == "Checking for new messages"
        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from whatsapp and contains 'Checking for new messages': %s",
            skip_notification
        )


        if (myNotification.isInboxStyle) {
            skip_notification = true
        }
        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from whatsapp and inboxStyle: %s",
            skip_notification
        )


        if (myNotification.text == "Incoming voice call") {
            skip_notification = true
        }
        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from whatsapp and voice call: %s",
            skip_notification
        )



        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from whatsapp and ID == 3 (media): %s",
            skip_notification
        )

        if (myNotification.title!!.contains("Deleting messages")) {
            skip_notification = true
        }
        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from whatsapp deleting messages: %s",
            skip_notification
        )


        return skip_notification
    }

    private fun filterFacebookMessenger(myNotification: NotificationInfo): Boolean {
        Timber.tag("AppInfo").d("processFacebookNotification")
        val skip_notification = myNotification.title == "Chat heads active"

        Timber.tag("AppInfo").d(
            ">>>>>> Checking if notification is from facebook messenger and contains 'Chat heads active': %s",
            skip_notification
        )

        return skip_notification
    }

    private fun isNotificationExistByTitleTextTimestamp(myNotification: NotificationInfo): Boolean {
        var found = false

        Timber.tag("AppInfo").d("------- isNotificationExistByTitleTextTimestamp")
        if (myNotification.isAppNotification) {
            Timber.tag("AppInfo").d(
                "------- [Title] %s, [Text] %s, [Timestamp] %s",
                myNotification.title, formatText(myNotification),
                myNotification.timestamp
            )
        }

        for (i in notifications.indices) {
            val m = notifications[i]

            m.logNotification()

            val equalTimeStamps = DateTimeUtil.getTimeDiffInSeconds(
                m.timestamp!!,
                myNotification.timestamp!!
            ) <= 2

            if (myNotification.isAppNotification || m.isAppNotification) {
                val equalText = m.text!!.contains(formatText(myNotification))
                val equalTitle = m.title == myNotification.title

                Timber.tag("AppInfo").d(
                    "-- Checking [Title] %s, [Text] %s, [Timestamp] %s",
                    m.title,
                    m.text,
                    m.timestamp
                )

                if (equalText && equalTitle && equalTimeStamps) {
                    found = true
                    break
                }
            }
        }

        return found
    }

    fun addAppNotification(context: Context, myNotification: NotificationInfo): Boolean {
        if (shouldSkipAppNotification(context, myNotification)) {
            return false
        }
        //addGroupNotification(myNotification)
        notifications.sort()
        return true
    }

    private fun addGroupNotification(myNotification: NotificationInfo) {
        Timber.tag("AppInfo").d("myNotification.getGroupType(): %d", myNotification.groupType)

        var pos = -1
        for (i in notifications.indices) {
            if (notifications[i].packageName == myNotification.packageName &&
                notifications[i].title == myNotification.title
            ) {
                pos = i
                break
            }
        }
        Timber.tag("AppInfo").d("pos: %d", pos)
        if (pos != -1) {
            var text = notifications[pos].text
            text = formatText(myNotification) + "\n" + text
            notifications[pos].text = text
            notifications[pos].timestamp = myNotification.timestamp
            if (notifications[pos].postTime !== myNotification.postTime) {
                notifications[pos].postTime = myNotification.postTime
            }
            notifications[pos].count += 1
        } else {
            val text = formatText(myNotification)
            myNotification.text = text
            myNotification.count += 1
            notifications.add(myNotification)
        }
    }

    private fun formatText(myNotification: NotificationInfo): String {
        val timestamp = DateTimeUtil.toHrMinAmPm(myNotification.timestamp!!)

        return timestamp + ": " + myNotification.text
    }

    fun clearNotifications() {
        notifications.clear()
    }

    fun toJson(): String? {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromJson(json: String?): NotificationsInfoManager {
            val gson = Gson()
            return gson.fromJson<NotificationsInfoManager>(
                json,
                NotificationsInfoManager::class.java
            )
        }
    }
}
