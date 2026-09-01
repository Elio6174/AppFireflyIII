package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/** Valores con los que arranca el formulario. Deja todo en blanco/null para "Nueva transacción". */
data class TransactionFormInitialValues(
    val type: String = "withdrawal",
    val amount: String = "",
    val description: String = "",
    val otherParty: String = "",
    val category: String = "",
    val notes: String = "",
    val tagsInput: String = "",
    val foreignAmount: String = "",
    val foreignCurrency: String = "",
    val accountId: String? = null,
    val budgetName: String? = null,
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null
)

/**
 * Cuerpo completo del formulario de transacción (monto, fecha/hora, cuenta, categoría,
 * presupuesto, etiquetas, moneda extranjera, opciones de envío). Reciclado tanto por
 * Nueva Transacción como por la edición de un movimiento existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormBody(
    title: String,
    submitLabel: String,
    initialValues: TransactionFormInitialValues,
    saveState: SaveState,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository,
    allowTypeChange: Boolean = true,
    onSave: (
        type: String,
        date: String,
        amount: String,
        description: String,
        sourceId: String?,
        destinationName: String?,
        sourceName: String?,
        destinationId: String?,
        categoryName: String?,
        budgetName: String?,
        notes: String?,
        tags: List<String>?,
        foreignAmount: String?,
        foreignCurrencyCode: String?,
        applyRules: Boolean,
        fireWebhooks: Boolean
    ) -> Unit,
    onSavedNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var transactionType by remember { mutableStateOf(initialValues.type) }
    var amount by remember { mutableStateOf(initialValues.amount) }
    var description by remember { mutableStateOf(initialValues.description) }
    var otherParty by remember { mutableStateOf(initialValues.otherParty) }
    var category by remember { mutableStateOf(initialValues.category) }
    var notes by remember { mutableStateOf(initialValues.notes) }
    var tagsInput by remember { mutableStateOf(initialValues.tagsInput) }
    var foreignAmount by remember { mutableStateOf(initialValues.foreignAmount) }
    var foreignCurrency by remember { mutableStateOf(initialValues.foreignCurrency) }

    var applyRules by remember { mutableStateOf(true) }
    var fireWebhooks by remember { mutableStateOf(true) }
    var returnToCreateAnother by remember { mutableStateOf(false) }
    var resetFormAfterSubmit by remember { mutableStateOf(false) }

    var foreignSectionExpanded by remember { mutableStateOf(false) }
    var shippingSectionExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var selectedDateMillis by remember { mutableStateOf(initialValues.dateMillis) }
    var selectedHour by remember { mutableStateOf(initialValues.hour) }
    var selectedMinute by remember { mutableStateOf(initialValues.minute) }

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
            selectedAccount = assets.find { it.id == initialValues.accountId } ?: assets.firstOrNull()
        }
        budgetRepository.getBudgets().onSuccess { loaded ->
            budgets = loaded
            selectedBudget = loaded.find { it.attributes.name == initialValues.budgetName }
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            if (resetFormAfterSubmit) {
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
            }
            if (!returnToCreateAnother) {
                onSavedNavigateBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = transactionType == "withdrawal",
                onClick = { if (allowTypeChange) { transactionType = "withdrawal"; selectedBudget = null } },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                enabled = allowTypeChange
            ) { Text("Gasto") }
            SegmentedButton(
                selected = transactionType == "deposit",
                onClick = { if (allowTypeChange) { transactionType = "deposit"; selectedBudget = null } },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                enabled = allowTypeChange
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

        when (saveState) {
            is SaveState.Error -> {
                Text(saveState.message, color = MaterialTheme.colorScheme.error)
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
                    onSave(
                        "withdrawal",
                        dateStr,
                        amount,
                        description,
                        account.id,
                        otherParty,
                        null,
                        null,
                        category,
                        selectedBudget?.attributes?.name,
                        notes,
                        tags,
                        foreignAmount,
                        foreignCurrency,
                        applyRules,
                        fireWebhooks
                    )
                } else {
                    onSave(
                        "deposit",
                        dateStr,
                        amount,
                        description,
                        null,
                        null,
                        otherParty,
                        account.id,
                        category,
                        null,
                        notes,
                        tags,
                        foreignAmount,
                        foreignCurrency,
                        applyRules,
                        fireWebhooks
                    )
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
                Text(submitLabel, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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