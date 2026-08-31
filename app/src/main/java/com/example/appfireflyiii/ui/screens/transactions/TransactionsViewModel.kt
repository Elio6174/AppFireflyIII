package com.example.appfireflyiii.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class TransactionsUiState {
    data object Loading : TransactionsUiState()
    data class Success(
        val transactions: List<TransactionGroup>,
        val monthLabel: String,
        val canGoForward: Boolean
    ) : TransactionsUiState()
    data class Error(val message: String) : TransactionsUiState()
}

class TransactionsViewModel(
    private val repository: TransactionRepository,
    val filterType: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsUiState>(TransactionsUiState.Loading)
    val uiState: StateFlow<TransactionsUiState> = _uiState

    private var monthOffset = 0

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = TransactionsUiState.Loading

            val (start, end, label) = monthRange(monthOffset)

            repository.getTransactionsByRange(start, end)
                .onSuccess { groups ->
                    val filtered = groups
                        .map { group ->
                            group.copy(
                                attributes = group.attributes.copy(
                                    transactions = group.attributes.transactions.filter {
                                        it.type != "opening balance" &&
                                                (filterType == null || it.type == filterType)
                                    }
                                )
                            )
                        }
                        .filter { it.attributes.transactions.isNotEmpty() }

                    val sorted = filtered.sortedByDescending { group ->
                        group.attributes.transactions.firstOrNull()?.date ?: ""
                    }
                    _uiState.value = TransactionsUiState.Success(
                        transactions = sorted,
                        monthLabel = label,
                        canGoForward = monthOffset < 0
                    )
                }
                .onFailure { error ->
                    _uiState.value = TransactionsUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun previousMonth() {
        monthOffset -= 1
        loadTransactions()
    }

    fun nextMonth() {
        if (monthOffset < 0) {
            monthOffset += 1
            loadTransactions()
        }
    }

    private fun monthRange(offset: Int): Triple<String, String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = dateFormat.format(calendar.time)
        val label = labelFormat.format(calendar.time).replaceFirstChar { it.uppercase() }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, daysInMonth)
        val end = dateFormat.format(calendar.time)

        return Triple(start, end, label)
    }
}

class TransactionsViewModelFactory(
    private val repository: TransactionRepository,
    private val filterType: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(repository, filterType) as T
    }
}