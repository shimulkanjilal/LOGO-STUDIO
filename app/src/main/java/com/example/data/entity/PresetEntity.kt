package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LogoPlacement
import com.example.model.PositionPreset

@Entity(tableName = "logo_presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val presetName: String,
    val normalizedX: Float = 0.78f,
    val normalizedY: Float = 0.78f,
    val normalizedWidth: Float,
    val normalizedMargin: Float,
    val opacity: Float,
    val removeWhiteBg: Boolean = false,
    val adaptiveScale: Boolean = true,
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val shadowEnabled: Boolean = false,
    val shadowRadius: Float = 10f,
    val watermarkText: String = "",
    val textColor: Long = 0xFFFFFFFF,
    val isTextBold: Boolean = true,
    val fontSizeFraction: Float = 0.035f,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toPlacement(): LogoPlacement {
        val positionPreset = try {
            PositionPreset.valueOf(presetName)
        } catch (_: Exception) {
            PositionPreset.BOTTOM_RIGHT
        }
        return LogoPlacement(
            preset = positionPreset,
            normalizedX = normalizedX,
            normalizedY = normalizedY,
            normalizedWidth = normalizedWidth,
            normalizedMargin = normalizedMargin,
            opacity = opacity,
            removeWhiteBg = removeWhiteBg,
            adaptiveScale = adaptiveScale,
            rotationDegrees = rotationDegrees,
            flipHorizontal = flipHorizontal,
            shadowEnabled = shadowEnabled,
            shadowRadius = shadowRadius,
            watermarkText = watermarkText,
            textColor = textColor,
            isTextBold = isTextBold,
            fontSizeFraction = fontSizeFraction
        )
    }

    companion object {
        fun fromPlacement(name: String, placement: LogoPlacement): PresetEntity {
            return PresetEntity(
                name = name,
                presetName = placement.preset.name,
                normalizedX = placement.normalizedX,
                normalizedY = placement.normalizedY,
                normalizedWidth = placement.normalizedWidth,
                normalizedMargin = placement.normalizedMargin,
                opacity = placement.opacity,
                removeWhiteBg = placement.removeWhiteBg,
                adaptiveScale = placement.adaptiveScale,
                rotationDegrees = placement.rotationDegrees,
                flipHorizontal = placement.flipHorizontal,
                shadowEnabled = placement.shadowEnabled,
                shadowRadius = placement.shadowRadius,
                watermarkText = placement.watermarkText,
                textColor = placement.textColor,
                isTextBold = placement.isTextBold,
                fontSizeFraction = placement.fontSizeFraction
            )
        }
    }
}
