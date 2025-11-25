package com.projects.allnotificationblocker.blockthemall.data.db.dao

import androidx.lifecycle.*
import androidx.room.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

@Dao
interface ProfilesDao {
    @Insert
    suspend fun insert(record: Profile): Long

    @Update
    suspend fun update(record: Profile)

    @Delete
    suspend fun delete(record: Profile)

    @Query("DELETE FROM profiles_table")
    suspend fun deleteAllProfiles()

    @Query("SELECT * FROM profiles_table ORDER BY pkey DESC")
    fun getAllProfiles(): LiveData<MutableList<Profile>>
}