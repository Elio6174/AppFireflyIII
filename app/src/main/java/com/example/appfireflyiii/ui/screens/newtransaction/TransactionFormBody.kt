package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.focus.onFocusChanged

private val WithdrawalColor = Color(0xFFEF4444)
private val DepositColor = Color(0xFF22C55E)
private val TransferColor = Color(0xFF3B82F6)

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

    var ownAccounts by remember { mutableStateOf<List<AccountData>>(emptyList()) }

    var selectedSourceAccount by remember { mutableStateOf<AccountData?>(null) }
    var selectedDestinationAccount by remember { mutableStateOf<AccountData?>(null) }

    var otherParty by remember { mutableStateOf(initialValues.otherParty) }
    var otherPartyAccount by remember { mutableStateOf<AccountData?>(null) }

    var budgets by remember { mutableStateOf<List<BudgetData>>(emptyList()) }
    var selectedBudget by remember { mutableStateOf<BudgetData?>(null) }
    var budgetMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        accountRepository.getAccounts().onSuccess { accounts ->
            val own = accounts.filter {
                it.attributes.type != "expense" &&
                        it.attributes.type != "revenue" &&
                        it.attributes.type != "cash" &&
                        it.attributes.type != "initial-balance"
            }
            ownAccounts = own
            val preselected = own.find { it.id == initialValues.accountId }
                ?: own.firstOrNull { it.attributes.type == "asset" }
                ?: own.firstOrNull()
            when (initialValues.type) {
                "deposit" -> selectedDestinationAccount = preselected
                else -> selectedSourceAccount = preselected
            }
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
                otherPartyAccount = null
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

    val accentColor = when (transactionType) {
        "withdrawal" -> WithdrawalColor
        "deposit" -> DepositColor
        else -> TransferColor
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

        TransactionTypeSelector(
            selected = transactionType,
            allowChange = allowTypeChange,
            onSelect = {
                transactionType = it
                selectedBudget = null
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        AmountHeroSection(
            amount = amount,
            onAmountChange = { amount = it },
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormSection(title = "Detalles", icon = Icons.Filled.Description) {
            SleekTextField(value = description, onValueChange = { description = it }, label = "Descripción")
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Cuentas", icon = Icons.Filled.AccountBalanceWallet) {
            when (transactionType) {
                "withdrawal" -> {
                    OwnAccountDropdown(
                        label = "Cuenta origen",
                        accounts = ownAccounts,
                        selected = selectedSourceAccount,
                        onSelect = { selectedSourceAccount = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AccountAutocompleteField(
                        label = "Pagado a (opcional)",
                        query = otherParty,
                        onQueryChange = { otherParty = it; otherPartyAccount = null },
                        suggestions = ownAccounts,
                        onAccountSelected = {
                            otherPartyAccount = it
                            otherParty = it.attributes.name
                        }
                    )
                }
                "deposit" -> {
                    AccountAutocompleteField(
                        label = "Recibido de (opcional)",
                        query = otherParty,
                        onQueryChange = { otherParty = it; otherPartyAccount = null },
                        suggestions = ownAccounts,
                        onAccountSelected = {
                            otherPartyAccount = it
                            otherParty = it.attributes.name
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OwnAccountDropdown(
                        label = "Cuenta destino",
                        accounts = ownAccounts,
                        selected = selectedDestinationAccount,
                        onSelect = { selectedDestinationAccount = it }
                    )
                }
                else -> {
                    OwnAccountDropdown(
                        label = "Cuenta origen",
                        accounts = ownAccounts,
                        selected = selectedSourceAccount,
                        onSelect = { selectedSourceAccount = it }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                val temp = selectedSourceAccount
                                selectedSourceAccount = selectedDestinationAccount
                                selectedDestinationAccount = temp
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Filled.SwapVert,
                                contentDescription = "Intercambiar origen y destino",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OwnAccountDropdown(
                        label = "Cuenta destino",
                        accounts = ownAccounts,
                        selected = selectedDestinationAccount,
                        onSelect = { selectedDestinationAccount = it }
                    )

                    if (selectedSourceAccount != null &&
                        selectedSourceAccount?.id == selectedDestinationAccount?.id
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "El origen y el destino no pueden ser la misma cuenta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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
                        .clip(RoundedCornerShape(14.dp))
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
                        .clip(RoundedCornerShape(14.dp))
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

        val canSubmit = amount.isNotBlank() && description.isNotBlank() && when (transactionType) {
            "withdrawal" -> selectedSourceAccount != null
            "deposit" -> selectedDestinationAccount != null
            else -> selectedSourceAccount != null &&
                    selectedDestinationAccount != null &&
                    selectedSourceAccount?.id != selectedDestinationAccount?.id
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
                val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

                if (amount.isBlank() || description.isBlank()) return@Button

                when (transactionType) {
                    "withdrawal" -> {
                        val source = selectedSourceAccount ?: return@Button
                        onSave(
                            "withdrawal",
                            dateStr,
                            amount,
                            description,
                            source.id,
                            if (otherPartyAccount == null) otherParty.ifBlank { null } else null,
                            null,
                            otherPartyAccount?.id,
                            category,
                            selectedBudget?.attributes?.name,
                            notes,
                            tags,
                            foreignAmount,
                            foreignCurrency,
                            applyRules,
                            fireWebhooks
                        )
                    }
                    "deposit" -> {
                        val destination = selectedDestinationAccount ?: return@Button
                        onSave(
                            "deposit",
                            dateStr,
                            amount,
                            description,
                            otherPartyAccount?.id,
                            null,
                            if (otherPartyAccount == null) otherParty.ifBlank { null } else null,
                            destination.id,
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
                    else -> {
                        val source = selectedSourceAccount ?: return@Button
                        val destination = selectedDestinationAccount ?: return@Button
                        if (source.id == destination.id) return@Button
                        onSave(
                            "transfer",
                            dateStr,
                            amount,
                            description,
                            source.id,
                            null,
                            null,
                            destination.id,
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
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            enabled = saveState != SaveState.Saving && canSubmit
        ) {
            if (saveState == SaveState.Saving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(submitLabel, fontWeight = FontWeight.SemiBold, color = Color.White)
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
fun AmountHeroSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Monto",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = {
                Text(
                    "0.00",
                    style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            textStyle = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = accentColor
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun TransactionTypeSelector(
    selected: String,
    allowChange: Boolean,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        Triple("withdrawal", "Gasto", WithdrawalColor),
        Triple("deposit", "Ingreso", DepositColor),
        Triple("transfer", "Transferencia", TransferColor)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label, color) ->
            val isSelected = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent)
                    .then(
                        if (allowChange) Modifier.clickable { onSelect(value) } else Modifier
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnAccountDropdown(
    label: String,
    accounts: List<AccountData>,
    selected: AccountData?,
    onSelect: (AccountData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        SleekDropdownBox(
            label = label,
            value = selected?.attributes?.name ?: "Selecciona una cuenta",
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(account.attributes.name)
                            Text(
                                if (account.attributes.type == "liabilities") "Pasivo" else "Activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onSelect(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAutocompleteField(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<AccountData>,
    onAccountSelected: (AccountData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(query, suggestions) {
        if (query.isBlank()) suggestions
        else suggestions.filter { it.attributes.name.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) expanded = true
                },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(account.attributes.name)
                            Text(
                                if (account.attributes.type == "liability") "Pasivo" else "Activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SleekDropdownBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
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