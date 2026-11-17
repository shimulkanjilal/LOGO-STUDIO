// © 2026 App Owner. All rights reserved.
package com.example.model

import android.net.Uri
import java.util.UUID

data class ActiveLogoLayer(
    val id: String = UUID.randomUUID().toString(),
    val logo: LogoItem,
    val placement: LogoPlacement = LogoPlacement.defaultPlacement()
)

data class PhotoItem(
    val id: String,
    val uri: Uri,
    val displayName: String,
    val width: Int = 0,
    val height: Int = 0,
    val customPlacement: LogoPlacement? = null, // Backward compatibility
    val customLayers: List<ActiveLogoLayer>? = null // Multi-logo layer overrides
) {
    val isCustomized: Boolean
        get() = customPlacement != null || customLayers != null
}

data class LogoItem(
    val id: String,
    val name: String,
    val uri: Uri? = null,
    val drawableResId: Int? = null,
    val aspectRatio: Float = 1.0f,
    val isBuiltIn: Boolean = (drawableResId != null)
)

enum class ExportFormat(val displayName: String, val mimeType: String, val extension: String) {
    JPEG("JPEG (.jpg)", "image/jpeg", ".jpg"),
    PNG("PNG (.png)", "image/png", ".png"),
    WEBP("WebP (.webp)", "image/webp", ".webp")
}

enum class ResizeMode(val displayName: String, val maxDimension: Int) {
    ORIGINAL("Original Resolution", 0),
    RESIZE_2048("Web Standard (2048px)", 2048),
    RESIZE_1080("Social 1080p (1080px)", 1080)
}

data class ExportSettings(
    val exportFormat: ExportFormat = ExportFormat.JPEG,
    val resizeMode: ResizeMode = ResizeMode.ORIGINAL,
    val stripExif: Boolean = true,
    val filenameTemplate: String = "LOGO_{name}_{index}",
    val jpegQuality: Int = 98,
    val preservePng: Boolean = true,
    val addTimestampToFilename: Boolean = false,
    val filenameSuffix: String = "_LOGO",
    val folderName: String = "Pictures/LogoOverlay"
)

