// © 2026 App Owner. All rights reserved.
package com.example.model

import android.graphics.RectF

enum class PositionPreset(val displayName: String) {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    CENTER_LEFT("Center Left"),
    CENTER("Center"),
    CENTER_RIGHT("Center Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right"),
    CUSTOM("Custom")
}

data class LogoPlacement(
    val preset: PositionPreset = PositionPreset.BOTTOM_RIGHT,
    val normalizedX: Float = 0.78f,       // Relative left (allows negative for zero-padding / flush bleed)
    val normalizedY: Float = 0.78f,       // Relative top
    val normalizedWidth: Float = 0.18f,   // 18% default photo width
    val normalizedMargin: Float = 0.035f, // Default margin (allows down to 0.0f for absolute flush corner)
    val opacity: Float = 1.0f,            // 0.10 .. 1.0
    val removeWhiteBg: Boolean = false,   // Magic white-background removal filter!
    val adaptiveScale: Boolean = true,    // Equal visual proportion across 9:16 portrait and 16:9 landscape!
    val rotationDegrees: Float = 0f,      // -180f .. 180f rotation
    val flipHorizontal: Boolean = false,  // Horizontal mirror
    val shadowEnabled: Boolean = false,   // Drop shadow / glow
    val shadowRadius: Float = 10f,        // 0 .. 30f blur radius
    val shadowColor: Long = 0xAA000000,   // ARGB long color
    val shadowDx: Float = 3f,
    val shadowDy: Float = 3f,
    val watermarkText: String = "",       // Optional text overlay watermark (e.g. copyright, club name)
    val textColor: Long = 0xFFFFFFFF,     // Text color ARGB
    val isTextBold: Boolean = true,
    val fontSizeFraction: Float = 0.035f  // Font size relative to minOf(width, height)
) {

    /**
     * Accurately projects normalized logo placement onto real image pixels.
     * Uses adaptive short-edge reference scaling and corner anchoring so watermarks
     * maintain the exact same size and corner margins across 9:16, 16:9, and any aspect ratio.
     */
    fun calculatePixelBounds(
        imageWidth: Int,
        imageHeight: Int,
        logoAspectRatio: Float
    ): RectF {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return RectF(0f, 0f, 0f, 0f)
        }

        val safeAspect = if (logoAspectRatio > 0f) logoAspectRatio else 1.0f

        // Consistent reference dimension across mixed aspect ratios:
        // By scaling relative to the short edge min(width, height), a 1080p 9:16 portrait photo
        // and a 1080p 16:9 landscape photo yield the EXACT same watermark dimensions!
        val refDim = if (adaptiveScale) {
            minOf(imageWidth, imageHeight).toFloat()
        } else {
            imageWidth.toFloat()
        }

        var logoW = refDim * normalizedWidth.coerceIn(0.04f, 0.95f)
        var logoH = logoW / safeAspect

        // Ensure logo does not exceed 85% of photo height or width
        if (logoH > imageHeight * 0.85f) {
            logoH = imageHeight * 0.85f
            logoW = logoH * safeAspect
        }
        if (logoW > imageWidth * 0.85f) {
            logoW = imageWidth * 0.85f
            logoH = logoW / safeAspect
        }

        val margin = refDim * normalizedMargin.coerceAtLeast(0f)

        // Reserve bottom space for watermark text caption so it never clips off the bottom edge
        val textReservePx = if (watermarkText.isNotBlank()) {
            (refDim * fontSizeFraction).coerceAtLeast(16f) * 1.35f
        } else {
            0f
        }

        val left: Float
        val top: Float

        if (preset != PositionPreset.CUSTOM) {
            left = when (preset) {
                PositionPreset.TOP_LEFT,
                PositionPreset.CENTER_LEFT,
                PositionPreset.BOTTOM_LEFT -> margin

                PositionPreset.TOP_CENTER,
                PositionPreset.CENTER,
                PositionPreset.BOTTOM_CENTER -> (imageWidth - logoW) / 2f

                PositionPreset.TOP_RIGHT,
                PositionPreset.CENTER_RIGHT,
                PositionPreset.BOTTOM_RIGHT -> imageWidth - logoW - margin

                PositionPreset.CUSTOM -> 0f
            }

            top = when (preset) {
                PositionPreset.TOP_LEFT,
                PositionPreset.TOP_CENTER,
                PositionPreset.TOP_RIGHT -> margin

                PositionPreset.CENTER_LEFT,
                PositionPreset.CENTER,
                PositionPreset.CENTER_RIGHT -> (imageHeight - logoH) / 2f

                PositionPreset.BOTTOM_LEFT,
                PositionPreset.BOTTOM_CENTER,
                PositionPreset.BOTTOM_RIGHT -> imageHeight - logoH - margin - textReservePx

                PositionPreset.CUSTOM -> 0f
            }
        } else {
            // Unified continuous edge-aware mapping:
            // normalizedX = 0f -> flush left, 1f -> flush right (adapts smoothly across 9:16 & 16:9)
            // normalizedY = 0f -> flush top, 1f -> flush bottom (prevents falling off bottom on 16:9)
            val availableW = (imageWidth - logoW).coerceAtLeast(1f)
            val availableH = (imageHeight - logoH - textReservePx).coerceAtLeast(1f)
            left = normalizedX * availableW
            top = normalizedY * availableH
        }

        // Clamp strictly within safe photo bounds; auto-shift upward if text caption would overflow bottom
        val minLeft = -logoW * 0.15f
        val maxLeft = (imageWidth - logoW * 0.85f).coerceAtLeast(0f)
        val minTop = -logoH * 0.15f
        val bottomMarginPad = if (watermarkText.isNotBlank()) (refDim * 0.01f).coerceAtLeast(4f) else 0f
        val maxTop = if (watermarkText.isNotBlank()) {
            (imageHeight - logoH - textReservePx - bottomMarginPad).coerceAtLeast(0f)
        } else {
            (imageHeight - logoH * 0.85f).coerceAtLeast(0f)
        }

        val clampedLeft = left.coerceIn(minLeft, maxLeft)
        val clampedTop = top.coerceIn(minTop, maxTop)

        return RectF(clampedLeft, clampedTop, clampedLeft + logoW, clampedTop + logoH)
    }

    companion object {
        fun calculateCustomNormalizedPosition(
            leftPx: Float,
            topPx: Float,
            imageWidth: Float,
            imageHeight: Float,
            logoWidth: Float,
            logoHeight: Float,
            watermarkText: String = "",
            fontSizeFraction: Float = 0.035f
        ): Pair<Float, Float> {
            val refDim = minOf(imageWidth, imageHeight)
            val textReservePx = if (watermarkText.isNotBlank()) {
                (refDim * fontSizeFraction).coerceAtLeast(16f) * 1.35f
            } else {
                0f
            }
            val availableW = (imageWidth - logoWidth).coerceAtLeast(1f)
            val availableH = (imageHeight - logoHeight - textReservePx).coerceAtLeast(1f)
            val normX = (leftPx / availableW).coerceIn(-0.15f, 1.15f)
            val normY = (topPx / availableH).coerceIn(-0.15f, 1.15f)
            return Pair(normX, normY)
        }

        fun defaultPlacement(): LogoPlacement = LogoPlacement(
            preset = PositionPreset.BOTTOM_RIGHT,
            normalizedWidth = 0.18f,
            normalizedMargin = 0.035f,
            opacity = 1.0f
        )

        fun flushCornerPlacement(preset: PositionPreset = PositionPreset.BOTTOM_RIGHT): LogoPlacement = LogoPlacement(
            preset = preset,
            normalizedWidth = 0.18f,
            normalizedMargin = 0.0f,
            opacity = 1.0f
        )

        fun fromPreset(
            preset: PositionPreset,
            width: Float = 0.18f,
            margin: Float = 0.015f,
            opacity: Float = 1.0f
        ): LogoPlacement {
            return LogoPlacement(
                preset = preset,
                normalizedWidth = width,
                normalizedMargin = margin,
                opacity = opacity
            )
        }
    }
}
