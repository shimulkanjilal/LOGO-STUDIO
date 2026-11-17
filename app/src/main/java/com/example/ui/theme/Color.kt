// © 2026 App Owner. All rights reserved.
package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Logo Studio Brand Identity Colors (Derived from Logo Studio Icon)
val NubccBlueTop = Color(0xFF070C22)
val NubccBlackBottom = Color(0xFF000316)
val NubccPurpleAccent = Color(0xFF3E1F7A)
val NubccElectricViolet = Color(0xFF8B5CF6)
val NubccCyan = Color(0xFF22D3EE)
val NubccAmber = Color(0xFFC084FC) // Neon Orchid-Lavender accent matching left rim glow

val StudioIndigo = Color(0xFF4F46E5)
val StudioTealCyan = Color(0xFF06B6D4)
val StudioCarbonTop = Color(0xE80D142C)
val StudioCarbonBottom = Color(0xE8060A18)

val NubccTextPrimary = Color(0xFFF8FAFC)
val NubccTextSecondary = Color(0xFFB4C0D9)
val NubccTextMuted = Color(0xFF7C8AB8)

// Dark Obsidian Carbon Surfaces & Neon Glassmorphism
val NubccSurfaceDark = Color(0xFF080D1E)
val NubccGlassSurface = Color(0xD90A1024)       // Deep obsidian carbon card fill
val NubccGlassSurfaceSubtle = Color(0x990C1329) // Subtle translucent carbon
val NubccGlassBorder = Color(0x706366F1)        // Luminous indigo-violet rim
val NubccGlassBorderSubtle = Color(0x2E94A3B8)

// Gradient Brushes matching the Logo Studio Icon
val NubccBackgroundGradient = Brush.verticalGradient(
    colors = listOf(NubccBlueTop, Color(0xFF03071B), NubccBlackBottom)
)

val NubccCardGradient = Brush.linearGradient(
    colors = listOf(StudioCarbonTop, StudioCarbonBottom)
)

val NubccAccentGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF9333EA), Color(0xFF6366F1), Color(0xFF06B6D4))
)

val StudioNeonBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x99A855F7),
        Color(0x446366F1),
        Color(0x9922D3EE)
    )
)

/**
 * Renders the Logo Studio ambient background: deep obsidian midnight base (#000316)
 * with subtle radial neon violet (top-left) and electric cyan (right/bottom) glow halos.
 */
fun Modifier.studioAuraBackground(): Modifier = this.drawBehind {
    drawRect(brush = NubccBackgroundGradient)

    val maxDim = maxOf(size.width, size.height)
    if (maxDim > 0f) {
        // Left-side violet neon halo (matching the left rim of the icon)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x2B9333EA),
                    Color(0x0D6366F1),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.12f, size.height * 0.14f),
                radius = maxDim * 0.58f
            ),
            center = Offset(size.width * 0.12f, size.height * 0.14f),
            radius = maxDim * 0.58f
        )

        // Right-side cyan neon halo (matching the right pen-nib glow of the icon)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x2422D3EE),
                    Color(0x0A06B6D4),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.88f, size.height * 0.36f),
                radius = maxDim * 0.52f
            ),
            center = Offset(size.width * 0.88f, size.height * 0.36f),
            radius = maxDim * 0.52f
        )
    }
}
