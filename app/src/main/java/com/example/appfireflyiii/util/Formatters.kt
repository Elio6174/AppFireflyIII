package com.example.appfireflyiii.util

import java.util.Locale
import kotlin.math.abs
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

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
