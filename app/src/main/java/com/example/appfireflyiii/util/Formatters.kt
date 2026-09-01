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

fun formatShortDate(dateString: String): String {
    val datePart = dateString.take(10) // "yyyy-MM-dd"
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))

    val date = inputFormat.parse(datePart) ?: return datePart
    val formatted = outputFormat.format(date)

    // El locale es-MX agrega un punto tras el mes abreviado (ej. "15 ago. 2026"); lo quitamos
    // y ponemos la primera letra del mes en mayúscula (ej. "15 Ago 2026").
    return formatted.replace(".", "")
        .split(" ")
        .joinToString(" ") { part ->
            if (part.length == 3 && part[0].isLetter()) part.replaceFirstChar { it.uppercase() } else part
        }
}

fun currentMonthLabel(): String {
    val format = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
    val label = format.format(Date())
    return label.replaceFirstChar { it.uppercase() }
}