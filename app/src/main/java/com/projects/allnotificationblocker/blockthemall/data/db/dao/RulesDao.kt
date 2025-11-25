package com.projects.allnotificationblocker.blockthemall.data.db.dao

import androidx.lifecycle.*
import androidx.room.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

@Dao
interface RulesDao {
    @Insert
    suspend fun insert(record: Rule)

    @Insert
    suspend fun insertAll(records: List<Rule>)

    @Update
    suspend fun update(record: Rule)

    @Delete
    suspend fun delete(record: Rule)

    @Query("DELETE FROM rules_table")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM rules_table WHERE profileId = :profileId")
    suspend fun deleteRulesForProfile(profileId: Int)

    @Query("SELECT * FROM rules_table ORDER BY pkey DESC")
    fun getAllRecords(): LiveData<MutableList<Rule>>

    @Query("SELECT * FROM rules_table WHERE profileId IS NULL ORDER BY pkey DESC")
    suspend fun getGlobalRules(): List<Rule>

    @Query("SELECT * FROM rules_table WHERE profileId = :profileId ORDER BY pkey DESC")
    suspend fun getRulesForProfile(profileId: Int): List<Rule>
}