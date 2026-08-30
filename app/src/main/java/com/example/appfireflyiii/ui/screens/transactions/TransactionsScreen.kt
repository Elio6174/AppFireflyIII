package com.example.appfireflyiii.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.RedExpense
import com.example.appfireflyiii.util.formatAmount
import com.example.appfireflyiii.util.formatRelativeDate

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado con botón de volver y título, igual al resto de las pantallas
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Movimientos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
                        Text("No se pudo cargar: ${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadTransactions() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is TransactionsUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MonthSelector(
                            monthLabel = state.monthLabel,
                            canGoForward = state.canGoForward,
                            onPrevious = { viewModel.previousMonth() },
                            onNext = { viewModel.nextMonth() }
                        )

                        if (state.transactions.isEmpty()) {
                            Text(
                                "Sin movimientos este mes.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
fun MonthSelector(
    monthLabel: String,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
        }
        Text(
            monthLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = if (canGoForward) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
    val amountColor = if (isExpense) RedExpense else AssetColor
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${transaction.categoryName ?: "Sin categoría"} · ${formatRelativeDate(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "$amountPrefix${formatAmount(transaction.amount, transaction.currencySymbol)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}