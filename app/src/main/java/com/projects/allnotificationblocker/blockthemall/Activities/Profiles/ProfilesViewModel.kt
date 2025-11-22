package com.projects.allnotificationblocker.blockthemall.Activities.Profiles

import android.app.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.data.repo.*
import kotlinx.coroutines.*

class ProfilesViewModel(application: Application): AndroidViewModel(application) {
    private val repository: ProfilesRepository = ProfilesRepository(application)

    val allRecords: LiveData<MutableList<Profile>> = repository.getAllRecords()

    fun insert(record: Profile) = viewModelScope.launch {
        repository.insert(record)
    }

    fun update(record: Profile) = viewModelScope.launch {
        repository.update(record)
    }

    fun delete(record: Profile) = viewModelScope.launch {
        repository.delete(record)
    }

    fun deleteAllRecords() = viewModelScope.launch {
        repository.deleteAllRecords()
    }
}