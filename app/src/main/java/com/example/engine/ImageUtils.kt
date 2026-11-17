// © 2026 App Owner. All rights reserved.
package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import com.example.model.LogoItem
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

    /**
     * Reads image dimensions without allocating bitmap memory.
     */
    fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                val rotation = getExifOrientation(context, uri)
                if (rotation == 90 || rotation == 270) {
                    Pair(options.outHeight, options.outWidth)
                } else {
                    Pair(options.outWidth, options.outHeight)
                }
            } else {
                // Safe fallback: sample decode to guarantee non-zero true aspect ratio
                val safeOpts = BitmapFactory.Options().apply { inSampleSize = 4 }
                val bmp = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, safeOpts)
                }
                if (bmp != null) {
                    val res = Pair(bmp.width, bmp.height)
                    bmp.recycle()
                    res
                } else {
                    Pair(1, 1)
                }
            }
        } catch (_: Exception) {
            Pair(1, 1)
        }
    }

    /**
     * Resolves the display filename of a URI.
     */
    fun getDisplayName(context: Context, uri: Uri): String {
        var name = "Photo_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val resolved = cursor.getString(nameIndex)
                    if (!resolved.isNullOrBlank()) {
                        name = resolved
                    }
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return name
    }

    /**
     * Detects EXIF rotation angle (0, 90, 180, 270).
     */
    fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Decodes a downscaled bitmap optimized for smooth preview and batch grid display.
     */
    fun loadDownscaledBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1280
    ): Bitmap? {
        return try {
            val (origW, origH) = getImageDimensions(context, uri)
            if (origW <= 0 || origH <= 0) return null

            var sampleSize = 1
            var halfW = origW / 2
            var halfH = origH / 2
            while (halfW / sampleSize >= maxDimension || halfH / sampleSize >= maxDimension) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            // Apply EXIF rotation if needed
            val rotation = getExifOrientation(context, uri)
            if (rotation != 0 && bitmap != null) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }
            bitmap
        } catch (_: OutOfMemoryError) {
            System.gc()
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads the full-resolution photo bitmap for final high-fidelity export.
     * Preserves full pixel dimensions unless physical device RAM strictly forces safe downsampling.
     */
    fun loadFullResolutionBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }

            var bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val rotation = getExifOrientation(context, uri)
            if (rotation != 0 && bitmap != null) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }
            bitmap
        } catch (_: OutOfMemoryError) {
            System.gc()
            // Fallback gracefully with minimal 2x downsample on low memory devices
            try {
                val safeOptions = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = true
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, safeOptions)
                }
            } catch (_: Exception) {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads a logo (built-in vector or user Uri) in maximum high-fidelity quality.
     * For vectors, renders at exact (targetWidth x targetHeight) on an ARGB_8888 canvas.
     * For user PNGs/JPEGs, decodes at full original resolution without destructive downsampling.
     */
    fun loadLogoBitmap(
        context: Context,
        logoItem: LogoItem,
        targetWidth: Int = 0,
        targetHeight: Int = 0
    ): Bitmap? {
        return try {
            if (logoItem.isBuiltIn && logoItem.drawableResId != null) {
                val drawable = ContextCompat.getDrawable(context, logoItem.drawableResId) ?: return null
                val width = if (targetWidth > 0) targetWidth else drawable.intrinsicWidth.coerceAtLeast(600)
                val height = if (targetHeight > 0) targetHeight else drawable.intrinsicHeight.coerceAtLeast(600)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
                bitmap
            } else if (logoItem.uri != null) {
                // Decode from user selected gallery image in pristine original resolution
                val (logoW, logoH) = getImageDimensions(context, logoItem.uri)
                if (logoW <= 0 || logoH <= 0) return null

                // Only downsample with safe power of 2 if logo exceeds 4096px to avoid OOM
                var inSample = 1
                while ((logoW / inSample) > 4096 || (logoH / inSample) > 4096) {
                    inSample *= 2
                }

                val opts = BitmapFactory.Options().apply {
                    inSampleSize = inSample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                    inDither = false
                }

                val rawLogo = context.contentResolver.openInputStream(logoItem.uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, opts)
                } ?: return null

                // Handle EXIF orientation on logo if applicable
                val rotation = getExifOrientation(context, logoItem.uri)
                val orientedLogo = if (rotation != 0) {
                    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                    val rotated = Bitmap.createBitmap(rawLogo, 0, 0, rawLogo.width, rawLogo.height, matrix, true)
                    if (rotated != rawLogo) {
                        rawLogo.recycle()
                    }
                    rotated
                } else {
                    rawLogo
                }

                orientedLogo
            } else {
                null
            }
        } catch (_: OutOfMemoryError) {
            System.gc()
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Intelligently removes solid white or near-white backgrounds from imported logos.
     * Leaves colored emblem, dark artwork, and text with clean transparent borders.
     */
    fun removeWhiteBackground(bitmap: Bitmap, tolerance: Int = 225): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val a = (color ushr 24) and 0xFF
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF

            if (r >= tolerance && g >= tolerance && b >= tolerance) {
                // Pure or near-white pixel -> Completely transparent
                pixels[i] = 0x00000000
            } else if (r >= tolerance - 30 && g >= tolerance - 30 && b >= tolerance - 30) {
                // Feather edge for smooth anti-aliased borders
                val minRgb = minOf(r, minOf(g, b))
                val factor = (minRgb - (tolerance - 30)) / 30f
                val newAlpha = (a * (1f - factor)).toInt().coerceIn(0, 255)
                pixels[i] = (newAlpha shl 24) or (r shl 16) or (g shl 8) or b
            } else {
                pixels[i] = color
            }
        }
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Allowlist of non-location camera/timestamp EXIF tags safe to retain when stripExif is false.
     * GPS / location tags are unconditionally excluded for privacy.
     */
    val SAFE_NON_LOCATION_EXIF_TAGS: Array<String> = arrayOf(
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_F_NUMBER,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_ISO_SPEED_RATINGS,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_FLASH
    )

    private val GPS_TAGS_TO_CLEAR: Array<String> = arrayOf(
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_PROCESSING_METHOD
    )

    internal fun copyNonLocationExifAttributes(srcExif: ExifInterface, destExif: ExifInterface) {
        for (tag in SAFE_NON_LOCATION_EXIF_TAGS) {
            val value = srcExif.getAttribute(tag)
            if (!value.isNullOrEmpty()) {
                destExif.setAttribute(tag, value)
            }
        }
        for (gpsTag in GPS_TAGS_TO_CLEAR) {
            destExif.setAttribute(gpsTag, null)
        }
        destExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
    }

    /**
     * Copies non-location camera/timestamp metadata from source image to exported image when stripExif is false.
     * Always strips all GPS/location tags and skips TAG_ORIENTATION because exported pixels are already rotated upright.
     */
    fun copyExifMetadata(context: Context, sourceUri: Uri, destUri: Uri) {
        try {
            val sourceValues = mutableMapOf<String, String>()
            context.contentResolver.openInputStream(sourceUri)?.use { inStream ->
                val srcExif = ExifInterface(inStream)
                for (tag in SAFE_NON_LOCATION_EXIF_TAGS) {
                    val value = srcExif.getAttribute(tag)
                    if (!value.isNullOrEmpty()) {
                        sourceValues[tag] = value
                    }
                }
            }

            if (destUri.scheme == "file" && !destUri.path.isNullOrEmpty()) {
                val destExif = ExifInterface(destUri.path!!)
                for ((tag, value) in sourceValues) {
                    destExif.setAttribute(tag, value)
                }
                for (gpsTag in GPS_TAGS_TO_CLEAR) {
                    destExif.setAttribute(gpsTag, null)
                }
                destExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                destExif.saveAttributes()
            } else {
                context.contentResolver.openFileDescriptor(destUri, "rw")?.use { pfd ->
                    val destExif = ExifInterface(pfd.fileDescriptor)
                    for ((tag, value) in sourceValues) {
                        destExif.setAttribute(tag, value)
                    }
                    for (gpsTag in GPS_TAGS_TO_CLEAR) {
                        destExif.setAttribute(gpsTag, null)
                    }
                    destExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                    destExif.saveAttributes()
                }
            }
        } catch (_: Exception) {
            // Safely ignore formats (e.g. PNG) that do not support EXIF chunks
        }
    }
}
