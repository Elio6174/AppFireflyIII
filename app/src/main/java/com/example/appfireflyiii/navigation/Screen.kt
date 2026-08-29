package com.example.appfireflyiii.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    data object Dashboard : Screen("dashboard", "Inicio", Icons.Filled.Home)
    data object Accounts : Screen("accounts", "Cuentas", Icons.Filled.AccountBalanceWallet)
    data object NewTransaction : Screen("new_transaction", "Nueva", Icons.Filled.Add)
    data object Reports : Screen("reports", "Reportes", Icons.Filled.BarChart)
    data object More : Screen("more", "Más", Icons.Filled.Menu)

    data object TransactionDetail : Screen("transaction_detail/{id}", "Detalle") {
        fun createRoute(id: String) = "transaction_detail/$id"
    }
    data object CategoryDetail : Screen("category_detail/{categoryId}", "Categoría") {
        fun createRoute(categoryId: String) = "category_detail/$categoryId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Accounts,
    Screen.NewTransaction,
    Screen.Reports,
    Screen.More
)