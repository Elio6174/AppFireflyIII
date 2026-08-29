package com.example.appfireflyiii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.appfireflyiii.navigation.FireflyNavHost
import com.example.appfireflyiii.ui.components.FireflyBottomNavBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FireflyApp()
            }
        }
    }
}

@Composable
fun FireflyApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { FireflyBottomNavBar(navController) }
    ) { innerPadding ->
        FireflyNavHost(navController)
    }
}