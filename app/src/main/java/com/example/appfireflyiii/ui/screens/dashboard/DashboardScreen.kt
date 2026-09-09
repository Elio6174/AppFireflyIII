package com.example.appfireflyiii.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfireflyiii.ui.theme.DetailScreenBg as ScreenBg
import androidx.navigation.NavController
import com.example.appfireflyiii.data.model.TransactionSplit
import com.example.appfireflyiii.navigation.Screen
import com.example.appfireflyiii.util.formatAmount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

import com.example.appfireflyiii.ui.theme.DetailCardBg as CardBg
import com.example.appfireflyiii.ui.theme.DetailCardBorder as CardBorder
import com.example.appfireflyiii.ui.theme.DetailIconBadgeBg as IconBadgeBg
import com.example.appfireflyiii.ui.theme.DetailLabelGray as LabelGray
import com.example.appfireflyiii.ui.theme.DetailSubLabelGray as SubLabelGray
import com.example.appfireflyiii.ui.theme.DetailWithdrawalColor as WithdrawalColor
import com.example.appfireflyiii.ui.theme.DetailDepositColor as DepositColor

private val ChartColor1 = Color(0xFFFFA726)
private val ChartColor2 = Color(0xFF42A5F5)
private val ChartColor3 = Color(0xFF26A69A)
private val ChartColor4 = Color(0xFFEC407A)
private val chartPalette = listOf(ChartColor1, ChartColor2, ChartColor3, ChartColor4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is DashboardUiState.Loading) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadDashboard()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                if (!isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No se pudo cargar: ${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDashboard() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(state.data, navController)
            }
        }
    }
}

