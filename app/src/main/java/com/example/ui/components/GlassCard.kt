// © 2026 App Owner. All rights reserved.
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NubccCardGradient
import com.example.ui.theme.NubccGlassSurface
import com.example.ui.theme.StudioNeonBorderGradient

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = NubccGlassSurface,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val borderStroke = if (borderColor != null) {
        BorderStroke(borderWidth, borderColor)
    } else {
        BorderStroke(borderWidth, StudioNeonBorderGradient)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (backgroundColor == NubccGlassSurface) NubccCardGradient else Brush.linearGradient(listOf(backgroundColor, backgroundColor)))
            .border(borderStroke, shape)
    ) {
        content()
    }
}

@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderStroke = if (borderColor != null) {
        BorderStroke(1.dp, borderColor)
    } else {
        BorderStroke(1.dp, StudioNeonBorderGradient)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xEE111936),
                        Color(0xEB0A1024),
                        Color(0xEE1A1038)
                    )
                )
            )
            .border(borderStroke, shape)
    ) {
        content()
    }
}
