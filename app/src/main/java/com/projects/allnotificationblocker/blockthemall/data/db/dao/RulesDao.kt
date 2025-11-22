package com.projects.allnotificationblocker.blockthemall.data.db.dao

import androidx.lifecycle.*
import androidx.room.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

@Dao
interface RulesDao {
    @Insert suspend fun insert(record: Rule)

    @Update suspend fun update(record: Rule)

    @Delete suspend fun delete(record: Rule)

    @Query("DELETE FROM rules_table") suspend fun deleteAllRecords()

    @Query("SELECT * FROM rules_table ORDER BY pkey DESC")
    fun getAllRecords(): LiveData<MutableList<Rule>>
}