package com.projects.allnotificationblocker.blockthemall.data.repo

import android.app.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.*
import com.projects.allnotificationblocker.blockthemall.data.db.dao.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

class ProfilesRepository(application: Application) {
    private val dao: ProfilesDao
    private val allProfiles: LiveData<MutableList<Profile>>

    init {
        val database = AppDatabase.Companion.getInstance(application)
        dao = database.profilesDao()
        allProfiles = dao.getAllProfiles()
    }

    suspend fun insert(profile: Profile) {
        dao.insert(profile)
    }

    suspend fun update(profile: Profile) {
        dao.update(profile)
    }

    suspend fun delete(profile: Profile) {
        dao.delete(profile)
    }

    suspend fun deleteAllRecords() {
        dao.deleteAllProfiles()
    }

    fun getAllRecords(): LiveData<MutableList<Profile>> = allProfiles
}