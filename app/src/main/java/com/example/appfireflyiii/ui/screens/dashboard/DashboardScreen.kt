package com.example.appfireflyiii.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.RedExpense
import com.example.appfireflyiii.util.formatAmount
import androidx.compose.ui.graphics.Color
import com.example.appfireflyiii.ui.theme.CardGradientEnd
import com.example.appfireflyiii.ui.theme.CardGradientStart
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.ArrowForward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is DashboardUiState.Loading) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadDashboard()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                if (!isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No se pudo cargar: ${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDashboard() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(state.data, navController)
            }
        }
    }
}

@Composable
fun DashboardContent(data: DashboardData, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Hola de nuevo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CardGradientStart, CardGradientEnd)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Patrimonio neto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAmount(data.netWorth.toDouble(), data.currencySymbol),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text(
                            "↑ ${formatAmount(data.monthlyIncome.toDouble(), data.currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AssetColor,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(Screen.Transactions.createRoute("deposit"))
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "↓ ${formatAmount(data.monthlyExpense.toDouble(), data.currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RedExpense,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(Screen.Transactions.createRoute("withdrawal"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAction(
                    label = "Nueva",
                    icon = Icons.Filled.Add,
                    onClick = { navController.navigate(Screen.NewTransaction.route) }
                )
                QuickAction(
                    label = "Cuentas",
                    icon = Icons.Filled.AccountBalanceWallet,
                    onClick = { navController.navigate(Screen.Accounts.route) }
                )
                QuickAction(
                    label = "Movimientos",
                    icon = Icons.Filled.Receipt,
                    onClick = { navController.navigate(Screen.Transactions.createRoute()) }
                )
                QuickAction(
                    label = "Reportes",
                    icon = Icons.Filled.BarChart,
                    onClick = { navController.navigate(Screen.Reports.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Movimientos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate(Screen.Transactions.createRoute()) }) {
                    Text("Ver todos")
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (data.recentTransactions.isEmpty()) {
            Text(
                "No hay movimientos este mes.",
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                data.recentTransactions.forEach { split ->
                    RecentTransactionRow(split) {
                        val groupId = split.groupId ?: return@RecentTransactionRow
                        val journalId = split.journalId ?: return@RecentTransactionRow
                        navController.navigate(Screen.TransactionDetail.createRoute(groupId, journalId))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .then(Modifier),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onClick) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun RecentTransactionRow(split: TransactionSplit, onClick: () -> Unit = {}) {
    val isExpense = split.type == "withdrawal"
    val amountColor = if (isExpense) RedExpense else AssetColor
    val prefix = if (isExpense) "-" else "+"
    val amountValue = split.amount.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(split.description, style = MaterialTheme.typography.bodyLarge)
                Text(
                    split.categoryName ?: "Sin categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$prefix${formatAmount(amountValue, split.currencySymbol)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}