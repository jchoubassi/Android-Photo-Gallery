package com.example.photogallery.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Size
import com.example.photogallery.media.MediaStoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//decode imgs and load into memory
object BitmapLoader {
    suspend fun loadThumb(context: Context, id: String, orientation: Int, target: Size): Bitmap? =
        withContext(Dispatchers.IO) {
            val key = "thumb:${id}:${target.width}x${target.height}"
            BitmapCache.get(key)?.let { return@withContext it }

            val uri = MediaStoreRepo.uriFor(id)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            val sample = computeInSampleSize(bounds.outWidth, bounds.outHeight, target.width, target.height)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }?.let { rotateIfNeeded(it, orientation) }

            bmp?.also { BitmapCache.put(key, it) }
        }

    suspend fun loadLarge(context: Context, id: String, orientation: Int, maxSidePx: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val key = "large:$id:$maxSidePx"
            BitmapCache.get(key)?.let { return@withContext it }

            val uri = MediaStoreRepo.uriFor(id)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            val (tw, th) = targetByMaxSide(bounds.outWidth, bounds.outHeight, maxSidePx)
            val sample = computeInSampleSize(bounds.outWidth, bounds.outHeight, tw, th)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }?.let { rotateIfNeeded(it, orientation) }

            bmp?.also { BitmapCache.put(key, it) }
        }

    private fun computeInSampleSize(w: Int, h: Int, reqW: Int, reqH: Int): Int {
        var inSampleSize = 1
        if (h > reqH || w > reqW) {
            var halfH = h / 2
            var halfW = w / 2
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun targetByMaxSide(w: Int, h: Int, maxSide: Int): Pair<Int, Int> {
        if (w <= 0 || h <= 0) return maxSide to maxSide
        val scale = maxSide.toFloat() / maxOf(w, h).toFloat()
        return (w * scale).toInt().coerceAtLeast(1) to (h * scale).toInt().coerceAtLeast(1)
    }

    private fun rotateIfNeeded(src: Bitmap, orientationDeg: Int): Bitmap {
        if (orientationDeg == 0) return src
        val m = Matrix().apply { postRotate(orientationDeg.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }
}
