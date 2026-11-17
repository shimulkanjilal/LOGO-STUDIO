// © 2026 App Owner. All rights reserved.
package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ExportFormat
import com.example.model.ExportSettings
import com.example.model.PositionPreset
import com.example.model.ResizeMode

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nubcc_studio_prefs", Context.MODE_PRIVATE)

    var exportFormat: ExportFormat
        get() = try {
            ExportFormat.valueOf(prefs.getString(KEY_EXPORT_FORMAT, ExportFormat.JPEG.name) ?: ExportFormat.JPEG.name)
        } catch (_: Exception) {
            ExportFormat.JPEG
        }
        set(value) = prefs.edit().putString(KEY_EXPORT_FORMAT, value.name).apply()

    var resizeMode: ResizeMode
        get() = try {
            ResizeMode.valueOf(prefs.getString(KEY_RESIZE_MODE, ResizeMode.ORIGINAL.name) ?: ResizeMode.ORIGINAL.name)
        } catch (_: Exception) {
            ResizeMode.ORIGINAL
        }
        set(value) = prefs.edit().putString(KEY_RESIZE_MODE, value.name).apply()

    var jpegQuality: Int
        get() = prefs.getInt(KEY_JPEG_QUALITY, 98)
        set(value) = prefs.edit().putInt(KEY_JPEG_QUALITY, value.coerceIn(50, 100)).apply()

    var stripExif: Boolean
        get() = prefs.getBoolean(KEY_STRIP_EXIF, true)
        set(value) = prefs.edit().putBoolean(KEY_STRIP_EXIF, value).apply()

    var filenameTemplate: String
        get() = prefs.getString(KEY_FILENAME_TEMPLATE, "LOGO_{name}_{index}") ?: "LOGO_{name}_{index}"
        set(value) = prefs.edit().putString(KEY_FILENAME_TEMPLATE, value).apply()

    var defaultPreset: PositionPreset
        get() = try {
            PositionPreset.valueOf(prefs.getString(KEY_DEFAULT_PRESET, PositionPreset.BOTTOM_RIGHT.name) ?: PositionPreset.BOTTOM_RIGHT.name)
        } catch (_: Exception) {
            PositionPreset.BOTTOM_RIGHT
        }
        set(value) = prefs.edit().putString(KEY_DEFAULT_PRESET, value.name).apply()

    fun getExportSettings(): ExportSettings {
        return ExportSettings(
            exportFormat = exportFormat,
            resizeMode = resizeMode,
            stripExif = stripExif,
            filenameTemplate = filenameTemplate,
            jpegQuality = jpegQuality
        )
    }

    companion object {
        private const val KEY_EXPORT_FORMAT = "export_format"
        private const val KEY_RESIZE_MODE = "resize_mode"
        private const val KEY_JPEG_QUALITY = "jpeg_quality"
        private const val KEY_STRIP_EXIF = "strip_exif"
        private const val KEY_FILENAME_TEMPLATE = "filename_template"
        private const val KEY_DEFAULT_PRESET = "default_preset"
    }
}
