package com.example.appfireflyiii.util

import java.util.Locale
import kotlin.math.abs

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
