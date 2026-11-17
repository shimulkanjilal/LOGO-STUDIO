package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveLogoLayer
import com.example.model.LogoItem
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary

@Composable
fun LogoLayersBar(
    layers: List<ActiveLogoLayer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onAddLayerClick: () -> Unit,
    onRemoveLayerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = NubccAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE LOGO LAYERS (${layers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.0.sp,
                    color = NubccAmber
                )
            }

            Text(
                text = "Tap a layer to edit",
                fontSize = 11.sp,
                color = NubccTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Layer chips
            layers.forEachIndexed { index, layer ->
                val isSelected = (layer.id == selectedLayerId)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x557C3AED) else Color(0x22FFFFFF))
                        .border(
                            1.5.dp,
                            if (isSelected) NubccCyan else Color(0x33FFFFFF),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectLayer(layer.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("layer_tab_$index")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Layer ${index + 1}: ${layer.logo.name.take(14)}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) NubccCyan else NubccTextPrimary
                        )

                        if (layers.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33FF4444))
                                    .clickable { onRemoveLayerClick(layer.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove layer",
                                    tint = Color(0xFFFF7777),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Button to Add Another Logo Layer
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x3338BDF8))
                    .border(1.dp, NubccCyan, RoundedCornerShape(10.dp))
                    .clickable { onAddLayerClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("add_another_logo_layer_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Layer",
                        tint = NubccCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ ADD LOGO LAYER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NubccCyan
                    )
                }
            }
        }
    }
}
