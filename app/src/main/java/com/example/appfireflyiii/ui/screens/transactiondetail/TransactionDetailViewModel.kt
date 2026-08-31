package com.example.appfireflyiii.ui.screens.transactiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.data.model.TransactionSplitUpdateRequest
import com.example.appfireflyiii.data.model.TransactionUpdateRequest
import com.example.appfireflyiii.data.repository.TransactionRepository
import com.example.appfireflyiii.ui.screens.newtransaction.SaveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TransactionDetailUiState {
    data object Loading : TransactionDetailUiState()
    data class Loaded(val group: TransactionGroup, val split: TransactionSplit) : TransactionDetailUiState()
    data object Deleting : TransactionDetailUiState()
    data object Deleted : TransactionDetailUiState()
    data class Error(val message: String) : TransactionDetailUiState()
}

class TransactionDetailViewModel(
    private val repository: TransactionRepository,
    private val groupId: String,
    private val journalId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState

    // Estado dedicado solo a la acción de guardar (edición), separado del estado de carga/borrado,
    // así el formulario reciclado de Nueva Transacción puede usarlo tal cual espera SaveState.
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TransactionDetailUiState.Loading
            repository.getTransaction(groupId)
                .onSuccess { group ->
                    val split = group.attributes.transactions.find { it.journalId == journalId }
                        ?: group.attributes.transactions.firstOrNull()
                    if (split != null) {
                        _uiState.value = TransactionDetailUiState.Loaded(group, split)
                    } else {
                        _uiState.value = TransactionDetailUiState.Error("Movimiento no encontrado")
                    }
                }
                .onFailure { error ->
                    _uiState.value = TransactionDetailUiState.Error(error.message ?: "Error al cargar")
                }
        }
    }

    fun save(
        type: String,
        date: String,
        amount: String,
        description: String,
        sourceId: String?,
        destinationName: String?,
        sourceName: String?,
        destinationId: String?,
        categoryName: String?,
        budgetName: String?,
        notes: String?,
        tags: List<String>?,
        foreignAmount: String?,
        foreignCurrencyCode: String?,
        applyRules: Boolean,
        fireWebhooks: Boolean
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val request = TransactionUpdateRequest(
                applyRules = applyRules,
                fireWebhooks = fireWebhooks,
                transactions = listOf(
                    TransactionSplitUpdateRequest(
                        journalId = journalId,
                        type = type,
                        date = date,
                        amount = amount,
                        description = description,
                        sourceId = sourceId,
                        sourceName = sourceName,
                        destinationId = destinationId,
                        destinationName = destinationName?.ifBlank { null },
                        categoryName = categoryName?.ifBlank { null },
                        budgetName = budgetName?.ifBlank { null },
                        notes = notes?.ifBlank { null },
                        tags = tags?.takeIf { it.isNotEmpty() },
                        foreignAmount = foreignAmount?.ifBlank { null },
                        foreignCurrencyCode = foreignCurrencyCode?.ifBlank { null }
                    )
                )
            )
            repository.updateTransaction(groupId, request)
                .onSuccess {
                    _saveState.value = SaveState.Success
                    load() // refresca los datos mostrados con lo recién guardado
                }
                .onFailure { _saveState.value = SaveState.Error(it.message ?: "Error al guardar") }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.value = TransactionDetailUiState.Deleting
            repository.deleteTransaction(groupId)
                .onSuccess { _uiState.value = TransactionDetailUiState.Deleted }
                .onFailure { _uiState.value = TransactionDetailUiState.Error(it.message ?: "Error al eliminar") }
        }
    }
}

class TransactionDetailViewModelFactory(
    private val repository: TransactionRepository,
    private val groupId: String,
    private val journalId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionDetailViewModel(repository, groupId, journalId) as T
    }
}