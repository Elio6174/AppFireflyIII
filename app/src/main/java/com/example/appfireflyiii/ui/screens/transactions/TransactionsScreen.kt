package com.example.appfireflyiii.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.model.TransactionSplit
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.draw.clip

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.transactions) { group ->
                        group.attributes.transactions.forEach { split ->
                            TransactionCard(split)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionSplit) {
    val isExpense = transaction.type == "withdrawal"
    val amountColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
    val amountPrefix = if (isExpense) "-" else "+"
    val avatarColor = if (isExpense)
        MaterialTheme.colorScheme.errorContainer
    else
        Color(0xFFD5EFD8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpense) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${transaction.categoryName ?: "Sin categoría"} · ${transaction.date.take(10)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$amountPrefix${transaction.currencySymbol ?: ""}${transaction.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor
            )
        }
    }
}