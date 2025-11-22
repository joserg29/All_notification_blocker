package com.projects.allnotificationblocker.blockthemall.data.repo

import android.content.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

class NotificationsRepo(context: Context) {
    private val dao = AppDatabase.getInstance(context).notificationsDao()
    private val allNotifications: LiveData<MutableList<NotificationInfo>> =
        dao.getAllNotificationsLive()

    suspend fun insert(notification: NotificationInfo) {
        dao.upsert(notification)
    }

    suspend fun delete(notification: NotificationInfo) {
        dao.delete(notification)
    }

    suspend fun deleteAllRecords() {
        dao.deleteAllNotifications()
    }

    fun getAllRecordsLive(): LiveData<MutableList<NotificationInfo>> = allNotifications
    suspend fun getAllRecords(): MutableList<NotificationInfo> =
        dao.getAllNotifications()

}