package com.example.appfireflyiii.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appfireflyiii.data.local.TokenStorage

@Composable
fun TokenSetupScreen(
    tokenStorage: TokenStorage,
    onTokenSaved: () -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Configura tu acceso",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pega tu Personal Access Token de Firefly III. Solo lo vas a necesitar esta vez — después entras con tu huella.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            label = { Text("Token") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                tokenStorage.saveToken(tokenInput.trim())
                onTokenSaved()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y continuar")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}