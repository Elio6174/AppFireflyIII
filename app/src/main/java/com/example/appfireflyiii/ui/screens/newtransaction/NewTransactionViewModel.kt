package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.TransactionSplitRequest
import com.example.appfireflyiii.data.model.TransactionStoreRequest
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class NewTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun save(
        type: String,
        date: String,
        amount: String,
        description: String,
        sourceId: String?,
        destinationName: String?,
        sourceName: String?,
        destinationId: String?,
        categoryName: String?
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            val split = TransactionSplitRequest(
                type = type,
                date = date,
                amount = amount,
                description = description,
                sourceId = sourceId,
                sourceName = sourceName,
                destinationId = destinationId,
                destinationName = destinationName,
                categoryName = categoryName?.ifBlank { null }
            )

            repository.createTransaction(TransactionStoreRequest(transactions = listOf(split)))
                .onSuccess { _saveState.value = SaveState.Success }
                .onFailure { _saveState.value = SaveState.Error(it.message ?: "Error al guardar") }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }
}

class NewTransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NewTransactionViewModel(repository) as T
    }
}