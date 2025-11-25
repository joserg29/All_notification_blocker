package com.projects.allnotificationblocker.blockthemall.data.repo

import android.app.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.*
import com.projects.allnotificationblocker.blockthemall.data.db.dao.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

class RulesRepository(application: Application) {
    private val dao: RulesDao
    val allRecords: LiveData<MutableList<Rule>>

    init {
        val database = AppDatabase.Companion.getInstance(application)
        dao = database.rulesDao()
        allRecords = dao.getAllRecords()
    }

    suspend fun insert(record: Rule) {
        dao.insert(record)
    }

    suspend fun insertAll(records: List<Rule>) {
        dao.insertAll(records)
    }

    suspend fun update(record: Rule) {
        dao.update(record)
    }

    suspend fun delete(record: Rule) {
        dao.delete(record)
    }

    suspend fun deleteAllRules() {
        dao.deleteAllRecords()
    }

    suspend fun deleteRulesForProfile(profileId: Int) {
        dao.deleteRulesForProfile(profileId)
    }

    suspend fun getRulesForProfile(profileId: Int): List<Rule> {
        return dao.getRulesForProfile(profileId)
    }

    suspend fun getGlobalRules(): List<Rule> {
        return dao.getGlobalRules()
    }
}