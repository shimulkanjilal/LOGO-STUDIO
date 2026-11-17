// © 2026 App Owner. All rights reserved.
package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.components.GlassCard
import com.example.ui.components.NoticeBanner
import com.example.ui.components.NubccMainClubLogo
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.NubccAccentGradient
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccTextMuted
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StudioNeonBorderGradient
import com.example.ui.theme.studioAuraBackground

@Composable
fun HomeScreen(
    onNavigateToSinglePhoto: (Uri?) -> Unit,
    onNavigateToBatch: (List<Uri>) -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToRecentExports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    noticeText: String = "",
    userDisplayName: String = "",
    userEmail: String = "",
    onSignOut: (() -> Unit)? = null
) {
    // Single Photo Picker Launcher
    val singlePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onNavigateToSinglePhoto(uri)
        }
    }

    // Batch Multi-Photo Picker Launcher
    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onNavigateToBatch(uris)
        }
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val exportSettings = remember { SettingsManager(context).getExportSettings() }
    var dismissedNotice by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onSignOut != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xD90A1024))
                        .border(1.dp, StudioNeonBorderGradient, RoundedCornerShape(14.dp))
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Signed-in account",
                        tint = NubccCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userDisplayName.ifBlank { "Studio Member" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NubccTextPrimary,
                            maxLines = 1
                        )
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = userEmail,
                                fontSize = 11.sp,
                                color = NubccTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = NubccAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Branding Top Middle Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Studio Main Icon (Top Middle Centered)
                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x4D8B5CF6),
                                        Color(0x2622D3EE),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.6f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, StudioNeonBorderGradient, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        NubccMainClubLogo(modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LOGO",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = NubccCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STUDIO",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = NubccTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Photo Watermark & Batch Overlay Suite",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NubccAmber,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Original-Resolution Branding & Export Tools",
                    fontSize = 12.sp,
                    color = NubccTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = noticeText.isNotBlank() && dismissedNotice != noticeText
            ) {
                Column {
                    NoticeBanner(
                        noticeText = noticeText,
                        onDismiss = { dismissedNotice = noticeText }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Main Action Hero Card: Quick Studio (Single Photo Mode)
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x2B8B5CF6))
                                .border(1.dp, Color(0x558B5CF6), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = NubccCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Single Photo Mode",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NubccTextPrimary
                            )
                            Text(
                                text = "Place & fine-tune watermark on 1 photo",
                                fontSize = 12.sp,
                                color = NubccTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "ADD PHOTO",
                        icon = Icons.Default.AddPhotoAlternate,
                        onClick = {
                            singlePhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "home_add_photo_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Batch Hero Card (Multi-Photo)
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x2622D3EE))
                                .border(1.dp, Color(0x5522D3EE), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = NubccCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Batch Logo Overlay",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NubccTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NubccAccentGradient)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FAST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NubccTextPrimary
                                    )
                                }
                            }
                            Text(
                                text = "Apply logo to 10, 25, or 50+ photos at once",
                                fontSize = 12.sp,
                                color = NubccTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryButton(
                        text = "BATCH LOGO OVERLAY",
                        icon = Icons.Default.Layers,
                        onClick = {
                            multiPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "home_batch_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Navigation Tiles
            Text(
                text = "STUDIO UTILITIES",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = NubccCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Recent Exports Tile
                UtilityTile(
                    title = "Recent Exports",
                    subtitle = "Gallery history",
                    icon = Icons.Default.History,
                    onClick = onNavigateToRecentExports,
                    modifier = Modifier.weight(1f),
                    testTag = "home_recent_exports_tile"
                )

                // Saved Presets Tile
                UtilityTile(
                    title = "Saved Presets",
                    subtitle = "Corner & center",
                    icon = Icons.Default.WorkspacePremium,
                    onClick = onNavigateToPresets,
                    modifier = Modifier.weight(1f),
                    testTag = "home_presets_tile"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Tile (Reflects actual saved export preferences)
            val exifText = if (exportSettings.stripExif) "Strip EXIF" else "Keep EXIF"
            UtilityTile(
                title = "Export & Quality Settings",
                subtitle = "${exportSettings.exportFormat.name} • ${exportSettings.resizeMode.displayName} • ${exportSettings.jpegQuality}% • $exifText",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(),
                testTag = "home_settings_tile"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Footer Info
            Text(
                text = "Logo Studio • Creative Watermark Suite",
                fontSize = 11.sp,
                color = NubccTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Clickable developer link with external icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        try {
                            uriHandler.openUri("https://shimulkanjilal.me/")
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("footer_developer_portfolio_link")
            ) {
                Text(
                    text = "Developed by ",
                    fontSize = 11.sp,
                    color = NubccTextMuted
                )
                Text(
                    text = "Shimul Kanjilal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NubccCyan,
                    textDecoration = TextDecoration.Underline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Shimul Kanjilal Portfolio",
                    tint = NubccCyan,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun UtilityTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    GlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0x336366F1))
                    .border(1.dp, Color(0x4422D3EE), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NubccCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NubccTextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = NubccTextSecondary,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}
