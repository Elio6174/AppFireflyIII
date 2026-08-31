package com.example.appfireflyiii.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.navigation.bottomNavItems

@Composable
fun FireflyBottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEachIndexed { index, screen ->
            val isCentralButton = screen is Screen.NewTransaction

            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = screen.icon!!,
                        contentDescription = screen.label,
                        modifier = if (isCentralButton) Modifier.size(32.dp) else Modifier
                    )
                },
                label = { Text(screen.label) }
            )
        }
    }
}