package com.example.appfireflyiii.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private val chartColors = listOf(
    Color(0xFF6750A4), Color(0xFF4CAF50), Color(0xFFFF9800),
    Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9E9E9E)
)

private val monthAbbrev = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

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
                val pagerState = rememberPagerState(pageCount = { 2 })

                Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                    ReportPeriodControls(
                        periodType = state.periodType,
                        periodLabel = state.periodLabel,
                        canGoForward = state.canGoForward,
                        onPeriodTypeChange = { viewModel.setPeriodType(it) },
                        onPrevious = { viewModel.previousPeriod() },
                        onNext = { viewModel.nextPeriod() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(2) { index ->
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

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> CategoryBreakdownPage(state.data, state.periodType)
                            1 -> SpendChartPage(state.data, state.periodType)
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
    periodType: ReportPeriod,
    periodLabel: String,
    canGoForward: Boolean,
    onPeriodTypeChange: (ReportPeriod) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = periodType == ReportPeriod.MONTH,
                onClick = { onPeriodTypeChange(ReportPeriod.MONTH) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Por mes") }
            SegmentedButton(
                selected = periodType == ReportPeriod.YEAR,
                onClick = { onPeriodTypeChange(ReportPeriod.YEAR) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Por año") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Anterior")
            }
            Text(
                periodLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onNext, enabled = canGoForward) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Siguiente",
                    tint = if (canGoForward) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownPage(data: ReportsData, periodType: ReportPeriod) {
    val periodWord = if (periodType == ReportPeriod.MONTH) "mes" else "año"

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Gastos por categoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Total del $periodWord: ${data.currencySymbol}${data.totalExpense.setScale(2)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (data.categories.isEmpty()) {
            Text("Sin gastos registrados en este período.")
        }

        data.categories.forEachIndexed { index, category ->
            val color = chartColors[index % chartColors.size]
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(category.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${data.currencySymbol}${category.amount.setScale(2)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.fraction.coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
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

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        val barColor = MaterialTheme.colorScheme.primary
        val values = data.spendSeries
        val maxValue = values.maxOrNull()?.takeIf { it > 0f } ?: 1f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (values.isEmpty()) return@Canvas

            val barCount = values.size
            val gap = 2.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount

            values.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * size.height
                val left = index * (barWidth + gap)
                val top = size.height - barHeight

                drawRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight)
                )
            }
        }

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