package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.ImageUtils
import com.example.model.ActiveLogoLayer
import com.example.model.LogoItem
import com.example.model.PhotoItem
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveMultiWatermarkCanvas
import com.example.ui.components.InteractiveWatermarkCanvas
import com.example.ui.components.LogoLayersBar
import com.example.ui.components.LogoSelectorRow
import com.example.ui.components.NubccBrandHeader
import com.example.ui.components.PlacementControls
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.NubccAmber
import com.example.ui.theme.NubccCyan
import com.example.ui.theme.NubccElectricViolet
import com.example.ui.theme.NubccGlassBorder
import com.example.ui.theme.NubccTextMuted
import com.example.ui.theme.NubccTextPrimary
import com.example.ui.theme.NubccTextSecondary
import com.example.ui.theme.studioAuraBackground
import com.example.ui.viewmodel.BatchStudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BatchStudioScreen(
    viewModel: BatchStudioViewModel,
    initialUris: List<Uri>,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tabs: 0 -> Config & Representative, 1 -> Batch Photos Grid & Previews
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(initialUris) {
        if (initialUris.isNotEmpty() && uiState.photos.isEmpty()) {
            viewModel.addPhotos(initialUris)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Add more photos picker launcher
    val addMorePhotosPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addPhotos(uris)
        }
    }

    // Picker for adding new logo layers in batch mode (single or multiple!)
    val addBatchLayerPicker = rememberLauncherForActivityResult(
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            NubccBrandHeader(
                title = "BATCH LOGO STUDIO",
                subtitle = "${uiState.photos.size} Photos Selected",
                onBackClick = onNavigateBack,
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.canUndo) Color(0x33FFFFFF) else Color(0x11FFFFFF))
                                .testTag("batch_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) NubccTextPrimary else Color(0x44FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.canRedo) Color(0x33FFFFFF) else Color(0x11FFFFFF))
                                .testTag("batch_redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) NubccTextPrimary else Color(0x44FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (uiState.photos.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    addMorePhotosPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3338BDF8))
                                    .testTag("batch_add_more_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add More Photos",
                                    tint = NubccCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            )

            // Tabs: Global Setup vs Batch Previews
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0x18FFFFFF),
                contentColor = NubccCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NubccCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "PLACEMENT & LOGO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_placement")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "ALL PHOTOS (${uiState.photos.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_all_photos")
                )
            }

            if (uiState.photos.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = NubccCyan,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Photos Selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NubccTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose photos from your gallery to start adding your logo watermark.",
                            fontSize = 13.sp,
                            color = NubccTextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PrimaryButton(
                            text = "SELECT PHOTOS",
                            icon = Icons.Default.AddPhotoAlternate,
                            onClick = {
                                addMorePhotosPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            testTag = "batch_select_photos_empty_button"
                        )
                    }
                }
            } else if (selectedTab == 0) {
                // TAB 0: Global Placement Configuration
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Preview Photo Carousel Switcher (Allows testing watermark across 9:16 and 16:9 photos in batch)
                    if (uiState.photos.size > 1) {
                        val currentIdx = uiState.previewPhotoIndex
                        val currentPhoto = uiState.photos.getOrNull(currentIdx)
                        val aspectLabel = if (currentPhoto != null && currentPhoto.width > 0 && currentPhoto.height > 0) {
                            val ratio = currentPhoto.width.toFloat() / currentPhoto.height.toFloat()
                            when {
                                ratio < 0.65f -> "9:16 Portrait"
                                ratio in 0.65f..0.85f -> "3:4 Portrait"
                                ratio in 0.85f..1.15f -> "1:1 Square"
                                ratio in 1.15f..1.45f -> "4:3 Landscape"
                                else -> "16:9 Landscape"
                            }
                        } else "Photo"

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PREVIEW PHOTO (CHECK 9:16 & 16:9)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = NubccCyan
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Photo ${currentIdx + 1} of ${uiState.photos.size}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NubccTextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(NubccElectricViolet)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = aspectLabel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (currentIdx > 0) viewModel.setPreviewPhotoIndex(currentIdx - 1)
                                            },
                                            enabled = currentIdx > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous Photo",
                                                tint = if (currentIdx > 0) NubccTextPrimary else NubccTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (currentIdx < uiState.photos.size - 1) viewModel.setPreviewPhotoIndex(currentIdx + 1)
                                            },
                                            enabled = currentIdx < uiState.photos.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Photo",
                                                tint = if (currentIdx < uiState.photos.size - 1) NubccTextPrimary else NubccTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Thumbnail Selector
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    uiState.photos.forEachIndexed { idx, photo ->
                                        val isCurrent = (idx == currentIdx)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0F1A2E))
                                                .border(
                                                    if (isCurrent) 2.dp else 1.dp,
                                                    if (isCurrent) NubccCyan else Color(0x33FFFFFF),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.setPreviewPhotoIndex(idx) }
                                        ) {
                                            AsyncImage(
                                                model = photo.uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Representative Photo Preview Canvas with multi-logo rendering
                    InteractiveMultiWatermarkCanvas(
                        baseBitmap = uiState.representativeBitmap,
                        layers = uiState.globalLayers,
                        selectedLayerId = uiState.selectedLayerId,
                        onSelectLayer = { viewModel.selectLayer(it) },
                        onPlacementChange = { layerId, newPlacement ->
                            viewModel.updateLayerPlacement(layerId, newPlacement, recordHistory = false)
                        },
                        onDragStart = { viewModel.onDragStart() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Global Multi-Logo Layers Bar
                    if (uiState.globalLayers.isNotEmpty()) {
                        LogoLayersBar(
                            layers = uiState.globalLayers,
                            selectedLayerId = uiState.selectedLayerId,
                            onSelectLayer = { viewModel.selectLayer(it) },
                            onAddLayerClick = {
                                addBatchLayerPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveLayerClick = { viewModel.removeLogoLayer(it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Step 2: Logo Selector & Importer
                    LogoSelectorRow(
                        selectedLogo = uiState.selectedLogo,
                        onLogoSelect = { viewModel.setLogo(it) },
                        customLogos = uiState.customLogos,
                        onAddCustomLogos = { viewModel.addCustomLogos(it) },
                        onRemoveCustomLogo = { viewModel.removeCustomLogo(it) },
                        onAddNewLogoLayer = { viewModel.addNewLogoLayer(it) }
                    )

                    if (uiState.globalLayers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 3: Global Placement Controls for Active Layer
                        PlacementControls(
                            placement = uiState.globalPlacement,
                            onPlacementChange = { viewModel.updateGlobalPlacement(it, recordHistory = false) },
                            onResetToCorner = { viewModel.resetGlobalToCorner() },
                            onAutoPlace = { viewModel.autoPlaceGlobalLogo() },
                            logoAspectRatio = uiState.selectedLogo?.aspectRatio ?: 1.0f,
                            onBeginEdit = { viewModel.recordHistorySnapshot() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Apply to All Photos Button
                    PrimaryButton(
                        text = "APPLY TO ALL ${uiState.photos.size} PHOTOS",
                        icon = Icons.Default.Done,
                        onClick = {
                            selectedTab = 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "apply_to_all_button"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save All Images Direct Action
                    SecondaryButton(
                        text = "SAVE ALL ${uiState.photos.size} IMAGES NOW",
                        icon = Icons.Default.Save,
                        onClick = { viewModel.exportAllImages() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "batch_save_all_button_tab0"
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // TAB 1: Grid of All Photos with Live Overlays
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.photos.size} Photos in Batch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NubccTextPrimary
                        )

                        Text(
                            text = "Tap a photo to edit override",
                            fontSize = 11.sp,
                            color = NubccCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.photos, key = { _, item -> item.id }) { index, photo ->
                            BatchThumbnailCard(
                                photo = photo,
                                index = index,
                                totalCount = uiState.photos.size,
                                globalLayers = uiState.globalLayers,
                                onRemove = { viewModel.removePhoto(photo.id) },
                                onClick = { viewModel.setEditingPhotoIndex(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export Primary Button
                    PrimaryButton(
                        text = "SAVE ALL ${uiState.photos.size} IMAGES",
                        icon = Icons.Default.Save,
                        enabled = !uiState.isExporting && uiState.photos.isNotEmpty(),
                        onClick = { viewModel.exportAllImages() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "batch_save_all_button"
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Individual Photo Override Editor Dialog
        if (uiState.editingPhotoIndex != null) {
            val idx = uiState.editingPhotoIndex!!
            if (idx in uiState.photos.indices) {
                val currentPhoto = uiState.photos[idx]
                val activePlacement = currentPhoto.customPlacement ?: uiState.globalPlacement

                IndividualOverrideDialog(
                    photo = currentPhoto,
                    index = idx,
                    total = uiState.photos.size,
                    logoItem = uiState.selectedLogo,
                    placement = activePlacement,
                    isCustomized = currentPhoto.isCustomized,
                    onPlacementChange = { viewModel.updateIndividualPlacement(it) },
                    onAutoPlace = { bmp -> viewModel.autoPlaceIndividualLogo(bmp) },
                    onResetToGlobal = { viewModel.resetIndividualToGlobal() },
                    onDismiss = { viewModel.setEditingPhotoIndex(null) }
                )
            }
        }

        // Batch Export Progress Dialog
        if (uiState.exportProgress != null) {
            BatchProgressDialog(
                progress = uiState.exportProgress!!,
                onDismiss = { viewModel.dismissExportProgress() }
            )
        }
    }
}

@Composable
private fun BatchThumbnailCard(
    photo: PhotoItem,
    index: Int,
    totalCount: Int,
    globalLayers: List<ActiveLogoLayer>,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val activeLayers = photo.customLayers
        ?: if (photo.customPlacement != null && globalLayers.isNotEmpty()) {
            listOf(globalLayers[0].copy(placement = photo.customPlacement)) + globalLayers.drop(1)
        } else {
            globalLayers
        }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1A2E))
            .border(
                1.5.dp,
                if (photo.isCustomized) NubccAmber else NubccGlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        // Thumbnail Photo
        AsyncImage(
            model = photo.uri,
            contentDescription = "Photo thumbnail",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Accurate Proportionate Watermark Overlays for all active layers
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val cardW = constraints.maxWidth
            val cardH = constraints.maxHeight
            val cardRefDim = minOf(cardW, cardH).toFloat()

            activeLayers.forEach { layer ->
                val placement = layer.placement
                val logoItem = layer.logo
                val logoAspect = if (logoItem.aspectRatio > 0f) logoItem.aspectRatio else 1.0f
                val bounds = placement.calculatePixelBounds(
                    imageWidth = cardW,
                    imageHeight = cardH,
                    logoAspectRatio = logoAspect
                )
                val wDp = with(density) { bounds.width().toDp() }
                val hDp = with(density) { bounds.height().toDp() }
                val xDp = with(density) { bounds.left.toDp() }
                val yDp = with(density) { bounds.top.toDp() }
                val textPx = (cardRefDim * placement.fontSizeFraction).coerceAtLeast(5f)
                val textSp = with(density) { textPx.toSp() }
                val textOffsetYDp = with(density) { (bounds.height() + textPx * 0.1f).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .size(width = wDp, height = hDp)
                        .graphicsLayer {
                            rotationZ = placement.rotationDegrees
                        }
                        .alpha(placement.opacity)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = if (placement.flipHorizontal) -1f else 1f
                            }
                    ) {
                        if (logoItem.drawableResId != null) {
                            Image(
                                painter = painterResource(logoItem.drawableResId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else if (logoItem.uri != null) {
                            AsyncImage(
                                model = logoItem.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    if (placement.watermarkText.isNotBlank()) {
                        Text(
                            text = placement.watermarkText,
                            fontSize = textSp,
                            fontWeight = if (placement.isTextBold) FontWeight.Bold else FontWeight.Normal,
                            color = Color(placement.textColor),
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .wrapContentWidth(unbounded = true)
                                .offset(y = textOffsetYDp)
                        )
                    }
                }
            }
        }

        // Photo Index Badge: e.g. "1 / 12"
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color(0xCC060B16), RoundedCornerShape(topEnd = 6.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${index + 1}/$totalCount",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = NubccTextPrimary
            )
        }

        // Custom indicator tag
        if (photo.isCustomized) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(NubccAmber, RoundedCornerShape(bottomEnd = 6.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EDITED",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }

        // Remove Button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color(0xAA000000), CircleShape)
                .testTag("remove_photo_${photo.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun IndividualOverrideDialog(
    photo: PhotoItem,
    index: Int,
    total: Int,
    logoItem: com.example.model.LogoItem?,
    placement: com.example.model.LogoPlacement,
    isCustomized: Boolean,
    onPlacementChange: (com.example.model.LogoPlacement) -> Unit,
    onAutoPlace: (Bitmap) -> Unit,
    onResetToGlobal: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val photoBitmap by produceState<Bitmap?>(initialValue = null, photo.uri) {
        value = withContext(Dispatchers.IO) {
            com.example.engine.ImageUtils.loadDownscaledBitmap(context, photo.uri, 900)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF0C1424),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Photo ${index + 1} of $total",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NubccTextPrimary
                    )
                    Text(
                        text = if (isCustomized) "Custom Override Applied" else "Using Global Settings",
                        fontSize = 11.sp,
                        color = if (isCustomized) NubccAmber else NubccCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Interactive canvas for this photo (with async loading spinner)
                if (photoBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF070D1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NubccCyan)
                    }
                } else {
                    InteractiveWatermarkCanvas(
                        baseBitmap = photoBitmap,
                        logoItem = logoItem,
                        placement = placement,
                        onPlacementChange = onPlacementChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PlacementControls(
                    placement = placement,
                    onPlacementChange = onPlacementChange,
                    onResetToCorner = {
                        onPlacementChange(com.example.model.LogoPlacement.defaultPlacement())
                    },
                    onAutoPlace = { photoBitmap?.let { onAutoPlace(it) } },
                    logoAspectRatio = logoItem?.aspectRatio ?: 1.0f
                )

                if (isCustomized) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResetToGlobal,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33F59E0B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = NubccAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESET TO GLOBAL SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NubccAmber
                        )
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "APPLY & CLOSE",
                onClick = onDismiss,
                modifier = Modifier.height(42.dp),
                testTag = "override_dialog_apply_button"
            )
        }
    )
}

@Composable
private fun BatchProgressDialog(
    progress: com.example.engine.ExportProgress,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (progress.isComplete) onDismiss()
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF0A1222),
        icon = {
            if (progress.isComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NubccCyan,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = NubccAmber,
                    modifier = Modifier.size(44.dp)
                )
            }
        },
        title = {
            Text(
                text = if (progress.isComplete) "${progress.successfulCount} IMAGES SAVED" else "Applying Logo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NubccTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!progress.isComplete) {
                    Text(
                        text = "${progress.current} / ${progress.total}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        color = NubccCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val progressFraction = if (progress.total > 0) {
                        progress.current.toFloat() / progress.total.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NubccElectricViolet,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Processing photo ${progress.current} of ${progress.total}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NubccTextPrimary
                    )

                    Text(
                        text = progress.currentPhotoName,
                        fontSize = 11.sp,
                        color = NubccTextSecondary,
                        maxLines = 1
                    )
                } else {
                    // Completed View
                    Text(
                        text = "Batch overlay process finished successfully!",
                        fontSize = 14.sp,
                        color = NubccTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Exported", fontSize = 11.sp, color = NubccTextSecondary)
                            Text(
                                "${progress.successfulCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NubccCyan
                            )
                        }
                        if (progress.failedCount > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Failed", fontSize = 11.sp, color = NubccTextSecondary)
                                Text(
                                    "${progress.failedCount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Folder", fontSize = 11.sp, color = NubccTextSecondary)
                            Text(
                                progress.outputFolder.ifBlank { "Pictures/LogoOverlay" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NubccAmber
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress.isComplete) {
                PrimaryButton(
                    text = "VIEW SAVED IMAGES",
                    onClick = {
                        val firstUri = progress.exportedUris.firstOrNull()
                        onDismiss()
                        if (firstUri != null) {
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(firstUri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(viewIntent)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.height(42.dp),
                    testTag = "batch_progress_view_saved_button"
                )
            }
        },
        dismissButton = {
            if (progress.isComplete) {
                SecondaryButton(
                    text = "DONE",
                    onClick = onDismiss,
                    modifier = Modifier.height(42.dp),
                    testTag = "batch_progress_done_button"
                )
            }
        }
    )
}
