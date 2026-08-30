package com.example.appfireflyiii.ui.screens.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AccountDetailData(
    val account: AccountData,
    val dailyBalance: List<Float>, // balance reconstruido, un valor por día del mes
    val transactions: List<TransactionSplit>
)

sealed class AccountDetailUiState {
    data object Loading : AccountDetailUiState()
    data class Success(val data: AccountDetailData) : AccountDetailUiState()
    data class Error(val message: String) : AccountDetailUiState()
}

class AccountDetailViewModel(
    private val accountRepository: AccountRepository,
    private val accountId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountDetailUiState>(AccountDetailUiState.Loading)
    val uiState: StateFlow<AccountDetailUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AccountDetailUiState.Loading

            val accountsResult = accountRepository.getAccounts()
            if (accountsResult.isFailure) {
                _uiState.value = AccountDetailUiState.Error(
                    accountsResult.exceptionOrNull()?.message ?: "Error al cargar la cuenta"
                )
                return@launch
            }

            val account = accountsResult.getOrThrow().find { it.id == accountId }
            if (account == null) {
                _uiState.value = AccountDetailUiState.Error("Cuenta no encontrada")
                return@launch
            }

            val (start, end, daysInMonth) = currentMonthRange()

            accountRepository.getAccountTransactions(accountId, start, end)
                .onSuccess { groups ->
                    val currentBalance = account.attributes.currentBalance.toDoubleOrNull() ?: 0.0

                    // Movimiento neto por día (positivo si aumentó el saldo de esta cuenta, negativo si bajó)
                    val dailyNet = DoubleArray(daysInMonth)
                    val allSplits = mutableListOf<TransactionSplit>()

                    groups.forEach { group ->
                        group.attributes.transactions.forEach { split ->
                            allSplits.add(split)
                            val amount = split.amount.toDoubleOrNull() ?: 0.0
                            val day = split.date.take(10).takeLast(2).toIntOrNull()
                            if (day == null || day !in 1..daysInMonth) return@forEach

                            val isSource = split.sourceId == accountId
                            val isDestination = split.destinationId == accountId

                            when {
                                isSource -> dailyNet[day - 1] -= amount
                                isDestination -> dailyNet[day - 1] += amount
                            }
                        }
                    }

                    // Reconstruye el balance de cada día partiendo del balance actual hacia atrás
                    val totalNet = dailyNet.sum()
                    var runningFromEnd = totalNet
                    val dailyBalance = DoubleArray(daysInMonth)
                    for (day in daysInMonth downTo 1) {
                        dailyBalance[day - 1] = currentBalance - runningFromEnd + dailyNet[day - 1]
                        runningFromEnd -= dailyNet[day - 1]
                    }

                    _uiState.value = AccountDetailUiState.Success(
                        AccountDetailData(
                            account = account,
                            dailyBalance = dailyBalance.map { it.toFloat() },
                            transactions = allSplits.sortedByDescending { it.date }.take(15)
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = AccountDetailUiState.Error(error.message ?: "Error al cargar transacciones")
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

class AccountDetailViewModelFactory(
    private val accountRepository: AccountRepository,
    private val accountId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AccountDetailViewModel(accountRepository, accountId) as T
    }
}