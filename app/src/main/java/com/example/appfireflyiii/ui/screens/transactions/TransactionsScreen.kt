package com.example.appfireflyiii.ui.screens.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.util.formatAmount
import com.example.appfireflyiii.util.formatShortDate
import androidx.compose.foundation.shape.RoundedCornerShape

import com.example.appfireflyiii.ui.theme.DetailScreenBg as ScreenBg
import com.example.appfireflyiii.ui.theme.DetailCardBg as CardBg
import com.example.appfireflyiii.ui.theme.DetailCardBorder as CardBorder
import com.example.appfireflyiii.ui.theme.DetailIconBadgeBg as IconBadgeBg
import com.example.appfireflyiii.ui.theme.DetailIconBadgeTint as IconBadgeTint
import com.example.appfireflyiii.ui.theme.DetailLabelGray as LabelGray
import com.example.appfireflyiii.ui.theme.DetailSubLabelGray as SubLabelGray
import com.example.appfireflyiii.ui.theme.DetailWithdrawalColor as WithdrawalColor
import com.example.appfireflyiii.ui.theme.DetailDepositColor as DepositColor
import com.example.appfireflyiii.ui.theme.DetailDividerColor as DividerColor

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                val title = when (viewModel.filterType) {
                    "withdrawal" -> "Gastos"
                    "deposit" -> "Ingresos"
                    else -> "Movimientos"
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is TransactionsUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is TransactionsUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No se pudo cargar: ${state.message}", color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadTransactions() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    is TransactionsUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                            MonthSelector(
                                monthLabel = state.monthLabel,
                                canGoForward = state.canGoForward,
                                onPrevious = { viewModel.previousMonth() },
                                onNext = { viewModel.nextMonth() }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SummaryCard(
                                totalIncome = state.totalIncome,
                                totalExpense = state.totalExpense,
                                currencySymbol = state.currencySymbol
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.transactions.isEmpty()) {
                                Text(
                                    "Sin movimientos este mes.",
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
                                    color = SubLabelGray
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(state.transactions) { group ->
                                        group.attributes.transactions.forEach { split ->
                                            TransactionCard(
                                                transaction = split,
                                                onClick = {
                                                    val journalId = split.journalId ?: return@TransactionCard
                                                    navController.navigate(
                                                        Screen.TransactionDetail.createRoute(group.id, journalId)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    monthLabel: String,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior", tint = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = SubLabelGray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = if (canGoForward) Color.White else SubLabelGray.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun SummaryCard(totalIncome: Double, totalExpense: Double, currencySymbol: String?) {
    val symbol = currencySymbol ?: "$"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 18.dp)
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "INGRESOS TOTALES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = LabelGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "+${formatAmount(totalIncome.toString(), symbol)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DepositColor
            )
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(DividerColor)
        )
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "GASTOS TOTALES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = LabelGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "-${formatAmount(totalExpense.toString(), symbol)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WithdrawalColor
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionSplit,
    onClick: () -> Unit = {}
) {
    val isExpense = transaction.type == "withdrawal"
    val amountColor = if (isExpense) WithdrawalColor else DepositColor
    val amountPrefix = if (isExpense) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${transaction.categoryName ?: "Sin categoría"} · ${formatShortDate(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = SubLabelGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            if (!transaction.tags.isNullOrEmpty()) {
                TagChips(tags = transaction.tags, modifier = Modifier.widthIn(max = 130.dp))
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                "$amountPrefix${formatAmount(transaction.amount, transaction.currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

@Composable
fun TagChips(tags: List<String>, modifier: Modifier = Modifier) {
    val visibleTag = tags.firstOrNull()
    val extraCount = tags.size - 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visibleTag != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IconBadgeBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    visibleTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = SubLabelGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (extraCount > 0) {
            Text(
                "+$extraCount",
                style = MaterialTheme.typography.labelSmall,
                color = SubLabelGray
            )
        }
    }
}