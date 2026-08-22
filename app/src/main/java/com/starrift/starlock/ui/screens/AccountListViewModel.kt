package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountItem
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountListViewModel(
    private val repository: AppRepository,
    private val appId: Long
) : ViewModel() {

    val app: StateFlow<AppItem?> =
        repository.getAppById(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allAccounts: StateFlow<List<AccountItem>> =
        repository.getAccountsForApp(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountItem>> = allAccounts

    val favoriteAccounts: StateFlow<List<AccountItem>> =
        allAccounts.map { list ->
            list.filter { it.isFavorite }.sortedBy { it.name.lowercase() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nonFavoriteAccounts: StateFlow<List<AccountItem>> =
        allAccounts.map { list ->
            list.filter { !it.isFavorite }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    val searchResults: StateFlow<List<AccountItem>> =
        allAccounts.combine(_searchQuery) { list, query ->
            if (query.isBlank()) emptyList()
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun addAccount(name: String, iconPath: String?) {
        viewModelScope.launch {
            repository.addAccount(appId, name, iconPath)
        }
    }

    fun updateAccount(id: Long, name: String, iconPath: String?) {
        viewModelScope.launch {
            val existing = allAccounts.value.find { it.id == id }
            repository.updateAccount(
                AccountItem(
                    id = id,
                    appId = appId,
                    name = name,
                    iconPath = iconPath,
                    isFavorite = existing?.isFavorite ?: false
                )
            )
        }
    }

    fun archiveAccount(accountIds: Set<Long>) {
        viewModelScope.launch {
            accountIds.forEach { id -> repository.archiveAccount(id) }
        }
    }

    fun deleteAccount(account: AccountItem) {
        viewModelScope.launch {
            repository.softDeleteAccount(account.id)
        }
    }

    fun toggleFavorite(accountIds: Set<Long>, makeFavorite: Boolean) {
        viewModelScope.launch {
            accountIds.forEach { id -> repository.setAccountFavorite(id, makeFavorite) }
        }
    }
}

class AccountListViewModelFactory(
    private val repository: AppRepository,
    private val appId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountListViewModel::class.java)) {
            return AccountListViewModel(repository, appId) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
