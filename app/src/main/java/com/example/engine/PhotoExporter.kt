// © 2026 App Owner. All rights reserved.
package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.AppDatabase
import com.example.data.entity.RecentExportEntity
import com.example.model.ActiveLogoLayer
import com.example.model.ExportSettings
import com.example.model.LogoItem
import com.example.model.LogoPlacement
import com.example.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ExportProgress(
    val current: Int,
    val total: Int,
    val currentPhotoName: String,
    val isComplete: Boolean = false,
    val successfulCount: Int = 0,
    val failedCount: Int = 0,
    val exportedUris: List<Uri> = emptyList(),
    val outputFolder: String = "Pictures/LogoOverlay"
)

object PhotoExporter {

    /**
     * Exports a single photo with MULTIPLE logo layers in FULL original resolution or selected resize target.
     */
    suspend fun exportSingleWithLayers(
        context: Context,
        photoUri: Uri,
        layers: List<ActiveLogoLayer>,
        settings: ExportSettings = ExportSettings(),
        photoIndex: Int = 1
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val originalName = ImageUtils.getDisplayName(context, photoUri)

            val (compressFormat, extension) = when (settings.exportFormat) {
                com.example.model.ExportFormat.PNG -> Pair(Bitmap.CompressFormat.PNG, ".png")
                com.example.model.ExportFormat.WEBP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Pair(Bitmap.CompressFormat.WEBP_LOSSY, ".webp")
                    } else {
                        @Suppress("DEPRECATION")
                        Pair(Bitmap.CompressFormat.WEBP, ".webp")
                    }
                }
                com.example.model.ExportFormat.JPEG -> Pair(Bitmap.CompressFormat.JPEG, ".jpg")
            }

            // 1. Load photo (memory-safe handling for 12MP+ photos)
            val loadedBitmap: Bitmap? = try {
                ImageUtils.loadFullResolutionBitmap(context, photoUri)
            } catch (_: OutOfMemoryError) {
                System.gc()
                ImageUtils.loadDownscaledBitmap(context, photoUri, 2560)
            }

            if (loadedBitmap == null) {
                return@withContext Result.failure(Exception("Failed to decode photo memory"))
            }

            var activeBitmap: Bitmap = loadedBitmap

            // 2. Check resize mode
            if (settings.resizeMode != com.example.model.ResizeMode.ORIGINAL) {
                val maxDim = settings.resizeMode.maxDimension
                val currentMax = maxOf(activeBitmap.width, activeBitmap.height)
                if (currentMax > maxDim) {
                    val scale = maxDim.toFloat() / currentMax.toFloat()
                    val targetW = (activeBitmap.width * scale).roundToInt().coerceAtLeast(1)
                    val targetH = (activeBitmap.height * scale).roundToInt().coerceAtLeast(1)
                    val resized = Bitmap.createScaledBitmap(activeBitmap, targetW, targetH, true)
                    if (resized != activeBitmap) {
                        activeBitmap.recycle()
                        activeBitmap = resized
                    }
                }
            }

            val width = activeBitmap.width
            val height = activeBitmap.height

            // 3. Sequentially composite each active logo layer
            var currentBitmap: Bitmap = activeBitmap

            for (layer in layers) {
                val bounds = layer.placement.calculatePixelBounds(
                    imageWidth = width,
                    imageHeight = height,
                    logoAspectRatio = layer.logo.aspectRatio
                )

                val targetLogoW = bounds.width().roundToInt().coerceAtLeast(10)
                val targetLogoH = bounds.height().roundToInt().coerceAtLeast(10)

                val logoBitmap = ImageUtils.loadLogoBitmap(
                    context = context,
                    logoItem = layer.logo,
                    targetWidth = targetLogoW,
                    targetHeight = targetLogoH
                ) ?: continue

                val nextBitmap = WatermarkCompositor.composite(
                    baseBitmap = currentBitmap,
                    logoBitmap = logoBitmap,
                    placement = layer.placement
                )

                if (currentBitmap != activeBitmap && currentBitmap != nextBitmap) {
                    currentBitmap.recycle()
                }
                if (logoBitmap != nextBitmap) {
                    logoBitmap.recycle()
                }

                currentBitmap = nextBitmap
            }

            // 4. Generate filename using template
            val exportDisplayName = generateFilename(
                template = settings.filenameTemplate,
                originalName = originalName,
                index = photoIndex,
                extension = extension
            )

            // 5. Save to MediaStore with privacy-preserved or retained EXIF
            val savedUri = saveToMediaStore(
                context = context,
                bitmap = currentBitmap,
                displayName = exportDisplayName,
                compressFormat = compressFormat,
                mimeType = settings.exportFormat.mimeType,
                quality = settings.jpegQuality,
                folderName = settings.folderName
            ) ?: return@withContext Result.failure(Exception("Failed to write image to gallery storage"))

            // Recycle composited bitmap
            currentBitmap.recycle()

            if (!settings.stripExif) {
                ImageUtils.copyExifMetadata(context, photoUri, savedUri)
            }

            // Record into Room Database
            try {
                val db = AppDatabase.getInstance(context)
                db.recentExportDao().insertExport(
                    RecentExportEntity(
                        displayName = exportDisplayName,
                        mediaStoreUri = savedUri.toString(),
                        width = width,
                        height = height,
                        photoCount = 1
                    )
                )
            } catch (_: Exception) {}

            Result.success(savedUri)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            System.gc()
        }
    }

    fun generateFilename(
        template: String,
        originalName: String,
        index: Int,
        extension: String
    ): String {
        val baseName = originalName.substringBeforeLast(".")
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val timeStr = SimpleDateFormat("HHmmss", Locale.US).format(Date())
        val indexStr = String.format(Locale.US, "%03d", index)

        val cleanTemplate = if (template.isBlank()) "LOGO_{name}_{index}" else template
        val resolved = cleanTemplate
            .replace("{name}", baseName)
            .replace("{index}", indexStr)
            .replace("{001}", indexStr)
            .replace("{date}", dateStr)
            .replace("{time}", timeStr)
            .replace("{event}", "EVENT")

        return if (resolved.endsWith(extension, ignoreCase = true)) resolved else "$resolved$extension"
    }

    /**
     * Backward-compatible export single with 1 logo.
     */
    suspend fun exportSingle(
        context: Context,
        photoUri: Uri,
        logoItem: LogoItem,
        placement: LogoPlacement,
        settings: ExportSettings = ExportSettings()
    ): Result<Uri> = exportSingleWithLayers(
        context = context,
        photoUri = photoUri,
        layers = listOf(ActiveLogoLayer(logo = logoItem, placement = placement)),
        settings = settings
    )

    /**
     * Batch export with multi-logo layers.
     */
    fun exportBatchWithLayers(
        context: Context,
        photos: List<PhotoItem>,
        globalLayers: List<ActiveLogoLayer>,
        settings: ExportSettings = ExportSettings()
    ): Flow<ExportProgress> = flow {
        val total = photos.size
        var successCount = 0
        var failCount = 0
        val savedUris = mutableListOf<Uri>()

        emit(
            ExportProgress(
                current = 0,
                total = total,
                currentPhotoName = if (photos.isNotEmpty()) photos[0].displayName else "",
                isComplete = false
            )
        )

        for (i in photos.indices) {
            val photo = photos[i]
            val activeLayers = photo.customLayers
                ?: if (photo.customPlacement != null && globalLayers.isNotEmpty()) {
                    listOf(globalLayers[0].copy(placement = photo.customPlacement)) + globalLayers.drop(1)
                } else {
                    globalLayers
                }

            emit(
                ExportProgress(
                    current = i + 1,
                    total = total,
                    currentPhotoName = photo.displayName,
                    isComplete = false,
                    successfulCount = successCount,
                    failedCount = failCount,
                    exportedUris = savedUris
                )
            )

            val result = exportSingleWithLayers(
                context = context,
                photoUri = photo.uri,
                layers = activeLayers,
                settings = settings,
                photoIndex = i + 1
            )

            if (result.isSuccess) {
                successCount++
                result.getOrNull()?.let { savedUris.add(it) }
            } else {
                failCount++
            }
        }

        // Record batch summary in recent exports
        if (savedUris.isNotEmpty()) {
            try {
                val db = AppDatabase.getInstance(context)
                db.recentExportDao().insertExport(
                    RecentExportEntity(
                        displayName = "Batch Export ($successCount photos)",
                        mediaStoreUri = savedUris.first().toString(),
                        width = 0,
                        height = 0,
                        photoCount = successCount
                    )
                )
            } catch (_: Exception) {}
        }

        emit(
            ExportProgress(
                current = total,
                total = total,
                currentPhotoName = "Complete",
                isComplete = true,
                successfulCount = successCount,
                failedCount = failCount,
                exportedUris = savedUris,
                outputFolder = settings.folderName
            )
        )
    }.flowOn(Dispatchers.IO)

    /**
     * Backward-compatible exportBatch with 1 logo.
     */
    fun exportBatch(
        context: Context,
        photos: List<PhotoItem>,
        logoItem: LogoItem,
        globalPlacement: LogoPlacement,
        settings: ExportSettings = ExportSettings()
    ): Flow<ExportProgress> = exportBatchWithLayers(
        context = context,
        photos = photos,
        globalLayers = listOf(ActiveLogoLayer(logo = logoItem, placement = globalPlacement)),
        settings = settings
    )

    private fun saveToMediaStore(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        compressFormat: Bitmap.CompressFormat,
        mimeType: String,
        quality: Int,
        folderName: String
    ): Uri? {
        val resolver = context.contentResolver
        val subFolder = folderName.removePrefix("Pictures/").trim('/').ifBlank { "LogoOverlay" }
        val relativePath = Environment.DIRECTORY_PICTURES + File.separator + subFolder

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        return try {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use { stream ->
                bitmap.compress(compressFormat, quality, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }
}
