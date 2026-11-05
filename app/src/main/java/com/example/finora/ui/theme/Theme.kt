package com.example.finora.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { true }

private val FinoraDarkColorScheme = darkColorScheme(
    primary = FinoraPrimary,
    onPrimary = Color.White,
    primaryContainer = FinoraPrimaryContainer,
    onPrimaryContainer = FinoraPrimaryLight,
    secondary = FinoraSecondary,
    onSecondary = Color.White,
    secondaryContainer = FinoraSecondaryContainer,
    onSecondaryContainer = FinoraSecondary,
    tertiary = FinoraIncome,
    onTertiary = Color.White,
    background = FinoraDarkBackground,
    onBackground = FinoraDarkTextPrimary,
    surface = FinoraDarkSurface,
    onSurface = FinoraDarkTextPrimary,
    surfaceVariant = FinoraDarkSurfaceVariant,
    onSurfaceVariant = FinoraDarkTextSecondary,
    outline = FinoraDarkBorder,
    outlineVariant = Color(0xFF1E2540),
    error = FinoraExpense,
    onError = Color.White,
    errorContainer = FinoraExpenseContainer,
    onErrorContainer = FinoraExpense,
    inverseSurface = FinoraLightSurface,
    inverseOnSurface = FinoraLightTextPrimary,
    inversePrimary = FinoraPrimaryDark,
    surfaceTint = FinoraPrimary,
    scrim = Color(0xFF000000)
)

private val FinoraLightColorScheme = lightColorScheme(
    primary = FinoraPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = FinoraPrimaryContainerLight,
    onPrimaryContainer = FinoraPrimaryDark,
    secondary = FinoraSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = FinoraSecondaryContainerLight,
    onSecondaryContainer = FinoraSecondaryDark,
    tertiary = FinoraIncomeDark,
    onTertiary = Color.White,
    background = FinoraLightBackground,
    onBackground = FinoraLightTextPrimary,
    surface = FinoraLightSurface,
    onSurface = FinoraLightTextPrimary,
    surfaceVariant = FinoraLightSurfaceVariant,
    onSurfaceVariant = FinoraLightTextSecondary,
    outline = FinoraLightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = FinoraExpenseDark,
    onError = Color.White,
    errorContainer = FinoraExpenseContainerLight,
    onErrorContainer = FinoraExpenseDark,
    inverseSurface = FinoraDarkSurface,
    inverseOnSurface = FinoraDarkTextPrimary,
    inversePrimary = FinoraPrimaryLight,
    surfaceTint = FinoraPrimaryDark,
    scrim = Color(0xFF000000)
)

@Composable
fun FinoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FinoraDarkColorScheme else FinoraLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinoraTypography,
            content = content
        )
    }
}

// Extended color accessors
object FinoraColors {
    @Composable
    fun isDark() = LocalIsDarkTheme.current

    @Composable
    fun income() = if (LocalIsDarkTheme.current) FinoraIncome else FinoraIncomeDark

    @Composable
    fun expense() = if (LocalIsDarkTheme.current) FinoraExpense else FinoraExpenseDark

    @Composable
    fun warning() = if (LocalIsDarkTheme.current) FinoraWarning else FinoraWarningDark

    @Composable
    fun incomeContainer() = if (LocalIsDarkTheme.current) FinoraIncomeContainer else FinoraIncomeContainerLight

    @Composable
    fun expenseContainer() = if (LocalIsDarkTheme.current) FinoraExpenseContainer else FinoraExpenseContainerLight

    @Composable
    fun warningContainer() = if (LocalIsDarkTheme.current) FinoraWarningContainer else FinoraWarningContainerLight

    @Composable
    fun surfaceElevated() = if (LocalIsDarkTheme.current) FinoraDarkSurfaceElevated else FinoraLightSurfaceElevated

    @Composable
    fun textTertiary() = if (LocalIsDarkTheme.current) FinoraDarkTextTertiary else FinoraLightTextTertiary

    @Composable
    fun cardGradient() = if (LocalIsDarkTheme.current) FinoraCardGradient else FinoraCardGradientLight
}

internal fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
