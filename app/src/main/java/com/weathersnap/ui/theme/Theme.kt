package com.weathersnap.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Scheme = darkColorScheme(
    primary            = Amber,
    onPrimary          = Background,
    primaryContainer   = AmberDim,
    onPrimaryContainer = Amber,
    background         = Background,
    onBackground       = TextPrimary,
    surface            = Surface1,
    onSurface          = TextPrimary,
    surfaceVariant     = Surface2,
    onSurfaceVariant   = TextSecondary,
    outline            = Border,
    outlineVariant     = BorderLight,
    error              = ErrorColor,
    onError            = TextPrimary,
    errorContainer     = ErrorDim,
    onErrorContainer   = ErrorColor,
    scrim              = Color(0xCC000000),
)

@Composable
fun WeatherSnapTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = Scheme, typography = Typography, content = content)
}
