package com.battledawn.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Red,
    onPrimary = OnSurface,
    primaryContainer = DarkRed,
    onPrimaryContainer = OnSurface,

    secondary = Blue,
    onSecondary = OnSurface,
    secondaryContainer = DarkBlue,
    onSecondaryContainer = OnSurface,

    tertiary = EnergyColor,
    onTertiary = DarkGray,
    tertiaryContainer = DarkBlue,
    onTertiaryContainer = OnSurface,

    error = DangerRed,
    onError = OnSurface,
    errorContainer = DarkRed,
    onErrorContainer = OnSurface,

    background = BackgroundDark,
    onBackground = OnBackground,

    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = MediumGray,
    onSurfaceVariant = OnBackground,

    outline = LightGray,
    outlineVariant = MediumGray
)

private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = OnSurface,
    primaryContainer = LightRed,
    onPrimaryContainer = DarkGray,

    secondary = Blue,
    onSecondary = OnSurface,
    secondaryContainer = LightBlue,
    onSecondaryContainer = DarkGray,

    tertiary = EnergyColor,
    onTertiary = DarkGray,
    tertiaryContainer = LightBlue,
    onTertiaryContainer = DarkGray,

    error = DangerRed,
    onError = OnSurface,
    errorContainer = LightRed,
    onErrorContainer = DarkGray,

    background = OnSurface,
    onBackground = DarkGray,

    surface = OnBackground,
    onSurface = DarkGray,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,

    outline = MediumGray,
    outlineVariant = LightGray
)

@Composable
fun BattleDawnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
