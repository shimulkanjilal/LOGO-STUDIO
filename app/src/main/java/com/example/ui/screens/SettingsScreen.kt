// © 2026 App Owner. All rights reserved.
package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SettingsManager
import com.example.model.ExportFormat
import com.example.model.ExportSettings
import com.example.model.ResizeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.NubccBrandHeader
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccTextMuted
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioNeonBorderGradient
import com.example.ui.theme.studioAuraBackground
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    userDisplayName: String = "",
    userEmail: String = "",
    onSignOut: (() -> Unit)? = null
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var selectedFormat by remember { mutableStateOf(settingsManager.exportFormat) }
    var selectedResize by remember { mutableStateOf(settingsManager.resizeMode) }
    var stripExif by remember { mutableStateOf(settingsManager.stripExif) }
    var filenameTemplate by remember { mutableStateOf(settingsManager.filenameTemplate) }
    var jpegQuality by remember { mutableFloatStateOf(settingsManager.jpegQuality.toFloat()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NubccBrandHeader(
                title = "STUDIO SETTINGS",
                subtitle = "Export options & studio info",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section 1: Export Settings
                Text(
                    text = "EXPORT CONFIGURATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.0.sp,
                    color = NubccCyan,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 1. Export Format (JPEG, PNG, WebP)
                        Text(
                            text = "Export Image Format",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NubccTextPrimary
                        )
                        Text(
                            text = "Select encoding format for exported watermarked photos",
                            fontSize = 11.sp,
                            color = NubccTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExportFormat.entries.forEach { format ->
                                val isSelected = (selectedFormat == format)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NubccElectricViolet else Color(0x1EFFFFFF))
                                        .border(1.dp, if (isSelected) NubccCyan else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedFormat = format
                                            settingsManager.exportFormat = format
                                        }
                                        .testTag("format_${format.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = format.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) NubccTextPrimary else NubccTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Output Resolution / Resize Target
                        Text(
                            text = "Output Image Resolution",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NubccTextPrimary
                        )
                        Text(
                            text = "Downscale for fast social uploads or preserve full megapixels",
                            fontSize = 11.sp,
                            color = NubccTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ResizeMode.entries.forEach { mode ->
                                val isSelected = (selectedResize == mode)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NubccElectricViolet else Color(0x1EFFFFFF))
                                        .border(1.dp, if (isSelected) NubccCyan else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedResize = mode
                                            settingsManager.resizeMode = mode
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                        .testTag("resize_${mode.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = when (mode) {
                                                ResizeMode.ORIGINAL -> "Original"
                                                ResizeMode.RESIZE_2048 -> "2048px"
                                                ResizeMode.RESIZE_1080 -> "1080p"
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) NubccTextPrimary else NubccTextSecondary
                                        )
                                        Text(
                                            text = when (mode) {
                                                ResizeMode.ORIGINAL -> "Full Res"
                                                ResizeMode.RESIZE_2048 -> "Web Standard"
                                                ResizeMode.RESIZE_1080 -> "Social"
                                            },
                                            fontSize = 9.sp,
                                            color = if (isSelected) NubccAmber else NubccTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. EXIF & Location Removal (Privacy)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Strip Camera EXIF Metadata",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NubccTextPrimary
                                )
                                Text(
                                    text = "GPS location is always stripped; toggle to also remove camera & lens tags",
                                    fontSize = 11.sp,
                                    color = NubccTextSecondary
                                )
                            }
                            Switch(
                                checked = stripExif,
                                onCheckedChange = {
                                    stripExif = it
                                    settingsManager.stripExif = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NubccElectricViolet
                                ),
                                modifier = Modifier.testTag("settings_strip_exif_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Filename Template
                        Text(
                            text = "Export Filename Pattern",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NubccTextPrimary
                        )
                        Text(
                            text = "Tags: {name}, {index}, {event}, {date}, {time}",
                            fontSize = 11.sp,
                            color = NubccCyan
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = filenameTemplate,
                            onValueChange = {
                                filenameTemplate = it
                                settingsManager.filenameTemplate = it
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("filename_template_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NubccCyan,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = NubccTextPrimary,
                                unfocusedTextColor = NubccTextPrimary,
                                cursorColor = NubccCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Quick Template Preset Chips
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "LOGO_{event}_{index}",
                                "LOGO_{name}_{001}",
                                "{name}_watermarked"
                            ).forEach { tpl ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x22FFFFFF))
                                        .clickable {
                                            filenameTemplate = tpl
                                            settingsManager.filenameTemplate = tpl
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(text = tpl, fontSize = 9.sp, color = NubccAmber)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. JPEG / Compression Quality
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Encoding Quality",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NubccTextPrimary
                                )
                                Text(
                                    text = "Full original fidelity preservation",
                                    fontSize = 11.sp,
                                    color = NubccTextSecondary
                                )
                            }
                            Text(
                                text = "${jpegQuality.roundToInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NubccAmber
                            )
                        }

                        Slider(
                            value = jpegQuality,
                            onValueChange = {
                                jpegQuality = it
                                settingsManager.jpegQuality = it.roundToInt()
                            },
                            valueRange = 80f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NubccCyan,
                                activeTrackColor = NubccElectricViolet,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("settings_jpeg_quality_slider")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Output Folder Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Output Gallery Folder",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NubccTextPrimary
                                )
                                Text(
                                    text = "Pictures/LogoOverlay",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NubccCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Default Watermark Defaults
                Text(
                    text = "DEFAULT WATERMARK RULES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.0.sp,
                    color = NubccCyan,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Placement", fontSize = 13.sp, color = NubccTextSecondary)
                            Text("Bottom Right Corner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NubccTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Logo Width", fontSize = 13.sp, color = NubccTextSecondary)
                            Text("18% of photo width", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NubccAmber)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Margin", fontSize = 13.sp, color = NubccTextSecondary)
                            Text("3.5% from edges", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NubccCyan)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Opacity", fontSize = 13.sp, color = NubccTextSecondary)
                            Text("100% (Solid)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NubccTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (onSignOut != null) {
                    Text(
                        text = "MEMBER ACCOUNT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.0.sp,
                        color = NubccCyan,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account profile",
                                    tint = NubccCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userDisplayName.ifBlank { "Studio Member" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NubccTextPrimary
                                    )
                                    if (userEmail.isNotBlank()) {
                                        Text(
                                            text = userEmail,
                                            fontSize = 12.sp,
                                            color = NubccTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onSignOut,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_sign_out_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33EF4444),
                                    contentColor = Color(0xFFFCA5A5)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SIGN OUT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Section 3: About Logo Studio
                Text(
                    text = "ABOUT LOGO STUDIO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.0.sp,
                    color = NubccCyan,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, StudioNeonBorderGradient, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_studio_icon),
                                contentDescription = "Logo Studio Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(76.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Logo Studio",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = NubccTextPrimary
                        )

                        Text(
                            text = "Watermark & Batch Overlay Suite",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NubccCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Version 1.0 (Production Build)",
                            fontSize = 11.sp,
                            color = NubccAmber,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Designed and engineered for event photography, press releases, social media publishing, and creative portfolios.",
                            fontSize = 11.sp,
                            color = NubccTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
