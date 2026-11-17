package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.AsyncImage
import com.example.engine.ImageUtils
import com.example.model.LogoItem
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCardGradient
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.StudioNeonBorderGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LogoSelectorRow(
    selectedLogo: LogoItem?,
    onLogoSelect: (LogoItem?) -> Unit,
    customLogos: List<LogoItem> = emptyList(),
    onAddCustomLogos: (List<LogoItem>) -> Unit = {},
    onRemoveCustomLogo: ((String) -> Unit)? = null,
    onAddNewLogoLayer: ((LogoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Single Photo Picker Launcher
    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val item = withContext(Dispatchers.IO) {
                    val (w, h) = ImageUtils.getImageDimensions(context, uri)
                    val aspect = if (h > 0) w.toFloat() / h.toFloat() else 1.0f
                    LogoItem(
                        id = "custom_${System.currentTimeMillis()}",
                        name = ImageUtils.getDisplayName(context, uri),
                        uri = uri,
                        aspectRatio = aspect
                    )
                }
                onAddCustomLogos(listOf(item))
                onLogoSelect(item)
            }
        }
    }

    // Multiple Logo Picker Launcher (Select 2, 3, or more logos at once!)
    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val baseTs = System.currentTimeMillis()
                val newLogos = withContext(Dispatchers.IO) {
                    uris.mapIndexed { idx, uri ->
                        val (w, h) = ImageUtils.getImageDimensions(context, uri)
                        val aspect = if (h > 0) w.toFloat() / h.toFloat() else 1.0f
                        LogoItem(
                            id = "custom_${baseTs}_$idx",
                            name = ImageUtils.getDisplayName(context, uri),
                            uri = uri,
                            aspectRatio = aspect
                        )
                    }
                }
                onAddCustomLogos(newLogos)
                if (newLogos.isNotEmpty()) {
                    onLogoSelect(newLogos.first())
                    if (onAddNewLogoLayer != null && newLogos.size > 1) {
                        newLogos.drop(1).forEach { extraLogo ->
                            onAddNewLogoLayer(extraLogo)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NubccCardGradient)
            .border(1.dp, StudioNeonBorderGradient, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = NubccCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WATERMARK LOGO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.0.sp,
                    color = NubccCyan
                )
            }

            if (selectedLogo != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3338BDF8))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "IMPORTED LOGO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NubccCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Logo Card Preview
        if (selectedLogo != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1424))
                    .border(1.dp, Color(0x667C3AED), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF131D32))
                            .border(1.dp, NubccGlassBorder, RoundedCornerShape(10.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedLogo.uri != null) {
                            AsyncImage(
                                model = selectedLogo.uri,
                                contentDescription = selectedLogo.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else if (selectedLogo.drawableResId != null) {
                            Image(
                                painter = painterResource(selectedLogo.drawableResId),
                                contentDescription = selectedLogo.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedLogo.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NubccTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (selectedLogo.aspectRatio >= 1.5f) "Wide Landscape (${String.format("%.1f", selectedLogo.aspectRatio)}:1)" else "Square / Crest (1:1)",
                            fontSize = 11.sp,
                            color = NubccTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Change / Replace Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x337C3AED))
                                    .border(1.dp, NubccElectricViolet, RoundedCornerShape(8.dp))
                                    .clickable {
                                        singlePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("change_logo_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = NubccCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Change",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NubccTextPrimary
                                    )
                                }
                            }

                            // Remove Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FF4444))
                                    .border(1.dp, Color(0x66FF4444), RoundedCornerShape(8.dp))
                                    .clickable { onLogoSelect(null) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("remove_logo_button")
                            ) {
                                Text(
                                    text = "Remove",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7777)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Empty State Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1838BDF8))
                    .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No Logo Selected for this Layer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NubccTextPrimary
                        )
                        Text(
                            text = "Import a transparent PNG or JPG from gallery",
                            fontSize = 11.sp,
                            color = NubccTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            singlePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NubccElectricViolet),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("IMPORT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multiple Logo Adding Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Pick Multiple Logos from Gallery
            Button(
                onClick = {
                    multiPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("add_multiple_logos_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x3338BDF8),
                    contentColor = NubccCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, NubccCyan)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = NubccCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IMPORT MULTIPLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NubccCyan
                    )
                }
            }

            // 2. Pick Single Custom Logo
            Button(
                onClick = {
                    singlePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("add_single_logo_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x337C3AED),
                    contentColor = NubccTextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, NubccElectricViolet)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = NubccAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IMPORT 1 LOGO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NubccTextPrimary
                    )
                }
            }
        }

        // Custom Logos Tray (All user-imported logos)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR IMPORTED LOGOS (${customLogos.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NubccAmber,
                letterSpacing = 0.5.sp
            )

            if (customLogos.isNotEmpty()) {
                Text(
                    text = "Tap to apply",
                    fontSize = 10.sp,
                    color = NubccTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (customLogos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x11FFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logos imported yet. Tap 'Import' above to add your logo files from gallery.",
                    fontSize = 12.sp,
                    color = NubccTextSecondary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                customLogos.forEach { logo ->
                    val isSelected = (selectedLogo?.id == logo.id)
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x447C3AED) else Color(0x18FFFFFF))
                            .border(
                                1.5.dp,
                                if (isSelected) NubccAmber else Color(0x22FFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onLogoSelect(logo) }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = logo.uri,
                            contentDescription = logo.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Selected Badge
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NubccAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        // Remove from library button
                        if (onRemoveCustomLogo != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x88000000))
                                    .clickable { onRemoveCustomLogo(logo.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete logo from library",
                                    tint = Color(0xFFFF7777),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
