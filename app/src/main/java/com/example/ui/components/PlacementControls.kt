package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogoPlacement
import com.example.model.PositionPreset
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextMuted
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import kotlin.math.roundToInt

@Composable
fun PlacementControls(
    placement: LogoPlacement,
    onPlacementChange: (LogoPlacement) -> Unit,
    onResetToCorner: () -> Unit,
    onAutoPlace: (() -> Unit)? = null,
    logoAspectRatio: Float = 1.0f,
    onBeginEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var sizeDragging by remember { mutableStateOf(false) }
    var marginDragging by remember { mutableStateOf(false) }
    var opacityDragging by remember { mutableStateOf(false) }
    var rotationDragging by remember { mutableStateOf(false) }
    var shadowDragging by remember { mutableStateOf(false) }

    fun nudge(deltaX: Float, deltaY: Float) {
        onBeginEdit?.invoke()
        val refSize = 1000
        val currentBounds = placement.calculatePixelBounds(
            imageWidth = refSize,
            imageHeight = refSize,
            logoAspectRatio = logoAspectRatio
        )
        val logoW = currentBounds.width()
        val logoH = currentBounds.height()
        val textReservePx = if (placement.watermarkText.isNotBlank()) {
            (refSize * placement.fontSizeFraction).coerceAtLeast(16f) * 1.35f
        } else {
            0f
        }
        val bottomPad = if (placement.watermarkText.isNotBlank()) (refSize * 0.01f).coerceAtLeast(4f) else 0f

        val minLeft = -logoW * 0.15f
        val maxLeft = (refSize - logoW * 0.85f).coerceAtLeast(0f)
        val minTop = -logoH * 0.15f
        val maxTop = if (placement.watermarkText.isNotBlank()) {
            (refSize - logoH - textReservePx - bottomPad).coerceAtLeast(0f)
        } else {
            (refSize - logoH * 0.85f).coerceAtLeast(0f)
        }

        val newLeft = (currentBounds.left + deltaX * refSize).coerceIn(minLeft, maxLeft)
        val newTop = (currentBounds.top + deltaY * refSize).coerceIn(minTop, maxTop)

        val (normX, normY) = LogoPlacement.calculateCustomNormalizedPosition(
            leftPx = newLeft,
            topPx = newTop,
            imageWidth = refSize.toFloat(),
            imageHeight = refSize.toFloat(),
            logoWidth = logoW,
            logoHeight = logoH,
            watermarkText = placement.watermarkText,
            fontSizeFraction = placement.fontSizeFraction
        )

        onPlacementChange(
            placement.copy(
                preset = PositionPreset.CUSTOM,
                normalizedX = normX,
                normalizedY = normY
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, NubccGlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Row 1: Header + Reset, Flush and Auto Corner Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOGO POSITION & PRESETS",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.0.sp,
                color = NubccCyan
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Auto Place Button (Edge density least busy corner)
                if (onAutoPlace != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, NubccAmber, RoundedCornerShape(8.dp))
                            .clickable { onAutoPlace() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("auto_place_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Auto Place",
                            tint = NubccAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "AUTO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = NubccAmber
                        )
                    }
                }

                // 0% Flush Corner Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3338BDF8))
                        .border(1.dp, NubccCyan, RoundedCornerShape(8.dp))
                        .clickable {
                            onBeginEdit?.invoke()
                            onPlacementChange(
                                placement.copy(
                                    preset = PositionPreset.BOTTOM_RIGHT,
                                    normalizedMargin = 0.0f
                                )
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("flush_corner_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = "Flush Corner",
                        tint = NubccCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FLUSH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = NubccCyan
                    )
                }

                // Reset Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x334A2E83))
                        .border(1.dp, Color(0x667C3AED), RoundedCornerShape(8.dp))
                        .clickable { onResetToCorner() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("reset_to_corner_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset to Corner",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "RESET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = NubccTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 9-Position Grid
        val presets = listOf(
            listOf(PositionPreset.TOP_LEFT, PositionPreset.TOP_CENTER, PositionPreset.TOP_RIGHT),
            listOf(PositionPreset.CENTER_LEFT, PositionPreset.CENTER, PositionPreset.CENTER_RIGHT),
            listOf(PositionPreset.BOTTOM_LEFT, PositionPreset.BOTTOM_CENTER, PositionPreset.BOTTOM_RIGHT)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPresets.forEach { preset ->
                        val isSelected = (placement.preset == preset)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) NubccElectricViolet else Color(0x1EFFFFFF)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NubccCyan else Color(0x22FFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onBeginEdit?.invoke()
                                    onPlacementChange(
                                        placement.copy(
                                            preset = preset
                                        )
                                    )
                                }
                                .testTag("preset_${preset.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NubccTextPrimary else NubccTextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Precision Corner Nudge Pad (Allows exact pixel-by-pixel push into corner)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x16FFFFFF))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CORNER NUDGE PAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NubccAmber
                )
                Text(
                    text = "Tap arrows to push right into corner",
                    fontSize = 10.sp,
                    color = NubccTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Left
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { nudge(-0.015f, 0f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Nudge Left",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Up
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { nudge(0f, -0.015f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Nudge Up",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Down
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { nudge(0f, 0.015f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Nudge Down",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Right
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { nudge(0.015f, 0f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Nudge Right",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Size Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Logo Size",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NubccTextPrimary
            )
            Text(
                text = "${(placement.normalizedWidth * 100).roundToInt()}% of width",
                fontSize = 12.sp,
                color = NubccAmber,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = placement.normalizedWidth,
            onValueChange = { newW ->
                if (!sizeDragging) {
                    sizeDragging = true
                    onBeginEdit?.invoke()
                }
                onPlacementChange(placement.copy(normalizedWidth = newW))
            },
            onValueChangeFinished = { sizeDragging = false },
            valueRange = 0.05f..0.65f,
            colors = SliderDefaults.colors(
                thumbColor = NubccCyan,
                activeTrackColor = NubccElectricViolet,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.testTag("size_slider")
        )

        // Margin Slider (Includes 0% absolute flush corner)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Edge Margin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NubccTextPrimary
                )
                if (placement.normalizedMargin <= 0.005f) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(Flush Corner)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccCyan
                    )
                }
            }
            Text(
                text = "${(placement.normalizedMargin * 100).roundToInt()}%",
                fontSize = 12.sp,
                color = NubccAmber,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = placement.normalizedMargin,
            onValueChange = { newMargin ->
                if (!marginDragging) {
                    marginDragging = true
                    onBeginEdit?.invoke()
                }
                onPlacementChange(placement.copy(normalizedMargin = newMargin))
            },
            onValueChangeFinished = { marginDragging = false },
            valueRange = 0.0f..0.12f,
            colors = SliderDefaults.colors(
                thumbColor = NubccCyan,
                activeTrackColor = NubccElectricViolet,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.testTag("margin_slider")
        )

        // Opacity Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Watermark Opacity",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NubccTextPrimary
            )
            Text(
                text = "${(placement.opacity * 100).roundToInt()}%",
                fontSize = 12.sp,
                color = NubccAmber,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = placement.opacity,
            onValueChange = { newOpacity ->
                if (!opacityDragging) {
                    opacityDragging = true
                    onBeginEdit?.invoke()
                }
                onPlacementChange(placement.copy(opacity = newOpacity))
            },
            onValueChangeFinished = { opacityDragging = false },
            valueRange = 0.10f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = NubccCyan,
                activeTrackColor = NubccElectricViolet,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.testTag("opacity_slider")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Auto White Background Removal Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (placement.removeWhiteBg) Color(0x3310B981) else Color(0x16FFFFFF))
                .border(
                    1.dp,
                    if (placement.removeWhiteBg) Color(0xFF10B981) else Color(0x22FFFFFF),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(removeWhiteBg = !placement.removeWhiteBg))
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("remove_white_bg_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "Remove White BG",
                    tint = if (placement.removeWhiteBg) Color(0xFF10B981) else NubccCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Remove White Background",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccTextPrimary
                    )
                    Text(
                        text = if (placement.removeWhiteBg) "White box removed • Transparent PNG style" else "Tap to erase solid white box from logos",
                        fontSize = 10.sp,
                        color = if (placement.removeWhiteBg) Color(0xFF6EE7B7) else NubccTextSecondary
                    )
                }
            }
            Switch(
                checked = placement.removeWhiteBg,
                onCheckedChange = { isChecked ->
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(removeWhiteBg = isChecked))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981)
                ),
                modifier = Modifier.testTag("remove_white_bg_switch")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Adaptive Aspect Ratio Sizing (Equal size on 9:16 portrait & 16:9 landscape)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (placement.adaptiveScale) Color(0x3338BDF8) else Color(0x16FFFFFF))
                .border(
                    1.dp,
                    if (placement.adaptiveScale) NubccCyan else Color(0x22FFFFFF),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(adaptiveScale = !placement.adaptiveScale))
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("adaptive_scale_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Equal 9:16 / 16:9 Scale",
                    tint = if (placement.adaptiveScale) NubccCyan else NubccTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Auto-Match 9:16 & 16:9 Size",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccTextPrimary
                    )
                    Text(
                        text = if (placement.adaptiveScale) "Active • Equal logo size & corner lock on mixed photos" else "Manual photo width scaling",
                        fontSize = 10.sp,
                        color = if (placement.adaptiveScale) NubccCyan else NubccTextSecondary
                    )
                }
            }
            Switch(
                checked = placement.adaptiveScale,
                onCheckedChange = { isChecked ->
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(adaptiveScale = isChecked))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NubccCyan
                ),
                modifier = Modifier.testTag("adaptive_scale_switch")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- SECTION: ROTATION & FLIP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROTATION & ORIENTATION",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = NubccCyan
            )
            Text(
                text = "${placement.rotationDegrees.roundToInt()}°",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NubccAmber
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flip Horizontal Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (placement.flipHorizontal) NubccElectricViolet else Color(0x1EFFFFFF))
                    .border(1.dp, if (placement.flipHorizontal) NubccCyan else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .clickable {
                        onBeginEdit?.invoke()
                        onPlacementChange(placement.copy(flipHorizontal = !placement.flipHorizontal))
                    }
                    .testTag("flip_horizontal_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Flip Horizontal",
                        tint = if (placement.flipHorizontal) NubccCyan else NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Flip H",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccTextPrimary
                    )
                }
            }

            // Quick 90° Rotate Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x1EFFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .clickable {
                        onBeginEdit?.invoke()
                        val next = (placement.rotationDegrees + 90f).let { if (it > 180f) it - 360f else it }
                        onPlacementChange(placement.copy(rotationDegrees = next))
                    }
                    .testTag("rotate_90_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "+90 deg",
                        tint = NubccTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+90°",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccTextPrimary
                    )
                }
            }

            // 0° Reset Rotation Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x1EFFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .clickable {
                        onBeginEdit?.invoke()
                        onPlacementChange(placement.copy(rotationDegrees = 0f))
                    }
                    .testTag("reset_rotation_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "0° Reset",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NubccTextSecondary
                )
            }
        }

        Slider(
            value = placement.rotationDegrees,
            onValueChange = {
                if (!rotationDragging) {
                    rotationDragging = true
                    onBeginEdit?.invoke()
                }
                onPlacementChange(placement.copy(rotationDegrees = it))
            },
            onValueChangeFinished = { rotationDragging = false },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(
                thumbColor = NubccAmber,
                activeTrackColor = NubccElectricViolet,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.testTag("rotation_slider")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- SECTION: DROP SHADOW & GLOW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (placement.shadowEnabled) Color(0x337C3AED) else Color(0x16FFFFFF))
                .border(1.dp, if (placement.shadowEnabled) NubccCyan else Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BlurOn,
                    contentDescription = "Drop Shadow",
                    tint = if (placement.shadowEnabled) NubccCyan else NubccTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Drop Shadow & Glow",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccTextPrimary
                    )
                    Text(
                        text = if (placement.shadowEnabled) "Active • Enhances contrast on complex photos" else "Add depth to logo",
                        fontSize = 10.sp,
                        color = NubccTextSecondary
                    )
                }
            }
            Switch(
                checked = placement.shadowEnabled,
                onCheckedChange = {
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(shadowEnabled = it))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NubccElectricViolet
                ),
                modifier = Modifier.testTag("shadow_switch")
            )
        }

        if (placement.shadowEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shadow Blur Radius", fontSize = 12.sp, color = NubccTextSecondary)
                Text("${placement.shadowRadius.roundToInt()}px", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NubccAmber)
            }
            Slider(
                value = placement.shadowRadius,
                onValueChange = {
                    if (!shadowDragging) {
                        shadowDragging = true
                        onBeginEdit?.invoke()
                    }
                    onPlacementChange(placement.copy(shadowRadius = it))
                },
                onValueChangeFinished = { shadowDragging = false },
                valueRange = 2f..30f,
                colors = SliderDefaults.colors(
                    thumbColor = NubccCyan,
                    activeTrackColor = NubccElectricViolet,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- SECTION: TEXT WATERMARK ---
        Text(
            text = "EVENT & BRAND TEXT WATERMARK",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = NubccCyan
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = placement.watermarkText,
            onValueChange = { onPlacementChange(placement.copy(watermarkText = it)) },
            placeholder = { Text("e.g. Studio Brand • Event 2026", color = Color(0x66FFFFFF), fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("watermark_text_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NubccCyan,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedTextColor = NubccTextPrimary,
                unfocusedTextColor = NubccTextPrimary,
                cursorColor = NubccCyan
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text Watermark Style Options (Bold + Colors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold toggle button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                .background(if (placement.isTextBold) NubccElectricViolet else Color(0x22FFFFFF))
                .border(1.dp, if (placement.isTextBold) NubccCyan else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                .clickable {
                    onBeginEdit?.invoke()
                    onPlacementChange(placement.copy(isTextBold = !placement.isTextBold))
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FormatBold,
                contentDescription = "Bold Text",
                tint = NubccTextPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("BOLD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NubccTextPrimary)
        }

            // Color Chips
            val colors = listOf(
                Pair("White", 0xFFFFFFFF),
                Pair("Amber", 0xFFF59E0B),
                Pair("Cyan", 0xFF38BDF8),
                Pair("Dark", 0xFF0F172A)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                colors.forEach { (label, colVal) ->
                    val isColSelected = (placement.textColor == colVal)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(colVal))
                            .border(
                                width = if (isColSelected) 2.dp else 1.dp,
                                color = if (isColSelected) NubccAmber else Color(0x44FFFFFF),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onBeginEdit?.invoke()
                                onPlacementChange(placement.copy(textColor = colVal))
                            }
                    )
                }
            }
        }
    }
}
