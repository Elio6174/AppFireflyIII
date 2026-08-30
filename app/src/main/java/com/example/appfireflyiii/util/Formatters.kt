package com.example.appfireflyiii.util

import java.util.Locale
import kotlin.math.abs
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun Modifier.verticalScrollColumn(): Modifier = composed {
    this.verticalScroll(rememberScrollState())
}

fun formatAmount(value: Double, symbol: String?): String {
    val sign = if (value < 0) "-" else ""
    val formatted = String.format(Locale.US, "%,.2f", abs(value))
    return "$sign${symbol ?: ""}$formatted"
}

fun formatAmount(value: String, symbol: String?): String {
    return formatAmount(value.toDoubleOrNull() ?: 0.0, symbol)
}

fun formatAccountNumber(number: String): String {
    return number.chunked(4).joinToString(" ")
}

fun formatRelativeDate(dateString: String): String {
    val datePart = dateString.take(10) // "yyyy-MM-dd"
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val today = Calendar.getInstance()
    val todayStr = format.format(today.time)

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_MONTH, -1)
    val yesterdayStr = format.format(yesterday.time)

    return when (datePart) {
        todayStr -> "Hoy"
        yesterdayStr -> "Ayer"
        else -> datePart
    }
}

fun currentMonthLabel(): String {
    val format = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
    val label = format.format(Date())
    return label.replaceFirstChar { it.uppercase() }
}