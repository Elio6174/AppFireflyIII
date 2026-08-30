package com.example.appfireflyiii.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
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
import com.example.appfireflyiii.data.model.AccountData
import java.util.Locale

// Colores de identidad por grupo (fuera de la función para no recrearlos en cada recomposición)
private val AssetColor = Color(0xFF10B981)      // verde esmeralda
private val LiabilityColor = Color(0xFFF59E0B)  // ámbar
private val NeutralColor = Color(0xFF9CA3AF)    // gris neutro

@Composable
fun AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AccountsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is AccountsUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se pudo cargar: ${state.message}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadAccounts() }) {
                        Text("Reintentar")
                    }
                }
            }
            is AccountsUiState.Success -> {
                val visibleAccounts = state.accounts.filterNot {
                    it.attributes.type == "cash"
                }

                val assets = visibleAccounts.filter { it.attributes.type == "asset" }
                val liabilities = visibleAccounts.filter {
                    it.attributes.type == "liabilities" || it.attributes.type == "liability"
                }
                val others = visibleAccounts.filterNot { account ->
                    account.attributes.type in listOf("asset", "liabilities", "liability")
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (assets.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Activos",
                                total = assets.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 },
                                symbol = assets.firstOrNull()?.attributes?.currencySymbol,
                                accentColor = AssetColor
                            )
                        }
                        items(assets) { account ->
                            AccountCard(account, accentColor = AssetColor)
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (liabilities.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Pasivos",
                                total = liabilities.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 },
                                symbol = liabilities.firstOrNull()?.attributes?.currencySymbol,
                                accentColor = LiabilityColor
                            )
                        }
                        items(liabilities) { account ->
                            AccountCard(account, accentColor = LiabilityColor)
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (others.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Otras cuentas",
                                total = others.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 },
                                symbol = others.firstOrNull()?.attributes?.currencySymbol,
                                accentColor = NeutralColor
                            )
                        }
                        items(others) { account ->
                            AccountCard(account, accentColor = NeutralColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    total: Double,
    symbol: String?,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = formatAmount(total, symbol),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AccountCard(account: AccountData, accentColor: Color) {
    val attrs = account.attributes
    val balance = attrs.currentBalance.toDoubleOrNull() ?: 0.0
    val icon = when (attrs.type) {
        "cash" -> Icons.Filled.Payments
        "liabilities" -> Icons.Filled.CreditCard
        else -> when (attrs.accountRole) {
            "cashWalletAsset" -> Icons.Filled.AccountBalanceWallet
            else -> Icons.Filled.AccountBalance
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = attrs.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatAmount(balance, attrs.currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (balance < 0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/** Formatea el monto con separador de miles y 2 decimales, símbolo pegado y signo claro. */
private fun formatAmount(value: Double, symbol: String?): String {
    val sign = if (value < 0) "-" else ""
    val formatted = String.format(Locale.US, "%,.2f", kotlin.math.abs(value))
    return "$sign${symbol ?: ""}$formatted"
}

private fun formatAmount(value: String, symbol: String?): String {
    return formatAmount(value.toDoubleOrNull() ?: 0.0, symbol)
}