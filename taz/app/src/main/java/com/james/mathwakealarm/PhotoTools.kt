package com.james.mathwakealarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.abs
import kotlin.math.max

object PhotoStore {
    fun createCaptureUri(context: Context, prefix: String = "reference"): Uri {
        val folder = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("${prefix}_", ".jpg", folder)
        return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }
}

object ImageSimilarity {
    fun bestScore(context: Context, captured: Uri, references: List<String>): Float {
        val target = signature(context, captured) ?: return 0f
        return references.mapNotNull { uri ->
            runCatching { signature(context, Uri.parse(uri)) }.getOrNull()?.let { compare(target, it) }
        }.maxOrNull() ?: 0f
    }

    private fun signature(context: Context, uri: Uri): FloatArray? {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
        val scaled = Bitmap.createScaledBitmap(bitmap, 24, 24, true)
        if (scaled !== bitmap) bitmap.recycle()

        val values = FloatArray(24 * 24 * 3)
        var index = 0
        for (y in 0 until 24) {
            for (x in 0 until 24) {
                val pixel = scaled.getPixel(x, y)
                values[index++] = ((pixel shr 16) and 0xff) / 255f
                values[index++] = ((pixel shr 8) and 0xff) / 255f
                values[index++] = (pixel and 0xff) / 255f
            }
        }
        scaled.recycle()
        return values
    }

    private fun compare(a: FloatArray, b: FloatArray): Float {
        val length = max(1, minOf(a.size, b.size))
        var difference = 0f
        for (i in 0 until length) difference += abs(a[i] - b[i])
        return (1f - difference / length).coerceIn(0f, 1f)
    }
}
