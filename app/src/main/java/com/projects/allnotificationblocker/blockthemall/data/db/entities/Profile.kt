package com.projects.allnotificationblocker.blockthemall.data.db.entities

import androidx.room.*
import com.google.gson.*
import timber.log.*

@Entity(tableName = "profiles_table")
data class Profile(
    var name: String,
    var description: String,
    var rules: String,
    @PrimaryKey(autoGenerate = true) var pkey: Int = 0,
) {


    fun toJson(): String? {
        val gson = Gson()
        return gson.toJson(this)
    }

    fun logProfile() {
        Timber.tag("AppInfo")
            .d("Profile Name: %s, Description: %s, Rules: %s", name, description, rules)
    }

    companion object {
        fun fromJson(json: String?): Profile {
            val gson = Gson()
            return gson.fromJson<Profile>(json, Profile::class.java)
        }
    }
}