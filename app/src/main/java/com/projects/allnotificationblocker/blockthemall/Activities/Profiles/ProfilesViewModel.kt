package com.projects.allnotificationblocker.blockthemall.Activities.Profiles

import android.app.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.data.repo.*
import kotlinx.coroutines.*

class ProfilesViewModel(application: Application): AndroidViewModel(application) {
    private val repository: ProfilesRepository = ProfilesRepository(application)

    val allRecords: LiveData<MutableList<Profile>> = repository.getAllRecords()

    suspend fun insert(record: Profile): Long {
        return repository.insert(record)
    }

    suspend fun update(record: Profile) {
        repository.update(record)
    }

    suspend fun delete(record: Profile) {
        repository.delete(record)
    }

    suspend fun deleteAllRecords() {
        repository.deleteAllRecords()
    }
}