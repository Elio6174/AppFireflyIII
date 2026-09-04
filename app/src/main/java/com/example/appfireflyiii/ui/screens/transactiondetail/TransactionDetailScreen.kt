package com.example.appfireflyiii.ui.screens.transactiondetail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository
import com.example.appfireflyiii.ui.screens.newtransaction.TransactionFormBody
import com.example.appfireflyiii.ui.screens.newtransaction.TransactionFormInitialValues
import kotlinx.coroutines.launch
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
import com.example.appfireflyiii.ui.theme.DetailDeleteRed as DeleteRed
import com.example.appfireflyiii.ui.theme.DetailSheetHandleColor as SheetHandleColor
import com.example.appfireflyiii.ui.theme.DetailCancelButtonBg as CancelButtonBg
import androidx.compose.material.icons.filled.ArrowDownward
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
    val context = LocalContext.current

    val deleteSheetProgress = remember { Animatable(0f) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingSplit by remember { mutableStateOf<TransactionSplit?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f - 0.08f * deleteSheetProgress.value
                    scaleX = scale
                    scaleY = scale
                    shape = RoundedCornerShape(28.dp * deleteSheetProgress.value)
                    clip = true
                }
                .background(ScreenBg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquareIconButton(icon = Icons.Filled.ArrowBack, contentDescription = "Volver") {
                    if (isEditing) isEditing = false else navController.popBackStack()
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) "Editar movimiento" else "Detalle del movimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (!isEditing) {
                    val loadedState = uiState as? TransactionDetailUiState.Loaded
                    SquareIconButton(icon = Icons.Filled.IosShare, contentDescription = "Compartir") {
                        val split = loadedState?.split ?: return@SquareIconButton
                        val shareText = buildString {
                            append(split.description)
                            append(": ")
                            append(formatAmountSimple(split.amount, split.currencySymbol))
                            append(" — ")
                            append(split.date.take(10))
                        }
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

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
                            Text("No se pudo cargar: ${state.message}", color = Color.White)
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
                                onSavedNavigateBack = { isEditing = false }
                            )
                        } else {
                            TransactionDetailContent(
                                split = state.split,
                                onEditClick = { isEditing = true },
                                onDeleteRequest = {
                                    deletingSplit = state.split
                                    showDeleteConfirm = true
                                    scope.launch { deleteSheetProgress.animateTo(1f, tween(280)) }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                scope.launch {
                                    deleteSheetProgress.animateTo(0f, tween(220))
                                    showDeleteConfirm = false
                                }
                            }
                        )
                    }
            )
            deletingSplit?.let { split ->
                DeleteConfirmationSheet(
                    split = split,
                    amountPrefix = if (split.type == "withdrawal") "-" else "+",
                    progress = deleteSheetProgress,
                    onDismiss = { showDeleteConfirm = false },
                    onConfirm = {
                        showDeleteConfirm = false
                        viewModel.delete()
                    }
                )
            }
        }
    }
}

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
    } catch (_: Exception) { }

    return TransactionFormInitialValues(
        type = split.type,
        amount = split.amount.trimStart('-'),
        description = split.description,
        otherParty = if (split.type == "withdrawal") split.destinationName ?: "" else split.sourceName ?: "",
        category = split.categoryName ?: "",
        notes = split.notes ?: "",
        tagsInput = split.tags?.joinToString(", ") ?: "",
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
    onDeleteRequest: () -> Unit
) {
    val isExpense = split.type == "withdrawal"
    val amountPrefix = if (isExpense) "-" else "+"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        BorderedCard(shape = RoundedCornerShape(28.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$amountPrefix${formatAmountSimple(split.amount, split.currencySymbol)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    split.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (!split.journalId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Id de transacción: #${split.journalId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubLabelGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "INFORMACIÓN", icon = Icons.Filled.Info) {
            InfoRow(label = "Tipo") { TypeBadge(split.type) }
            RowDivider()
            InfoRowPlain(label = "Fecha", value = formatDate(split.date), subvalue = formatTime(split.date))

            if (!split.categoryName.isNullOrBlank()) {
                RowDivider()
                InfoRow(label = "Categoría") { Pill(text = split.categoryName, color = CategoryIndigo) }
            }

            if (!split.tags.isNullOrEmpty()) {
                RowDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (split.tags.size > 1) "Etiquetas" else "Etiqueta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LabelGray
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        split.tags.forEach { tag -> Pill(text = tag, color = TagAmber) }
                    }
                }
            }

            if (!split.budgetName.isNullOrBlank()) {
                RowDivider()
                InfoRowPlain(label = "Presupuesto", value = split.budgetName)
            }
        }

        if (!split.sourceName.isNullOrBlank() || !split.destinationName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard(title = "CUENTAS", icon = Icons.Filled.CreditCard) {
                if (!split.sourceName.isNullOrBlank()) {
                    AccountRow(label = "ORIGEN", name = split.sourceName)
                }
                if (!split.sourceName.isNullOrBlank() && !split.destinationName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (!split.destinationName.isNullOrBlank()) {
                    AccountRow(label = "DESTINO", name = split.destinationName)
                }
            }
        }

        if (!split.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard(title = "NOTAS", icon = Icons.Filled.Notes) {
                Text(
                    split.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onDeleteRequest() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WithdrawalColor.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardBg,
                    contentColor = WithdrawalColor
                )
            ) {
                Text("Eliminar", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("Editar", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BoxScope.DeleteConfirmationSheet(
    split: TransactionSplit,
    amountPrefix: String,
    progress: Animatable<Float, AnimationVector1D>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var sheetHeightPx by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .onGloballyPositioned { sheetHeightPx = it.size.height.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                translationY = (1f - progress.value) * sheetHeightPx
            }
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(CardBg)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (progress.value < 0.6f) {
                                progress.animateTo(0f, tween(220))
                                onDismiss()
                            } else {
                                progress.animateTo(1f, tween(220))
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val delta = dragAmount / sheetHeightPx
                        scope.launch { progress.snapTo((progress.value - delta).coerceIn(0f, 1f)) }
                    }
                )
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(SheetHandleColor)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "¿Eliminar movimiento?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Esta acción no se puede deshacer. Se actualizarán los balances vinculados a esta transacción.",
            style = MaterialTheme.typography.bodyMedium,
            color = LabelGray
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    split.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    formatDate(split.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = LabelGray
                )
            }
            Text(
                "$amountPrefix${formatAmountSimple(split.amount, split.currencySymbol)}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                color = if (amountPrefix == "-") WithdrawalColor else DepositColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
        ) {
            Text("Eliminar definitivamente", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                scope.launch {
                    progress.animateTo(0f, tween(220))
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CancelButtonBg)
        ) {
            Text("Cancelar", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))
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
private fun SquareIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(IconBadgeBg)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(18.dp))
        }
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
            Spacer(modifier = Modifier.height(4.dp))
            RowDivider()
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = DividerColor, thickness = 1.dp)
}

@Composable
private fun InfoRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LabelGray)
        trailing()
    }
}

