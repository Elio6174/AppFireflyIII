package com.example.appfireflyiii.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CategorySpend(
    val name: String,
    val amount: BigDecimal,
    val fraction: Float // 0f a 1f, relativo a la categoría más grande
)

data class ReportsData(
    val categories: List<CategorySpend>,
    val dailySpend: List<Float>, // un valor por día del mes
    val totalExpense: BigDecimal,
    val currencySymbol: String
)

sealed class ReportsUiState {
    data object Loading : ReportsUiState()
    data class Success(val data: ReportsData) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}

class ReportsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading

            val (start, end, daysInMonth) = currentMonthRange()

            repository.getTransactionsByRange(start, end)
                .onSuccess { groups ->
                    var currencySymbol = "$"
                    val categoryTotals = mutableMapOf<String, BigDecimal>()
                    val dailyTotals = MutableList(daysInMonth) { BigDecimal.ZERO }

                    groups.forEach { group ->
                        group.attributes.transactions.forEach { split ->
                            if (split.type != "withdrawal") return@forEach

                            val amount = split.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            split.currencySymbol?.let { currencySymbol = it }

                            val categoryName = split.categoryName ?: "Sin categoría"
                            categoryTotals[categoryName] =
                                (categoryTotals[categoryName] ?: BigDecimal.ZERO) + amount

                            val day = split.date.take(10).takeLast(2).toIntOrNull()
                            if (day != null && day in 1..daysInMonth) {
                                dailyTotals[day - 1] = dailyTotals[day - 1] + amount
                            }
                        }
                    }

                    val sorted = categoryTotals.entries.sortedByDescending { it.value }
                    val top = sorted.take(5)
                    val restSum = sorted.drop(5).fold(BigDecimal.ZERO) { acc, e -> acc + e.value }

                    val finalCategories = top.map { it.key to it.value }.toMutableList()
                    if (restSum > BigDecimal.ZERO) finalCategories.add("Otros" to restSum)

                    val maxAmount = finalCategories.maxOfOrNull { it.second } ?: BigDecimal.ONE
                    val categorySpends = finalCategories.map { (name, amount) ->
                        CategorySpend(
                            name = name,
                            amount = amount,
                            fraction = if (maxAmount > BigDecimal.ZERO)
                                (amount.toFloat() / maxAmount.toFloat()) else 0f
                        )
                    }

                    val totalExpense = categoryTotals.values.fold(BigDecimal.ZERO) { a, b -> a + b }

                    _uiState.value = ReportsUiState.Success(
                        ReportsData(
                            categories = categorySpends,
                            dailySpend = dailyTotals.map { it.toFloat() },
                            totalExpense = totalExpense,
                            currencySymbol = currencySymbol
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = ReportsUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    private fun currentMonthRange(): Triple<String, String, Int> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = format.format(calendar.time)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, daysInMonth)
        val end = format.format(calendar.time)

        return Triple(start, end, daysInMonth)
    }
}

class ReportsViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReportsViewModel(repository) as T
    }
}