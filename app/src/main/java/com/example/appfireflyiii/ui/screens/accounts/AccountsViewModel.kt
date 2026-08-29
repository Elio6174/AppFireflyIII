package com.example.appfireflyiii.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AccountsUiState {
    data object Loading : AccountsUiState()
    data class Success(val accounts: List<AccountData>) : AccountsUiState()
    data class Error(val message: String) : AccountsUiState()
}

class AccountsViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState: StateFlow<AccountsUiState> = _uiState

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = AccountsUiState.Loading
            repository.getAccounts()
                .onSuccess { accounts ->
                    _uiState.value = AccountsUiState.Success(accounts)
                }
                .onFailure { error ->
                    _uiState.value = AccountsUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}