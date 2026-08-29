package com.example.appfireflyiii.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appfireflyiii.ui.screens.accounts.AccountsScreen
import com.example.appfireflyiii.ui.screens.dashboard.DashboardScreen
import com.example.appfireflyiii.ui.screens.more.MoreScreen
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionScreen
import com.example.appfireflyiii.ui.screens.reports.ReportsScreen

@Composable
fun FireflyNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Accounts.route) { AccountsScreen(navController) }
        composable(Screen.NewTransaction.route) { NewTransactionScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
        composable(Screen.More.route) { MoreScreen(navController) }

        composable(Screen.TransactionDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            // TransactionDetailScreen(id) — la armamos más adelante
        }
        composable(Screen.CategoryDetail.route) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            // CategoryDetailScreen(categoryId) — la armamos más adelante
        }
    }
}