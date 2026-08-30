package com.example.appfireflyiii.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DashboardData(
    val netWorth: BigDecimal,
    val monthlyIncome: BigDecimal,
    val monthlyExpense: BigDecimal,
    val currencySymbol: String,
    val recentTransactions: List<TransactionSplit>
)

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val accountsResult = accountRepository.getAccounts()
            val (start, end) = currentMonthRange()
            val monthTransactionsResult = transactionRepository.getTransactionsByRange(start, end)
            val recentResult = transactionRepository.getTransactions(page = 1)

            if (accountsResult.isFailure) {
                _uiState.value = DashboardUiState.Error(
                    accountsResult.exceptionOrNull()?.message ?: "Error al cargar cuentas"
                )
                return@launch
            }
            if (monthTransactionsResult.isFailure) {
                _uiState.value = DashboardUiState.Error(
                    monthTransactionsResult.exceptionOrNull()?.message ?: "Error al cargar transacciones"
                )
                return@launch
            }

            val accounts = accountsResult.getOrThrow()
            val transactionGroups = monthTransactionsResult.getOrThrow()

            val netWorthAccounts = accounts.filter {
                it.attributes.type == "asset" || it.attributes.type == "liabilities" || it.attributes.type == "liability"
            }
            val netWorth = netWorthAccounts.fold(BigDecimal.ZERO) { acc, account ->
                acc + (account.attributes.currentBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            }
            val currencySymbol = netWorthAccounts.firstOrNull()?.attributes?.currencySymbol ?: "$"

            var income = BigDecimal.ZERO
            var expense = BigDecimal.ZERO
            transactionGroups.forEach { group ->
                group.attributes.transactions.forEach { split ->
                    val amount = split.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    when (split.type) {
                        "deposit" -> income += amount
                        "withdrawal" -> expense += amount
                    }
                }
            }

            val recentSplits = recentResult.getOrNull()
                ?.flatMap { it.attributes.transactions }
                ?.sortedByDescending { it.date }
                ?.take(5)
                ?: emptyList()

            _uiState.value = DashboardUiState.Success(
                DashboardData(
                    netWorth = netWorth,
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    currencySymbol = currencySymbol,
                    recentTransactions = recentSplits
                )
            )
        }
    }

    private fun currentMonthRange(): Pair<String, String> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = format.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = format.format(calendar.time)
        return start to end
    }
}

class DashboardViewModelFactory(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(accountRepository, transactionRepository) as T
    }
}