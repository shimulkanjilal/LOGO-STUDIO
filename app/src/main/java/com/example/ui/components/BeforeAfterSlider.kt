package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveLogoLayer
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary

@Composable
fun BeforeAfterComparison(
    baseBitmap: Bitmap?,
    layers: List<ActiveLogoLayer>,
    modifier: Modifier = Modifier
) {
    var showWatermarked by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, NubccGlassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BEFORE / AFTER PREVIEW",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.0.sp,
                color = NubccCyan
            )

            // Switch Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, NubccGlassBorder, RoundedCornerShape(20.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!showWatermarked) NubccElectricViolet else Color.Transparent)
                        .clickable { showWatermarked = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("preview_before_button")
                ) {
                    Text(
                        text = "BEFORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!showWatermarked) NubccTextPrimary else NubccTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (showWatermarked) NubccCyan else Color.Transparent)
                        .clickable { showWatermarked = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("preview_after_button")
                ) {
                    Text(
                        text = "AFTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showWatermarked) Color.Black else NubccTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display current view with ALL active layers
        InteractiveMultiWatermarkCanvas(
            baseBitmap = baseBitmap,
            layers = if (showWatermarked) layers else emptyList(),
            selectedLayerId = null,
            onSelectLayer = {},
            onPlacementChange = { _, _ -> },
            isInteractive = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BeforeAfterComparison(
    baseBitmap: Bitmap?,
    logoItem: LogoItem?,
    placement: LogoPlacement,
    modifier: Modifier = Modifier
) {
    val layers = if (logoItem != null) {
        listOf(ActiveLogoLayer(id = "single_layer", logo = logoItem, placement = placement))
    } else emptyList()
    BeforeAfterComparison(
        baseBitmap = baseBitmap,
        layers = layers,
        modifier = modifier
    )
}
