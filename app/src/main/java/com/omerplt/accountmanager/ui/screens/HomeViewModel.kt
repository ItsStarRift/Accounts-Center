package com.omerplt.accountmanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerplt.accountmanager.data.AppCategory
import com.omerplt.accountmanager.data.AppRepository
import com.omerplt.accountmanager.data.AppWithAccountCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val allApps: StateFlow<List<AppWithAccountCount>> =
        repository.getAllAppsWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    val alphabeticalGroups: StateFlow<List<Pair<Char, List<AppWithAccountCount>>>> =
        allApps.map { apps ->
            apps.groupBy { it.name.first().uppercaseChar() }
                .toSortedMap()
                .map { (letter, items) -> letter to items }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<AppWithAccountCount>> =
        allApps.combine(_searchQuery) { apps, query ->
            if (query.isBlank()) emptyList()
            else apps.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun addApp(name: String, category: AppCategory, iconPath: String?) {
        viewModelScope.launch {
            repository.addApp(name, category, iconPath)
        }
    }

    fun updateApp(id: Long, name: String, category: AppCategory, iconPath: String?) {
        viewModelScope.launch {
            repository.updateApp(AppItem(id = id, name = name, category = category, iconPath = iconPath))
        }
    }

    fun deleteApp(app: AppWithAccountCount) {
        viewModelScope.launch {
            repository.deleteApp(
                com.omerplt.accountmanager.data.AppItem(
                    id = app.id,
                    name = app.name,
                    category = app.category,
                    iconPath = app.iconPath
                )
            )
        }
    }
}
