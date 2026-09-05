package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.BudgetData
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.ui.screens.editaccount.SleekTextField
import com.example.appfireflyiii.util.formatAmount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

import com.example.appfireflyiii.ui.theme.DetailScreenBg as ScreenBg
import com.example.appfireflyiii.ui.theme.DetailCardBg as CardBg
import com.example.appfireflyiii.ui.theme.DetailCardBorder as CardBorder
import com.example.appfireflyiii.ui.theme.DetailIconBadgeBg as IconBadgeBg
import com.example.appfireflyiii.ui.theme.DetailIconBadgeTint as IconBadgeTint
import com.example.appfireflyiii.ui.theme.DetailSectionTitleColor as SectionTitleColor
import com.example.appfireflyiii.ui.theme.DetailDividerColor as DividerColor
import com.example.appfireflyiii.ui.theme.DetailLabelGray as LabelGray
import com.example.appfireflyiii.ui.theme.DetailSubLabelGray as SubLabelGray
import com.example.appfireflyiii.ui.theme.DetailWithdrawalColor as WithdrawalColor
import com.example.appfireflyiii.ui.theme.DetailDepositColor as DepositColor
import com.example.appfireflyiii.ui.theme.DetailTransferColor as TransferColor
import com.example.appfireflyiii.ui.theme.DetailCategoryIndigo as CategoryIndigo
import com.example.appfireflyiii.ui.theme.DetailTagAmber as TagAmber

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
    var tags by remember {
        mutableStateOf(
            initialValues.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }
    var foreignAmount by remember { mutableStateOf(initialValues.foreignAmount) }
    var foreignCurrency by remember { mutableStateOf(initialValues.foreignCurrency) }

    var applyRules by remember { mutableStateOf(true) }
    var fireWebhooks by remember { mutableStateOf(true) }
    var returnToCreateAnother by remember { mutableStateOf(false) }
    var resetFormAfterSubmit by remember { mutableStateOf(false) }

    var foreignSectionExpanded by remember { mutableStateOf(false) }
    var shippingSectionExpanded by remember { mutableStateOf(true) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var selectedDateMillis by remember { mutableStateOf(initialValues.dateMillis) }
    var selectedHour by remember { mutableStateOf(initialValues.hour) }
    var selectedMinute by remember { mutableStateOf(initialValues.minute) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("d MMM yyyy", Locale("es", "MX")).format(Date(it))
    } ?: "Hoy"
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

    var showSourcePicker by remember { mutableStateOf(false) }
    var showDestinationPicker by remember { mutableStateOf(false) }
    var showOtherPartyPicker by remember { mutableStateOf(false) }
    var showBudgetPicker by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    var showAddTagField by remember { mutableStateOf(false) }

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
                tags = emptyList()
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

    val amountLabel = when (transactionType) {
        "withdrawal" -> "MONTO DEL GASTO"
        "deposit" -> "MONTO DEL INGRESO"
        else -> "MONTO A TRANSFERIR"
    }
    val amountPrefix = when (transactionType) {
        "withdrawal" -> "-$"
        "deposit" -> "+$"
        else -> "⇄$"
    }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            TransactionTypeSelector(
                selected = transactionType,
                allowChange = allowTypeChange,
                onSelect = {
                    transactionType = it
                    selectedBudget = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AmountHeroCard(
                label = amountLabel,
                prefix = amountPrefix,
                amount = amount,
                onAmountChange = { amount = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "DETALLES", icon = Icons.Filled.Description) {
                FieldLabel("DESCRIPCIÓN")
                Spacer(modifier = Modifier.height(6.dp))
                SleekTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "¿En qué gastaste? (Ej. Compras del súper)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "CUENTAS", icon = Icons.Filled.CreditCard) {
                when (transactionType) {
                    "withdrawal" -> {
                        FieldLabel("CUENTA ORIGEN")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.AttachMoney,
                            name = selectedSourceAccount?.attributes?.name ?: "Selecciona una cuenta",
                            subtitle = selectedSourceAccount?.let {
                                "Saldo disp: ${formatAmount(it.attributes.currentBalance, it.attributes.currencySymbol)}"
                            } ?: "Sin asignar",
                            actionLabel = "Cambiar",
                            onClick = { showSourcePicker = true }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FieldLabel("PAGADO A (OPCIONAL)")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.Person,
                            name = otherParty.ifBlank { "Beneficiario o comercio" },
                            subtitle = if (otherParty.isBlank()) "Sin asignar · Opcional" else "Toca para cambiar",
                            actionLabel = "Elegir",
                            onClick = { showOtherPartyPicker = true }
                        )
                    }
                    "deposit" -> {
                        FieldLabel("RECIBIDO DE (OPCIONAL)")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.Person,
                            name = otherParty.ifBlank { "Pagador o fuente" },
                            subtitle = if (otherParty.isBlank()) "Sin asignar · Opcional" else "Toca para cambiar",
                            actionLabel = "Elegir",
                            onClick = { showOtherPartyPicker = true }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FieldLabel("CUENTA DESTINO")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.AttachMoney,
                            name = selectedDestinationAccount?.attributes?.name ?: "Selecciona una cuenta",
                            subtitle = selectedDestinationAccount?.let {
                                "Saldo disp: ${formatAmount(it.attributes.currentBalance, it.attributes.currencySymbol)}"
                            } ?: "Sin asignar",
                            actionLabel = "Cambiar",
                            onClick = { showDestinationPicker = true }
                        )
                    }
                    else -> {
                        FieldLabel("CUENTA ORIGEN")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.AttachMoney,
                            name = selectedSourceAccount?.attributes?.name ?: "Selecciona una cuenta",
                            subtitle = selectedSourceAccount?.let {
                                "Saldo disp: ${formatAmount(it.attributes.currentBalance, it.attributes.currencySymbol)}"
                            } ?: "Sin asignar",
                            actionLabel = "Cambiar",
                            onClick = { showSourcePicker = true }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FieldLabel("CUENTA DESTINO")
                        Spacer(modifier = Modifier.height(6.dp))
                        AccountSelectRow(
                            icon = Icons.Filled.AttachMoney,
                            name = selectedDestinationAccount?.attributes?.name ?: "Selecciona una cuenta",
                            subtitle = selectedDestinationAccount?.let {
                                "Saldo disp: ${formatAmount(it.attributes.currentBalance, it.attributes.currencySymbol)}"
                            } ?: "Sin asignar",
                            actionLabel = "Cambiar",
                            onClick = { showDestinationPicker = true }
                        )

                        if (selectedSourceAccount != null &&
                            selectedSourceAccount?.id == selectedDestinationAccount?.id
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "El origen y el destino no pueden ser la misma cuenta",
                                style = MaterialTheme.typography.labelSmall,
                                color = WithdrawalColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleRowCard(
                title = "Cantidad extranjera",
                subtitle = "Conversor y comisiones de cambio",
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

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "FECHA Y HORA", icon = Icons.Filled.CalendarMonth) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateTimeBox(
                        modifier = Modifier.weight(1f),
                        label = "FECHA",
                        value = displayDate,
                        onClick = { showDatePicker = true }
                    )
                    DateTimeBox(
                        modifier = Modifier.weight(1f),
                        label = "HORA",
                        value = displayTime,
                        onClick = { showTimePicker = true }
                    )
                }
                if (selectedDateMillis != null || selectedHour != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Toca para restablecer a la fecha/hora actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TransferColor,
                        modifier = Modifier.clickable {
                            selectedDateMillis = null
                            selectedHour = null
                            selectedMinute = null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = "CLASIFICACIÓN", icon = Icons.Filled.Category) {
                FieldLabel("CATEGORÍA")
                Spacer(modifier = Modifier.height(6.dp))
                CategoryField(
                    value = category,
                    onValueChange = { category = it }
                )

                if (transactionType == "withdrawal" && budgets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FieldLabel("PRESUPUESTO")
                    Spacer(modifier = Modifier.height(6.dp))
                    BudgetSelectRow(
                        name = selectedBudget?.attributes?.name ?: "(ninguno)",
                        onClick = { showBudgetPicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                FieldLabel("ETIQUETAS")
                Spacer(modifier = Modifier.height(6.dp))
                TagsEditor(
                    tags = tags,
                    onRemoveTag = { tag -> tags = tags - tag },
                    showAddField = showAddTagField,
                    newTagText = newTagText,
                    onNewTagTextChange = { newTagText = it },
                    onStartAdd = { showAddTagField = true },
                    onCommitAdd = {
                        val cleaned = newTagText.trim()
                        if (cleaned.isNotBlank() && !tags.contains(cleaned)) {
                            tags = tags + cleaned
                        }
                        newTagText = ""
                        showAddTagField = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                FieldLabel("NOTAS ADICIONALES")
                Spacer(modifier = Modifier.height(6.dp))
                SleekTextField(value = notes, onValueChange = { notes = it }, label = "Notas, ticket o detalles adicionales...", minLines = 3)
            }

            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleRowCard(
                title = "Opciones de envío",
                subtitle = null,
                icon = Icons.Filled.Send,
                expanded = shippingSectionExpanded,
                onToggle = { shippingSectionExpanded = !shippingSectionExpanded }
            ) {
                SleekCheckRow(
                    label = "Después de guardar, volver aquí para crear otro",
                    checked = returnToCreateAnother,
                    onCheckedChange = { returnToCreateAnother = it },
                    accentWhenUnchecked = TagAmber
                )
                SleekCheckRow(
                    label = "Restablecer formulario después del envío",
                    checked = resetFormAfterSubmit,
                    onCheckedChange = { resetFormAfterSubmit = it },
                    accentWhenUnchecked = TagAmber
                )
                RowDivider()
                SleekCheckRow(
                    label = "Aplicar reglas",
                    checked = applyRules,
                    onCheckedChange = { applyRules = it }
                )
                SleekCheckRow(
                    label = "Disparar webhooks",
                    checked = fireWebhooks,
                    onCheckedChange = { fireWebhooks = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (saveState) {
                is SaveState.Error -> {
                    Text(saveState.message, color = WithdrawalColor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is SaveState.Success -> {
                    Text("¡Guardado!", color = DepositColor)
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

                    if (amount.isBlank() || description.isBlank()) return@Button

                    when (transactionType) {
                        "withdrawal" -> {
                            val source = selectedSourceAccount ?: return@Button
                            onSave(
                                "withdrawal", dateStr, amount, description, source.id,
                                if (otherPartyAccount == null) otherParty.ifBlank { null } else null,
                                null, otherPartyAccount?.id, category,
                                selectedBudget?.attributes?.name, notes, tags,
                                foreignAmount, foreignCurrency, applyRules, fireWebhooks
                            )
                        }
                        "deposit" -> {
                            val destination = selectedDestinationAccount ?: return@Button
                            onSave(
                                "deposit", dateStr, amount, description, otherPartyAccount?.id, null,
                                if (otherPartyAccount == null) otherParty.ifBlank { null } else null,
                                destination.id, category, null, notes, tags,
                                foreignAmount, foreignCurrency, applyRules, fireWebhooks
                            )
                        }
                        else -> {
                            val source = selectedSourceAccount ?: return@Button
                            val destination = selectedDestinationAccount ?: return@Button
                            if (source.id == destination.id) return@Button
                            onSave(
                                "transfer", dateStr, amount, description, source.id, null, null,
                                destination.id, category, null, notes, tags,
                                foreignAmount, foreignCurrency, applyRules, fireWebhooks
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = Color.White.copy(alpha = 0.3f)),
                enabled = saveState != SaveState.Saving && canSubmit
            ) {
                if (saveState == SaveState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(submitLabel, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSourcePicker) {
        AccountPickerDialog(
            accounts = ownAccounts,
            onDismiss = { showSourcePicker = false },
            onSelect = { selectedSourceAccount = it; showSourcePicker = false }
        )
    }
    if (showDestinationPicker) {
        AccountPickerDialog(
            accounts = ownAccounts,
            onDismiss = { showDestinationPicker = false },
            onSelect = { selectedDestinationAccount = it; showDestinationPicker = false }
        )
    }
    if (showOtherPartyPicker) {
        PartyPickerDialog(
            initialQuery = otherParty,
            suggestions = ownAccounts,
            onDismiss = { showOtherPartyPicker = false },
            onSelectAccount = {
                otherPartyAccount = it
                otherParty = it.attributes.name
                showOtherPartyPicker = false
            },
            onSelectFreeText = {
                otherPartyAccount = null
                otherParty = it
                showOtherPartyPicker = false
            }
        )
    }
    if (showBudgetPicker) {
        BudgetPickerDialog(
            budgets = budgets,
            onDismiss = { showBudgetPicker = false },
            onSelect = { selectedBudget = it; showBudgetPicker = false }
        )
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
private fun BorderedCard(
    shape: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = DividerColor, thickness = 1.dp)
}

@Composable
private fun FieldLabel(text: String) {
    val optionalRegex = remember { Regex("""\s*\(OPCIONAL\)""", RegexOption.IGNORE_CASE) }
    val match = optionalRegex.find(text)

    if (match != null) {
        val mainText = text.substring(0, match.range.first)
        val annotatedText = androidx.compose.ui.text.buildAnnotatedString {
            withStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            ) {
                append(mainText)
            }
            append(" ")
            withStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
            ) {
                append("(opcional)")
            }
        }
        Text(
            annotatedText,
            style = MaterialTheme.typography.labelSmall,
            color = LabelGray
        )
    } else {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
            color = LabelGray
        )
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    BorderedCard(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IconBadgeBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = IconBadgeTint, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                    fontWeight = FontWeight.Bold,
                    color = SectionTitleColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            RowDivider()
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun CollapsibleRowCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BorderedCard(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize().padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IconBadgeBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = IconBadgeTint, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SubLabelGray)
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = SubLabelGray
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}

@Composable
private fun AmountHeroCard(label: String, prefix: String, amount: String, onAmountChange: (String) -> Unit) {
    BorderedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = LabelGray,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(IconBadgeBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("MXN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        prefix,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = SubLabelGray
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicAmountField(amount = amount, onAmountChange = onAmountChange)
                }
            }
        }
    }
}

@Composable
private fun BasicAmountField(amount: String, onAmountChange: (String) -> Unit) {
    BasicTextField(
        value = amount,
        onValueChange = onAmountChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center   // <- centra el texto ingresado
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.defaultMinSize(minWidth = 110.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),   // <- ocupa todo el ancho disponible
                contentAlignment = Alignment.Center   // <- centra el contenido (texto o placeholder)
            ) {
                if (amount.isEmpty()) {
                    Text(
                        "0.00",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = SubLabelGray.copy(alpha = 0.5f),
                        maxLines = 1,
                        softWrap = false
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun AccountSelectRow(icon: ImageVector, name: String, subtitle: String, actionLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IconBadgeBg)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = IconBadgeTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SubLabelGray)
        }
        Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = LabelGray, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetSelectRow(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = SubLabelGray)
    }
}

@Composable
private fun DateTimeBox(modifier: Modifier = Modifier, label: String, value: String, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = LabelGray)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@Composable
private fun CategoryField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(CategoryIndigo)
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Categoría (opcional)", color = SubLabelGray) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TagsEditor(
    tags: List<String>,
    onRemoveTag: (String) -> Unit,
    showAddField: Boolean,
    newTagText: String,
    onNewTagTextChange: (String) -> Unit,
    onStartAdd: () -> Unit,
    onCommitAdd: () -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TagAmber.copy(alpha = 0.16f))
                    .border(1.dp, TagAmber.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .clickable { onRemoveTag(tag) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#$tag", style = MaterialTheme.typography.labelMedium, color = TagAmber, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = TagAmber, modifier = Modifier.size(12.dp))
            }
        }

        if (showAddField) {
            TextField(
                value = newTagText,
                onValueChange = onNewTagTextChange,
                placeholder = { Text("nueva etiqueta", color = SubLabelGray) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                modifier = Modifier
                    .width(140.dp)
                    .onFocusChanged { if (!it.isFocused && newTagText.isNotBlank()) onCommitAdd() },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedIndicatorColor = TagAmber,
                    unfocusedIndicatorColor = CardBorder
                ),
                trailingIcon = {
                    IconButton(onClick = onCommitAdd) {
                        Icon(Icons.Filled.Check, contentDescription = "Agregar", tint = TagAmber)
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clickable { onStartAdd() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+ Añadir etiqueta", style = MaterialTheme.typography.labelMedium, color = SubLabelGray)
            }
        }
    }
}

@Composable
private fun SleekCheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentWhenUnchecked: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) Color.White else Color.Transparent)
                .border(1.dp, if (checked) Color.White else CardBorder, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (!checked && accentWhenUnchecked != null) accentWhenUnchecked else Color.White,
            modifier = Modifier.weight(1f)
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
            .background(CardBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label, color) ->
            val isSelected = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .then(if (allowChange) Modifier.clickable { onSelect(value) } else Modifier)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else SubLabelGray
                )
            }
        }
    }
}

@Composable
private fun AccountPickerDialog(accounts: List<AccountData>, onDismiss: () -> Unit, onSelect: (AccountData) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BorderedCard(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp).heightIn(max = 420.dp)) {
                Text("Selecciona una cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { account ->
                        AccountSelectRow(
                            icon = Icons.Filled.AttachMoney,
                            name = account.attributes.name,
                            subtitle = "Saldo disp: ${formatAmount(account.attributes.currentBalance, account.attributes.currencySymbol)}",
                            actionLabel = "",
                            onClick = { onSelect(account) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetPickerDialog(budgets: List<BudgetData>, onDismiss: () -> Unit, onSelect: (BudgetData?) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        BorderedCard(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp).heightIn(max = 420.dp)) {
                Text("Selecciona un presupuesto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        BudgetSelectRow(name = "(ninguno)", onClick = { onSelect(null) })
                    }
                    items(budgets) { budget ->
                        BudgetSelectRow(name = budget.attributes.name, onClick = { onSelect(budget) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PartyPickerDialog(
    initialQuery: String,
    suggestions: List<AccountData>,
    onDismiss: () -> Unit,
    onSelectAccount: (AccountData) -> Unit,
    onSelectFreeText: (String) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    val filtered = remember(query, suggestions) {
        if (query.isBlank()) suggestions
        else suggestions.filter { it.attributes.name.contains(query, ignoreCase = true) }
    }
    Dialog(onDismissRequest = onDismiss) {
        BorderedCard(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp).heightIn(max = 460.dp)) {
                Text("Beneficiario o comercio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Escribe un nombre", color = SubLabelGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = TransferColor,
                        unfocusedIndicatorColor = CardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { account ->
                        AccountSelectRow(
                            icon = Icons.Filled.Person,
                            name = account.attributes.name,
                            subtitle = if (account.attributes.type == "liability") "Pasivo" else "Activo",
                            actionLabel = "",
                            onClick = { onSelectAccount(account) }
                        )
                    }
                }
                if (query.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSelectFreeText(query) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TransferColor)
                    ) {
                        Text("Usar \"$query\"", color = Color.White)
                    }
                }
            }
        }
    }
}