@Composable
private fun InfoRowPlain(label: String, value: String, subvalue: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LabelGray)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            if (!subvalue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subvalue, style = MaterialTheme.typography.labelSmall, color = SubLabelGray)
            }
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val (label, color, icon) = when (type) {
        "withdrawal" -> Triple("Gasto", WithdrawalColor, Icons.Filled.ArrowDownward)
        "deposit" -> Triple("Ingreso", DepositColor, Icons.Filled.ArrowUpward)
        else -> Triple("Transferencia", TransferColor, Icons.Filled.SwapHoriz)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun AccountRow(label: String, name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
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
            Icon(
                Icons.Filled.AttachMoney,
                contentDescription = null,
                tint = IconBadgeTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = SubLabelGray
            )
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}


private fun formatAmountSimple(amount: String, currencySymbol: String?): String {
    val clean = amount.trimStart('-')
    val number = clean.toDoubleOrNull()
    val formatted = if (number != null) String.format(Locale.getDefault(), "%,.2f", number) else clean
    return "${currencySymbol ?: ""}$formatted"
}

private fun formatDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val formatter = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatTime(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val formatter = SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault())
        val parsed = parser.parse(isoDate)
        if (parsed != null) formatter.format(parsed) else ""
    } catch (_: Exception) {
        ""
    }
}