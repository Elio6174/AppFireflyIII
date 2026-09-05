package com.example.appfireflyiii.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.data.repository.CategoryRepository
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.navigation.bottomNavItems
import com.example.appfireflyiii.ui.screens.accounts.AccountsScreen
import com.example.appfireflyiii.ui.screens.accounts.AccountsViewModel
import com.example.appfireflyiii.ui.screens.accounts.AccountsViewModelFactory
import com.example.appfireflyiii.ui.screens.dashboard.DashboardScreen
import com.example.appfireflyiii.ui.screens.dashboard.DashboardViewModel
import com.example.appfireflyiii.ui.screens.dashboard.DashboardViewModelFactory
import com.example.appfireflyiii.ui.screens.more.MoreScreen
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionScreen
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionViewModel
import com.example.appfireflyiii.ui.screens.newtransaction.NewTransactionViewModelFactory
import com.example.appfireflyiii.ui.screens.reports.ReportsScreen
import com.example.appfireflyiii.ui.screens.reports.ReportsViewModel
import com.example.appfireflyiii.ui.screens.reports.ReportsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(
    navController: NavController,
    pagerState: androidx.compose.foundation.pager.PagerState,
    dashboardViewModelFactory: DashboardViewModelFactory,
    accountsViewModelFactory: AccountsViewModelFactory,
    newTransactionViewModelFactory: NewTransactionViewModelFactory,
    reportsViewModelFactory: ReportsViewModelFactory,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            FireflyBottomNavBar(
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { page ->
            when (bottomNavItems[page]) {
                Screen.Dashboard -> {
                    val vm: DashboardViewModel = viewModel(factory = dashboardViewModelFactory)
                    DashboardScreen(navController, vm)
                }
                Screen.Accounts -> {
                    val vm: AccountsViewModel = viewModel(factory = accountsViewModelFactory)
                    AccountsScreen(navController, vm)
                }
                Screen.NewTransaction -> {
                    val vm: NewTransactionViewModel = viewModel(factory = newTransactionViewModelFactory)
                    NewTransactionScreen(navController, vm, accountRepository, budgetRepository, categoryRepository)
                }
                Screen.Reports -> {
                    val vm: ReportsViewModel = viewModel(factory = reportsViewModelFactory)
                    ReportsScreen(navController, vm)
                }
                Screen.More -> MoreScreen(navController)
                else -> {}
            }
        }
    }
}