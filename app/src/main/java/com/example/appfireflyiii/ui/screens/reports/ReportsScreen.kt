package com.example.appfireflyiii.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfireflyiii.util.colorsForAccounts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Size as GeoSize
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.appfireflyiii.util.goldenAngleColors
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore

private val chartColors = listOf(
    Color(0xFF6750A4), Color(0xFF4CAF50), Color(0xFFFF9800),
    Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9E9E9E)
)

private val monthAbbrev = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeightAdaptivePager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    modifier: Modifier = Modifier,
    contentKey: Any? = null,
    pageContent: @Composable (Int) -> Unit
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val unboundedConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        val measuredHeight = subcompose("measure-${pagerState.currentPage}-$contentKey") {
            pageContent(pagerState.currentPage)
        }.map { it.measure(unboundedConstraints) }
            .maxOfOrNull { it.height } ?: 0

        val pagerConstraints = constraints.copy(minHeight = measuredHeight, maxHeight = measuredHeight)

        val pagerPlaceable = subcompose("pager") {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                pageContent(page)
            }
        }.map { it.measure(pagerConstraints) }

        layout(constraints.maxWidth, measuredHeight) {
            pagerPlaceable.forEach { it.placeRelative(0, 0) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ReportsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ReportsUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se pudo cargar: ${state.message}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadReports() }) {
                        Text("Reintentar")
                    }
                }
            }
            is ReportsUiState.Success -> {
                val categoriesPagerState = rememberPagerState(pageCount = { 2 })
                val tagsPagerState = rememberPagerState(pageCount = { 1 })
                val spendPagerState = rememberPagerState(pageCount = { 1 })
                var showEmptyTags by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    Text(
                        "Reportes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ReportPeriodControls(
                        periodLabel = state.periodLabel,
                        canGoForward = state.canGoForward,
                        onPrevious = { viewModel.previousPeriod() },
                        onNext = { viewModel.nextPeriod() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    AccountBalancesSection(state.data, state.periodType)
                    Spacer(modifier = Modifier.height(16.dp))

                    CarouselCard(pagerState = categoriesPagerState, pageCount = 2) { page ->
                        when (page) {
                            0 -> CategoryPieChartPage(state.data, state.periodType)
                            1 -> CategoryBreakdownPage(state.data, state.periodType)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CarouselCard(
                        pagerState = tagsPagerState,
                        pageCount = 1,
                        contentKey = showEmptyTags
                    ) { page ->
                        when (page) {
                            0 -> TagSpendPage(
                                state.data,
                                state.periodType,
                                showEmptyTags = showEmptyTags,
                                onToggleEmptyTags = { showEmptyTags = !showEmptyTags }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CarouselCard(pagerState = spendPagerState, pageCount = 1) { page ->
                        when (page) {
                            0 -> SpendChartPage(state.data, state.periodType)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPeriodControls(
    periodLabel: String,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
        }
        Text(
            periodLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Mes siguiente",
                tint = if (canGoForward) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun AccountBalancesSection(data: ReportsData, periodType: ReportPeriod) {
    val accountColors = remember(data.accountBalances) {
        colorsForAccounts(data.accountBalances.map { it.accountId }.sorted())
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Balances de cuentas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                if (periodType == ReportPeriod.MONTH) "Evolución diaria de este mes" else "Evolución mensual de este año",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.accountBalances.isEmpty()) {
                Text("No hay cuentas para mostrar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                MultiAccountBalanceChart(
                    series = data.accountBalances,
                    accountColors = accountColors,
                    currencySymbol = data.currencySymbol,
                    periodType = periodType
                )
            }
        }
    }
}

@Composable
fun MultiAccountBalanceChart(
    series: List<AccountBalanceSeries>,
    accountColors: Map<String, Color>,
    currencySymbol: String,
    periodType: ReportPeriod
) {
    val plottable = series.filter { it.values.size >= 2 }
    if (plottable.isEmpty()) return

    val density = LocalDensity.current
    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(GeoSize.Zero) }

    val pointCount = plottable.maxOf { it.values.size }
    val allValues = plottable.flatMap { it.values }
    val minValue = allValues.min()
    val maxValue = allValues.max()
    val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(plottable) {
                    detectDragGestures(
                        onDragStart = { offset -> selectedIndex = nearestChartIndex(offset.x, canvasSize.width, pointCount) },
                        onDrag = { change, _ -> selectedIndex = nearestChartIndex(change.position.x, canvasSize.width, pointCount) },
                        onDragEnd = { selectedIndex = null }
                    )
                }
                .pointerInput(plottable) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = nearestChartIndex(offset.x, canvasSize.width, pointCount)
                            tryAwaitRelease()
                            selectedIndex = null
                        }
                    )
                }
        ) {
            canvasSize = size
            val stepX = size.width / (pointCount - 1)

            plottable.forEach { s ->
                val color = accountColors[s.accountId] ?: Color.Gray
                val path = androidx.compose.ui.graphics.Path()
                s.values.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - ((value - minValue) / range) * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 3.5f))
            }

            if (minValue < 0f && maxValue > 0f) {
                val zeroY = size.height - ((0f - minValue) / range) * size.height
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(0f, zeroY),
                    end = Offset(size.width, zeroY),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }

            selectedIndex?.let { idx ->
                val x = idx * stepX
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
                plottable.forEach { s ->
                    if (idx < s.values.size) {
                        val y = size.height - ((s.values[idx] - minValue) / range) * size.height
                        val color = accountColors[s.accountId] ?: Color.Gray
                        drawCircle(color = color, radius = 6f, center = Offset(x, y))
                        drawCircle(color = Color.White, radius = 2f, center = Offset(x, y))
                    }
                }
            }
        }

        selectedIndex?.let { idx ->
            val stepX = if (canvasSize.width > 0) canvasSize.width / (pointCount - 1) else 0f
            val xPx = idx * stepX
            val xDp = with(density) { xPx.toDp() }
            val tooltipOffsetX = (xDp - 70.dp).coerceAtLeast(0.dp)

            Box(
                modifier = Modifier
                    .offset(x = tooltipOffsetX, y = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        chartIndexLabel(idx, periodType),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    plottable.forEach { s ->
                        if (idx < s.values.size) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(accountColors[s.accountId] ?: Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    s.accountName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 90.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "$currencySymbol${"%,.2f".format(s.values[idx])}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun nearestChartIndex(touchX: Float, canvasWidth: Float, count: Int): Int {
    if (canvasWidth <= 0f || count < 2) return 0
    val stepX = canvasWidth / (count - 1)
    return (touchX / stepX).toInt().coerceIn(0, count - 1)
}

private fun chartIndexLabel(index: Int, periodType: ReportPeriod): String {
    return if (periodType == ReportPeriod.MONTH) {
        "Día ${index + 1}"
    } else {
        monthAbbrev.getOrElse(index) { "" }
    }
}

@Composable
fun CategoryBreakdownPage(data: ReportsData, periodType: ReportPeriod) {
    val periodWord = if (periodType == ReportPeriod.MONTH) "mes" else "año"
    val colors = remember(data.categories.size) { goldenAngleColors(data.categories.size) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
    ) {
        Text("Gastos por categoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "Total del $periodWord · ${data.currencySymbol}${data.totalExpense.setScale(2)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (data.categories.isEmpty()) {
            Text("Sin gastos registrados en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        data.categories.forEachIndexed { index, category ->
            val color = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }
            val percentage = if (data.totalExpense.toFloat() > 0f)
                (category.amount.toFloat() / data.totalExpense.toFloat() * 100).let { "%.0f".format(it) }
            else "0"

            Column(modifier = Modifier.padding(bottom = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "${data.currencySymbol}${category.amount.setScale(2)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.fraction.coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun SpendChartPage(data: ReportsData, periodType: ReportPeriod) {
    val title = if (periodType == ReportPeriod.MONTH) "Gasto por día" else "Gasto por mes"
    val subtitle = if (periodType == ReportPeriod.MONTH) "Este mes" else "Este año"
    val unitWord = if (periodType == ReportPeriod.MONTH) "día" else "mes"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        val values = data.spendSeries
        if (values.size < 2) {
            Text("Sin datos suficientes para mostrar la gráfica.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }

        SpendLineChart(values = values, currencySymbol = data.currencySymbol, periodType = periodType)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (periodType == ReportPeriod.MONTH) {
                Text("1", style = MaterialTheme.typography.labelSmall)
                Text("${values.size}", style = MaterialTheme.typography.labelSmall)
            } else {
                Text("Ene", style = MaterialTheme.typography.labelSmall)
                Text("Dic", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val peakIndex = values.indexOf(values.maxOrNull() ?: 0f)
        val peakAmount = values.maxOrNull() ?: 0f
        if (peakAmount > 0f) {
            val peakLabel = if (periodType == ReportPeriod.MONTH) {
                "día ${peakIndex + 1}"
            } else {
                monthAbbrev.getOrElse(peakIndex) { "" }
            }
            Text(
                "${unitWord.replaceFirstChar { it.uppercase() }} con más gasto: $peakLabel (${data.currencySymbol}${"%.2f".format(peakAmount)})",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SpendLineChart(values: List<Float>, currencySymbol: String, periodType: ReportPeriod) {
    val lineColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(GeoSize.Zero) }

    val minValue = values.min().coerceAtMost(0f)
    val maxValue = values.max().takeIf { it > 0f } ?: 1f
    val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(values) {
                    detectDragGestures(
                        onDragStart = { offset -> selectedIndex = nearestChartIndex(offset.x, canvasSize.width, values.size) },
                        onDrag = { change, _ -> selectedIndex = nearestChartIndex(change.position.x, canvasSize.width, values.size) },
                        onDragEnd = { selectedIndex = null }
                    )
                }
                .pointerInput(values) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = nearestChartIndex(offset.x, canvasSize.width, values.size)
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
            drawPath(path = path, color = lineColor, style = Stroke(width = 3.5f))

            values.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - ((value - minValue) / range) * size.height
                drawCircle(color = lineColor, radius = 4f, center = Offset(x, y))
            }

            selectedIndex?.let { idx ->
                val x = idx * stepX
                val y = size.height - ((values[idx] - minValue) / range) * size.height
                drawLine(
                    color = lineColor.copy(alpha = 0.3f),
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
                        chartIndexLabel(idx, periodType),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "$currencySymbol${"%,.2f".format(values[idx])}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

private fun Modifier.verticalScrollColumn(): Modifier = composed {
    this.verticalScroll(rememberScrollState())
}

@Composable
fun CategoryPieChartPage(data: ReportsData, periodType: ReportPeriod) {
    val periodWord = if (periodType == ReportPeriod.MONTH) "mes" else "año"
    val colors = remember(data.categories.size) { goldenAngleColors(data.categories.size) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
    ) {
        Text("Gastos por categoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Total del $periodWord: ${data.currencySymbol}${data.totalExpense.setScale(2)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (data.categories.isEmpty()) {
            Text("Sin gastos registrados en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }

        val total = data.categories.sumOf { it.amount }.toFloat().takeIf { it > 0f } ?: 1f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(data.categories) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                            val minDim = minOf(size.width, size.height).toFloat()

                            if (distance > minDim / 2f) {
                                selectedIndex = null
                                return@detectTapGestures
                            }

                            var angle = Math.toDegrees(kotlin.math.atan2(dy, dx).toDouble()).toFloat() + 90f
                            if (angle < 0f) angle += 360f

                            var accumulated = 0f
                            for ((index, category) in data.categories.withIndex()) {
                                val sweep = (category.amount.toFloat() / total) * 360f
                                if (angle in accumulated..(accumulated + sweep)) {
                                    selectedIndex = index
                                    break
                                }
                                accumulated += sweep
                            }
                        }
                    }
            ) {
                var startAngle = -90f

                data.categories.forEachIndexed { index, category ->
                    val sweep = (category.amount.toFloat() / total) * 360f
                    val isSelected = selectedIndex == index
                    val inset = if (isSelected) 0f else size.minDimension * 0.03f

                    drawArc(
                        color = colors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(inset / 2f, inset / 2f),
                        size = androidx.compose.ui.geometry.Size(size.width - inset, size.height - inset)
                    )
                    startAngle += sweep
                }
            }

            selectedIndex?.let { idx ->
                val category = data.categories[idx]
                val percentage = (category.amount.toFloat() / total * 100).let { "%.0f".format(it) }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$percentage%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Text(
                            category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CarouselCard(
    pagerState: androidx.compose.foundation.pager.PagerState,
    pageCount: Int,
    contentKey: Any? = null,
    pageContent: @Composable (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .animateContentSize()
        ) {
            HeightAdaptivePager(pagerState = pagerState, contentKey = contentKey, pageContent = pageContent)

            if (pageCount > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pageCount) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (selected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagSpendPage(data: ReportsData, periodType: ReportPeriod, showEmptyTags: Boolean, onToggleEmptyTags: () -> Unit) {
    val periodWord = if (periodType == ReportPeriod.MONTH) "mes" else "año"
    val colors = remember(data.tagSpends.size) { goldenAngleColors(data.tagSpends.size) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
    ) {
        Text("Gastos por etiqueta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Ordenadas de mayor a menor · este $periodWord",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (data.tagSpends.isEmpty()) {
            Text("Sin gastos etiquetados en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                data.tagSpends.forEachIndexed { index, tag ->
                    val color = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                tag.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${data.currencySymbol}${tag.amount.setScale(2)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(tag.fraction.coerceIn(0.02f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }

        if (data.tagsWithoutSpend.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleEmptyTags() }
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Etiquetas sin gastos (${data.tagsWithoutSpend.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    if (showEmptyTags) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showEmptyTags) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.tagsWithoutSpend.forEach { tagName ->
                        Text(
                            tagName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}