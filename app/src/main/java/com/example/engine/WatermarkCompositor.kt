// © 2026 App Owner. All rights reserved.
package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import kotlin.math.roundToInt

object WatermarkCompositor {

    /**
     * Composites a logo bitmap onto the base bitmap using the normalized placement configuration.
     * Preserves pristine original vector/raster fidelity using subpixel matrix scaling and anti-aliasing.
     */
    fun composite(
        baseBitmap: Bitmap,
        logoBitmap: Bitmap,
        placement: LogoPlacement
    ): Bitmap {
        val result = if (baseBitmap.isMutable) {
            baseBitmap
        } else {
            baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val canvas = Canvas(result)
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            alpha = (placement.opacity.coerceIn(0.1f, 1.0f) * 255).roundToInt()
        }

        val logoAspect = if (logoBitmap.height > 0) {
            logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
        } else 1.0f

        val bounds = placement.calculatePixelBounds(
            imageWidth = result.width,
            imageHeight = result.height,
            logoAspectRatio = logoAspect
        )

        val srcRectF = RectF(0f, 0f, logoBitmap.width.toFloat(), logoBitmap.height.toFloat())
        val destRectF = RectF(bounds.left, bounds.top, bounds.right, bounds.bottom)

        val matrix = Matrix()
        matrix.setRectToRect(srcRectF, destRectF, Matrix.ScaleToFit.FILL)

        if (placement.flipHorizontal) {
            matrix.postScale(-1f, 1f, destRectF.centerX(), destRectF.centerY())
        }
        if (placement.rotationDegrees != 0f) {
            matrix.postRotate(placement.rotationDegrees, destRectF.centerX(), destRectF.centerY())
        }

        val drawLogo = if (placement.removeWhiteBg) {
            ImageUtils.removeWhiteBackground(logoBitmap)
        } else {
            logoBitmap
        }

        val refDim = minOf(result.width, result.height).coerceAtLeast(1)
        val resScale = (refDim / 1000f).coerceAtLeast(0.4f)

        // Draw Shadow Layer behind logo if enabled
        if (placement.shadowEnabled && placement.shadowRadius > 0f) {
            try {
                val shadowAlphaMask = drawLogo.extractAlpha()
                if (shadowAlphaMask != null) {
                    val scaledBlur = (placement.shadowRadius * resScale).coerceIn(1f, 80f)
                    val scaledDx = placement.shadowDx * resScale
                    val scaledDy = placement.shadowDy * resScale
                    val shadowPaint = Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                        color = placement.shadowColor.toInt()
                        alpha = ((placement.opacity * 0.75f).coerceIn(0.1f, 1.0f) * 255).roundToInt()
                        maskFilter = android.graphics.BlurMaskFilter(
                            scaledBlur,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    val shadowMatrix = Matrix(matrix).apply {
                        postTranslate(scaledDx, scaledDy)
                    }
                    canvas.drawBitmap(shadowAlphaMask, shadowMatrix, shadowPaint)
                    shadowAlphaMask.recycle()
                }
            } catch (_: Exception) {}
        }

        // Draw Logo Bitmap
        canvas.drawBitmap(drawLogo, matrix, paint)

        // Draw Text Watermark if specified
        if (placement.watermarkText.isNotBlank()) {
            val textSizePx = (refDim * placement.fontSizeFraction).coerceAtLeast(16f)

            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = textSizePx
                color = placement.textColor.toInt()
                alpha = (placement.opacity.coerceIn(0.1f, 1.0f) * 255).roundToInt()
                isFakeBoldText = placement.isTextBold
                textAlign = Paint.Align.CENTER
                if (placement.shadowEnabled) {
                    setShadowLayer(
                        (placement.shadowRadius * resScale).coerceIn(1f, 40f),
                        placement.shadowDx * resScale,
                        placement.shadowDy * resScale,
                        placement.shadowColor.toInt()
                    )
                }
            }

            val fm = textPaint.fontMetrics
            val maxSafeBaseline = result.height - fm.descent - (refDim * 0.008f).coerceAtLeast(2f)
            val desiredBaseline = destRectF.bottom + textSizePx * 0.95f
            val textY = desiredBaseline.coerceAtMost(maxSafeBaseline)

            // Also ensure textX does not run off horizontal edges for centered text
            val textHalfW = textPaint.measureText(placement.watermarkText) / 2f
            val padX = (refDim * 0.01f).coerceAtLeast(4f)
            val textX = destRectF.centerX().coerceIn(
                (textHalfW + padX).coerceAtMost(result.width / 2f),
                (result.width - textHalfW - padX).coerceAtLeast(result.width / 2f)
            )

            canvas.save()
            if (placement.rotationDegrees != 0f) {
                canvas.rotate(placement.rotationDegrees, destRectF.centerX(), destRectF.centerY())
            }
            canvas.drawText(placement.watermarkText, textX, textY, textPaint)
            canvas.restore()
        }

        if (drawLogo != logoBitmap) {
            drawLogo.recycle()
        }
        return result
    }

    /**
     * Generates a preview bitmap for responsive UI rendering (downscaled for fast rendering).
     */
    fun generatePreview(
        context: Context,
        photoUri: Uri,
        logoItem: LogoItem,
        placement: LogoPlacement,
        previewDimension: Int = 1200
    ): Bitmap? {
        val baseBmp = ImageUtils.loadDownscaledBitmap(context, photoUri, previewDimension) ?: return null
        val bounds = placement.calculatePixelBounds(
            imageWidth = baseBmp.width,
            imageHeight = baseBmp.height,
            logoAspectRatio = logoItem.aspectRatio
        )

        val targetLogoW = bounds.width().roundToInt().coerceAtLeast(10)
        val targetLogoH = bounds.height().roundToInt().coerceAtLeast(10)

        val logoBmp = ImageUtils.loadLogoBitmap(
            context = context,
            logoItem = logoItem,
            targetWidth = targetLogoW,
            targetHeight = targetLogoH
        ) ?: return baseBmp

        val composited = composite(baseBmp, logoBmp, placement)
        if (logoBmp != composited) {
            logoBmp.recycle()
        }
        return composited
    }
}
