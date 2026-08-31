package com.example.appfireflyiii.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.CardGradientEnd
import com.example.appfireflyiii.ui.theme.CardGradientStart
import com.example.appfireflyiii.ui.theme.LiabilityColor
import com.example.appfireflyiii.ui.theme.NeutralAccountColor
import com.example.appfireflyiii.util.formatAmount
import com.example.appfireflyiii.util.formatAccountNumber
import androidx.compose.foundation.clickable
import com.example.appfireflyiii.navigation.Screen
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private data class AccountsComputed(
    val assets: List<AccountData>,
    val liabilities: List<AccountData>,
    val others: List<AccountData>,
    val assetsTotal: Double,
    val liabilitiesTotal: Double
)

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
                val computed = remember(state.accounts) {
                    val visibleAccounts = state.accounts.filterNot { it.attributes.type == "cash" }
                    val assets = visibleAccounts.filter { it.attributes.type == "asset" }
                    val liabilities = visibleAccounts.filter {
                        it.attributes.type == "liabilities" || it.attributes.type == "liability"
                    }
                    val others = visibleAccounts.filterNot { account ->
                        account.attributes.type in listOf("asset", "liabilities", "liability")
                    }
                    val assetsTotal = assets.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 }
                    val liabilitiesTotal = liabilities.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 }

                    AccountsComputed(assets, liabilities, others, assetsTotal, liabilitiesTotal)
                }
                val (assets, liabilities, others, assetsTotal, liabilitiesTotal) = computed

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tus cuentas",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { navController.navigate(Screen.CreateAccount.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Nueva cuenta",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        AccountCardCarousel(assets = assets, navController = navController, onAccountClick = { accountId ->
                            navController.navigate(Screen.AccountDetail.createRoute(accountId))
                        })
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    if (liabilities.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Pasivos",
                                total = liabilitiesTotal,
                                symbol = liabilities.firstOrNull()?.attributes?.currencySymbol,
                                accentColor = LiabilityColor
                            )
                        }
                        item {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                LiabilitiesGroup(liabilities)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (others.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Otras cuentas",
                                total = others.sumOf { it.attributes.currentBalance.toDoubleOrNull() ?: 0.0 },
                                symbol = others.firstOrNull()?.attributes?.currencySymbol,
                                accentColor = NeutralAccountColor
                            )
                        }
                        items(others) { account ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                AccountListItem(account, accentColor = NeutralAccountColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AccountCardCarousel(assets: List<AccountData>, navController: NavController, onAccountClick: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { assets.size })

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val account = assets[page]
            val icon = when (account.attributes.accountRole) {
                "cashWalletAsset" -> Icons.Filled.AccountBalanceWallet
                else -> Icons.Filled.CreditCard
            }
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onAccountClick(account.id) }
            ) {
                BankCard(
                    label = account.attributes.name.uppercase(),
                    subtitle = "Cuenta activa",
                    amount = formatAmount(account.attributes.currentBalance, account.attributes.currencySymbol),
                    icon = icon,
                    accountNumber = account.attributes.accountNumber,
                    onEditClick = {
                        navController.navigate(Screen.EditAccount.createRoute(account.id))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(assets.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
fun BankCard(
    label: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    accountNumber: String? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CardGradientStart, CardGradientEnd)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(modifier = Modifier.fillMaxSize().padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row {
                    if (onDeleteClick != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .clickable { onDeleteClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar cuenta",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (onEditClick != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                                .clickable { onEditClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar cuenta",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (!accountNumber.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    formatAccountNumber(accountNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                amount,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, total: Double, symbol: String?, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            formatAmount(total, symbol),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AccountListItem(account: AccountData, accentColor: Color) {
    val attrs = account.attributes
    val balance = attrs.currentBalance.toDoubleOrNull() ?: 0.0
    val icon = when (attrs.type) {
        "cash" -> Icons.Filled.Payments
        "liabilities" -> Icons.Filled.CreditCard
        else -> Icons.Filled.AccountBalance
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                attrs.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatAmount(balance, attrs.currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LiabilitiesGroup(liabilities: List<AccountData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            liabilities.forEachIndexed { index, account ->
                val attrs = account.attributes
                val balance = attrs.currentBalance.toDoubleOrNull() ?: 0.0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        attrs.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        formatAmount(balance, attrs.currencySymbol),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (index < liabilities.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}