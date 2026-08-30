package com.example.appfireflyiii

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfireflyiii.auth.BiometricAuthManager
import com.example.appfireflyiii.data.local.TokenStorage
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.ui.components.FireflyBottomNavBar
import com.example.appfireflyiii.ui.screens.accounts.AccountsScreen
import com.example.appfireflyiii.ui.screens.auth.LoginScreen
import com.example.appfireflyiii.ui.screens.auth.TokenSetupScreen
import com.example.appfireflyiii.ui.screens.dashboard.DashboardScreen
import com.example.appfireflyiii.ui.screens.more.MoreScreen
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionScreen
import com.example.appfireflyiii.ui.screens.reports.ReportsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appfireflyiii.data.network.FireflyClient
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.ui.screens.accounts.AccountsViewModel
import com.example.appfireflyiii.ui.screens.accounts.AccountsViewModelFactory
import com.example.appfireflyiii.data.repository.TransactionRepository
import com.example.appfireflyiii.ui.screens.transactions.TransactionsScreen
import com.example.appfireflyiii.ui.screens.transactions.TransactionsViewModel
import com.example.appfireflyiii.ui.screens.transactions.TransactionsViewModelFactory
import com.example.appfireflyiii.ui.screens.dashboard.DashboardViewModel
import com.example.appfireflyiii.ui.screens.dashboard.DashboardViewModelFactory
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionScreen
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionViewModel
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionViewModelFactory
import com.example.appfireflyiii.ui.screens.reports.ReportsScreen
import com.example.appfireflyiii.ui.screens.reports.ReportsViewModel
import com.example.appfireflyiii.ui.screens.reports.ReportsViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FireflyApp(activity = this)
            }
        }
    }
}

@Composable
fun FireflyApp(activity: FragmentActivity) {
    val navController = rememberNavController()
    val tokenStorage = remember { TokenStorage(activity) }
    val biometricAuthManager = remember { BiometricAuthManager(activity) }
    val fireflyApi = remember { FireflyClient.create { tokenStorage.getToken() } }
    val accountRepository = remember { AccountRepository(fireflyApi) }
    val accountsViewModelFactory = remember { AccountsViewModelFactory(accountRepository) }
    val transactionRepository = remember { TransactionRepository(fireflyApi) }
    val transactionsViewModelFactory = remember { TransactionsViewModelFactory(transactionRepository) }
    val dashboardViewModelFactory = remember { DashboardViewModelFactory(accountRepository, transactionRepository) }
    val newTransactionViewModelFactory = remember { NewTransactionViewModelFactory(transactionRepository) }
    val reportsViewModelFactory = remember { ReportsViewModelFactory(transactionRepository) }

    var isAuthenticated by remember { mutableStateOf(false) }
    val hasToken = remember { tokenStorage.getToken() != null }

    val startDestination = when {
        !hasToken -> Screen.TokenSetup.route
        !isAuthenticated -> Screen.Login.route
        else -> Screen.Dashboard.route
    }

    Scaffold(
        bottomBar = {
            if (isAuthenticated) FireflyBottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Screen.TokenSetup.route) {
                TokenSetupScreen(tokenStorage) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TokenSetup.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Login.route) {
                LoginScreen(biometricAuthManager) {
                    isAuthenticated = true
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(factory = dashboardViewModelFactory)
                DashboardScreen(navController, viewModel)
            }
            composable(Screen.Accounts.route) {
                val viewModel: AccountsViewModel = viewModel(factory = accountsViewModelFactory)
                AccountsScreen(navController, viewModel)
            }
            composable(Screen.Transactions.route) {
                val viewModel: TransactionsViewModel = viewModel(factory = transactionsViewModelFactory)
                TransactionsScreen(navController, viewModel)
            }
            composable(Screen.NewTransaction.route) {
                val viewModel: NewTransactionViewModel = viewModel(factory = newTransactionViewModelFactory)
                NewTransactionScreen(navController, viewModel, accountRepository)
            }
            composable(Screen.Reports.route) {
                val viewModel: ReportsViewModel = viewModel(factory = reportsViewModelFactory)
                ReportsScreen(navController, viewModel)
            }
            composable(Screen.More.route) { MoreScreen(navController) }
        }
    }
}