package com.example.appfireflyiii.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.appfireflyiii.navigation.Screen

@Composable
fun DashboardScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { navController.navigate(Screen.Transactions.route) }) {
            Text("Ver transacciones")
        }
    }
}