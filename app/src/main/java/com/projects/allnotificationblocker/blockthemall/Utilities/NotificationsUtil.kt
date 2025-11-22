package com.projects.allnotificationblocker.blockthemall.Utilities

import android.app.*
import android.content.*
import android.view.*
import android.widget.*
import androidx.core.app.*
import androidx.core.content.*
import com.projects.allnotificationblocker.blockthemall.Activities.Home.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.CHANNEL_ID
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import timber.log.*

object NotificationsUtil {
    const val myPackageName: String =
        "com.projects.allnotificationblocker.blockthemall.notificationbar"


    fun formatNotificationsAsMarkdown(infos: List<NotificationInfo>): String {
        var i = 0
        return infos.joinToString(separator = "\n") { info ->
            var res = if (i > 0 && infos[i].title == infos[i - 1].title) {//if same title
                "\n"
            } else {
                "### ${info.title?.replace("\n", " ")}\n"
            }
            res = res.plus("`${DateTimeUtil.toHrMinAmPm(info.timestamp!!)}`:${info.text}")
            ++i
            res
        }
    }

    fun getGroupTitle(notifications: List<NotificationInfo>): String {
        return if (notifications.all { notifications[0].title == it.title }) {
            notifications[0].title ?: "`(${notifications.size})`"
        } else {
            notifications[0].title + " *and " + (notifications.size - 1) + " more*"
        }

    }

    fun upsertPermanentNotification(
        context: Context,
        id: Int,
        title: String?,
        message: String?,
        icon: Int,
    ) {
        return //todo
        val resultIntent = Intent(context, HomeActivity::class.java)
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val customView = RemoteViews(
            context.packageName,
            R.layout.custom_notification
        )
        val clickPendingIntent: PendingIntent?
        customView.setTextViewText(R.id.text_view_collapsed_1, context.getString(R.string.app_name))
        Timber.tag("AppInfo").d("createUpdatePermentantNotification - title: %s", title)
        customView.setTextViewText(R.id.text_view_title, title)
        customView.setImageViewResource(R.id.image_view_notification, icon)
        val clickIntent = Intent(myPackageName)
        val icon1 = R.drawable.appicon
        customView.setViewVisibility(R.id.text_view_notification_action, View.VISIBLE)
        customView.setViewVisibility(R.id.image_view_notification2, View.GONE)
        customView.setTextViewText(
            R.id.text_view_notification_action,
            "Exit " + context.getString(R.string.app_name)
        )
        customView.setImageViewResource(R.id.image_view_notification2, R.drawable.appicon)
        customView.setImageViewResource(R.id.image_view_notification, R.drawable.appicon)
        customView.setTextViewText(R.id.text_view_title, title)
        clickIntent.putExtra("action", 0)
        clickPendingIntent = PendingIntent.getBroadcast(
            context,
            0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        customView.setOnClickPendingIntent(R.id.text_view_notification_action, clickPendingIntent)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon1)
            .setContentTitle(title)
            .setColorized(true)
            .setDefaults(0)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentText(message)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_MAX)
            .setColor(ContextCompat.getColor(context, R.color.white))
            .setContentIntent(resultPendingIntent)

        val notification = builder.build()
        notification.flags =
            notification.flags or (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR)
        val mNotifyMgr =
            checkNotNull(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        mNotifyMgr.notify(id, builder.build())
    }
}

fun String.takeMax(length: Int): String {
    return if (this.length > length) {
        this.substring(0, length) + "..."
    } else {
        this
    }
}