package com.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class CompressionResult(
    val file: File,
    val originalBytes: Long,
    val compressedBytes: Long
)

object ImageCompressor {
    private const val MAX_DIMENSION = 1080
    private const val QUALITY = 75

    suspend fun compress(context: Context, sourceFile: File): CompressionResult =
        withContext(Dispatchers.IO) {
            val originalBytes = sourceFile.length()

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, MAX_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
                ?: error("Failed to decode bitmap")

            val scaled = scaleBitmap(bitmap, MAX_DIMENSION)
            if (scaled !== bitmap) bitmap.recycle()

            val outDir = File(context.filesDir, "compressed_images").apply { mkdirs() }
            val outFile = File(outDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            scaled.recycle()

            CompressionResult(outFile, originalBytes, outFile.length())
        }

    private fun calculateSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var size = 1
        while (width / (size * 2) >= maxDim && height / (size * 2) >= maxDim) size *= 2
        return size
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
