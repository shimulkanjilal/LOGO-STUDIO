package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.ImageUtils
import com.example.model.ActiveLogoLayer
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import com.example.model.PositionPreset
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccGlassBorder
import kotlin.math.roundToInt

@Composable
fun InteractiveMultiWatermarkCanvas(
    baseBitmap: Bitmap?,
    layers: List<ActiveLogoLayer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onPlacementChange: (layerId: String, newPlacement: LogoPlacement) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onDragStart: (() -> Unit)? = null
) {
    if (baseBitmap == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .size(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1AFFFFFF))
                .border(1.dp, NubccGlassBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No Photo Loaded", color = Color(0x88FFFFFF), fontSize = 14.sp)
        }
        return
    }

    val photoAspect = if (baseBitmap.height > 0) {
        baseBitmap.width.toFloat() / baseBitmap.height.toFloat()
    } else 1.0f

    // Keep latest references for pointerInput without restarting gestures
    val currentLayers = rememberUpdatedState(layers)
    val currentSelectedLayerId = rememberUpdatedState(selectedLayerId)
    val currentOnSelectLayer = rememberUpdatedState(onSelectLayer)
    val currentOnPlacementChange = rememberUpdatedState(onPlacementChange)
    val currentOnDragStart = rememberUpdatedState(onDragStart)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070D1A))
            .border(1.dp, Color(0x407C3AED), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availableW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val maxCanvasH = availableW * 2.2f

        // Calculate fitting canvas size preserving photo aspect ratio (including ultra-tall images)
        var canvasW = availableW
        var canvasH = canvasW / photoAspect
        if (canvasH > maxCanvasH) {
            canvasH = maxCanvasH
            canvasW = (canvasH * photoAspect).coerceAtLeast(120f)
        } else if (canvasH < 160f) {
            canvasH = 160f
        }

        val canvasWDp = with(density) { canvasW.toDp() }
        val canvasHDp = with(density) { canvasH.toDp() }

        Box(
            modifier = Modifier
                .size(width = canvasWDp, height = canvasHDp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (isInteractive) {
                        Modifier.pointerInput(canvasW, canvasH, isInteractive) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val downPos = down.position
                                val layersList = currentLayers.value

                                // Hit test layers in reverse order (topmost layer on top)
                                var targetLayer: ActiveLogoLayer? = null
                                val hitPaddingPx = 24.dp.toPx()

                                for (layer in layersList.asReversed()) {
                                    val aspect = if (layer.logo.aspectRatio > 0f) layer.logo.aspectRatio else 1.0f
                                    val bounds = layer.placement.calculatePixelBounds(
                                        imageWidth = canvasW.roundToInt(),
                                        imageHeight = canvasH.roundToInt(),
                                        logoAspectRatio = aspect
                                    )

                                    if (downPos.x >= (bounds.left - hitPaddingPx) &&
                                        downPos.x <= (bounds.right + hitPaddingPx) &&
                                        downPos.y >= (bounds.top - hitPaddingPx) &&
                                        downPos.y <= (bounds.bottom + hitPaddingPx)
                                    ) {
                                        targetLayer = layer
                                        break
                                    }
                                }

                                // If touch is outside all layers but a layer is currently selected,
                                // check if the touch was within reasonable proximity to the selected layer
                                if (targetLayer == null && currentSelectedLayerId.value != null) {
                                    val selected = layersList.find { it.id == currentSelectedLayerId.value }
                                    if (selected != null) {
                                        val aspect = if (selected.logo.aspectRatio > 0f) selected.logo.aspectRatio else 1.0f
                                        val bounds = selected.placement.calculatePixelBounds(
                                            imageWidth = canvasW.roundToInt(),
                                            imageHeight = canvasH.roundToInt(),
                                            logoAspectRatio = aspect
                                        )
                                        if (downPos.x >= (bounds.left - hitPaddingPx * 1.5f) &&
                                            downPos.x <= (bounds.right + hitPaddingPx * 1.5f) &&
                                            downPos.y >= (bounds.top - hitPaddingPx * 1.5f) &&
                                            downPos.y <= (bounds.bottom + hitPaddingPx * 1.5f)
                                        ) {
                                            targetLayer = selected
                                        }
                                    }
                                }

                                if (targetLayer == null) {
                                    // Touch is on base photo; do not consume so parent vertical scroll can work
                                    return@awaitEachGesture
                                }

                                // Consume down event so parent vertical scroll does not steal this gesture
                                down.consume()

                                val activeLayerId = targetLayer.id
                                if (currentSelectedLayerId.value != activeLayerId) {
                                    currentOnSelectLayer.value(activeLayerId)
                                }

                                var hasStartedDrag = false
                                var lastPlacement = targetLayer.placement
                                val logoAspect = if (targetLayer.logo.aspectRatio > 0f) targetLayer.logo.aspectRatio else 1.0f

                                var currentBounds = lastPlacement.calculatePixelBounds(
                                    imageWidth = canvasW.roundToInt(),
                                    imageHeight = canvasH.roundToInt(),
                                    logoAspectRatio = logoAspect
                                )

                                do {
                                    val event = awaitPointerEvent()
                                    val canceled = event.changes.any { it.isConsumed }
                                    if (canceled) break

                                    val activePointers = event.changes.filter { it.pressed }
                                    if (activePointers.isEmpty()) break

                                    val panChange = event.calculatePan()
                                    val zoomChange = event.calculateZoom()
                                    val rotationChange = event.calculateRotation()

                                    if (panChange != Offset.Zero || zoomChange != 1f || rotationChange != 0f) {
                                        if (!hasStartedDrag) {
                                            hasStartedDrag = true
                                            currentOnDragStart.value?.invoke()
                                        }

                                        // Consume pointer changes to keep smooth 60fps/120fps gesture stream
                                        event.changes.forEach { it.consume() }

                                        // Smooth zoom scaling relative to short edge
                                        val refDim = minOf(canvasW, canvasH)
                                        val newWidth = (lastPlacement.normalizedWidth * zoomChange).coerceIn(0.04f, 0.85f)
                                        var newRotation = lastPlacement.rotationDegrees + rotationChange
                                        if (newRotation > 180f) newRotation -= 360f
                                        if (newRotation < -180f) newRotation += 360f
                                        newRotation = newRotation.coerceIn(-180f, 180f)

                                        var newLogoW = refDim * newWidth
                                        var newLogoH = newLogoW / logoAspect

                                        if (newLogoH > canvasH * 0.85f) {
                                            newLogoH = canvasH * 0.85f
                                            newLogoW = newLogoH * logoAspect
                                        }
                                        if (newLogoW > canvasW * 0.85f) {
                                            newLogoW = canvasW * 0.85f
                                            newLogoH = newLogoW / logoAspect
                                        }

                                        val textReservePx = if (lastPlacement.watermarkText.isNotBlank()) {
                                            (refDim * lastPlacement.fontSizeFraction).coerceAtLeast(16f) * 1.35f
                                        } else {
                                            0f
                                        }
                                        val bottomPad = if (lastPlacement.watermarkText.isNotBlank()) (refDim * 0.01f).coerceAtLeast(4f) else 0f

                                        val minLeft = -newLogoW * 0.15f
                                        val maxLeft = (canvasW - newLogoW * 0.85f).coerceAtLeast(0f)
                                        val minTop = -newLogoH * 0.15f
                                        val maxTop = if (lastPlacement.watermarkText.isNotBlank()) {
                                            (canvasH - newLogoH - textReservePx - bottomPad).coerceAtLeast(0f)
                                        } else {
                                            (canvasH - newLogoH * 0.85f).coerceAtLeast(0f)
                                        }

                                        val newLeft = (currentBounds.left + panChange.x).coerceIn(minLeft, maxLeft)
                                        val newTop = (currentBounds.top + panChange.y).coerceIn(minTop, maxTop)

                                        val (normX, normY) = LogoPlacement.calculateCustomNormalizedPosition(
                                            leftPx = newLeft,
                                            topPx = newTop,
                                            imageWidth = canvasW,
                                            imageHeight = canvasH,
                                            logoWidth = newLogoW,
                                            logoHeight = newLogoH,
                                            watermarkText = lastPlacement.watermarkText,
                                            fontSizeFraction = lastPlacement.fontSizeFraction
                                        )

                                        val newPlacement = lastPlacement.copy(
                                            preset = PositionPreset.CUSTOM,
                                            normalizedX = normX,
                                            normalizedY = normY,
                                            normalizedWidth = newWidth,
                                            rotationDegrees = newRotation
                                        )

                                        lastPlacement = newPlacement
                                        currentBounds = RectF(newLeft, newTop, newLeft + newLogoW, newTop + newLogoH)
                                        currentOnPlacementChange.value(activeLayerId, newPlacement)
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    } else Modifier
                )
        ) {
            // 1. Base Photo
            Image(
                bitmap = baseBitmap.asImageBitmap(),
                contentDescription = "Photo canvas",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // 2. Render all active logo layers
            val context = LocalContext.current
            layers.forEach { layer ->
                val isSelected = (layer.id == selectedLayerId)
                val placement = layer.placement
                val logoItem = layer.logo

                // High-fidelity bitmap loading with automatic white-background removal
                val layerBitmap = remember(logoItem.uri, logoItem.drawableResId, placement.removeWhiteBg) {
                    var bmp = ImageUtils.loadLogoBitmap(
                        context = context,
                        logoItem = logoItem,
                        targetWidth = 1000,
                        targetHeight = 1000
                    )
                    if (bmp != null && placement.removeWhiteBg) {
                        val transparent = ImageUtils.removeWhiteBackground(bmp)
                        if (transparent != bmp) {
                            bmp.recycle()
                            bmp = transparent
                        }
                    }
                    bmp
                }

                // True aspect ratio directly derived from actual pixel dimensions
                val logoAspect = if (layerBitmap != null && layerBitmap.height > 0) {
                    layerBitmap.width.toFloat() / layerBitmap.height.toFloat()
                } else if (logoItem.aspectRatio > 0f) {
                    logoItem.aspectRatio
                } else 1.0f

                val bounds = placement.calculatePixelBounds(
                    imageWidth = canvasW.roundToInt(),
                    imageHeight = canvasH.roundToInt(),
                    logoAspectRatio = logoAspect
                )

                val logoWDp = with(density) { bounds.width().toDp() }
                val logoHDp = with(density) { bounds.height().toDp() }
                val logoOffsetX = with(density) { bounds.left.toDp() }
                val logoOffsetY = with(density) { bounds.top.toDp() }

                val canvasRefDim = minOf(canvasW, canvasH)
                val resScale = (canvasRefDim / 1000f).coerceAtLeast(0.35f)
                val shadowDxDp = with(density) { (placement.shadowDx * resScale).toDp() }
                val shadowDyDp = with(density) { (placement.shadowDy * resScale).toDp() }
                val previewTextSizePx = (canvasRefDim * placement.fontSizeFraction).coerceAtLeast(10f)
                val previewTextSp = with(density) { previewTextSizePx.toSp() }
                val textOffsetYDp = with(density) { (bounds.height() + previewTextSizePx * 0.15f).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = logoOffsetX, y = logoOffsetY)
                        .size(width = logoWDp, height = logoHDp)
                        .graphicsLayer {
                            rotationZ = placement.rotationDegrees
                        }
                        .clickable { onSelectLayer(layer.id) }
                ) {
                    // Inner Logo Container (Applies flipHorizontal ONLY to the logo & its shadow, never to text!)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = if (placement.flipHorizontal) -1f else 1f
                            }
                    ) {
                        // Shape-accurate drop shadow behind transparent logo
                        if (placement.shadowEnabled && layerBitmap != null) {
                            Image(
                                bitmap = layerBitmap.asImageBitmap(),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color(placement.shadowColor)),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = shadowDxDp, y = shadowDyDp)
                                    .alpha((placement.opacity * 0.6f).coerceIn(0.08f, 0.85f)),
                                contentScale = ContentScale.Fit,
                                filterQuality = FilterQuality.Medium
                            )
                        }

                        // Logo Image Content - Pristine sharpness with no distortion
                        if (layerBitmap != null) {
                            Image(
                                bitmap = layerBitmap.asImageBitmap(),
                                contentDescription = "Logo Layer",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(placement.opacity),
                                contentScale = ContentScale.Fit,
                                filterQuality = FilterQuality.High
                            )
                        } else if (logoItem.isBuiltIn && logoItem.drawableResId != null) {
                            Image(
                                painter = painterResource(logoItem.drawableResId),
                                contentDescription = "Logo Layer",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(placement.opacity),
                                contentScale = ContentScale.Fit
                            )
                        } else if (logoItem.uri != null) {
                            AsyncImage(
                                model = logoItem.uri,
                                contentDescription = "Logo Layer",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(placement.opacity),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Text Watermark Caption (Never mirrored by flipHorizontal, proportionally scaled, never wraps tightly)
                    if (placement.watermarkText.isNotBlank()) {
                        Text(
                            text = placement.watermarkText,
                            fontSize = previewTextSp,
                            fontWeight = if (placement.isTextBold) FontWeight.Bold else FontWeight.Normal,
                            color = Color(placement.textColor).copy(alpha = placement.opacity),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .wrapContentWidth(unbounded = true)
                                .offset(y = textOffsetYDp)
                                .background(Color(0x55000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    // Interactive Selection Border & Handles for the active layer
                    if (isInteractive && isSelected) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = NubccCyan,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Corner grip points
                            val handleRadius = 4.dp.toPx()
                            drawCircle(color = NubccAmber, radius = handleRadius, center = Offset(0f, 0f))
                            drawCircle(color = NubccAmber, radius = handleRadius, center = Offset(size.width, 0f))
                            drawCircle(color = NubccAmber, radius = handleRadius, center = Offset(0f, size.height))
                            drawCircle(color = NubccAmber, radius = handleRadius, center = Offset(size.width, size.height))
                        }
                    }
                }
            }

            // 3. Floating Quick Hint on Canvas (When a layer is selected and interactive)
            if (isInteractive && layers.isNotEmpty() && selectedLayerId != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x99000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Drag logo to move • Pinch with 2 fingers to resize",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xDDFFFFFF)
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible InteractiveWatermarkCanvas with 1 logo.
 */
@Composable
fun InteractiveWatermarkCanvas(
    baseBitmap: Bitmap?,
    logoItem: LogoItem?,
    placement: LogoPlacement,
    onPlacementChange: (LogoPlacement) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onDragStart: (() -> Unit)? = null
) {
    val singleLayer = if (logoItem != null) {
        listOf(ActiveLogoLayer(id = "single_layer", logo = logoItem, placement = placement))
    } else emptyList()

    InteractiveMultiWatermarkCanvas(
        baseBitmap = baseBitmap,
        layers = singleLayer,
        selectedLayerId = singleLayer.firstOrNull()?.id,
        onSelectLayer = {},
        onPlacementChange = { _, newPlacement -> onPlacementChange(newPlacement) },
        modifier = modifier,
        isInteractive = isInteractive,
        onDragStart = onDragStart
    )
}
