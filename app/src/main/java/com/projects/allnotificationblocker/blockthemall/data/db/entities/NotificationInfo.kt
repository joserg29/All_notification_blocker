package com.projects.allnotificationblocker.blockthemall.data.db.entities

import android.app.*
import android.content.*
import android.os.*
import android.service.notification.*
import androidx.room.*
import androidx.room.Entity
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import timber.log.*
import java.util.*

@Entity(tableName = "notifications")
data class NotificationInfo(
    @PrimaryKey
    var id: String,
    var title: String? = null,
    var packageName: String = "",
    var text: String? = null,
    var timestamp: String? = null,
    var bigText: String = "",
    var template: String? = null,
    var type: Int = 0,
    var subText: String? = null,
    var tag: String? = null,
    var number: Int = 0,
    var iconString: String? = null,
    var postTime: String? = null,
    var count: Int = 1,
    var notificationGroup: String? = null,
    var oldNotification: Boolean = false,
    var isBlocked: Boolean = false,
    var priority: Int = 0,
): Comparable<NotificationInfo?> {
    override fun compareTo(other: NotificationInfo?): Int {
        val otherTimestamp = other?.timestamp ?: return 1
        val thisTimestamp = this.timestamp ?: return -1
        return otherTimestamp.compareTo(thisTimestamp)
    }


    /**
     * Instantiates a new Notification info.
     *
     * @param intent the intent
     */

    val isAppNotification: Boolean
        get() = type == Constants.DATA_TYPE_APPLICATION//todo remove

    fun logNotification() {
        if (packageName == "com.android.systemui") {
            return
        }
        Timber.Forest.tag("AppInfo")
            .d("*************** Notification from " + packageName + " ***************")
        Timber.Forest.tag("AppInfo")
            .d("[ " + timestamp + "] id: " + id + ", title: " + title + ", text: " + text + ", subText: " + subText + ", bigText: " + bigText + ", Number: " + number + ", Template: " + template)
    }


    val isInboxStyle: Boolean
        get() {
            if (template == null) {
                return false
            }
            return template == "android.app.Notification\$InboxStyle"
        }

    fun hasText(): Boolean {
        return this.text != null && !this.text!!.isEmpty()
    }

    fun hasBigText(): Boolean {
        return !bigText.isEmpty()
    }

    fun hasSubText(): Boolean {
        return !subText.isNullOrBlank()
    }

    fun hasTitle(): Boolean {
        return this.title != null && !this.title!!.isEmpty() && (this.title?.lowercase(Locale.getDefault())
            ?.trim() != "null")
    }

    val bigTextSubstring: String
        get() {
            if (bigText.length > 25) {
                return bigText.substring(0, 25) + " ..."
            }
            return bigText
        }

    val isWhatsApp: Boolean
        get() = packageName == "com.whatsapp"

    val isFacebookMessenger: Boolean
        get() = packageName == "com.facebook.orca"

    val groupType: Int
        get() {
            if (this.isWhatsApp) {
                return Constants.WHATSAPP
            }

            if (this.isFacebookMessenger) {
                return Constants.MESSENGER
            }

            return -1
        }

    val isSocialAppCall: Boolean
        get() {
            if (this.isFacebookMessenger) {
                Timber.Forest.tag("AppInfo").d("$$$$$$ CALL - APP: %s, muting audio", "Facebook Messenger")
                return this.text!!.contains(FACEBOOK_MESSENGER_INCOMING)
            }

            if (this.isWhatsApp) {
                Timber.Forest.tag("AppInfo").d("$$$$$$ CALL - APP: %s, muting audio", "WhatsApp")
                return this.text!!.contains(WHATSAPP_INCOMING)
            }

            return false
        }

    val isSocialAppCallEnd: Boolean
        get() {
            if (this.isFacebookMessenger) {
                return (this.text!!.contains(FACEBOOK_MESSENGER_MISSED) || this.text!!.contains(
                    FACEBOOK_MESSENGER_OPEN
                ))
            }

            if (this.isWhatsApp) {
                return (this.title!!.contains(WHATSAPP_MISSED) || this.title!!.contains(
                    WHATSAPP_OPEN
                ))
            }

            return false
        }


    companion object {
        /**
         * The constant FACEBOOK_MESSENGER_INCOMING.
         */
        const val FACEBOOK_MESSENGER_INCOMING: String = "Calling from Messenger"
        /**
         * The constant FACEBOOK_MESSENGER_MISSED.
         */
        const val FACEBOOK_MESSENGER_MISSED: String = "You missed a call"
        /**
         * The constant FACEBOOK_MESSENGER_OPEN.
         */
        const val FACEBOOK_MESSENGER_OPEN: String = "Tap to return to call"
        /**
         * The constant WHATSAPP_INCOMING.
         */
        const val WHATSAPP_INCOMING: String = "Incoming voice call"
        /**
         * The constant WHATSAPP_MISSED.
         */
        const val WHATSAPP_MISSED: String = "Missed voice call"
        /**
         * The constant WHATSAPP_OPEN.
         */
        const val WHATSAPP_OPEN: String = "Connecting"

        fun createFromIntent(intent: Intent): NotificationInfo {
            val type = intent.getIntExtra("type", -1)

            return when (type) {
                Constants.DATA_TYPE_APPLICATION -> {
                    Timber.Forest.tag("AppInfo").d("TYPE: App Notification")
                    NotificationInfo(
                        id = intent.getStringExtra("id") ?: "${System.currentTimeMillis()}",
                        title = intent.getStringExtra("title"),
                        packageName = intent.getStringExtra("package_name")!!,
                        text = intent.getStringExtra("text"),
                        timestamp = intent.getStringExtra("timestamp"),
                        bigText = intent.getStringExtra("bigText") ?: "",
                        template = intent.getStringExtra("template"),
                        subText = intent.getStringExtra("subText"),
                        tag = intent.getStringExtra("tag"),
                        number = intent.getIntExtra("number", -1),
                        notificationGroup = intent.getStringExtra("notification_group"),
                        postTime = intent.getStringExtra("posttime"),
                        oldNotification = intent.getBooleanExtra("is_old", false),
                        isBlocked = intent.getBooleanExtra("is_blocked", false),
                        priority = intent.getIntExtra(
                            "level",
                            3
                        )// it 's NotificationManager.IMPORTANCE_DEFAULT
                    )
                }

                else -> throw IllegalArgumentException("Invalid type: $type")
            }

        }


        fun prepareIntent(
            notification: StatusBarNotification,
            isOld: Boolean,
            isBlocked: Boolean,
        ): Intent {
            Timber.Forest.tag("AppInfo").d("prepareIntent")
            val intent = Intent("com.projects.allnotificationblocker.blockthemall")
            val notificationExtras = notification.notification.extras
            var notificationText: String?
            var notificationTitle: String?

            notificationText = Util.extractText(
                notification.notification.extras.toString(), "android.text=", ", android."
            )

            notificationTitle = Util.extractText(
                notification.notification.extras.toString(), "android.title=", ", android."
            )

            Timber.Forest.tag("AppInfo")
                .d("title: %s, text: %s", notificationTitle, notificationText)
            val messageBundles =
                notificationExtras.get(Notification.EXTRA_MESSAGES) as Array<Parcelable>?
            if (messageBundles != null) {
                val msgBundle = messageBundles[0] as Bundle

                if (notificationTitle.isEmpty()) {
                    notificationTitle = msgBundle.getString("title") + "\n"
                }
                if (notificationText.isEmpty()) {
                    notificationText = msgBundle.getString("text") + "\n"
                }
            }
            Timber.Forest.tag("AppInfo")
                .d("title: %s, text: %s", notificationTitle, notificationText)
            val notificationBigText = Util.extractText(
                notification.notification.extras.toString(), "android.bigText=", ", android."
            )

            intent.putExtra("tag", notification.tag)

            intent.putExtra("type", Constants.DATA_TYPE_APPLICATION)
            intent.putExtra(
                "id",
                "${System.currentTimeMillis()}-${notification.id}}-${notification.packageName}"
            )
            Timber.tag("not").d("%s%s", notification.tag, notification.id)
            intent.putExtra("number", notification.notification.number)
            intent.putExtra("level", notification.notification.priority)

            intent.putExtra("title", notificationTitle)
            intent.putExtra("text", notificationText)
            intent.putExtra("bigText", notificationBigText)
            intent.putExtra(
                "subText",
                notificationExtras.getString("android.subText")
            )
            intent.putExtra(
                "template",
                notificationExtras.getString("android.template")
            )
            //intent.putExtra("icon", notification.getNotification().extras.getString("android.icon"));
            intent.putExtra("package_name", notification.packageName)

            Timber.Forest.tag("AppInfo")
                .d(
                    "notification.when(): %s",
                    DateTimeUtil.LongToDateString(notification.notification.`when`)
                )

            intent.putExtra(
                "timestamp",
                DateTimeUtil.LongToDateString(notification.notification.`when`)
            )
            intent.putExtra("posttime", notification.postTime.toString())
            intent.putExtra("notification_group", notification.notification.group)

            intent.putExtra("is_old", isOld)
            intent.putExtra("is_blocked", isBlocked)

            if (notification.notification.contentIntent != null) {
                //tag("AppInfo").d("intent: " + notification.getNotification().contentIntent.toString());

                val key = notification.packageName + "$#" + notification.postTime
                /* if (MyApplication.STORE_PENDING_INTENT) {
                     val parcel = Parcel.obtain()
                     notification.notification.contentIntent.writeToParcel(parcel, 0)
                     val bytes = parcel.marshall()
                     Timber.tag("AppInfo").d("bytes: " + bytes)
                     intent.putExtra("intent", bytes)
                 }
    */
                MyApplication.Companion.addIntent(
                    key, notification.notification.contentIntent
                )
            }

            return intent
        }

    }
}