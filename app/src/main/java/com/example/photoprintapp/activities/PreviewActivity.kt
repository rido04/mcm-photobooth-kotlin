package com.example.photoprintapp.activities

import android.content.Intent
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import com.example.photoprintapp.adapters.StickerAdapter
import com.example.photoprintapp.views.LayoutConfig
import com.example.photoprintapp.views.PhotoCanvasView
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private var photoPaths = listOf<String>()
    private var selectedFilter = "NONE"
    private var gridCount = 4

    private lateinit var photoCanvas: PhotoCanvasView
    private lateinit var rvStickers: RecyclerView

    private val allStickers = listOf(
        "sticker/3d-glasses.png", "sticker/balloons.png", "sticker/beard.png",
        "sticker/birthday-cake.png", "sticker/crocodile.png", "sticker/crocodile-love.png",
        "sticker/february.png", "sticker/flamingo.png", "sticker/gift.png",
        "sticker/giraffe.png", "sticker/glasses.png", "sticker/hat.png",
        "sticker/january.png", "sticker/king.png", "sticker/learning.png",
        "sticker/light-bulb.png", "sticker/mask.png", "sticker/moustache-glasses.png",
        "sticker/moustache-mexico.png", "sticker/pamela-hat.png", "sticker/panda-bear.png",
        "sticker/queen.png", "sticker/sun-glasses.png", "sticker/tiger.png"
    )

    // ─── Layout Config per filter (mirip Flutter) ─────────────────────
    // Adjust nilai-nilai ini supaya posisi foto pas di frame PNG
    // cellWidth/cellHeight = ukuran cell dalam fraction (0.0 - 1.0) dari ukuran canvas
    // leftPadding/topPadding = jarak dari tepi kiri/atas
    // hGap/vGap = jarak antar cell horizontal/vertikal
    private fun getLayoutConfig(filter: String, gridCount: Int): LayoutConfig {
        val key = "${filter.lowercase()}_$gridCount"
        return when (key) {

            // ── EMOJI ──────────────────────────────────────────────────
            "emoji_4" -> LayoutConfig(
                cellWidth = 0.47f,
                cellHeight = 0.420f,
                leftPadding = 0.02f,
                topPadding = 0.057f,
                hGap = 0.02f,
                vGap = 0.016f
            )
            "emoji_6" -> LayoutConfig(
                cellWidth = 0.305f,
                cellHeight = 0.29f,
                leftPadding = 0.320f,
                topPadding = 0.059f,
                hGap = 0.025f,
                vGap = 0.010f
            )

            // ── FOOTBALL ───────────────────────────────────────────────
            "football_4" -> LayoutConfig(
                cellWidth = 0.45f,
                cellHeight = 0.42f,
                leftPadding = 0.04f,
                topPadding = 0.07f,
                hGap = 0.024f,
                vGap = 0.015f
            )
            "football_6" -> LayoutConfig(
                cellWidth = 0.305f,
                cellHeight = 0.29f,
                leftPadding = 0.320f,
                topPadding = 0.058f,
                hGap = 0.025f,
                vGap = 0.010f
            )

            // ── VALENTINE ──────────────────────────────────────────────
            "valentine_4" -> LayoutConfig(
                cellWidth = 0.45f,
                cellHeight = 0.428f,
                leftPadding = 0.04f,
                topPadding = 0.05f,
                hGap = 0.02f,
                vGap = 0.01f
            )
            "valentine_6" -> LayoutConfig(
                cellWidth = 0.305f,
                cellHeight = 0.29f,
                leftPadding = 0.320f,
                topPadding = 0.055f,
                hGap = 0.025f,
                vGap = 0.010f
            )

            // ── FRIENDSHIP ─────────────────────────────────────────────
            "friendship_4" -> LayoutConfig(
                cellWidth = 0.44f,
                cellHeight = 0.41f,
                leftPadding = 0.04f,
                topPadding = 0.09f,
                hGap = 0.04f,
                vGap = 0.025f
            )
            "friendship_6" -> LayoutConfig(
                cellWidth = 0.38f,
                cellHeight = 0.27f,
                leftPadding = 0.04f,
                topPadding = 0.08f,
                hGap = 0.04f,
                vGap = 0.025f
            )

            // ── FLOWERS ────────────────────────────────────────────────
            "flowers_4" -> LayoutConfig(
                cellWidth = 0.45f,
                cellHeight = 0.40f,
                leftPadding = 0.025f,
                topPadding = 0.08f,
                hGap = 0.04f,
                vGap = 0.03f
            )
            "flowers_6" -> LayoutConfig(
                cellWidth = 0.39f,
                cellHeight = 0.27f,
                leftPadding = 0.025f,
                topPadding = 0.07f,
                hGap = 0.04f,
                vGap = 0.025f
            )

            // ── NONE / DEFAULT ─────────────────────────────────────────
            "none_4" -> LayoutConfig(
                cellWidth = 0.48f,
                cellHeight = 0.48f,
                leftPadding = 0.01f,
                topPadding = 0.01f,
                hGap = 0.02f,
                vGap = 0.02f
            )
            "none_6" -> LayoutConfig(
                cellWidth = 0.48f,
                cellHeight = 0.31f,
                leftPadding = 0.01f,
                topPadding = 0.01f,
                hGap = 0.02f,
                vGap = 0.02f
            )
            else -> if (gridCount == 6) LayoutConfig(
                cellWidth = 0.48f,
                cellHeight = 0.31f,
                leftPadding = 0.01f,
                topPadding = 0.01f,
                hGap = 0.02f,
                vGap = 0.02f
            ) else LayoutConfig(
                cellWidth = 0.48f,
                cellHeight = 0.48f,
                leftPadding = 0.01f,
                topPadding = 0.01f,
                hGap = 0.02f,
                vGap = 0.02f
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        photoPaths = intent.getStringArrayListExtra("photos") ?: emptyList()
        selectedFilter = intent.getStringExtra("filter") ?: "NONE"
        gridCount = intent.getIntExtra("gridCount", 4)

        photoCanvas = findViewById(R.id.photoCanvas)
        rvStickers = findViewById(R.id.rvStickers)

        setupCanvas()
        setupStickerPicker()
        setupButtons()
    }

    private fun setupCanvas() {
        // NONE = tidak pakai frame
        if (selectedFilter.uppercase() != "NONE") {
            val frameName = getFrameName()
            try {
                val stream = assets.open("frames/$frameName")
                photoCanvas.frameBitmap = BitmapFactory.decodeStream(stream)
                stream.close()
            } catch (e: Exception) {
                Toast.makeText(this, "Frame tidak ditemukan: $frameName", Toast.LENGTH_SHORT).show()
            }
        } else {
            photoCanvas.frameBitmap = null // tidak ada frame
        }

        photoPaths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) photoCanvas.photos.add(bmp)
            }
        }

        photoCanvas.gridCount = gridCount
        photoCanvas.layoutConfig = getLayoutConfig(selectedFilter, gridCount)
        photoCanvas.invalidate()
    }
    
    private fun getFrameName(): String {
        val suffix = when (selectedFilter.uppercase()) {
            "FOOTBALL" -> "_football"
            "VALENTINE" -> "_valentine"
            else -> ""
        }
        return "frame${gridCount}_photobooth$suffix.png"
    }

    private fun setupStickerPicker() {
        val adapter = StickerAdapter(this, allStickers) { stickerFile ->
            try {
                val stream = assets.open(stickerFile)
                val bmp = BitmapFactory.decodeStream(stream)
                stream.close()
                photoCanvas.setPendingSticker(bmp)
                Toast.makeText(this, "Tap di foto untuk taruh sticker\nDouble tap sticker untuk hapus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal load sticker", Toast.LENGTH_SHORT).show()
            }
        }
        rvStickers.layoutManager = GridLayoutManager(this, 2)
        rvStickers.adapter = adapter
    }

    private fun setupButtons() {
        // FIX: back = kembali 1 screen (finish), bukan keluar app
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnSimpan).setOnClickListener {
            saveToGallery()
        }

        findViewById<LinearLayout>(R.id.btnPrint).setOnClickListener {
            Toast.makeText(this, "🖨 Fungsi print segera hadir!", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnUlang).setOnClickListener {
            val intent = Intent(this, FilterSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun saveToGallery() {
        try {
            val finalBitmap = photoCanvas.renderFinalBitmap()
            val filename = "photobooth_${System.currentTimeMillis()}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MaxGPhotobooth")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                Toast.makeText(this, "✅ Foto tersimpan ke galeri!", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}