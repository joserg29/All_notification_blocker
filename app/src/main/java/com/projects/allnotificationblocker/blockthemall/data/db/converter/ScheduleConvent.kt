package com.projects.allnotificationblocker.blockthemall.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.projects.allnotificationblocker.blockthemall.domain.Schedule

class ScheduleConverters  {
    @TypeConverter
    fun fromJson(json:String?): Schedule?{
        return Gson().fromJson(json, Schedule::class.java)
    }

    @TypeConverter
    fun toJson(schedule: Schedule?): String?{
        return Gson().toJson(schedule)
    }
}