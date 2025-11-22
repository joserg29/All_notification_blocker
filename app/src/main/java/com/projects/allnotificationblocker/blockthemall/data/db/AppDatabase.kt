package com.projects.allnotificationblocker.blockthemall.data.db

import android.content.*
import androidx.room.*
import com.projects.allnotificationblocker.blockthemall.data.db.converter.ScheduleConverters
import com.projects.allnotificationblocker.blockthemall.data.db.dao.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.Schedule

@Database(
    entities = [Profile::class, Rule::class, NotificationInfo::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(ScheduleConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun profilesDao(): ProfilesDao
    abstract fun rulesDao(): RulesDao
    abstract fun notificationsDao(): NotificationsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
        }
    }
}