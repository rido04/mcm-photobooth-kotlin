package com.example.photoprintapp.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

data class StickerItem(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var scale: Float = 1f,
    var rotation: Float = 0f
)

data class LayoutConfig(
    val cellWidth: Float,   // fraction of frame width
    val cellHeight: Float,  // fraction of frame height
    val leftPadding: Float,
    val topPadding: Float,
    val hGap: Float,        // horizontal gap between cells
    val vGap: Float         // vertical gap between cells
)

class PhotoCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var frameBitmap: Bitmap? = null
    val photos = mutableListOf<Bitmap>()    
    var layoutConfig: LayoutConfig? = null
    var gridCount: Int = 4
    val stickers = mutableListOf<StickerItem>()

    private var selectedSticker: StickerItem? = null
    private var pendingStickerBitmap: Bitmap? = null

    // Touch tracking
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastSpan = 0f
    private var lastAngle = 0f
    private var isDragging = false
    private var isMultiTouch = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Double tap detector for delete
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            val toDelete = stickers.lastOrNull { sticker ->
                val dist = hypot(x - sticker.x, y - sticker.y)
                val radius = maxOf(sticker.bitmap.width, sticker.bitmap.height) * sticker.scale / 2f + 20f
                dist < radius
            }
            if (toDelete != null) {
                stickers.remove(toDelete)
                if (selectedSticker == toDelete) selectedSticker = null
                invalidate()
                return true
            }
            return false
        }
    })

    fun setPendingSticker(bmp: Bitmap?) {
        pendingStickerBitmap = bmp
    }

    fun deleteSelected() {
        selectedSticker?.let {
            stickers.remove(it)
            selectedSticker = null
            invalidate()
        }
    }

    // Compute photo slot rects from LayoutConfig
    private fun computeSlots(): List<RectF> {
        val cfg = layoutConfig ?: return emptyList()
        val w = width.toFloat()
        val h = height.toFloat()
        val cols = 2
        val rows = if (gridCount == 6) 3 else 2

        val cellW = cfg.cellWidth * w
        val cellH = cfg.cellHeight * h
        val left = cfg.leftPadding * w
        val top = cfg.topPadding * h
        val hGap = cfg.hGap * w
        val vGap = cfg.vGap * h

        val slots = mutableListOf<RectF>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = left + col * (cellW + hGap)
                val y = top + row * (cellH + vGap)
                slots.add(RectF(x, y, x + cellW, y + cellH))
                if (slots.size >= gridCount) return slots
            }
        }
        return slots
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Draw frame as background
        frameBitmap?.let {
            canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), paint)
        }

        // Draw photos in slots
        val slots = computeSlots()
        photos.forEachIndexed { i, bmp ->
            if (i < slots.size) {
                val rect = slots[i]
                canvas.save()
                canvas.clipRect(rect)
                val scale = maxOf(rect.width() / bmp.width, rect.height() / bmp.height)
                val bw = bmp.width * scale
                val bh = bmp.height * scale
                val ox = rect.left + (rect.width() - bw) / 2f
                val oy = rect.top + (rect.height() - bh) / 2f
                canvas.drawBitmap(bmp, null, RectF(ox, oy, ox + bw, oy + bh), paint)
                canvas.restore()
            }
        }

        // Draw frame overlay ON TOP of photos
        frameBitmap?.let {
            canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), paint)
        }

        // Draw stickers
        stickers.forEach { sticker ->
            val m = Matrix()
            m.postTranslate(-sticker.bitmap.width / 2f, -sticker.bitmap.height / 2f)
            m.postScale(sticker.scale, sticker.scale)
            m.postRotate(sticker.rotation)
            m.postTranslate(sticker.x, sticker.y)
            canvas.drawBitmap(sticker.bitmap, m, paint)

            if (sticker == selectedSticker) {
                val selPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                }
                val half = maxOf(sticker.bitmap.width, sticker.bitmap.height) * sticker.scale / 2f + 10f
                canvas.save()
                canvas.rotate(sticker.rotation, sticker.x, sticker.y)
                canvas.drawRect(sticker.x - half, sticker.y - half, sticker.x + half, sticker.y + half, selPaint)
                canvas.restore()

                // Draw X delete hint
                val hintPaint = Paint().apply {
                    color = Color.RED
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("✕ double tap hapus", sticker.x, sticker.y - half - 12f, hintPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                if (pendingStickerBitmap != null) {
                    val s = StickerItem(pendingStickerBitmap!!, x, y, 0.3f, 0f)
                    stickers.add(s)
                    selectedSticker = s
                    pendingStickerBitmap = null
                    invalidate()
                    return true
                }

                selectedSticker = stickers.lastOrNull { sticker ->
                    val dist = hypot(x - sticker.x, y - sticker.y)
                    val radius = maxOf(sticker.bitmap.width, sticker.bitmap.height) * sticker.scale / 2f + 20f
                    dist < radius
                }

                lastTouchX = x
                lastTouchY = y
                isDragging = selectedSticker != null
                invalidate()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (selectedSticker != null && event.pointerCount == 2) {
                    isMultiTouch = true
                    lastSpan = getSpan(event)
                    lastAngle = getAngle(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                if (isMultiTouch && event.pointerCount == 2 && selectedSticker != null) {
                    val span = getSpan(event)
                    val angle = getAngle(event)
                    if (lastSpan > 0) {
                        selectedSticker!!.scale *= (span / lastSpan)
                        selectedSticker!!.scale = selectedSticker!!.scale.coerceIn(0.05f, 3f)
                    }
                    selectedSticker!!.rotation += angle - lastAngle
                    lastSpan = span
                    lastAngle = angle
                } else if (isDragging && selectedSticker != null) {
                    selectedSticker!!.x += x - lastTouchX
                    selectedSticker!!.y += y - lastTouchY
                }

                lastTouchX = x
                lastTouchY = y
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> { isMultiTouch = false }
            MotionEvent.ACTION_UP -> { isDragging = false; isMultiTouch = false }
        }
        return true
    }

    private fun getSpan(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return hypot(dx, dy)
    }

    private fun getAngle(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    fun renderFinalBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        draw(canvas)
        return bmp
    }
}