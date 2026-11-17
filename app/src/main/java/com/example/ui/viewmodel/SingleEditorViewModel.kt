package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ImageUtils
import com.example.engine.PhotoExporter
import com.example.model.ActiveLogoLayer
import com.example.model.ExportSettings
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import com.example.model.PositionPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class SingleEditorUiState(
    val photoUri: Uri? = null,
    val photoBitmap: Bitmap? = null,
    val layers: List<ActiveLogoLayer> = emptyList(), // Clean start: no preloaded logos
    val selectedLayerId: String? = null,
    val customLogos: List<LogoItem> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isExporting: Boolean = false,
    val exportedUri: Uri? = null,
    val errorMessage: String? = null
) {
    val activeLayer: ActiveLogoLayer?
        get() = layers.find { it.id == selectedLayerId } ?: layers.firstOrNull()

    val selectedLogo: LogoItem?
        get() = activeLayer?.logo

    val placement: LogoPlacement
        get() = activeLayer?.placement ?: LogoPlacement.defaultPlacement()
}

class SingleEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SingleEditorUiState())
    val uiState: StateFlow<SingleEditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<List<ActiveLogoLayer>>()
    private val redoStack = mutableListOf<List<ActiveLogoLayer>>()

    fun setPhoto(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val bitmap = withContext(Dispatchers.IO) {
                ImageUtils.loadDownscaledBitmap(context, uri, 1280)
            }
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        photoUri = uri,
                        photoBitmap = bitmap,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Could not load selected photo format") }
            }
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
        if (_uiState.value.layers.any { it.logo.id == logoId }) {
            recordHistory()
        }
        _uiState.update { state ->
            val remainingLayers = state.layers.filterNot { it.logo.id == logoId }
            val nextSelected = if (remainingLayers.any { it.id == state.selectedLayerId }) {
                state.selectedLayerId
            } else {
                remainingLayers.firstOrNull()?.id
            }
            state.copy(
                customLogos = state.customLogos.filterNot { it.id == logoId },
                layers = remainingLayers,
                selectedLayerId = nextSelected
            )
        }
    }

    fun addNewLogoLayer(logo: LogoItem) {
        recordHistory()
        val currentLayers = _uiState.value.layers
        // Smart placement for next layer so they don't cover each other
        val nextPreset = when (currentLayers.size) {
            0 -> PositionPreset.BOTTOM_RIGHT
            1 -> PositionPreset.TOP_LEFT
            2 -> PositionPreset.TOP_RIGHT
            3 -> PositionPreset.BOTTOM_LEFT
            else -> PositionPreset.CENTER
        }

        val newLayer = ActiveLogoLayer(
            id = "layer_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            logo = logo,
            placement = LogoPlacement.fromPreset(nextPreset, width = 0.18f, margin = 0.035f)
        )

        _uiState.update {
            it.copy(
                layers = it.layers + newLayer,
                selectedLayerId = newLayer.id
            )
        }
    }

    fun removeLogoLayer(layerId: String) {
        val currentLayers = _uiState.value.layers
        recordHistory()
        val remaining = currentLayers.filterNot { it.id == layerId }
        val newSelectedId = if (_uiState.value.selectedLayerId == layerId) {
            remaining.lastOrNull()?.id
        } else {
            _uiState.value.selectedLayerId
        }

        _uiState.update {
            it.copy(layers = remaining, selectedLayerId = newSelectedId)
        }
    }

    fun selectLayer(layerId: String) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    fun setLogo(logo: LogoItem?) {
        recordHistory()
        val currentLayers = _uiState.value.layers.toMutableList()
        val activeId = _uiState.value.selectedLayerId

        if (currentLayers.isEmpty()) {
            if (logo != null) {
                val newLayer = ActiveLogoLayer(logo = logo)
                _uiState.update {
                    it.copy(layers = listOf(newLayer), selectedLayerId = newLayer.id)
                }
            }
            return
        }

        val idx = currentLayers.indexOfFirst { it.id == activeId }
        if (idx != -1) {
            if (logo != null) {
                currentLayers[idx] = currentLayers[idx].copy(logo = logo)
                _uiState.update { it.copy(layers = currentLayers) }
            } else {
                currentLayers.removeAt(idx)
                val nextSelected = currentLayers.firstOrNull()?.id
                _uiState.update { it.copy(layers = currentLayers, selectedLayerId = nextSelected) }
            }
        } else if (logo != null) {
            val newLayer = ActiveLogoLayer(logo = logo)
            _uiState.update {
                it.copy(layers = it.layers + newLayer, selectedLayerId = newLayer.id)
            }
        }
    }

    fun recordHistorySnapshot() {
        recordHistory()
    }

    fun onDragStart() {
        recordHistory()
    }

    fun updatePlacement(newPlacement: LogoPlacement, recordHistory: Boolean = true) {
        val activeId = _uiState.value.selectedLayerId
        if (activeId != null) {
            updateLayerPlacement(activeId, newPlacement, recordHistory)
        }
    }

    fun updateLayerPlacement(layerId: String, newPlacement: LogoPlacement, recordHistory: Boolean = false) {
        if (recordHistory) {
            recordHistory()
        }
        val currentLayers = _uiState.value.layers.toMutableList()
        val idx = currentLayers.indexOfFirst { it.id == layerId }
        if (idx != -1) {
            currentLayers[idx] = currentLayers[idx].copy(placement = newPlacement)
            _uiState.update {
                it.copy(
                    layers = currentLayers,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }

    fun autoPlaceLogo() {
        val bitmap = _uiState.value.photoBitmap ?: return
        val analysis = com.example.engine.SmartPlacementEngine.analyzePlacement(bitmap)
        val currentPlacement = _uiState.value.placement
        val newPlacement = currentPlacement.copy(
            preset = analysis.recommendedPreset,
            textColor = if (analysis.isDarkBackground) 0xFFFFFFFF else 0xFF0F172A,
            shadowEnabled = !analysis.isDarkBackground || currentPlacement.shadowEnabled
        )
        updatePlacement(newPlacement, recordHistory = true)
    }

    fun resetToCorner() {
        recordHistory()
        val defaultPlacement = LogoPlacement.defaultPlacement()
        updatePlacement(defaultPlacement, recordHistory = false)
    }

    private fun recordHistory() {
        val snapshot = _uiState.value.layers.map { it.copy() }
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
        val current = _uiState.value.layers.map { it.copy() }
        redoStack.add(current)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val currentSel = _uiState.value.selectedLayerId
        val nextSel = if (previous.any { it.id == currentSel }) currentSel else previous.firstOrNull()?.id
        _uiState.update {
            it.copy(
                layers = previous,
                selectedLayerId = nextSel,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.layers.map { it.copy() }
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        val currentSel = _uiState.value.selectedLayerId
        val nextSel = if (next.any { it.id == currentSel }) currentSel else next.firstOrNull()?.id
        _uiState.update {
            it.copy(
                layers = next,
                selectedLayerId = nextSel,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun exportPhoto(settings: ExportSettings? = null) {
        val uri = _uiState.value.photoUri ?: return
        val layers = _uiState.value.layers
        if (layers.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please import and select a logo before exporting.") }
            return
        }
        val context = getApplication<Application>()
        val activeSettings = settings ?: com.example.data.SettingsManager(context).getExportSettings()

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null, exportedUri = null) }
            val result = PhotoExporter.exportSingleWithLayers(
                context = context,
                photoUri = uri,
                layers = layers,
                settings = activeSettings
            )

            result.fold(
                onSuccess = { savedUri ->
                    _uiState.update { it.copy(isExporting = false, exportedUri = savedUri) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = err.message ?: "Failed to export photo"
                        )
                    }
                }
            )
        }
    }

    fun exportImage(settings: ExportSettings = ExportSettings()) = exportPhoto(settings)

    fun dismissExportDialog() {
        clearExportResult()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportedUri = null, errorMessage = null) }
    }
}
