package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.BudgetData
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.ui.screens.editaccount.FormSection
import com.example.appfireflyiii.ui.screens.editaccount.SleekTextField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    navController: NavController,
    viewModel: NewTransactionViewModel,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository
) {
    val saveState by viewModel.saveState.collectAsState()
    val scrollState = rememberScrollState()

    var transactionType by remember { mutableStateOf("withdrawal") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var otherParty by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var foreignAmount by remember { mutableStateOf("") }
    var foreignCurrency by remember { mutableStateOf("") }

    var applyRules by remember { mutableStateOf(true) }
    var fireWebhooks by remember { mutableStateOf(true) }
    var returnToCreateAnother by remember { mutableStateOf(false) }
    var resetFormAfterSubmit by remember { mutableStateOf(false) }

    var foreignSectionExpanded by remember { mutableStateOf(false) }
    var shippingSectionExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Arranca en null: si el usuario no toca fecha/hora, se usa la fecha/hora actual al guardar
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayDate = selectedDateMillis?.let { dateFormat.format(Date(it)) } ?: "Hoy"
    val displayTime = if (selectedHour != null && selectedMinute != null) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    } else "Ahora"

    var assetAccounts by remember { mutableStateOf<List<AccountData>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<AccountData?>(null) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    var budgets by remember { mutableStateOf<List<BudgetData>>(emptyList()) }
    var selectedBudget by remember { mutableStateOf<BudgetData?>(null) }
    var budgetMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        accountRepository.getAccounts().onSuccess { accounts ->
            val assets = accounts.filter { it.attributes.type == "asset" }
            assetAccounts = assets
            selectedAccount = assets.firstOrNull()
        }
        budgetRepository.getBudgets().onSuccess { budgets = it }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success && resetFormAfterSubmit) {
            amount = ""
            description = ""
            otherParty = ""
            category = ""
            notes = ""
            tagsInput = ""
            foreignAmount = ""
            foreignCurrency = ""
            selectedBudget = null
            selectedDateMillis = null
            selectedHour = null
            selectedMinute = null
        } else if (saveState is SaveState.Success) {
            amount = ""
            description = ""
            otherParty = ""
            category = ""
            selectedBudget = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nueva transacción", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = transactionType == "withdrawal",
                onClick = { transactionType = "withdrawal"; selectedBudget = null },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Gasto") }
            SegmentedButton(
                selected = transactionType == "deposit",
                onClick = { transactionType = "deposit"; selectedBudget = null },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Ingreso") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormSection(title = "Detalles", icon = Icons.Filled.Description) {
            SleekTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Monto",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = description, onValueChange = { description = it }, label = "Descripción")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cantidad extranjera: colapsable, justo debajo de Detalles
        CollapsibleSection(
            title = "Cantidad extranjera",
            icon = Icons.Filled.Public,
            expanded = foreignSectionExpanded,
            onToggle = { foreignSectionExpanded = !foreignSectionExpanded }
        ) {
            SleekTextField(
                value = foreignAmount,
                onValueChange = { foreignAmount = it },
                label = "Cantidad extranjera (opcional)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(
                value = foreignCurrency,
                onValueChange = { foreignCurrency = it.uppercase() },
                label = "Moneda (ej. USD)"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Fecha y hora", icon = Icons.Filled.CalendarMonth) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text("Fecha", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(displayDate, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showTimePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text("Hora", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(displayTime, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (selectedDateMillis != null || selectedHour != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Toca para restablecer a la fecha/hora actual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        selectedDateMillis = null
                        selectedHour = null
                        selectedMinute = null
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Cuentas", icon = Icons.Filled.AccountBalanceWallet) {
            ExposedDropdownMenuBox(
                expanded = accountMenuExpanded,
                onExpandedChange = { accountMenuExpanded = it }
            ) {
                SleekDropdownBox(
                    label = "Cuenta",
                    value = selectedAccount?.attributes?.name ?: "Selecciona una cuenta",
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

            Spacer(modifier = Modifier.height(10.dp))

            SleekTextField(
                value = otherParty,
                onValueChange = { otherParty = it },
                label = if (transactionType == "withdrawal") "Pagado a (opcional)" else "Recibido de (opcional)"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Clasificación", icon = Icons.Filled.Category) {
            SleekTextField(value = category, onValueChange = { category = it }, label = "Categoría (opcional)")

            if (transactionType == "withdrawal" && budgets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(
                    expanded = budgetMenuExpanded,
                    onExpandedChange = { budgetMenuExpanded = it }
                ) {
                    SleekDropdownBox(
                        label = "Presupuesto",
                        value = selectedBudget?.attributes?.name ?: "(ninguno)",
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = budgetMenuExpanded,
                        onDismissRequest = { budgetMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("(ninguno)") },
                            onClick = { selectedBudget = null; budgetMenuExpanded = false }
                        )
                        budgets.forEach { budget ->
                            DropdownMenuItem(
                                text = { Text(budget.attributes.name) },
                                onClick = { selectedBudget = budget; budgetMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SleekTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = "Etiquetas (separadas por coma)"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SleekTextField(value = notes, onValueChange = { notes = it }, label = "Notas", minLines = 3)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Opciones de envío: colapsable
        CollapsibleSection(
            title = "Opciones de envío",
            icon = Icons.Filled.Send,
            expanded = shippingSectionExpanded,
            onToggle = { shippingSectionExpanded = !shippingSectionExpanded }
        ) {
            OptionCheckboxRow(
                label = "Después de guardar, volver aquí para crear otro",
                checked = returnToCreateAnother,
                onCheckedChange = { returnToCreateAnother = it }
            )
            OptionCheckboxRow(
                label = "Restablecer formulario después del envío",
                checked = resetFormAfterSubmit,
                onCheckedChange = { resetFormAfterSubmit = it }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background)
            OptionCheckboxRow(
                label = "Aplicar reglas",
                checked = applyRules,
                onCheckedChange = { applyRules = it }
            )
            OptionCheckboxRow(
                label = "Disparar webhooks",
                checked = fireWebhooks,
                onCheckedChange = { fireWebhooks = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                val now = Calendar.getInstance()
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis ?: now.timeInMillis
                    set(Calendar.HOUR_OF_DAY, selectedHour ?: now.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, selectedMinute ?: now.get(Calendar.MINUTE))
                }
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val dateStr = isoFormat.format(cal.time)

                val account = selectedAccount
                val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

                if (amount.isBlank() || description.isBlank() || account == null) return@Button

                if (transactionType == "withdrawal") {
                    viewModel.save(
                        type = "withdrawal",
                        date = dateStr,
                        amount = amount,
                        description = description,
                        sourceId = account.id,
                        destinationName = otherParty,
                        sourceName = null,
                        destinationId = null,
                        categoryName = category,
                        budgetName = selectedBudget?.attributes?.name,
                        notes = notes,
                        tags = tags,
                        foreignAmount = foreignAmount,
                        foreignCurrencyCode = foreignCurrency,
                        applyRules = applyRules,
                        fireWebhooks = fireWebhooks
                    )
                } else {
                    viewModel.save(
                        type = "deposit",
                        date = dateStr,
                        amount = amount,
                        description = description,
                        sourceId = null,
                        destinationName = null,
                        sourceName = otherParty,
                        destinationId = account.id,
                        categoryName = category,
                        budgetName = null,
                        notes = notes,
                        tags = tags,
                        foreignAmount = foreignAmount,
                        foreignCurrencyCode = foreignCurrency,
                        applyRules = applyRules,
                        fireWebhooks = fireWebhooks
                    )
                }

                if (!returnToCreateAnother) {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = saveState != SaveState.Saving
        ) {
            if (saveState == SaveState.Saving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val now = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour ?: now.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: now.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Composable
fun SleekDropdownBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun OptionCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

/** Igual que FormSection, pero el encabezado es clickeable y el contenido se muestra/oculta. */
@Composable
fun CollapsibleSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}