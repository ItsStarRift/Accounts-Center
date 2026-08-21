package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AppCategory
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.data.AppWithAccountCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CategoryFilter { ALL, APPS, GAMES }

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val allApps: StateFlow<List<AppWithAccountCount>> =
        repository.getAllAppsWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _categoryFilter = MutableStateFlow(CategoryFilter.ALL)
    val categoryFilter: StateFlow<CategoryFilter> = _categoryFilter

    fun onCategoryFilterChange(filter: CategoryFilter) {
        _categoryFilter.value = filter
    }

    private val filteredApps: StateFlow<List<AppWithAccountCount>> =
        allApps.combine(_categoryFilter) { apps, filter ->
            when (filter) {
                CategoryFilter.ALL -> apps
                CategoryFilter.APPS -> apps.filter { it.category == AppCategory.UYGULAMA }
                CategoryFilter.GAMES -> apps.filter { it.category == AppCategory.OYUN }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alphabeticalGroups: StateFlow<List<Pair<Char, List<AppWithAccountCount>>>> =
        filteredApps.map { apps ->
            apps.filter { !it.isFavorite }
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap()
                .map { (letter, items) -> letter to items }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteApps: StateFlow<List<AppWithAccountCount>> =
        filteredApps.map { apps ->
            apps.filter { it.isFavorite }.sortedBy { it.name.lowercase() }
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
            val existing = allApps.value.find { it.id == id }
            repository.updateApp(
                AppItem(
                    id = id,
                    name = name,
                    category = category,
                    iconPath = iconPath,
                    isFavorite = existing?.isFavorite ?: false
                )
            )
        }
    }

    fun toggleFavorite(appIds: Set<Long>, makeFavorite: Boolean) {
        viewModelScope.launch {
            appIds.forEach { id -> repository.setFavorite(id, makeFavorite) }
        }
    }

    fun deleteApp(app: AppWithAccountCount) {
        viewModelScope.launch {
            repository.softDeleteApp(app.id)
        }
    }
}
