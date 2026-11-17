// © 2026 App Owner. All rights reserved.
package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioColorScheme = darkColorScheme(
    primary = NubccElectricViolet,
    onPrimary = NubccTextPrimary,
    primaryContainer = NubccPurpleAccent,
    onPrimaryContainer = NubccTextPrimary,
    secondary = NubccCyan,
    onSecondary = NubccBlackBottom,
    secondaryContainer = Color(0x2E22D3EE),
    onSecondaryContainer = NubccTextPrimary,
    tertiary = NubccAmber,
    onTertiary = NubccBlackBottom,
    background = NubccBlackBottom,
    onBackground = NubccTextPrimary,
    surface = NubccSurfaceDark,
    onSurface = NubccTextPrimary,
    surfaceVariant = Color(0xFF0E152B),
    onSurfaceVariant = NubccTextSecondary,
    outline = NubccGlassBorder,
    outlineVariant = NubccGlassBorderSubtle
)

@Composable
fun NubccTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
