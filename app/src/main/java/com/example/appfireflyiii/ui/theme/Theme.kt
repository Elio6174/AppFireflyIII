package com.example.appfireflyiii.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val DarkColors = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = CharcoalSurfaceHigh,
    background = CharcoalBackground,
    surface = CharcoalSurface,
    surfaceContainerHigh = CharcoalSurfaceHigh,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = RedExpense
)

private val LightColors = lightColorScheme(
    primary = AccentPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = RedExpenseLight
)

@Composable
fun AppFireflyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}