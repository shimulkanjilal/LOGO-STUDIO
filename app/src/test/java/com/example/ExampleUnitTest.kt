package com.example

import com.example.data.entity.PresetEntity
import com.example.model.LogoPlacement
import com.example.model.PositionPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LogoPlacementUnitTest {

    @Test
    fun testDefaultPlacementValues() {
        val defaultPlacement = LogoPlacement.defaultPlacement()
        assertEquals(PositionPreset.BOTTOM_RIGHT, defaultPlacement.preset)
        assertEquals(0.18f, defaultPlacement.normalizedWidth, 0.001f)
        assertEquals(0.035f, defaultPlacement.normalizedMargin, 0.001f)
        assertEquals(1.0f, defaultPlacement.opacity, 0.001f)
    }

    @Test
    fun testPixelBoundsStayWithinPhotoDimensions() {
        val placement = LogoPlacement.defaultPlacement()

        // Landscape 4000x3000
        val landscapeBounds = placement.calculatePixelBounds(
            imageWidth = 4000,
            imageHeight = 3000,
            logoAspectRatio = 2.0f
        )
        assertTrue("Left must be >= 0", landscapeBounds.left >= 0f)
        assertTrue("Top must be >= 0", landscapeBounds.top >= 0f)
        assertTrue("Right must be <= 4000", landscapeBounds.right <= 4000f)
        assertTrue("Bottom must be <= 3000", landscapeBounds.bottom <= 3000f)
        val refLandscape = minOf(4000, 3000).toFloat()
        assertEquals(refLandscape * 0.18f, landscapeBounds.width(), 1.0f)

        // Portrait 1080x1920
        val portraitBounds = placement.calculatePixelBounds(
            imageWidth = 1080,
            imageHeight = 1920,
            logoAspectRatio = 1.0f
        )
        assertTrue("Left must be >= 0", portraitBounds.left >= 0f)
        assertTrue("Top must be >= 0", portraitBounds.top >= 0f)
        assertTrue("Right must be <= 1080", portraitBounds.right <= 1080f)
        assertTrue("Bottom must be <= 1920", portraitBounds.bottom <= 1920f)
        val refPortrait = minOf(1080, 1920).toFloat()
        assertEquals(refPortrait * 0.18f, portraitBounds.width(), 1.0f)
    }

    @Test
    fun testAdaptiveScalingBetween916PortraitAnd169Landscape() {
        val placement = LogoPlacement.defaultPlacement()

        // 9:16 Portrait (1080x1920)
        val bounds916 = placement.calculatePixelBounds(
            imageWidth = 1080,
            imageHeight = 1920,
            logoAspectRatio = 2.0f
        )

        // 16:9 Landscape (1920x1080)
        val bounds169 = placement.calculatePixelBounds(
            imageWidth = 1920,
            imageHeight = 1080,
            logoAspectRatio = 2.0f
        )

        // Logo width and height must match across both aspect ratios!
        assertEquals("Width must match between 9:16 and 16:9", bounds916.width(), bounds169.width(), 0.1f)
        assertEquals("Height must match between 9:16 and 16:9", bounds916.height(), bounds169.height(), 0.1f)

        // Distance from bottom-right corner must be identical!
        val marginFromBottom916 = 1920f - bounds916.bottom
        val marginFromBottom169 = 1080f - bounds169.bottom
        assertEquals("Bottom margin must match between 9:16 and 16:9", marginFromBottom916, marginFromBottom169, 0.1f)

        // On 16:9 landscape, bottom of logo must NEVER exceed photo height
        assertTrue("Logo must not overflow bottom of 16:9 photo", bounds169.bottom <= 1080f)
    }

    @Test
    fun testAllNinePresetsCalculateValidCoordinates() {
        val presets = PositionPreset.values().filterNot { it == PositionPreset.CUSTOM }
        for (preset in presets) {
            val placement = LogoPlacement.fromPreset(preset, width = 0.20f, margin = 0.05f)
            val bounds = placement.calculatePixelBounds(
                imageWidth = 1000,
                imageHeight = 1000,
                logoAspectRatio = 1.0f
            )
            assertTrue("Preset ${preset.name} out of bounds horizontally", bounds.left >= 0f && bounds.right <= 1000f)
            assertTrue("Preset ${preset.name} out of bounds vertically", bounds.top >= 0f && bounds.bottom <= 1000f)
        }
    }

    @Test
    fun testPresetEntityRoundtrip() {
        val original = LogoPlacement(
            preset = PositionPreset.CUSTOM,
            normalizedX = 0.42f,
            normalizedY = 0.67f,
            normalizedWidth = 0.15f,
            normalizedMargin = 0.04f,
            opacity = 0.9f,
            removeWhiteBg = true,
            adaptiveScale = false,
            rotationDegrees = 45f,
            flipHorizontal = true,
            shadowEnabled = true,
            shadowRadius = 15f,
            watermarkText = "LOGO STUDIO 2026",
            textColor = 0xFF38BDF8,
            isTextBold = false,
            fontSizeFraction = 0.045f
        )

        val entity = PresetEntity.fromPlacement("My Studio Preset", original)
        assertEquals("My Studio Preset", entity.name)
        assertEquals("CUSTOM", entity.presetName)
        assertEquals(0.42f, entity.normalizedX, 0.001f)
        assertEquals(0.67f, entity.normalizedY, 0.001f)
        assertEquals(true, entity.removeWhiteBg)
        assertEquals(false, entity.adaptiveScale)
        assertEquals(45f, entity.rotationDegrees, 0.01f)
        assertEquals(true, entity.flipHorizontal)
        assertEquals(true, entity.shadowEnabled)
        assertEquals(15f, entity.shadowRadius, 0.01f)
        assertEquals("LOGO STUDIO 2026", entity.watermarkText)
        assertEquals(0xFF38BDF8, entity.textColor)
        assertEquals(false, entity.isTextBold)
        assertEquals(0.045f, entity.fontSizeFraction, 0.001f)

        val converted = entity.toPlacement()
        assertEquals(original.preset, converted.preset)
        assertEquals(0.42f, converted.normalizedX, 0.001f)
        assertEquals(0.67f, converted.normalizedY, 0.001f)
        assertEquals(original.normalizedWidth, converted.normalizedWidth, 0.001f)
        assertEquals(original.normalizedMargin, converted.normalizedMargin, 0.001f)
        assertEquals(original.opacity, converted.opacity, 0.001f)
        assertEquals(true, converted.removeWhiteBg)
        assertEquals(false, converted.adaptiveScale)
        assertEquals(45f, converted.rotationDegrees, 0.01f)
        assertEquals(true, converted.flipHorizontal)
        assertEquals(true, converted.shadowEnabled)
        assertEquals(15f, converted.shadowRadius, 0.01f)
        assertEquals("LOGO STUDIO 2026", converted.watermarkText)
        assertEquals(0xFF38BDF8, converted.textColor)
        assertEquals(false, converted.isTextBold)
        assertEquals(0.045f, converted.fontSizeFraction, 0.001f)
    }

    @Test
    fun testCustomDragContinuousAcrossMidpoint() {
        val width916 = 1080
        val height916 = 1920
        val logoAspect = 1.5f
        val base = LogoPlacement(preset = PositionPreset.CUSTOM, normalizedWidth = 0.20f)
        val initialBounds = base.calculatePixelBounds(width916, height916, logoAspect)
        val logoW = initialBounds.width()
        val logoH = initialBounds.height()

        // Sweep across midpoint 0.5f and confirm pixel positions increase strictly monotonically with no jump
        var prevLeft = -10000f
        var prevTop = -10000f
        for (step in 1..9) {
            val targetFrac = step * 0.1f
            val desiredLeft = targetFrac * (width916 - logoW)
            val desiredTop = targetFrac * (height916 - logoH)

            val (normX, normY) = LogoPlacement.calculateCustomNormalizedPosition(
                leftPx = desiredLeft,
                topPx = desiredTop,
                imageWidth = width916.toFloat(),
                imageHeight = height916.toFloat(),
                logoWidth = logoW,
                logoHeight = logoH
            )
            val updated = base.copy(normalizedX = normX, normalizedY = normY)
            val roundTrip = updated.calculatePixelBounds(width916, height916, logoAspect)

            assertEquals("Roundtrip X must match desired left at step $step", desiredLeft, roundTrip.left, 0.5f)
            assertEquals("Roundtrip Y must match desired top at step $step", desiredTop, roundTrip.top, 0.5f)
            assertTrue("X must increase monotonically across 0.5 boundary", roundTrip.left > prevLeft)
            assertTrue("Y must increase monotonically across 0.5 boundary", roundTrip.top > prevTop)
            prevLeft = roundTrip.left
            prevTop = roundTrip.top

            // Also confirm 16:9 landscape bounds stay strictly inside frame with same logo size
            val bounds169 = updated.calculatePixelBounds(1920, 1080, logoAspect)
            assertEquals(logoW, bounds169.width(), 0.5f)
            assertTrue("16:9 bottom must stay inside 1080px frame", bounds169.bottom <= 1080f)
            assertTrue("16:9 right must stay inside 1920px frame", bounds169.right <= 1920f)
        }
    }

    @Test
    fun testBottomWatermarkAutoShiftsTextInsidePhoto() {
        val placementWithText = LogoPlacement(
            preset = PositionPreset.BOTTOM_RIGHT,
            normalizedWidth = 0.20f,
            normalizedMargin = 0.0f, // 0% flush margin!
            watermarkText = "Studio Event 2026",
            fontSizeFraction = 0.04f
        )
        val placementNoText = placementWithText.copy(watermarkText = "")

        val w = 1920
        val h = 1080
        val refDim = minOf(w, h).toFloat()
        val boundsWithText = placementWithText.calculatePixelBounds(w, h, 1.0f)
        val boundsNoText = placementNoText.calculatePixelBounds(w, h, 1.0f)

        assertTrue("Logo bottom should shift upward when text caption is present", boundsWithText.bottom < boundsNoText.bottom)
        val expectedTextReserve = (refDim * placementWithText.fontSizeFraction).coerceAtLeast(16f) * 1.35f
        assertTrue(
            "Logo bottom + text height must fit cleanly inside photo height",
            boundsWithText.bottom + expectedTextReserve <= h.toFloat() + 1.0f
        )

        // Also test CUSTOM at extreme y = 1.0
        val customBottomText = placementWithText.copy(
            preset = PositionPreset.CUSTOM,
            normalizedX = 1.0f,
            normalizedY = 1.0f
        )
        val customBounds = customBottomText.calculatePixelBounds(w, h, 1.0f)
        assertTrue(
            "CUSTOM bottom-most watermark must still reserve room for caption inside frame",
            customBounds.bottom + expectedTextReserve <= h.toFloat() + 1.0f
        )
    }

    @Test
    fun testFilenameTemplateGeneration() {
        val filename1 = com.example.engine.PhotoExporter.generateFilename(
            template = "LOGO_{event}_{index}",
            originalName = "IMG_4920.jpg",
            index = 1,
            extension = ".jpg"
        )
        assertEquals("LOGO_EVENT_001.jpg", filename1)

        val filename2 = com.example.engine.PhotoExporter.generateFilename(
            template = "{name}_watermarked",
            originalName = "Outdoor_Concert.png",
            index = 5,
            extension = ".webp"
        )
        assertEquals("Outdoor_Concert_watermarked.webp", filename2)

        val filename3 = com.example.engine.PhotoExporter.generateFilename(
            template = "LOGO_{name}_{001}",
            originalName = "Photo.jpg",
            index = 12,
            extension = ".png"
        )
        assertEquals("LOGO_Photo_012.png", filename3)
    }

    @Test
    fun testSmartPlacementEngineCornerDetection() {
        // Create simple 300x300 bitmap with high contrast edges in top-left and smooth in bottom-right
        val bmp = android.graphics.Bitmap.createBitmap(300, 300, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val paint = android.graphics.Paint()

        // Top-left has noisy stripes
        paint.color = android.graphics.Color.BLACK
        for (i in 0 until 100 step 4) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 100f, paint)
        }

        val result = com.example.engine.SmartPlacementEngine.analyzePlacement(bmp)
        // Clean bottom corner should be favored over noisy top-left
        assertTrue(result.recommendedPreset == PositionPreset.BOTTOM_RIGHT || result.recommendedPreset == PositionPreset.BOTTOM_LEFT)
        bmp.recycle()
    }

    @Test
    fun testRemoteConfigAccessGateEvaluation() {
        // Default state allows access offline
        val defaultState = com.example.data.AppAccessState()
        assertEquals(com.example.data.AccessGateMode.ALLOWED, defaultState.accessMode)

        // Kill switch disables access even if version requirement is satisfied
        assertEquals(
            com.example.data.AccessGateMode.DISABLED,
            com.example.data.AppAccessState.evaluateAccessMode(
                appEnabled = false,
                installedVersionCode = 5,
                minVersionCode = 1L
            )
        )

        // Older build triggers UPDATE_REQUIRED when app is enabled
        assertEquals(
            com.example.data.AccessGateMode.UPDATE_REQUIRED,
            com.example.data.AppAccessState.evaluateAccessMode(
                appEnabled = true,
                installedVersionCode = 1,
                minVersionCode = 2L
            )
        )

        // Matching version allows access
        assertEquals(
            com.example.data.AccessGateMode.ALLOWED,
            com.example.data.AppAccessState.evaluateAccessMode(
                appEnabled = true,
                installedVersionCode = 2,
                minVersionCode = 2L
            )
        )
    }

    @Test
    fun testExifAllowlistNeverIncludesGpsTagsAndCopyExifStripsGps() {
        val safeTags = com.example.engine.ImageUtils.SAFE_NON_LOCATION_EXIF_TAGS.toList()
        assertTrue(
            "EXIF allowlist must never contain any GPS tags",
            safeTags.none { it.contains("GPS", ignoreCase = true) }
        )

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val cacheDir = context.cacheDir
        val srcFile = java.io.File(cacheDir, "exif_src_test.jpg")
        val dstFile = java.io.File(cacheDir, "exif_dst_test.jpg")

        val sampleBmp = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
        java.io.FileOutputStream(srcFile).use { sampleBmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
        java.io.FileOutputStream(dstFile).use { sampleBmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
        sampleBmp.recycle()

        val srcExif = android.media.ExifInterface(srcFile.absolutePath)
        srcExif.setAttribute(android.media.ExifInterface.TAG_MAKE, "Canon")
        srcExif.setAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE, "23/1,48/1,0/1")
        srcExif.setAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE_REF, "N")
        srcExif.setAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE, "90/1,24/1,0/1")
        srcExif.setAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE_REF, "E")
        srcExif.saveAttributes()

        // Also seed destination with GPS tags to ensure copyExifMetadata clears them too
        val preDstExif = android.media.ExifInterface(dstFile.absolutePath)
        preDstExif.setAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE, "23/1,48/1,0/1")
        preDstExif.saveAttributes()

        com.example.engine.ImageUtils.copyExifMetadata(
            context = context,
            sourceUri = android.net.Uri.fromFile(srcFile),
            destUri = android.net.Uri.fromFile(dstFile)
        )

        val dstExif = android.media.ExifInterface(dstFile.absolutePath)
        assertEquals("Canon", dstExif.getAttribute(android.media.ExifInterface.TAG_MAKE))
        assertEquals(null, dstExif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE))
        assertEquals(null, dstExif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE))

        srcFile.delete()
        dstFile.delete()
    }

    @Test
    fun testAuthManagerInitialStateAndModes() {
        // Without an active session, AuthManager cleanly reports unauthenticated
        assertEquals(null, com.example.data.AuthManager.currentUserOrNull())
        assertEquals(
            listOf("Sign In", "Sign Up"),
            com.example.ui.screens.AuthMode.entries.map { it.label }
        )
    }

    @Test
    fun testRemoteConfigInitialSafeDefaults() {
        val state = com.example.data.RemoteConfigManager.accessState.value
        assertEquals(true, state.appEnabled)
        assertEquals(com.example.data.AccessGateMode.ALLOWED, state.accessMode)
        assertEquals("", state.noticeText)
    }

    @Test
    fun testStrictSignInRequiresPriorSignUpValidation() {
        // New user attempting Sign In without signing up must be rejected and auto-created user deleted
        val newUserSignIn = com.example.data.AuthManager.validateAuthModeForUserStatus(
            mode = com.example.ui.screens.AuthMode.SIGN_IN,
            isNewUser = true
        )
        assertTrue(newUserSignIn is com.example.data.AuthValidationResult.Rejected)
        val rejectedSignIn = newUserSignIn as com.example.data.AuthValidationResult.Rejected
        assertEquals(true, rejectedSignIn.deleteAutoCreatedUser)
        assertEquals(com.example.ui.screens.AuthMode.SIGN_UP, rejectedSignIn.suggestedMode)

        // Existing user signing in succeeds
        assertEquals(
            com.example.data.AuthValidationResult.Allowed,
            com.example.data.AuthManager.validateAuthModeForUserStatus(
                mode = com.example.ui.screens.AuthMode.SIGN_IN,
                isNewUser = false
            )
        )

        // New user signing up succeeds
        assertEquals(
            com.example.data.AuthValidationResult.Allowed,
            com.example.data.AuthManager.validateAuthModeForUserStatus(
                mode = com.example.ui.screens.AuthMode.SIGN_UP,
                isNewUser = true
            )
        )

        // Existing user tapping Sign Up is redirected to Sign In without deleting their existing account
        val existingUserSignUp = com.example.data.AuthManager.validateAuthModeForUserStatus(
            mode = com.example.ui.screens.AuthMode.SIGN_UP,
            isNewUser = false
        )
        assertTrue(existingUserSignUp is com.example.data.AuthValidationResult.Rejected)
        val rejectedSignUp = existingUserSignUp as com.example.data.AuthValidationResult.Rejected
        assertEquals(false, rejectedSignUp.deleteAutoCreatedUser)
        assertEquals(com.example.ui.screens.AuthMode.SIGN_IN, rejectedSignUp.suggestedMode)
    }
}


