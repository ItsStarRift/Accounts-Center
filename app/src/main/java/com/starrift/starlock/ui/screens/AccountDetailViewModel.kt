package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountField
import com.starrift.starlock.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    private val repository: AppRepository,
    val accountId: Long
) : ViewModel() {

    val fields: StateFlow<List<AccountField>> = repository.getFieldsForAccount(accountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addField(label: String, value: String, isCustomLabel: Boolean) {
        viewModelScope.launch {
            val currentSize = fields.value.size
            repository.addField(
                accountId = accountId,
                label = label,
                value = value,
                isCustomLabel = isCustomLabel,
                orderIndex = currentSize
            )
        }
    }

    fun deleteField(field: AccountField) {
        viewModelScope.launch {
            repository.deleteField(field)
        }
    }
}
