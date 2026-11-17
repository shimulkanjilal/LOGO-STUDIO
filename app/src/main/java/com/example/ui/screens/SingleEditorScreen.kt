package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.engine.ImageUtils
import com.example.model.LogoItem
import com.example.ui.components.BeforeAfterComparison
import com.example.ui.components.InteractiveMultiWatermarkCanvas
import com.example.ui.components.LogoLayersBar
import com.example.ui.components.LogoSelectorRow
import com.example.ui.components.NubccBrandHeader
import com.example.ui.components.PlacementControls
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.studioAuraBackground
import com.example.ui.viewmodel.SingleEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SingleEditorScreen(
    viewModel: SingleEditorViewModel,
    initialPhotoUri: Uri?,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val exportSettings = remember(uiState.isExporting, uiState.exportedUri) {
        SettingsManager(context).getExportSettings()
    }

    LaunchedEffect(initialPhotoUri) {
        if (initialPhotoUri != null && uiState.photoUri == null) {
            viewModel.setPhoto(initialPhotoUri)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setPhoto(uri)
        }
    }

    // Picker for adding new logo layers (select single or multiple logos!)
    val addLayerPicker = rememberLauncherForActivityResult(
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
                viewModel.addCustomLogos(newLogos)
                newLogos.forEach { logo ->
                    viewModel.addNewLogoLayer(logo)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .studioAuraBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            NubccBrandHeader(
                title = "SINGLE PHOTO EDITOR",
                subtitle = "Drag & pinch to place logo",
                onBackClick = onNavigateBack,
                actions = {
                    // Undo & Redo buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.canUndo) Color(0x33FFFFFF) else Color(0x11FFFFFF))
                                .testTag("undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) NubccTextPrimary else Color(0x44FFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.canRedo) Color(0x33FFFFFF) else Color(0x11FFFFFF))
                                .testTag("redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) NubccTextPrimary else Color(0x44FFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Photo Empty State or Interactive Canvas
                if (uiState.photoBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, NubccGlassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = NubccCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Photo Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NubccTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap here to choose a photo from gallery",
                                fontSize = 12.sp,
                                color = NubccTextSecondary
                            )
                        }
                    }
                } else {
                    // Live multi-logo interactive canvas with smooth 60fps drag
                    InteractiveMultiWatermarkCanvas(
                        baseBitmap = uiState.photoBitmap,
                        layers = uiState.layers,
                        selectedLayerId = uiState.selectedLayerId,
                        onSelectLayer = { viewModel.selectLayer(it) },
                        onPlacementChange = { layerId, newPlacement ->
                            viewModel.updateLayerPlacement(layerId, newPlacement, recordHistory = false)
                        },
                        onDragStart = { viewModel.onDragStart() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Change Photo Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Change Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NubccCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("change_photo_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Logo Layers Bar (Manage multiple logos on this photo)
                if (uiState.layers.isNotEmpty()) {
                    LogoLayersBar(
                        layers = uiState.layers,
                        selectedLayerId = uiState.selectedLayerId,
                        onSelectLayer = { viewModel.selectLayer(it) },
                        onAddLayerClick = {
                            addLayerPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveLayerClick = { viewModel.removeLogoLayer(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Logo Selector & Importer (Select/Replace/Import logo for the active layer)
                LogoSelectorRow(
                    selectedLogo = uiState.selectedLogo,
                    onLogoSelect = { viewModel.setLogo(it) },
                    customLogos = uiState.customLogos,
                    onAddCustomLogos = { viewModel.addCustomLogos(it) },
                    onRemoveCustomLogo = { viewModel.removeCustomLogo(it) },
                    onAddNewLogoLayer = { viewModel.addNewLogoLayer(it) }
                )

                if (uiState.layers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Placement Controls for the active layer
                    PlacementControls(
                        placement = uiState.placement,
                        onPlacementChange = { viewModel.updatePlacement(it, recordHistory = false) },
                        onResetToCorner = { viewModel.resetToCorner() },
                        onAutoPlace = { viewModel.autoPlaceLogo() },
                        logoAspectRatio = uiState.selectedLogo?.aspectRatio ?: 1.0f,
                        onBeginEdit = { viewModel.recordHistorySnapshot() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Before / After Preview Section
                    if (uiState.photoBitmap != null) {
                        BeforeAfterComparison(
                            baseBitmap = uiState.photoBitmap,
                            layers = uiState.layers
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Image Button
                PrimaryButton(
                    text = if (uiState.isExporting) "EXPORTING PHOTO..." else "SAVE IMAGE",
                    icon = Icons.Default.Save,
                    enabled = !uiState.isExporting && uiState.photoUri != null,
                    onClick = { viewModel.exportImage() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_image_button"
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Loading Overlay during Export
        if (uiState.isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC060B16)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NubccCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rendering ${exportSettings.resizeMode.displayName}...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NubccTextPrimary
                    )
                    Text(
                        text = "Saving ${exportSettings.exportFormat.displayName} to ${exportSettings.folderName}",
                        fontSize = 12.sp,
                        color = NubccTextSecondary
                    )
                }
            }
        }

        // Export Success Dialog
        if (uiState.exportedUri != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissExportDialog() },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color(0xFF0F1A2E),
                titleContentColor = NubccTextPrimary,
                textContentColor = NubccTextSecondary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NubccCyan,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Photo Saved Successfully!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Exported as ${exportSettings.exportFormat.displayName} (${exportSettings.resizeMode.displayName}) to ${exportSettings.folderName} and added to your device gallery.",
                            fontSize = 13.sp
                        )
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "VIEW IN GALLERY",
                        onClick = {
                            val uri = uiState.exportedUri
                            viewModel.dismissExportDialog()
                            if (uri != null) {
                                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(viewIntent)
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.height(42.dp),
                        testTag = "dialog_view_gallery_button"
                    )
                },
                dismissButton = {
                    SecondaryButton(
                        text = "DONE",
                        onClick = { viewModel.dismissExportDialog() },
                        modifier = Modifier.height(42.dp),
                        testTag = "dialog_done_button"
                    )
                }
            )
        }
    }
}
