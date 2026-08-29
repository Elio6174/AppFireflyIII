package com.example.appfireflyiii.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TransactionsUiState {
    data object Loading : TransactionsUiState()
    data class Success(val transactions: List<TransactionGroup>) : TransactionsUiState()
    data class Error(val message: String) : TransactionsUiState()
}

class TransactionsViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsUiState>(TransactionsUiState.Loading)
    val uiState: StateFlow<TransactionsUiState> = _uiState

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = TransactionsUiState.Loading
            repository.getTransactions()
                .onSuccess { transactions ->
                    _uiState.value = TransactionsUiState.Success(transactions)
                }
                .onFailure { error ->
                    _uiState.value = TransactionsUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}

class TransactionsViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(repository) as T
    }
}