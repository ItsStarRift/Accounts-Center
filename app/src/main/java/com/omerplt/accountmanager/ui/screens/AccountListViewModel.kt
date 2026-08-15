package com.omerplt.accountmanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omerplt.accountmanager.data.AccountItem
import com.omerplt.accountmanager.data.AppItem
import com.omerplt.accountmanager.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountListViewModel(
    private val repository: AppRepository,
    private val appId: Long
) : ViewModel() {

    val app: StateFlow<AppItem?> =
        repository.getAppById(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accounts: StateFlow<List<AccountItem>> =
        repository.getAccountsForApp(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAccount(name: String, iconPath: String?) {
        viewModelScope.launch {
            repository.addAccount(appId, name, iconPath)
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
