package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.repository.AccountRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    navController: NavController,
    viewModel: NewTransactionViewModel,
    accountRepository: AccountRepository
) {
    val saveState by viewModel.saveState.collectAsState()
    val scrollState = rememberScrollState()

    var transactionType by remember { mutableStateOf("withdrawal") } // withdrawal | deposit
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var otherParty by remember { mutableStateOf("") } // a quién le pagaste / de dónde vino
    var category by remember { mutableStateOf("") }

    var assetAccounts by remember { mutableStateOf<List<AccountData>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<AccountData?>(null) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    // Carga las cuentas tipo "asset" una vez, para el selector
    LaunchedEffect(Unit) {
        accountRepository.getAccounts().onSuccess { accounts ->
            val assets = accounts.filter { it.attributes.type == "asset" }
            assetAccounts = assets
            selectedAccount = assets.firstOrNull()
        }
    }

    // Cuando se guarda con éxito, limpia el formulario
    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            amount = ""
            description = ""
            otherParty = ""
            category = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text("Nueva transacción", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        // Selector Gasto / Ingreso
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = transactionType == "withdrawal",
                onClick = { transactionType = "withdrawal" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Gasto") }
            SegmentedButton(
                selected = transactionType == "deposit",
                onClick = { transactionType = "deposit" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Ingreso") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("$") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de cuenta (dropdown simple)
        ExposedDropdownMenuBox(
            expanded = accountMenuExpanded,
            onExpandedChange = { accountMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedAccount?.attributes?.name ?: "Selecciona una cuenta",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cuenta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = accountMenuExpanded,
                onDismissRequest = { accountMenuExpanded = false }
            ) {
                assetAccounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.attributes.name) },
                        onClick = {
                            selectedAccount = account
                            accountMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = otherParty,
            onValueChange = { otherParty = it },
            label = { Text(if (transactionType == "withdrawal") "Pagado a" else "Recibido de") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = saveState) {
            is SaveState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            is SaveState.Success -> {
                Text("¡Guardado!", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            else -> {}
        }

        Button(
            onClick = {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val account = selectedAccount

                if (amount.isBlank() || description.isBlank() || account == null) return@Button

                if (transactionType == "withdrawal") {
                    viewModel.save(
                        type = "withdrawal",
                        date = today,
                        amount = amount,
                        description = description,
                        sourceId = account.id,
                        destinationName = otherParty.ifBlank { "Desconocido" },
                        sourceName = null,
                        destinationId = null,
                        categoryName = category
                    )
                } else {
                    viewModel.save(
                        type = "deposit",
                        date = today,
                        amount = amount,
                        description = description,
                        sourceId = null,
                        destinationName = null,
                        sourceName = otherParty.ifBlank { "Desconocido" },
                        destinationId = account.id,
                        categoryName = category
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = saveState != SaveState.Saving
        ) {
            Text(if (saveState == SaveState.Saving) "Guardando..." else "Guardar")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}