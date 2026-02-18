package com.example.photoprintapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.example.photoprintapp.models.FilterLayoutConfig
import com.example.photoprintapp.models.FilterType
import com.example.photoprintapp.models.SlotRect
import java.io.File
import java.io.FileOutputStream

object FilterService {

    /**
     * Merge photos into a frame and return the final PNG file.
     * - If filter has a frame → composite photos behind the frame PNG
     * - If no frame → arrange photos in a plain grid
     */
    fun createPhotoGrid(
        context: Context,
        photoPaths: List<String>,
        filterType: FilterType,
        gridCount: Int,
        framePath: String?   // e.g. "frames/frame4_photobooth.png", null if no frame
    ): File? {
        return try {
            val layout = FilterLayoutConfig.getLayout(filterType, gridCount)

            // Create canvas bitmap at native frame resolution
            val canvasBitmap = Bitmap.createBitmap(
                layout.frameWidth, layout.frameHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(canvasBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Step 1: Draw photos into each slot
            photoPaths.forEachIndexed { index, path ->
                if (index >= layout.slots.size) return@forEachIndexed
                val slot = layout.slots[index]
                val photoBitmap = decodeSampledBitmap(path, slot.width, slot.height) ?: return@forEachIndexed
                val cropped = centerCropBitmap(photoBitmap, slot.width, slot.height)
                val dst = Rect(slot.x, slot.y, slot.x + slot.width, slot.y + slot.height)
                canvas.drawBitmap(cropped, null, dst, paint)
                cropped.recycle()
                photoBitmap.recycle()
            }

            // Step 2: Draw frame on top (so frame decorations overlay the photos)
            if (framePath != null) {
                val frameBitmap = loadAssetBitmap(context, framePath)
                if (frameBitmap != null) {
                    val frameDst = Rect(0, 0, layout.frameWidth, layout.frameHeight)
                    canvas.drawBitmap(frameBitmap, null, frameDst, paint)
                    frameBitmap.recycle()
                }
            }

            // Step 3: Save result
            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val outFile = File(dir, "grid_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { out ->
                canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            canvasBitmap.recycle()
            outFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Merge sticker bitmaps onto a base image and save as PNG.
     */
    fun mergeStickersOntoPhoto(
        context: Context,
        baseImagePath: String,
        stickers: List<StickerOverlay>,   // see data class below
        containerWidth: Float,
        containerHeight: Float
    ): File? {
        return try {
            val baseBytes = File(baseImagePath).readBytes()
            val base = BitmapFactory.decodeByteArray(baseBytes, 0, baseBytes.size)
                ?.copy(Bitmap.Config.ARGB_8888, true) ?: return null

            val scaleX = base.width / containerWidth
            val scaleY = base.height / containerHeight

            val canvas = Canvas(base)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            stickers.forEach { sticker ->
                val stickerBitmap = loadAssetBitmap(context, sticker.assetPath) ?: return@forEach
                val scaledSize = (sticker.size * scaleX).toInt()
                val resized = Bitmap.createScaledBitmap(stickerBitmap, scaledSize, scaledSize, true)
                stickerBitmap.recycle()

                val scaledX = (sticker.centerX * scaleX - scaledSize / 2f).toInt()
                val scaledY = (sticker.centerY * scaleY - scaledSize / 2f).toInt()

                canvas.save()
                canvas.rotate(
                    Math.toDegrees(sticker.rotation.toDouble()).toFloat(),
                    (scaledX + scaledSize / 2f),
                    (scaledY + scaledSize / 2f)
                )
                canvas.drawBitmap(resized, scaledX.toFloat(), scaledY.toFloat(), paint)
                canvas.restore()
                resized.recycle()
            }

            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val outFile = File(dir, "final_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { out ->
                base.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            base.recycle()
            outFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private fun loadAssetBitmap(context: Context, assetPath: String): Bitmap? {
        return try {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
        val (h, w) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (h > reqH || w > reqW) {
            val halfH = h / 2; val halfW = w / 2
            while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun centerCropBitmap(source: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = targetW.toFloat() / targetH
        val (cropW, cropH) = if (sourceRatio > targetRatio)
            ((source.height * targetRatio).toInt() to source.height)
        else
            (source.width to (source.width / targetRatio).toInt())
        val startX = (source.width - cropW) / 2
        val startY = (source.height - cropH) / 2
        val cropped = Bitmap.createBitmap(source, startX, startY, cropW, cropH)
        return Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
    }
}

data class StickerOverlay(
    val assetPath: String,
    val centerX: Float,
    val centerY: Float,
    val size: Float,
    val rotation: Float
)