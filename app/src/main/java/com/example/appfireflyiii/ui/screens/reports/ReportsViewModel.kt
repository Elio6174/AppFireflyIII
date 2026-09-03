package com.example.appfireflyiii.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.TransactionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.appfireflyiii.data.repository.TagRepository

enum class ReportPeriod { MONTH, YEAR }

data class TagSpend(
    val name: String,
    val amount: BigDecimal,
    val fraction: Float
)

data class CategorySpend(
    val name: String,
    val amount: BigDecimal,
    val fraction: Float
)

data class AccountBalanceSeries(
    val accountId: String,
    val accountName: String,
    val values: List<Float>
)

data class ReportsData(
    val accountBalances: List<AccountBalanceSeries>,
    val categories: List<CategorySpend>,
    val tagSpends: List<TagSpend>,
    val tagsWithoutSpend: List<String>,
    val spendSeries: List<Float>,
    val totalExpense: BigDecimal,
    val currencySymbol: String
)

sealed class ReportsUiState {
    data object Loading : ReportsUiState()
    data class Success(
        val data: ReportsData,
        val periodLabel: String,
        val periodType: ReportPeriod,
        val canGoForward: Boolean
    ) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}

class ReportsViewModel(
    private val repository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState

    private var periodType = ReportPeriod.MONTH
    private var monthOffset = 0
    private var yearOffset = 0

    init {
        loadReports()
    }

    fun setPeriodType(type: ReportPeriod) {
        if (periodType == type) return
        periodType = type
        monthOffset = 0
        yearOffset = 0
        loadReports()
    }

    fun previousPeriod() {
        if (periodType == ReportPeriod.MONTH) monthOffset-- else yearOffset--
        loadReports()
    }

    fun nextPeriod() {
        val canGoForward = if (periodType == ReportPeriod.MONTH) monthOffset < 0 else yearOffset < 0
        if (!canGoForward) return
        if (periodType == ReportPeriod.MONTH) monthOffset++ else yearOffset++
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading

            val range = if (periodType == ReportPeriod.MONTH) monthRange() else yearRange()

            val transactionsResult = repository.getTransactionsByRange(range.start, range.end)
            if (transactionsResult.isFailure) {
                _uiState.value = ReportsUiState.Error(
                    transactionsResult.exceptionOrNull()?.message ?: "Error desconocido"
                )
                return@launch
            }

            val groups = transactionsResult.getOrThrow()

            var currencySymbol = "$"
            val categoryTotals = mutableMapOf<String, BigDecimal>()
            val bucketTotals = MutableList(range.bucketCount) { BigDecimal.ZERO }
            val tagTotals = mutableMapOf<String, BigDecimal>()

            groups.forEach { group ->
                group.attributes.transactions.forEach { split ->
                    if (split.type != "withdrawal") return@forEach

                    val amount = split.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    split.currencySymbol?.let { currencySymbol = it }

                    val categoryName = split.categoryName ?: "Sin categoría"
                    categoryTotals[categoryName] =
                        (categoryTotals[categoryName] ?: BigDecimal.ZERO) + amount

                    val bucketIndex = bucketIndexFor(split.date)
                    if (bucketIndex != null && bucketIndex in 0 until range.bucketCount) {
                        bucketTotals[bucketIndex] = bucketTotals[bucketIndex] + amount
                    }

                    split.tags?.forEach { tag ->
                        tagTotals[tag] = (tagTotals[tag] ?: BigDecimal.ZERO) + amount
                    }
                }
            }

            val sortedTags = tagTotals.entries.sortedByDescending { it.value }
            val maxTagAmount = sortedTags.maxOfOrNull { it.value } ?: BigDecimal.ONE
            val tagSpends = sortedTags.map { (name, amount) ->
                TagSpend(
                    name = name,
                    amount = amount,
                    fraction = if (maxTagAmount > BigDecimal.ZERO) (amount.toFloat() / maxTagAmount.toFloat()) else 0f
                )
            }

            val allTagNames = tagRepository.getTags().getOrNull()?.map { it.attributes.tag } ?: emptyList()
            val tagsWithoutSpend = allTagNames.filter { it !in tagTotals.keys }.sorted()

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

            val accountBalances = loadAccountBalances(range)

            val canGoForward = if (periodType == ReportPeriod.MONTH) monthOffset < 0 else yearOffset < 0

            _uiState.value = ReportsUiState.Success(
                data = ReportsData(
                    accountBalances = accountBalances,
                    categories = categorySpends,
                    tagSpends = tagSpends,
                    tagsWithoutSpend = tagsWithoutSpend,
                    spendSeries = bucketTotals.map { it.toFloat() },
                    totalExpense = totalExpense,
                    currencySymbol = currencySymbol
                ),
                periodLabel = range.label,
                periodType = periodType,
                canGoForward = canGoForward
            )
        }
    }

    private suspend fun loadAccountBalances(range: RangeInfo): List<AccountBalanceSeries> = coroutineScope {
        val accountsResult = accountRepository.getAccounts()
        val relevantAccounts = accountsResult.getOrNull()?.filter {
            it.attributes.type == "asset" || it.attributes.type == "liabilities" || it.attributes.type == "liability"
        } ?: return@coroutineScope emptyList()

        val deferredSeries = relevantAccounts.map { account ->
            async {
                val currentBalance = account.attributes.currentBalance.toDoubleOrNull() ?: 0.0

                accountRepository.getAccountTransactions(account.id, range.start, range.end)
                    .getOrNull()
                    ?.let { groups ->
                        val dailyNet = DoubleArray(range.bucketCount)

                        groups.forEach { group ->
                            group.attributes.transactions.forEach { split ->
                                val amount = split.amount.toDoubleOrNull() ?: 0.0
                                val bucketIndex = bucketIndexFor(split.date) ?: return@forEach
                                if (bucketIndex !in 0 until range.bucketCount) return@forEach

                                val isSource = split.sourceId == account.id
                                val isDestination = split.destinationId == account.id

                                when {
                                    isSource -> dailyNet[bucketIndex] -= amount
                                    isDestination -> dailyNet[bucketIndex] += amount
                                }
                            }
                        }

                        val balance = DoubleArray(range.bucketCount)
                        var suffixSum = 0.0
                        for (i in range.bucketCount - 1 downTo 0) {
                            balance[i] = currentBalance - suffixSum
                            suffixSum += dailyNet[i]
                        }

                        AccountBalanceSeries(
                            accountId = account.id,
                            accountName = account.attributes.name,
                            values = balance.map { it.toFloat() }
                        )
                    }
            }
        }

        deferredSeries.awaitAll().filterNotNull()
    }

    private fun bucketIndexFor(dateString: String): Int? {
        return if (periodType == ReportPeriod.MONTH) {
            dateString.take(10).takeLast(2).toIntOrNull()?.minus(1)
        } else {
            dateString.take(10).substring(5, 7).toIntOrNull()?.minus(1)
        }
    }

    private data class RangeInfo(val start: String, val end: String, val bucketCount: Int, val label: String)

    private fun monthRange(): RangeInfo {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = format.format(calendar.time)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, daysInMonth)
        val end = format.format(calendar.time)

        val labelFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val label = labelFormat.format(calendar.time).replaceFirstChar { it.uppercase() }

        return RangeInfo(start, end, daysInMonth, label)
    }

    private fun yearRange(): RangeInfo {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, yearOffset)

        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = format.format(calendar.time)

        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        val end = format.format(calendar.time)

        val year = calendar.get(Calendar.YEAR)
        return RangeInfo(start, end, 12, year.toString())
    }
}

class ReportsViewModelFactory(
    private val repository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val tagRepository: TagRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReportsViewModel(repository, accountRepository, tagRepository) as T
    }
}