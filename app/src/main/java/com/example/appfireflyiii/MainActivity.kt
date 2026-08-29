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
            composable(Screen.Dashboard.route) { DashboardScreen(navController) }
            composable(Screen.Accounts.route) { AccountsScreen(navController) }
            composable(Screen.NewTransaction.route) { NewTransactionScreen(navController) }
            composable(Screen.Reports.route) { ReportsScreen(navController) }
            composable(Screen.More.route) { MoreScreen(navController) }
        }
    }
}