package com.example.appfireflyiii.ui.screens.createaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.AccountStoreRequest
import com.example.appfireflyiii.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CreateAccountUiState {
    data object Idle : CreateAccountUiState()
    data object Saving : CreateAccountUiState()
    data object Saved : CreateAccountUiState()
    data class Error(val message: String) : CreateAccountUiState()
}

class CreateAccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateAccountUiState>(CreateAccountUiState.Idle)
    val uiState: StateFlow<CreateAccountUiState> = _uiState

    fun save(
        name: String,
        accountRole: String,
        accountNumber: String,
        openingBalance: String,
        notes: String,
        includeNetWorth: Boolean,
        active: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = CreateAccountUiState.Saving
            val request = AccountStoreRequest(
                name = name,
                accountRole = accountRole,
                accountNumber = accountNumber.ifBlank { null },
                openingBalance = openingBalance.ifBlank { null },
                notes = notes.ifBlank { null },
                active = active,
                includeNetWorth = includeNetWorth
            )
            repository.createAccount(request)
                .onSuccess { _uiState.value = CreateAccountUiState.Saved }
                .onFailure { _uiState.value = CreateAccountUiState.Error(it.message ?: "Error al crear la cuenta") }
        }
    }
}

class CreateAccountViewModelFactory(
    private val repository: AccountRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateAccountViewModel(repository) as T
    }
}