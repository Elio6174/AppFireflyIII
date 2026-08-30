package com.example.appfireflyiii.ui.screens.accountdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.ui.theme.AssetColor
import com.example.appfireflyiii.ui.theme.CardGradientEnd
import com.example.appfireflyiii.ui.theme.CardGradientStart
import com.example.appfireflyiii.ui.theme.RedExpense
import com.example.appfireflyiii.util.formatAmount
import com.example.appfireflyiii.util.verticalScrollColumn
import com.example.appfireflyiii.util.formatRelativeDate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.filled.ArrowForward
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.util.currentMonthLabel

@Composable
fun AccountDetailScreen(
    navController: NavController,
    viewModel: AccountDetailViewModel
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
            Text("Detalle de cuenta", style = MaterialTheme.typography.titleMedium)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is AccountDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AccountDetailUiState.Error -> {
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
                is AccountDetailUiState.Success -> {
                    AccountDetailContent(state.data, navController)
                }
            }
        }
    }
}

@Composable
fun AccountDetailContent(data: AccountDetailData, navController: NavController) {
    val attrs = data.account.attributes
    val balance = attrs.currentBalance.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScrollColumn()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(colors = listOf(CardGradientStart, CardGradientEnd)))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    attrs.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatAmount(balance, attrs.currencySymbol),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Evolución del mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                currentMonthLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Aproximado, reconstruido a partir de los movimientos del mes",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        BalanceLineChart(data.dailyBalance, attrs.currencySymbol)

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { navController.navigate(Screen.Transactions.route) }) {
                Text("Ver todos")
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (data.transactions.isEmpty()) {
            Text(
                "Sin movimientos este mes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                data.transactions.forEach { split ->
                    AccountTransactionRow(split)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // deja espacio para que la bottom nav no tape el último item
    }
}

@Composable
fun BalanceLineChart(values: List<Float>, currencySymbol: String?) {
    if (values.size < 2) return

    val lineColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val minValue = values.min()
    val maxValue = values.max()
    val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(values) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedIndex = nearestIndex(offset.x, canvasSize.width, values.size)
                        },
                        onDrag = { change, _ ->
                            selectedIndex = nearestIndex(change.position.x, canvasSize.width, values.size)
                        },
                        onDragEnd = { selectedIndex = null }
                    )
                }
                .pointerInput(values) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = nearestIndex(offset.x, canvasSize.width, values.size)
                            tryAwaitRelease()
                            selectedIndex = null
                        }
                    )
                }
        ) {
            canvasSize = size
            val stepX = size.width / (values.size - 1)

            val path = androidx.compose.ui.graphics.Path()
            values.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - ((value - minValue) / range) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 4f))

            selectedIndex?.let { idx ->
                val x = idx * stepX
                val y = size.height - ((values[idx] - minValue) / range) * size.height

                drawLine(
                    color = lineColor.copy(alpha = 0.4f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
                drawCircle(color = lineColor, radius = 8f, center = Offset(x, y))
                drawCircle(color = Color.White, radius = 3f, center = Offset(x, y))
            }
        }

        selectedIndex?.let { idx ->
            val stepX = if (canvasSize.width > 0) canvasSize.width / (values.size - 1) else 0f
            val xPx = idx * stepX
            val xDp = with(density) { xPx.toDp() }
            val tooltipOffsetX = (xDp - 55.dp).coerceAtLeast(0.dp)

            Box(
                modifier = Modifier
                    .offset(x = tooltipOffsetX, y = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        "Día ${idx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        formatAmount(values[idx].toDouble(), currencySymbol),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

private fun nearestIndex(touchX: Float, canvasWidth: Float, count: Int): Int {
    if (canvasWidth <= 0f || count < 2) return 0
    val stepX = canvasWidth / (count - 1)
    val index = (touchX / stepX).toInt()
    return index.coerceIn(0, count - 1)
}

@Composable
fun AccountTransactionRow(split: TransactionSplit) {
    val isExpense = split.type == "withdrawal"
    val amountColor = when (split.type) {
        "withdrawal" -> RedExpense
        "deposit" -> AssetColor
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when (split.type) {
        "withdrawal" -> "-"
        "deposit" -> "+"
        else -> ""
    }
    val amountValue = split.amount.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    split.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${split.categoryName ?: "Sin categoría"} · ${formatRelativeDate(split.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$prefix${formatAmount(amountValue, split.currencySymbol)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}