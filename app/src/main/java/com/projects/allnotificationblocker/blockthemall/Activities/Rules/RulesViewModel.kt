package com.projects.allnotificationblocker.blockthemall.Activities.Rules

import android.app.*
import androidx.lifecycle.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.data.repo.*
import kotlinx.coroutines.*

class RulesViewModel(application: Application): AndroidViewModel(application) {
    private val repository: RulesRepository = RulesRepository(application)

    val allRecords: LiveData<MutableList<Rule>> = repository.allRecords

    fun insert(rule: Rule) = viewModelScope.launch {
        repository.insert(rule)
    }

    fun insertAll(rules: List<Rule>) = viewModelScope.launch {
        repository.insertAll(rules)
    }

    fun update(rule: Rule) = viewModelScope.launch {
        repository.update(rule)
    }

    fun delete(rule: Rule) = viewModelScope.launch {
        repository.delete(rule)
    }

    fun deleteAllRules() = viewModelScope.launch {
        repository.deleteAllRules()
    }

    fun deleteRulesForProfile(profileId: Int) = viewModelScope.launch {
        repository.deleteRulesForProfile(profileId)
    }

    suspend fun replaceRulesForProfile(profileId: Int, rules: List<Rule>) {
        repository.deleteRulesForProfile(profileId)
        if (rules.isNotEmpty()) {
            repository.insertAll(rules)
        }
    }

    suspend fun getRulesForProfile(profileId: Int): List<Rule> {
        return repository.getRulesForProfile(profileId)
    }

    suspend fun getGlobalRules(): List<Rule> {
        return repository.getGlobalRules()
    }
}