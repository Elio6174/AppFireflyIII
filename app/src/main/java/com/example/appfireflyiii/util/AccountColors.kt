package com.example.appfireflyiii.util

import androidx.compose.ui.graphics.Color

fun colorsForAccounts(accountIds: List<String>): Map<String, Color> {
    val goldenAngle = 137.508f
    return accountIds.mapIndexed { index, id ->
        val hue = (index * goldenAngle) % 360f
        id to Color.hsv(hue = hue, saturation = 0.65f, value = 0.85f)
    }.toMap()
}

fun goldenAngleColors(count: Int, saturation: Float = 0.65f, value: Float = 0.85f): List<Color> {
    val goldenAngle = 137.508f
    return (0 until count).map { index ->
        val hue = (index * goldenAngle) % 360f
        hsvToColor(hue, saturation, value)
    }
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c

    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(red = r + m, green = g + m, blue = b + m)
}