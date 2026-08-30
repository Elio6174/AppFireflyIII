package com.example.appfireflyiii.ui.screens.transactiondetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.RedExpense
import com.example.appfireflyiii.util.formatAmount

@Composable
fun TransactionDetailScreen(
    navController: NavController,
    viewModel: TransactionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
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
                is TransactionDetailUiState.Saved, is TransactionDetailUiState.Deleted -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
                is TransactionDetailUiState.Loaded -> {
                    if (isEditing) {
                        TransactionEditForm(
                            split = state.split,
                            onCancel = { isEditing = false },
                            onSave = { date, amount, description, category, budget, notes ->
                                viewModel.save(date, amount, description, category, budget, notes)
                            }
                        )
                    } else {
                        TransactionDetailContent(
                            split = state.split,
                            onEditClick = { isEditing = true },
                            onDelete = { viewModel.delete() }
                        )
                    }
                }
                is TransactionDetailUiState.Saving -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
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
private fun TransactionEditForm(
    split: TransactionSplit,
    onCancel: () -> Unit,
    onSave: (
        date: String,
        amount: String,
        description: String,
        category: String?,
        budget: String?,
        notes: String?
    ) -> Unit
) {
    var description by remember(split) { mutableStateOf(split.description) }
    var amount by remember(split) { mutableStateOf(split.amount.trimStart('-')) }
    var date by remember(split) { mutableStateOf(split.date.take(10)) }
    var category by remember(split) { mutableStateOf(split.categoryName ?: "") }
    var budget by remember(split) { mutableStateOf(split.budgetName ?: "") }
    var notes by remember(split) { mutableStateOf(split.notes ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        FormSection(title = "Editar información") {
            SleekTextField(value = description, onValueChange = { description = it }, label = "Descripción")
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Monto",
                keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = date, onValueChange = { date = it }, label = "Fecha (AAAA-MM-DD)")
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = category, onValueChange = { category = it }, label = "Categoría")
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = budget, onValueChange = { budget = it }, label = "Presupuesto")
            Spacer(modifier = Modifier.height(10.dp))
            SleekTextField(value = notes, onValueChange = { notes = it }, label = "Notas")
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    onSave(
                        date,
                        amount,
                        description,
                        category.ifBlank { null },
                        budget.ifBlank { null },
                        notes.ifBlank { null }
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleekTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp)
    )
}

private fun transactionTypeLabel(type: String): String = when (type) {
    "withdrawal" -> "Gasto"
    "deposit" -> "Ingreso"
    "transfer" -> "Transferencia"
    else -> type.replaceFirstChar { it.uppercase() }
}