package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ExportProgress
import com.example.engine.ImageUtils
import com.example.engine.PhotoExporter
import com.example.model.ActiveLogoLayer
import com.example.model.ExportSettings
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import com.example.model.PhotoItem
import com.example.model.PositionPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class BatchStudioUiState(
    val photos: List<PhotoItem> = emptyList(),
    val previewPhotoIndex: Int = 0,
    val representativeBitmap: Bitmap? = null,
    val globalLayers: List<ActiveLogoLayer> = emptyList(), // Clean start: no preloaded logos
    val selectedLayerId: String? = null,
    val customLogos: List<LogoItem> = emptyList(),
    val editingPhotoIndex: Int? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: ExportProgress? = null,
    val errorMessage: String? = null
) {
    val activeLayer: ActiveLogoLayer?
        get() = globalLayers.find { it.id == selectedLayerId } ?: globalLayers.firstOrNull()

    val selectedLogo: LogoItem?
        get() = activeLayer?.logo

    val globalPlacement: LogoPlacement
        get() = activeLayer?.placement ?: LogoPlacement.defaultPlacement()

    val currentPreviewPhoto: PhotoItem?
        get() = photos.getOrNull(previewPhotoIndex)
}

class BatchStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BatchStudioUiState())
    val uiState: StateFlow<BatchStudioUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<List<ActiveLogoLayer>>()
    private val redoStack = mutableListOf<List<ActiveLogoLayer>>()
    private var representativeJob: Job? = null

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val newPhotos = withContext(Dispatchers.IO) {
                uris.map { uri ->
                    val (w, h) = ImageUtils.getImageDimensions(context, uri)
                    PhotoItem(
                        id = UUID.randomUUID().toString(),
                        uri = uri,
                        displayName = ImageUtils.getDisplayName(context, uri),
                        width = w,
                        height = h
                    )
                }
            }

            _uiState.update { current ->
                val updatedPhotos = current.photos + newPhotos
                current.copy(photos = updatedPhotos)
            }

            if (_uiState.value.representativeBitmap == null && newPhotos.isNotEmpty()) {
                loadRepresentativeBitmap(newPhotos.first().uri)
            }
        }
    }

    fun removePhoto(id: String) {
        _uiState.update { current ->
            val updated = current.photos.filterNot { it.id == id }
            val nextIdx = if (updated.isEmpty()) 0 else current.previewPhotoIndex.coerceIn(0, updated.lastIndex)
            val nextRep = if (updated.isNotEmpty()) current.representativeBitmap else null
            current.copy(photos = updated, previewPhotoIndex = nextIdx, representativeBitmap = nextRep)
        }
        val state = _uiState.value
        if (state.photos.isNotEmpty()) {
            loadRepresentativeBitmap(state.photos[state.previewPhotoIndex].uri)
        }
    }

    fun clearAllPhotos() {
        representativeJob?.cancel()
        _uiState.update { it.copy(photos = emptyList(), previewPhotoIndex = 0, representativeBitmap = null) }
    }

    fun setPreviewPhotoIndex(index: Int) {
        val photos = _uiState.value.photos
        if (index in photos.indices) {
            _uiState.update { it.copy(previewPhotoIndex = index) }
            loadRepresentativeBitmap(photos[index].uri)
        }
    }

    private fun loadRepresentativeBitmap(uri: Uri) {
        representativeJob?.cancel()
        representativeJob = viewModelScope.launch {
            val context = getApplication<Application>()
            val bmp = withContext(Dispatchers.IO) {
                ImageUtils.loadDownscaledBitmap(context, uri, 1080)
            }
            _uiState.update { it.copy(representativeBitmap = bmp) }
        }
    }

    fun addCustomLogos(newLogos: List<LogoItem>) {
        if (newLogos.isEmpty()) return
        _uiState.update { state ->
            val existingIds = state.customLogos.map { it.id }.toSet()
            val filtered = newLogos.filterNot { it.id in existingIds }
            state.copy(customLogos = state.customLogos + filtered)
        }
    }

    fun removeCustomLogo(logoId: String) {
        if (_uiState.value.globalLayers.any { it.logo.id == logoId }) {
            recordHistory()
        }
        _uiState.update { state ->
            val remainingLayers = state.globalLayers.filterNot { it.logo.id == logoId }
            val nextSelected = if (remainingLayers.any { it.id == state.selectedLayerId }) {
                state.selectedLayerId
            } else {
                remainingLayers.firstOrNull()?.id
            }
            state.copy(
                customLogos = state.customLogos.filterNot { it.id == logoId },
                globalLayers = remainingLayers,
                selectedLayerId = nextSelected
            )
        }
    }

    fun addNewLogoLayer(logo: LogoItem) {
        recordHistory()
        val currentLayers = _uiState.value.globalLayers
        val nextPreset = when (currentLayers.size) {
            0 -> PositionPreset.BOTTOM_RIGHT
            1 -> PositionPreset.TOP_LEFT
            2 -> PositionPreset.TOP_RIGHT
            3 -> PositionPreset.BOTTOM_LEFT
            else -> PositionPreset.CENTER
        }

        val newLayer = ActiveLogoLayer(
            id = "batch_layer_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            logo = logo,
            placement = LogoPlacement.fromPreset(nextPreset, width = 0.18f, margin = 0.035f)
        )

        _uiState.update {
            it.copy(
                globalLayers = it.globalLayers + newLayer,
                selectedLayerId = newLayer.id
            )
        }
    }

    fun removeLogoLayer(layerId: String) {
        recordHistory()
        val currentLayers = _uiState.value.globalLayers
        val remaining = currentLayers.filterNot { it.id == layerId }
        val newSelectedId = if (_uiState.value.selectedLayerId == layerId) {
            remaining.lastOrNull()?.id
        } else {
            _uiState.value.selectedLayerId
        }

        _uiState.update {
            it.copy(globalLayers = remaining, selectedLayerId = newSelectedId)
        }
    }

    fun selectLayer(layerId: String) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    fun setLogo(logo: LogoItem?) {
        recordHistory()
        val currentLayers = _uiState.value.globalLayers.toMutableList()
        val activeId = _uiState.value.selectedLayerId

        if (currentLayers.isEmpty()) {
            if (logo != null) {
                val newLayer = ActiveLogoLayer(logo = logo)
                _uiState.update {
                    it.copy(globalLayers = listOf(newLayer), selectedLayerId = newLayer.id)
                }
            }
            return
        }

        val idx = currentLayers.indexOfFirst { it.id == activeId }
        if (idx != -1) {
            if (logo != null) {
                currentLayers[idx] = currentLayers[idx].copy(logo = logo)
                _uiState.update { it.copy(globalLayers = currentLayers) }
            } else {
                currentLayers.removeAt(idx)
                val nextSelected = currentLayers.firstOrNull()?.id
                _uiState.update { it.copy(globalLayers = currentLayers, selectedLayerId = nextSelected) }
            }
        } else if (logo != null) {
            val newLayer = ActiveLogoLayer(logo = logo)
            _uiState.update {
                it.copy(globalLayers = it.globalLayers + newLayer, selectedLayerId = newLayer.id)
            }
        }
    }

    fun recordHistorySnapshot() {
        recordHistory()
    }

    fun onDragStart() {
        recordHistory()
    }

    fun updateGlobalPlacement(placement: LogoPlacement, recordHistory: Boolean = false) {
        val activeId = _uiState.value.selectedLayerId
        if (activeId != null) {
            updateLayerPlacement(activeId, placement, recordHistory)
        }
    }

    fun updateLayerPlacement(layerId: String, placement: LogoPlacement, recordHistory: Boolean = false) {
        if (recordHistory) {
            recordHistory()
        }
        val currentLayers = _uiState.value.globalLayers.toMutableList()
        val idx = currentLayers.indexOfFirst { it.id == layerId }
        if (idx != -1) {
            currentLayers[idx] = currentLayers[idx].copy(placement = placement)
            _uiState.update {
                it.copy(
                    globalLayers = currentLayers,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun autoPlaceGlobalLogo() {
        val bitmap = _uiState.value.representativeBitmap ?: return
        val analysis = com.example.engine.SmartPlacementEngine.analyzePlacement(bitmap)
        val currentPlacement = _uiState.value.globalPlacement
        val newPlacement = currentPlacement.copy(
            preset = analysis.recommendedPreset,
            textColor = if (analysis.isDarkBackground) 0xFFFFFFFF else 0xFF0F172A,
            shadowEnabled = !analysis.isDarkBackground || currentPlacement.shadowEnabled
        )
        updateGlobalPlacement(newPlacement, recordHistory = true)
    }

    fun resetGlobalToCorner() {
        recordHistory()
        updateGlobalPlacement(LogoPlacement.defaultPlacement(), recordHistory = false)
    }

    private fun recordHistory() {
        val snapshot = _uiState.value.globalLayers.map { it.copy() }
        if (undoStack.lastOrNull() == snapshot) return
        undoStack.add(snapshot)
        if (undoStack.size > 25) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _uiState.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.globalLayers.map { it.copy() }
        redoStack.add(current)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val currentSel = _uiState.value.selectedLayerId
        val nextSel = if (previous.any { it.id == currentSel }) currentSel else previous.firstOrNull()?.id
        _uiState.update {
            it.copy(
                globalLayers = previous,
                selectedLayerId = nextSel,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.globalLayers.map { it.copy() }
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        val currentSel = _uiState.value.selectedLayerId
        val nextSel = if (next.any { it.id == currentSel }) currentSel else next.firstOrNull()?.id
        _uiState.update {
            it.copy(
                globalLayers = next,
                selectedLayerId = nextSel,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun setEditingPhotoIndex(index: Int?) {
        _uiState.update { it.copy(editingPhotoIndex = index) }
    }

    fun updateIndividualPlacement(placement: LogoPlacement) {
        val index = _uiState.value.editingPhotoIndex ?: return
        val currentPhotos = _uiState.value.photos.toMutableList()
        if (index in currentPhotos.indices) {
            val photo = currentPhotos[index]
            val updatedLayers = photo.customLayers?.let { layers ->
                if (layers.isNotEmpty()) {
                    listOf(layers[0].copy(placement = placement)) + layers.drop(1)
                } else layers
            }
            currentPhotos[index] = photo.copy(customPlacement = placement, customLayers = updatedLayers)
            _uiState.update { it.copy(photos = currentPhotos) }
        }
    }

    fun autoPlaceIndividualLogo(photoBitmap: Bitmap) {
        val index = _uiState.value.editingPhotoIndex ?: return
        val currentPhotos = _uiState.value.photos.toMutableList()
        if (index !in currentPhotos.indices) return
        val photo = currentPhotos[index]
        val currentPlacement = photo.customPlacement ?: _uiState.value.globalPlacement
        val analysis = com.example.engine.SmartPlacementEngine.analyzePlacement(photoBitmap)
        val newPlacement = currentPlacement.copy(
            preset = analysis.recommendedPreset,
            textColor = if (analysis.isDarkBackground) 0xFFFFFFFF else 0xFF0F172A,
            shadowEnabled = !analysis.isDarkBackground || currentPlacement.shadowEnabled
        )
        updateIndividualPlacement(newPlacement)
    }

    fun resetIndividualToGlobal() {
        val index = _uiState.value.editingPhotoIndex ?: return
        val currentPhotos = _uiState.value.photos.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos[index] = currentPhotos[index].copy(customPlacement = null, customLayers = null)
            _uiState.update { it.copy(photos = currentPhotos) }
        }
    }

    fun exportAllImages(settings: ExportSettings? = null) {
        val photos = _uiState.value.photos
        if (photos.isEmpty()) return

        val globalLayers = _uiState.value.globalLayers
        if (globalLayers.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please import and select at least one logo before exporting") }
            return
        }

        val context = getApplication<Application>()
        val activeSettings = settings ?: com.example.data.SettingsManager(context).getExportSettings()

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = null, errorMessage = null) }
            PhotoExporter.exportBatchWithLayers(
                context = context,
                photos = photos,
                globalLayers = globalLayers,
                settings = activeSettings
            ).collect { progress ->
                _uiState.update { it.copy(exportProgress = progress) }
                if (progress.isComplete) {
                    _uiState.update { it.copy(isExporting = false) }
                }
            }
        }
    }

    fun dismissExportProgress() {
        _uiState.update { it.copy(exportProgress = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
