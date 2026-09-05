package com.example.appfireflyiii.ui.screens.editaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.ui.screens.accounts.BankCard
import com.example.appfireflyiii.util.formatAmount
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog

private val accountRoleOptions = listOf(
    "defaultAsset" to "Cuenta por defecto",
    "sharedAsset" to "Cuenta compartida",
    "savingAsset" to "Cuenta de ahorros",
    "ccAsset" to "Tarjeta de crédito",
    "cashWalletAsset" to "Billetera de efectivo"
)

@Composable
fun EditAccountScreen(
    navController: NavController,
    viewModel: EditAccountViewModel
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
            Text("Editar cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is EditAccountUiState.Loading, is EditAccountUiState.Deleting -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EditAccountUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No se pudo cargar: ${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.load() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is EditAccountUiState.Saved, is EditAccountUiState.Deleted -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    EditAccountForm(
                        state = state,
                        onSave = { name, number, role, notes, netWorth, active ->
                            viewModel.save(name, number, role, notes, netWorth, active)
                        },
                        onDelete = { viewModel.delete() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountForm(
    state: EditAccountUiState,
    onSave: (String, String, String, String, Boolean, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val loadedAccount = (state as? EditAccountUiState.Loaded)?.account
    val isSaving = state is EditAccountUiState.Saving

    var name by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.name ?: "") }
    var accountNumber by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.accountNumber ?: "") }
    var accountRole by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.accountRole ?: "defaultAsset") }
    var notes by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.notes ?: "") }
    var includeNetWorth by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.includeNetWorth ?: true) }
    var active by remember(loadedAccount) { mutableStateOf(loadedAccount?.attributes?.active ?: true) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val balanceText = loadedAccount?.attributes?.let {
        formatAmount(it.currentBalance, it.currencySymbol)
    } ?: ""

    val roleLabel = accountRoleOptions.find { it.first == accountRole }?.second ?: "Cuenta por defecto"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        BankCard(
            label = (name.ifBlank { "NOMBRE DE LA CUENTA" }),
            subtitle = "Vista previa",
            amount = balanceText,
            icon = Icons.Filled.AccountBalanceWallet,
            accountNumber = accountNumber,
            onDeleteClick = { showDeleteConfirm = true }
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

        if (state is EditAccountUiState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onSave(name, accountNumber, accountRole, notes, includeNetWorth, active) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = !isSaving && name.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar cuenta", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "¿Eliminar esta cuenta?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Esta acción no se puede deshacer. Se eliminará la cuenta y su historial visible desde esta app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                showDeleteConfirm = false
                                onDelete()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Eliminar", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleekTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.White.copy(alpha = 0.35f)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboardOptions,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        )
    )
}

@Composable
fun FormSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SwitchRow(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}