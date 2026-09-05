package com.example.appfireflyiii

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfireflyiii.auth.BiometricAuthManager
import com.example.appfireflyiii.data.local.TokenStorage
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.ui.screens.auth.LoginScreen
import com.example.appfireflyiii.ui.screens.auth.TokenSetupScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appfireflyiii.data.network.FireflyClient
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.ui.screens.accounts.AccountsViewModelFactory
import com.example.appfireflyiii.data.repository.TransactionRepository
import com.example.appfireflyiii.ui.screens.transactions.TransactionsScreen
import com.example.appfireflyiii.ui.screens.transactions.TransactionsViewModel
import com.example.appfireflyiii.ui.screens.transactions.TransactionsViewModelFactory
import com.example.appfireflyiii.ui.screens.dashboard.DashboardViewModelFactory
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionViewModelFactory
import com.example.appfireflyiii.ui.screens.reports.ReportsViewModelFactory
import com.example.appfireflyiii.ui.theme.AppFireflyTheme
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appfireflyiii.ui.screens.accountdetail.AccountDetailScreen
import com.example.appfireflyiii.ui.screens.editaccount.EditAccountScreen
import com.example.appfireflyiii.ui.screens.editaccount.EditAccountViewModel
import com.example.appfireflyiii.ui.screens.editaccount.EditAccountViewModelFactory
import com.example.appfireflyiii.ui.screens.createaccount.CreateAccountScreen
import com.example.appfireflyiii.ui.screens.createaccount.CreateAccountViewModel
import com.example.appfireflyiii.ui.screens.createaccount.CreateAccountViewModelFactory
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.data.repository.CategoryRepository
import com.example.appfireflyiii.ui.screens.transactiondetail.TransactionDetailScreen
import com.example.appfireflyiii.ui.screens.transactiondetail.TransactionDetailViewModel
import com.example.appfireflyiii.ui.screens.transactiondetail.TransactionDetailViewModelFactory
import androidx.compose.ui.Modifier
import com.example.appfireflyiii.ui.components.MainTabsScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.appfireflyiii.data.repository.TagRepository
import com.example.appfireflyiii.navigation.bottomNavItems


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.parseColor("#232328")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            AppFireflyTheme {
                FireflyApp(activity = this)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    val tagRepository = remember { TagRepository(fireflyApi) }
    val reportsViewModelFactory = remember { ReportsViewModelFactory(transactionRepository, accountRepository, tagRepository) }
    val budgetRepository = remember { BudgetRepository(fireflyApi) }
    val categoryRepository = remember { CategoryRepository(fireflyApi) }

    var isAuthenticated by remember { mutableStateOf(false) }
    val hasToken = remember { tokenStorage.getToken() != null }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        pageCount = { bottomNavItems.size }
    )
    val scope = rememberCoroutineScope()

    val startDestination = when {
        !hasToken -> Screen.TokenSetup.route
        !isAuthenticated -> Screen.Login.route
        else -> Screen.MainTabs.route
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            composable(Screen.MainTabs.route) {
                MainTabsScreen(
                    navController = navController,
                    pagerState = pagerState,
                    dashboardViewModelFactory = dashboardViewModelFactory,
                    accountsViewModelFactory = accountsViewModelFactory,
                    newTransactionViewModelFactory = newTransactionViewModelFactory,
                    reportsViewModelFactory = reportsViewModelFactory,
                    accountRepository = accountRepository,
                    budgetRepository = budgetRepository,
                    categoryRepository = categoryRepository
                )
            }

            listOf(Screen.Dashboard, Screen.Accounts, Screen.NewTransaction, Screen.Reports, Screen.More)
                .forEach { tabScreen ->
                    composable(tabScreen.route) {
                        LaunchedEffect(Unit) {
                            val index = bottomNavItems.indexOf(tabScreen)
                            if (index >= 0) pagerState.animateScrollToPage(index)
                            navController.popBackStack()
                        }
                    }
                }

            composable(
                route = Screen.Transactions.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("accountId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter")
                val accountId = backStackEntry.arguments?.getString("accountId")
                val factory = remember(filter, accountId) {
                    TransactionsViewModelFactory(transactionRepository, filter, accountRepository, accountId)
                }
                val viewModel: TransactionsViewModel = viewModel(
                    key = "${filter ?: "all"}_${accountId ?: "all"}",
                    factory = factory
                )
                TransactionsScreen(navController, viewModel)
            }

            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(navArgument("accountId") { type = NavType.StringType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                AccountDetailScreen(navController, accountId, accountRepository)
            }
            composable(
                route = Screen.EditAccount.route,
                arguments = listOf(navArgument("accountId") { type = NavType.StringType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                val factory = remember(accountId) { EditAccountViewModelFactory(accountRepository, accountId) }
                val viewModel: EditAccountViewModel = viewModel(key = accountId, factory = factory)
                EditAccountScreen(navController, viewModel)
            }
            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("journalId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                val journalId = backStackEntry.arguments?.getString("journalId") ?: ""
                val factory = remember(groupId, journalId) {
                    TransactionDetailViewModelFactory(transactionRepository, groupId, journalId)
                }
                val viewModel: TransactionDetailViewModel = viewModel(key = "$groupId/$journalId", factory = factory)
                TransactionDetailScreen(navController, viewModel, accountRepository, budgetRepository, categoryRepository)
            }
            composable(Screen.CreateAccount.route) {
                val factory = remember { CreateAccountViewModelFactory(accountRepository) }
                val viewModel: CreateAccountViewModel = viewModel(factory = factory)
                CreateAccountScreen(navController, viewModel)
            }
        }
    }
}