package com.example.photoprintapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import com.example.photoprintapp.adapters.StickerAdapter
import com.example.photoprintapp.models.FilterType
import com.example.photoprintapp.models.PlacedSticker
import com.example.photoprintapp.services.PrintApiService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTOS = "extra_photos"
        const val EXTRA_FILTER = "extra_filter"
        const val EXTRA_GRID_COUNT = "extra_grid_count"
    }

    private lateinit var imgPreview: ImageView
    private lateinit var frameStickers: FrameLayout
    private lateinit var rvStickers: RecyclerView
    private lateinit var btnPrint: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var tvStatus: TextView

    private var photoPaths: Array<String> = emptyArray()
    private var selectedFilter = FilterType.NONE
    private var gridCount = 1
    private var compositeBitmap: Bitmap? = null
    private val placedStickers = mutableListOf<PlacedSticker>()
    private var selectedStickerRes: Int? = null

    private val printService = PrintApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        photoPaths = intent.getStringArrayExtra(EXTRA_PHOTOS) ?: emptyArray()
        selectedFilter = FilterType.valueOf(
            intent.getStringExtra(EXTRA_FILTER) ?: FilterType.NONE.name
        )
        gridCount = intent.getIntExtra(EXTRA_GRID_COUNT, 1)

        initViews()
        buildCompositeImage()
        setupStickerPicker()
    }

    private fun initViews() {
        imgPreview = findViewById(R.id.imgPreview)
        frameStickers = findViewById(R.id.frameStickers)
        rvStickers = findViewById(R.id.rvStickers)
        btnPrint = findViewById(R.id.btnPrint)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        tvStatus = findViewById(R.id.tvStatus)

        btnPrint.setOnClickListener { printPhoto() }
        btnSave.setOnClickListener { savePhoto() }
        btnBack.setOnClickListener { finish() }

        // Tap di area preview = tempatkan sticker
        frameStickers.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && selectedStickerRes != null) {
                placeSticker(event.x, event.y)
                true
            } else false
        }
    }

    // ─────────────────────────────────────────────
    // Composite Image (gabungkan foto + frame)
    // ─────────────────────────────────────────────

    private fun buildCompositeImage() {
        tvStatus.text = "Memproses foto..."

        Thread {
            val bitmaps = photoPaths.mapNotNull { path ->
                BitmapFactory.decodeFile(path)
            }

            if (bitmaps.isEmpty()) {
                runOnUiThread { tvStatus.text = "Tidak ada foto" }
                return@Thread
            }

            val result = createGrid(bitmaps, gridCount)
            compositeBitmap = result

            runOnUiThread {
                imgPreview.setImageBitmap(result)
                tvStatus.text = "Siap — tambah sticker atau langsung print!"
                btnPrint.isEnabled = true
                btnSave.isEnabled = true
            }
        }.start()
    }

    private fun createGrid(bitmaps: List<Bitmap>, count: Int): Bitmap {
        val cellW = 640
        val cellH = 480
        val cols = if (count <= 2) 1 else 2
        val rows = (count + cols - 1) / cols
        val totalW = cellW * cols
        val totalH = cellH * rows

        val result = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        bitmaps.forEachIndexed { i, bmp ->
            if (i >= count) return@forEachIndexed
            val col = i % cols
            val row = i / cols
            val scaled = Bitmap.createScaledBitmap(bmp, cellW, cellH, true)
            canvas.drawBitmap(scaled, (col * cellW).toFloat(), (row * cellH).toFloat(), paint)
            if (scaled != bmp) scaled.recycle()
        }

        // Apply frame overlay jika ada
        val frameRes = selectedFilter.getFrameResId(count)
        if (frameRes != 0) {
            val frameBmp = BitmapFactory.decodeResource(resources, frameRes)
            val scaledFrame = Bitmap.createScaledBitmap(frameBmp, totalW, totalH, true)
            canvas.drawBitmap(scaledFrame, 0f, 0f, paint)
            if (scaledFrame != frameBmp) scaledFrame.recycle()
            frameBmp.recycle()
        }

        return result
    }

    // ─────────────────────────────────────────────
    // Sticker
    // ─────────────────────────────────────────────

    private fun setupStickerPicker() {
        // FIX: Use Android default drawables instead of custom ones
        val stickers = listOf(
            android.R.drawable.star_big_on,
            android.R.drawable.btn_star_big_on,
            android.R.drawable.ic_menu_info_details,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_preferences
        )

        val adapter = StickerAdapter(stickers) { resId ->
            selectedStickerRes = if (selectedStickerRes == resId) null else resId
            tvStatus.text = if (selectedStickerRes != null)
                "Tap di foto untuk menempatkan sticker"
            else "Sticker dibatalkan"
        }

        rvStickers.layoutManager = GridLayoutManager(this, 2)
        rvStickers.adapter = adapter
    }

    private fun placeSticker(x: Float, y: Float) {
        val resId = selectedStickerRes ?: return
        val sticker = PlacedSticker(resId, x, y, 120f)
        placedStickers.add(sticker)
        drawStickerOnFrame(sticker)
        selectedStickerRes = null
        tvStatus.text = "Sticker ditambahkan!"
    }

    private fun drawStickerOnFrame(sticker: PlacedSticker) {
        val iv = ImageView(this).apply {
            setImageResource(sticker.resId)
            layoutParams = FrameLayout.LayoutParams(
                sticker.size.toInt(), sticker.size.toInt()
            ).apply {
                leftMargin = (sticker.x - sticker.size / 2).toInt()
                topMargin = (sticker.y - sticker.size / 2).toInt()
            }
            tag = sticker
        }
        frameStickers.addView(iv)
    }

    // ─────────────────────────────────────────────
    // Print & Save
    // ─────────────────────────────────────────────

    private fun getFinalBitmap(): Bitmap? {
        val base = compositeBitmap ?: return null

        if (placedStickers.isEmpty()) return base

        // Gabungkan sticker ke bitmap
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        val scaleX = result.width.toFloat() / frameStickers.width
        val scaleY = result.height.toFloat() / frameStickers.height

        placedStickers.forEach { s ->
            val bmp = BitmapFactory.decodeResource(resources, s.resId)
            val scaledSize = (s.size * scaleX).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, scaledSize, scaledSize, true)
            val left = (s.x - s.size / 2) * scaleX
            val top = (s.y - s.size / 2) * scaleY
            canvas.drawBitmap(scaled, left, top, paint)
            scaled.recycle()
            bmp.recycle()
        }

        return result
    }

    private fun printPhoto() {
        val bitmap = getFinalBitmap() ?: return
        tvStatus.text = "Mengirim ke printer..."
        btnPrint.isEnabled = false

        Thread {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            val base64 = android.util.Base64.encodeToString(
                out.toByteArray(), android.util.Base64.NO_WRAP
            )

            printService.printPhoto(base64, copies = 1) { success, msg ->
                runOnUiThread {
                    tvStatus.text = if (success) "✅ $msg" else "❌ $msg"
                    btnPrint.isEnabled = true
                }
            }
        }.start()
    }

    private fun savePhoto() {
        val bitmap = getFinalBitmap() ?: return
        tvStatus.text = "Menyimpan..."

        Thread {
            val file = File(cacheDir, "final_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            runOnUiThread {
                tvStatus.text = "✅ Tersimpan: ${file.name}"
            }
        }.start()
    }
}