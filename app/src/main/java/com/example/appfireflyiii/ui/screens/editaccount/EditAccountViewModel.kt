package com.example.appfireflyiii.ui.screens.editaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.AccountUpdateRequest
import com.example.appfireflyiii.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EditAccountUiState {
    data object Loading : EditAccountUiState()
    data class Loaded(val account: AccountData) : EditAccountUiState()
    data object Saving : EditAccountUiState()
    data object Saved : EditAccountUiState()
    data object Deleting : EditAccountUiState()
    data object Deleted : EditAccountUiState()
    data class Error(val message: String) : EditAccountUiState()
}

class EditAccountViewModel(
    private val repository: AccountRepository,
    private val accountId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditAccountUiState>(EditAccountUiState.Loading)
    val uiState: StateFlow<EditAccountUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = EditAccountUiState.Loading
            repository.getAccounts()
                .onSuccess { accounts ->
                    val account = accounts.find { it.id == accountId }
                    if (account != null) {
                        _uiState.value = EditAccountUiState.Loaded(account)
                    } else {
                        _uiState.value = EditAccountUiState.Error("Cuenta no encontrada")
                    }
                }
                .onFailure { error ->
                    _uiState.value = EditAccountUiState.Error(error.message ?: "Error al cargar")
                }
        }
    }

    fun save(
        name: String,
        accountNumber: String,
        accountRole: String,
        notes: String,
        includeNetWorth: Boolean,
        active: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = EditAccountUiState.Saving
            val request = AccountUpdateRequest(
                name = name,
                accountNumber = accountNumber.ifBlank { null },
                accountRole = accountRole.ifBlank { null },
                notes = notes.ifBlank { null },
                active = active,
                includeNetWorth = includeNetWorth
            )
            repository.updateAccount(accountId, request)
                .onSuccess { _uiState.value = EditAccountUiState.Saved }
                .onFailure { _uiState.value = EditAccountUiState.Error(it.message ?: "Error al guardar") }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.value = EditAccountUiState.Deleting
            repository.deleteAccount(accountId)
                .onSuccess { _uiState.value = EditAccountUiState.Deleted }
                .onFailure { _uiState.value = EditAccountUiState.Error(it.message ?: "Error al eliminar") }
        }
    }
}

class EditAccountViewModelFactory(
    private val repository: AccountRepository,
    private val accountId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditAccountViewModel(repository, accountId) as T
    }
}