package com.example.photoprintapp.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.photoprintapp.models.StickerData

class StickerCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val stickers = mutableListOf<StickerData>()
    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun addSticker(sticker: StickerData) {
        stickers.add(sticker)
        preloadBitmap(sticker.assetPath)
        invalidate()
    }

    fun clearStickers() {
        stickers.clear()
        invalidate()
    }

    fun getStickers(): List<StickerData> = stickers.toList()

    private fun preloadBitmap(assetPath: String) {
        if (!bitmapCache.containsKey(assetPath)) {
            try {
                context.assets.open(assetPath).use {
                    bitmapCache[assetPath] = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        stickers.forEach { sticker ->
            val bitmap = bitmapCache[sticker.assetPath] ?: return@forEach
            val matrix = Matrix()
            matrix.postScale(
                sticker.size / bitmap.width,
                sticker.size / bitmap.height
            )
            matrix.postRotate(
                Math.toDegrees(sticker.rotation.toDouble()).toFloat(),
                sticker.size / 2f,
                sticker.size / 2f
            )
            matrix.postTranslate(
                sticker.x - sticker.size / 2f,
                sticker.y - sticker.size / 2f
            )
            canvas.drawBitmap(bitmap, matrix, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}