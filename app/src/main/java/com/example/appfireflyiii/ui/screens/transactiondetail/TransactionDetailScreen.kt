package com.example.appfireflyiii.ui.screens.transactiondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.ui.screens.newtransaction.SaveState
import com.example.appfireflyiii.ui.screens.newtransaction.TransactionFormBody
import com.example.appfireflyiii.ui.screens.newtransaction.TransactionFormInitialValues
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.RedExpense
import com.example.appfireflyiii.util.formatAmount
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    navController: NavController,
    viewModel: TransactionDetailViewModel,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isEditing) isEditing = false else navController.popBackStack()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                if (isEditing) "Editar movimiento" else "Detalle del movimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is TransactionDetailUiState.Loading, is TransactionDetailUiState.Deleting -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TransactionDetailUiState.Error -> {
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
                is TransactionDetailUiState.Deleted -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
                is TransactionDetailUiState.Loaded -> {
                    if (isEditing) {
                        TransactionFormBody(
                            title = "Editar movimiento",
                            submitLabel = "Guardar cambios",
                            initialValues = initialValuesFrom(state.split),
                            saveState = saveState,
                            accountRepository = accountRepository,
                            budgetRepository = budgetRepository,
                            onSave = { type, date, amount, description, sourceId, destinationName, sourceName,
                                       destinationId, categoryName, budgetName, notes, tags, foreignAmount,
                                       foreignCurrencyCode, applyRules, fireWebhooks ->
                                viewModel.save(
                                    type = type,
                                    date = date,
                                    amount = amount,
                                    description = description,
                                    sourceId = sourceId,
                                    destinationName = destinationName,
                                    sourceName = sourceName,
                                    destinationId = destinationId,
                                    categoryName = categoryName,
                                    budgetName = budgetName,
                                    notes = notes,
                                    tags = tags,
                                    foreignAmount = foreignAmount,
                                    foreignCurrencyCode = foreignCurrencyCode,
                                    applyRules = applyRules,
                                    fireWebhooks = fireWebhooks
                                )
                            },
                            // al guardar bien, regresamos a la vista de solo lectura (no salimos de la pantalla)
                            onSavedNavigateBack = { isEditing = false }
                        )
                    } else {
                        TransactionDetailContent(
                            split = state.split,
                            onEditClick = { isEditing = true },
                            onDelete = { viewModel.delete() }
                        )
                    }
                }
            }
        }
    }
}

/** Convierte el split cargado en los valores iniciales que espera el formulario reciclado. */
private fun initialValuesFrom(split: TransactionSplit): TransactionFormInitialValues {
    var dateMillis: Long? = null
    var hour: Int? = null
    var minute: Int? = null
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val parsed = parser.parse(split.date)
        if (parsed != null) {
            dateMillis = parsed.time
            val cal = java.util.Calendar.getInstance().apply { time = parsed }
            hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            minute = cal.get(java.util.Calendar.MINUTE)
        }
    } catch (_: Exception) {
        // si el formato no matchea, se deja en null y el formulario usa "Hoy"/"Ahora"
    }

    return TransactionFormInitialValues(
        type = split.type,
        amount = split.amount.trimStart('-'),
        description = split.description,
        otherParty = if (split.type == "withdrawal") split.destinationName ?: "" else split.sourceName ?: "",
        category = split.categoryName ?: "",
        notes = split.notes ?: "",
        accountId = if (split.type == "withdrawal") split.sourceId else split.destinationId,
        budgetName = split.budgetName,
        dateMillis = dateMillis,
        hour = hour,
        minute = minute
    )
}

@Composable
private fun TransactionDetailContent(
    split: TransactionSplit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isExpense = split.type == "withdrawal"
    val amountColor = if (isExpense) RedExpense else AssetColor
    val amountPrefix = if (isExpense) "-" else "+"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$amountPrefix${formatAmount(split.amount, split.currencySymbol)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    split.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FormSection(title = "Información") {
            DetailRow(label = "Tipo", value = transactionTypeLabel(split.type))
            DetailRow(label = "Fecha", value = split.date.take(10))
            DetailRow(label = "Categoría", value = split.categoryName ?: "Sin categoría")
            if (!split.budgetName.isNullOrBlank()) {
                DetailRow(label = "Presupuesto", value = split.budgetName)
            }
        }

        if (!split.sourceName.isNullOrBlank() || !split.destinationName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            FormSection(title = "Cuentas") {
                if (!split.sourceName.isNullOrBlank()) {
                    DetailRow(label = "Origen", value = split.sourceName)
                }
                if (!split.destinationName.isNullOrBlank()) {
                    DetailRow(label = "Destino", value = split.destinationName)
                }
            }
        }

        if (!split.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            FormSection(title = "Notas") {
                Text(
                    split.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text("Eliminar")
            }
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Editar")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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
                        "¿Eliminar este movimiento?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Esta acción no se puede deshacer. Se eliminará el movimiento de forma permanente.",
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
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(10.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun transactionTypeLabel(type: String): String = when (type) {
    "withdrawal" -> "Gasto"
    "deposit" -> "Ingreso"
    "transfer" -> "Transferencia"
    else -> type.replaceFirstChar { it.uppercase() }
}