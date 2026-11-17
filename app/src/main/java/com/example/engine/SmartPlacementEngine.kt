// © 2026 App Owner. All rights reserved.
package com.example.engine

import android.graphics.Bitmap
import com.example.model.PositionPreset
import kotlin.math.abs

data class SmartPlacementResult(
    val recommendedPreset: PositionPreset,
    val cornerLuminance: Float, // 0..255
    val isDarkBackground: Boolean,
    val edgeDensity: Float,
    val cornerAnalyses: Map<PositionPreset, CornerMetrics>
)

data class CornerMetrics(
    val edgeDensity: Float,
    val averageLuminance: Float
)

object SmartPlacementEngine {

    /**
     * Analyzes corner regions of a photo bitmap to find the cleanest, least busy corner
     * with minimal visual clutter (lowest edge density).
     */
    fun analyzePlacement(bitmap: Bitmap): SmartPlacementResult {
        val width = bitmap.width
        val height = bitmap.height

        // Downsample if bitmap is large to make analysis instant (sub-millisecond)
        val sampleStep = (maxOf(width, height) / 300).coerceAtLeast(1)

        val cornerPresets = listOf(
            PositionPreset.BOTTOM_RIGHT,
            PositionPreset.BOTTOM_LEFT,
            PositionPreset.TOP_RIGHT,
            PositionPreset.TOP_LEFT
        )

        val metricsMap = mutableMapOf<PositionPreset, CornerMetrics>()

        for (preset in cornerPresets) {
            val (xStart, xEnd, yStart, yEnd) = when (preset) {
                PositionPreset.TOP_LEFT -> listOf(0, width / 3, 0, height / 3)
                PositionPreset.TOP_RIGHT -> listOf((width * 2) / 3, width - 1, 0, height / 3)
                PositionPreset.BOTTOM_LEFT -> listOf(0, width / 3, (height * 2) / 3, height - 1)
                PositionPreset.BOTTOM_RIGHT -> listOf((width * 2) / 3, width - 1, (height * 2) / 3, height - 1)
                else -> listOf(0, width, 0, height)
            }

            var totalGradient = 0L
            var totalLuminance = 0L
            var pixelCount = 0L

            for (y in yStart until yEnd - sampleStep step sampleStep) {
                for (x in xStart until xEnd - sampleStep step sampleStep) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel ushr 16) and 0xFF
                    val g = (pixel ushr 8) and 0xFF
                    val b = pixel and 0xFF
                    val lum = (0.299f * r + 0.587f * g + 0.114f * b)

                    // Right neighbor
                    val rightPixel = bitmap.getPixel(x + sampleStep, y)
                    val rightLum = 0.299f * ((rightPixel ushr 16) and 0xFF) +
                            0.587f * ((rightPixel ushr 8) and 0xFF) +
                            0.114f * (rightPixel and 0xFF)

                    // Bottom neighbor
                    val bottomPixel = bitmap.getPixel(x, y + sampleStep)
                    val bottomLum = 0.299f * ((bottomPixel ushr 16) and 0xFF) +
                            0.587f * ((bottomPixel ushr 8) and 0xFF) +
                            0.114f * (bottomPixel and 0xFF)

                    val grad = abs(lum - rightLum) + abs(lum - bottomLum)
                    totalGradient += grad.toLong()
                    totalLuminance += lum.toLong()
                    pixelCount++
                }
            }

            val count = pixelCount.coerceAtLeast(1)
            val avgEdge = totalGradient.toFloat() / count
            val avgLum = totalLuminance.toFloat() / count

            metricsMap[preset] = CornerMetrics(edgeDensity = avgEdge, averageLuminance = avgLum)
        }

        // Bias towards Bottom-Right slightly (standard club branding position)
        // If another corner is significantly less busy (e.g. 25% lower edge density), pick that one
        val sortedCorners = metricsMap.entries.sortedBy { entry ->
            val bias = if (entry.key == PositionPreset.BOTTOM_RIGHT) 0.85f else 1.0f
            entry.value.edgeDensity * bias
        }

        val bestCorner = sortedCorners.first()
        val bestPreset = bestCorner.key
        val bestMetrics = bestCorner.value
        val isDark = bestMetrics.averageLuminance < 115f

        return SmartPlacementResult(
            recommendedPreset = bestPreset,
            cornerLuminance = bestMetrics.averageLuminance,
            isDarkBackground = isDark,
            edgeDensity = bestMetrics.edgeDensity,
            cornerAnalyses = metricsMap
        )
    }
}
