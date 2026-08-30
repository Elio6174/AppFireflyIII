package com.example.appfireflyiii.ui.screens.createaccount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.ui.screens.accounts.BankCard
import com.example.appfireflyiii.ui.screens.editaccount.FormSection
import com.example.appfireflyiii.ui.screens.editaccount.SleekTextField
import com.example.appfireflyiii.ui.screens.editaccount.SwitchRow
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

private val accountRoleOptions = listOf(
    "defaultAsset" to "Cuenta por defecto",
    "sharedAsset" to "Cuenta compartida",
    "savingAsset" to "Cuenta de ahorros",
    "ccAsset" to "Tarjeta de crédito",
    "cashWalletAsset" to "Billetera de efectivo"
)

@Composable
fun CreateAccountScreen(
    navController: NavController,
    viewModel: CreateAccountViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Nueva cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (uiState is CreateAccountUiState.Saved) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }

        CreateAccountForm(
            state = uiState,
            onSave = { name, role, number, opening, notes, netWorth, active ->
                viewModel.save(name, role, number, opening, notes, netWorth, active)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountForm(
    state: CreateAccountUiState,
    onSave: (String, String, String, String, String, Boolean, Boolean) -> Unit
) {
    val isSaving = state is CreateAccountUiState.Saving

    var name by remember { mutableStateOf("") }
    var accountRole by remember { mutableStateOf("defaultAsset") }
    var accountNumber by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var includeNetWorth by remember { mutableStateOf(true) }
    var active by remember { mutableStateOf(true) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    val roleLabel = accountRoleOptions.find { it.first == accountRole }?.second ?: "Cuenta por defecto"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        BankCard(
            label = name.ifBlank { "NOMBRE DE LA CUENTA" },
            subtitle = "Vista previa",
            amount = if (openingBalance.isBlank()) "$0.00" else "$$openingBalance",
            icon = Icons.Filled.AccountBalanceWallet,
            accountNumber = accountNumber
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormSection(title = "Información básica", icon = Icons.Filled.Badge) {
            SleekTextField(value = name, onValueChange = { name = it }, label = "Nombre")

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = roleMenuExpanded,
                onExpandedChange = { roleMenuExpanded = it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text("Rol de cuenta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(roleLabel, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                ExposedDropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false }
                ) {
                    accountRoleOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                accountRole = value
                                roleMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SleekTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it },
                label = "Saldo inicial (opcional)"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormSection(title = "Detalles opcionales", icon = Icons.Filled.CreditCard) {
            SleekTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = "Número de cuenta")
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = notes, onValueChange = { notes = it }, label = "Notas", minLines = 3)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormSection(title = "Preferencias", icon = Icons.Filled.ToggleOn) {
            SwitchRow(
                icon = Icons.Filled.TrendingUp,
                label = "Incluir en valor neto",
                checked = includeNetWorth,
                onCheckedChange = { includeNetWorth = it }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background)
            SwitchRow(
                icon = Icons.Filled.Notes,
                label = "Cuenta activa",
                checked = active,
                onCheckedChange = { active = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state is CreateAccountUiState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onSave(name, accountRole, accountNumber, openingBalance, notes, includeNetWorth, active) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isSaving && name.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}