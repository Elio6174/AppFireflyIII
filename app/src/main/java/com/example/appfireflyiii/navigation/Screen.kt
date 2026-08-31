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
    data object Transactions : Screen("transactions?filter={filter}&accountId={accountId}", "Transacciones") {
        fun createRoute(filter: String? = null, accountId: String? = null): String {
            val params = mutableListOf<String>()
            if (filter != null) params.add("filter=$filter")
            if (accountId != null) params.add("accountId=$accountId")
            return if (params.isEmpty()) "transactions" else "transactions?" + params.joinToString("&")
        }
    }

    data object TokenSetup : Screen("token_setup", "Configurar")
    data object Login : Screen("login", "Ingresar")

    data object TransactionDetail : Screen("transaction_detail/{groupId}/{journalId}", "Detalle") {
        fun createRoute(groupId: String, journalId: String) = "transaction_detail/$groupId/$journalId"
    }
    data object CategoryDetail : Screen("category_detail/{categoryId}", "Categoría") {
        fun createRoute(categoryId: String) = "category_detail/$categoryId"
    }
    data object AccountDetail : Screen("account_detail/{accountId}", "Detalle de cuenta") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
    data object EditAccount : Screen("edit_account/{accountId}", "Editar cuenta") {
        fun createRoute(accountId: String) = "edit_account/$accountId"
    }
    data object CreateAccount : Screen("create_account", "Nueva cuenta")
    data object MainTabs : Screen("main_tabs", "Inicio")
}
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Accounts,
    Screen.NewTransaction,
    Screen.Reports,
    Screen.More
)