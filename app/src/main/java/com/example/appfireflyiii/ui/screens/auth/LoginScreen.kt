package com.example.appfireflyiii.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appfireflyiii.auth.BiometricAuthManager

@Composable
fun LoginScreen(
    biometricAuthManager: BiometricAuthManager,
    onAuthSuccess: () -> Unit
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = "Huella",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Toca para entrar", style = MaterialTheme.typography.titleMedium)

        errorMsg?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            biometricAuthManager.authenticate(
                onSuccess = { onAuthSuccess() },
                onError = { msg -> errorMsg = msg }
            )
        }) {
            Text("Usar huella")
        }
    }

    LaunchedEffect(Unit) {
        biometricAuthManager.authenticate(
            onSuccess = { onAuthSuccess() },
            onError = { msg -> errorMsg = msg }
        )
    }
}