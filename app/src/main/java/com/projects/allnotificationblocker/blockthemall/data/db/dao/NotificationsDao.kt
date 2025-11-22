package com.projects.allnotificationblocker.blockthemall.data.db.dao

import androidx.lifecycle.*
import androidx.room.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

@Dao
interface NotificationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notification: NotificationInfo)

    @Delete suspend fun delete(notification: NotificationInfo)

    @Query("DELETE FROM notifications") suspend fun deleteAllNotifications()

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsLive(): LiveData<MutableList<NotificationInfo>>
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): MutableList<NotificationInfo>
}