@Composable
fun DashboardContent(data: DashboardData, navController: NavController) {
    var balanceHidden by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "BIENVENIDO",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = SubLabelGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Hola de nuevo",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IconBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(20.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(WithdrawalColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NetWorthCard(
                    netWorth = data.netWorth.toDouble(),
                    monthlyIncome = data.monthlyIncome.toDouble(),
                    monthlyExpense = data.monthlyExpense.toDouble(),
                    currencySymbol = data.currencySymbol,
                    hidden = balanceHidden,
                    onToggleHidden = { balanceHidden = !balanceHidden },
                    onIncomeClick = { navController.navigate(Screen.Transactions.createRoute("deposit")) },
                    onExpenseClick = { navController.navigate(Screen.Transactions.createRoute("withdrawal")) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickAction(
                        label = "Nueva",
                        icon = Icons.Filled.Add,
                        onClick = { navController.navigate(Screen.NewTransaction.route) }
                    )
                    QuickAction(
                        label = "Cuentas",
                        icon = Icons.Filled.AccountBalanceWallet,
                        onClick = { navController.navigate(Screen.Accounts.route) }
                    )
                    QuickAction(
                        label = "Movimientos",
                        icon = Icons.Filled.Receipt,
                        onClick = { navController.navigate(Screen.Transactions.createRoute()) }
                    )
                    QuickAction(
                        label = "Reportes",
                        icon = Icons.Filled.BarChart,
                        onClick = { navController.navigate(Screen.Reports.route) }
                    )
                }

                if (data.balanceHistory != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    BalanceHistoryCard(history = data.balanceHistory)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Movimientos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(Screen.Transactions.createRoute()) }
                    ) {
                        Text("Ver todos", style = MaterialTheme.typography.bodyMedium, color = SubLabelGray)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = SubLabelGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (data.recentTransactions.isEmpty()) {
                Text(
                    "No hay movimientos este mes.",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = SubLabelGray
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    data.recentTransactions.forEach { split ->
                        RecentTransactionRow(split) {
                            val groupId = split.groupId ?: return@RecentTransactionRow
                            val journalId = split.journalId ?: return@RecentTransactionRow
                            navController.navigate(Screen.TransactionDetail.createRoute(groupId, journalId))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NetWorthCard(
    netWorth: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    currencySymbol: String,
    hidden: Boolean,
    onToggleHidden: () -> Unit,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PATRIMONIO NETO",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = LabelGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(IconBadgeBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("MXN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleHidden, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.RemoveRedEye,
                    contentDescription = "Mostrar/ocultar balance",
                    tint = SubLabelGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (hidden) "••••••" else formatSplitAmount(netWorth, currencySymbol),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NetWorthPill(
                icon = "↑",
                iconBg = DepositColor.copy(alpha = 0.16f),
                iconColor = DepositColor,
                label = "Ingresos",
                value = if (hidden) "••••" else "+${formatWithoutMx(monthlyIncome, currencySymbol)}",
                modifier = Modifier.weight(1f),
                onClick = onIncomeClick
            )
            NetWorthPill(
                icon = "↓",
                iconBg = WithdrawalColor.copy(alpha = 0.16f),
                iconColor = WithdrawalColor,
                label = "Gastos",
                value = if (hidden) "••••" else "-${formatWithoutMx(monthlyExpense, currencySymbol)}",
                modifier = Modifier.weight(1f),
                onClick = onExpenseClick
            )
        }
    }
}

private fun formatWithoutMx(amount: Double, currencySymbol: String): String {
    return formatAmount(amount, currencySymbol).replace("MX", "")
}

@Composable
private fun NetWorthPill(
    icon: String,
    iconBg: Color,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = iconColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SubLabelGray, maxLines = 1)
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = SubLabelGray, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BalanceHistoryCard(history: BalanceHistory) {
    var touchIndex by remember { mutableStateOf<Int?>(null) }
    var chartWidthPx by remember { mutableStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("Balances de cuentas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Evolución diaria de este mes", style = MaterialTheme.typography.bodySmall, color = SubLabelGray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(IconBadgeBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("${history.dayLabels.size} DÍAS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Balance actual", style = MaterialTheme.typography.bodySmall, color = SubLabelGray)
                Text(
                    formatSplitAmount(history.totalCurrent, history.currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            val isPositive = history.percentChange >= 0
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background((if (isPositive) DepositColor else WithdrawalColor).copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    "${if (isPositive) "↑" else "↓"} ${String.format(Locale.US, "%.1f", kotlin.math.abs(history.percentChange))}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) DepositColor else WithdrawalColor
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (history.series.isNotEmpty() && history.dayLabels.size > 1) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                BalanceLineChart(
                    history = history,
                    highlightIndex = touchIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { chartWidthPx = it.size.width.toFloat() }
                        .pointerInput(history.dayLabels.size) {
                            val pointCount = history.dayLabels.size
                            fun updateIndex(x: Float) {
                                if (chartWidthPx <= 0f || pointCount < 2) return
                                val stepX = chartWidthPx / (pointCount - 1)
                                val index = (x / stepX).roundToInt().coerceIn(0, pointCount - 1)
                                touchIndex = index
                            }
                            detectDragGestures(
                                onDragStart = { offset -> updateIndex(offset.x) },
                                onDrag = { change, _ -> updateIndex(change.position.x) },
                                onDragEnd = { touchIndex = null },
                                onDragCancel = { touchIndex = null }
                            )
                        }
                        .pointerInput(history.dayLabels.size) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val pointCount = history.dayLabels.size
                                    if (chartWidthPx > 0f && pointCount >= 2) {
                                        val stepX = chartWidthPx / (pointCount - 1)
                                        val index = (offset.x / stepX).roundToInt().coerceIn(0, pointCount - 1)
                                        touchIndex = index
                                    }
                                    tryAwaitRelease()
                                    touchIndex = null
                                }
                            )
                        }
                )

                touchIndex?.let { index ->
                    val pointCount = history.dayLabels.size
                    if (pointCount >= 2 && chartWidthPx > 0f) {
                        val stepXPx = chartWidthPx / (pointCount - 1)
                        val xPx = stepXPx * index
                        val xDp = with(density) { xPx.toDp() }
                        val maxOffsetX = with(density) { chartWidthPx.toDp() } - 140.dp

                        BalanceTooltip(
                            history = history,
                            dayIndex = index,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (xDp - 70.dp).coerceIn(0.dp, maxOffsetX.coerceAtLeast(0.dp)),
                                    y = (-12).dp
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                history.dayLabels.forEach { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SubLabelGray
                    )
                }
            }
        } else {
            Text(
                "Aún no hay suficientes datos este mes.",
                style = MaterialTheme.typography.bodySmall,
                color = SubLabelGray
            )
        }
    }
}

@Composable
private fun BalanceTooltip(history: BalanceHistory, dayIndex: Int, modifier: Modifier = Modifier) {
    val total = history.series.sumOf { it.values.getOrElse(dayIndex) { 0.0 } }

    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.9f))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            history.dayLabels[dayIndex],
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SubLabelGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            formatSplitAmount(total, history.currencySymbol),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (history.series.size > 1) {
            Spacer(modifier = Modifier.height(6.dp))
            history.series.forEachIndexed { seriesIndex, series ->
                val value = series.values.getOrElse(dayIndex) { 0.0 }
                val color = chartPalette[seriesIndex % chartPalette.size]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        formatSplitAmount(value, history.currencySymbol),
                        style = MaterialTheme.typography.labelSmall,
                        color = SubLabelGray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceLineChart(history: BalanceHistory, highlightIndex: Int?, modifier: Modifier = Modifier) {
    val allValues = history.series.flatMap { it.values }
    val minValue = allValues.minOrNull() ?: 0.0
    val maxValue = allValues.maxOrNull() ?: 1.0
    val range = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val pointCount = history.dayLabels.size
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        listOf(0.25f, 0.5f, 0.75f).forEach { fraction ->
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(0f, height * fraction),
                end = Offset(width, height * fraction),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        val stepX = width / (pointCount - 1).coerceAtLeast(1)
        val todayX = (pointCount - 1) * stepX

        drawLine(
            color = WithdrawalColor,
            start = Offset(todayX, 0f),
            end = Offset(todayX, height),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )

        if (highlightIndex != null) {
            val hx = highlightIndex * stepX
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(hx, 0f),
                end = Offset(hx, height),
                strokeWidth = 1.5f
            )
        }

        history.series.forEachIndexed { seriesIndex, series ->
            if (series.values.size < 2) return@forEachIndexed
            val color = chartPalette[seriesIndex % chartPalette.size]

            val points = series.values.mapIndexed { i, value ->
                val x = i * stepX
                val normalized = ((value - minValue) / range).toFloat()
                val y = height - (normalized * height)
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            val lastPoint = points.last()
            drawCircle(color = color, radius = 5f, center = lastPoint)
            drawCircle(color = Color.White, radius = 2f, center = lastPoint)

            if (highlightIndex != null && highlightIndex in points.indices) {
                val hPoint = points[highlightIndex]
                drawCircle(color = Color.White, radius = 6f, center = hPoint)
                drawCircle(color = color, radius = 4f, center = hPoint)
            }
        }
    }
}

@Composable
fun RecentTransactionRow(split: TransactionSplit, onClick: () -> Unit = {}) {
    val isExpense = split.type == "withdrawal"
    val amountColor = if (isExpense) WithdrawalColor else DepositColor
    val prefix = if (isExpense) "-" else "+"
    val amountValue = split.amount.toDoubleOrNull() ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(split.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                formatRelativeDateTime(split.date),
                style = MaterialTheme.typography.bodySmall,
                color = SubLabelGray
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 110.dp)
        ) {
            if (!split.categoryName.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(IconBadgeBg)
                        .widthIn(max = 110.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        split.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = SubLabelGray,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                "$prefix${formatAmount(amountValue, split.currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                textAlign = TextAlign.End
            )
        }
    }
}

private fun formatSplitAmount(value: Double, currencySymbol: String): String {
    return formatAmount(value, currencySymbol)
}

private fun formatRelativeDateTime(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val parsed = parser.parse(isoDate) ?: return isoDate.take(10)

        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = parsed }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = timeFormat.format(parsed)

        val isSameDay = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        when {
            isSameDay -> "Hoy, $time"
            isYesterday -> "Ayer, $time"
            else -> {
                val dateFormat = SimpleDateFormat("d MMM", Locale("es", "MX"))
                "${dateFormat.format(parsed)}, $time"
            }
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}