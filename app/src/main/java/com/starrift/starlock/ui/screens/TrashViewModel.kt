package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountFieldWithAccountName
import com.starrift.starlock.data.AccountWithAppName
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TrashTab { APPS, ACCOUNTS, FIELDS }

class TrashViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TrashTab.APPS)
    val selectedTab: StateFlow<TrashTab> = _selectedTab

    fun onTabChange(tab: TrashTab) {
        _selectedTab.value = tab
        exitSelectionMode()
    }

    val deletedApps: StateFlow<List<AppItem>> =
        repository.getDeletedApps()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedAccounts: StateFlow<List<AccountWithAppName>> =
        repository.getDeletedAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedFields: StateFlow<List<AccountFieldWithAccountName>> =
        repository.getDeletedFields()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun enterSelectionMode(initialId: Long) {
        _isSelectionMode.value = true
        _selectedIds.value = setOf(initialId)
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) current - id else current + id
        if (_selectedIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun restoreSelected() {
        viewModelScope.launch {
            val ids = _selectedIds.value
            when (_selectedTab.value) {
                TrashTab.APPS -> ids.forEach { repository.restoreApp(it) }
                TrashTab.ACCOUNTS -> ids.forEach { repository.restoreAccount(it) }
                TrashTab.FIELDS -> ids.forEach { repository.restoreField(it) }
            }
            exitSelectionMode()
        }
    }

    fun permanentlyDeleteSelected() {
        viewModelScope.launch {
            val ids = _selectedIds.value
            when (_selectedTab.value) {
                TrashTab.APPS -> ids.forEach { repository.permanentlyDeleteApp(it) }
                TrashTab.ACCOUNTS -> ids.forEach { repository.permanentlyDeleteAccount(it) }
                TrashTab.FIELDS -> ids.forEach { repository.permanentlyDeleteField(it) }
            }
            exitSelectionMode()
        }
    }

    fun restoreSingle(id: Long) {
        viewModelScope.launch {
            when (_selectedTab.value) {
                TrashTab.APPS -> repository.restoreApp(id)
                TrashTab.ACCOUNTS -> repository.restoreAccount(id)
                TrashTab.FIELDS -> repository.restoreField(id)
            }
        }
    }

    fun permanentlyDeleteSingle(id: Long) {
        viewModelScope.launch {
            when (_selectedTab.value) {
                TrashTab.APPS -> repository.permanentlyDeleteApp(id)
                TrashTab.ACCOUNTS -> repository.permanentlyDeleteAccount(id)
                TrashTab.FIELDS -> repository.permanentlyDeleteField(id)
            }
        }
    }
}

class TrashViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrashViewModel::class.java)) {
            return TrashViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
