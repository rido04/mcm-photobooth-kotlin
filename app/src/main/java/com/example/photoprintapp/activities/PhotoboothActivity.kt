package com.example.photoprintapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.CountDownTimer
import android.view.SurfaceView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import com.example.photoprintapp.adapters.PhotoGridAdapter
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import java.io.File
import java.io.FileOutputStream

class PhotoboothActivity : AppCompatActivity() {

    private var selectedFilter = "NONE"
    private var gridCount = 4
    private val capturedPhotos = mutableListOf<String?>()

    private var usbMonitor: USBMonitor? = null
    private var uvcCamera: UVCCamera? = null
    private var isCameraReady = false
    private var isCountingDown = false
    private var isCapturing = false
    private var countDownTimer: CountDownTimer? = null

    private lateinit var surfaceView: SurfaceView
    private lateinit var tvStatus: TextView
    private lateinit var tvFilterLabel: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var layoutCountdown: LinearLayout
    private lateinit var btnCapture: LinearLayout
    private lateinit var btnRetake: LinearLayout
    private lateinit var btnOk: LinearLayout
    private lateinit var btn4Foto: TextView
    private lateinit var btn6Foto: TextView
    private lateinit var rvPhotoGrid: RecyclerView
    private lateinit var photoGridAdapter: PhotoGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photobooth)

        selectedFilter = intent.getStringExtra("filter") ?: "NONE"

        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        tvStatus = findViewById(R.id.tvStatus)
        tvFilterLabel = findViewById(R.id.tvFilterLabel)
        tvCountdown = findViewById(R.id.tvCountdown)
        layoutCountdown = findViewById(R.id.layoutCountdown)
        btnCapture = findViewById(R.id.btnCapture)
        btnRetake = findViewById(R.id.btnRetake)
        btnOk = findViewById(R.id.btnOk)
        btn4Foto = findViewById(R.id.btn4Foto)
        btn6Foto = findViewById(R.id.btn6Foto)
        rvPhotoGrid = findViewById(R.id.rvPhotoGrid)

        tvFilterLabel.text = "Filter: $selectedFilter"

        initPhotoGrid()
        setupButtons()
        setupUsbCamera()
    }

    private fun initPhotoGrid() {
        capturedPhotos.clear()
        repeat(gridCount) { capturedPhotos.add(null) }

        // FIX THUMBNAIL: pass reference langsung, bukan .toMutableList() (copy)
        photoGridAdapter = PhotoGridAdapter(capturedPhotos)
        rvPhotoGrid.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPhotoGrid.adapter = photoGridAdapter
        updateButtonStates()
    }

    private fun setupButtons() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        btnCapture.setOnClickListener {
            if (!isCountingDown && !isCapturing && isCameraReady && hasEmpty()) {
                startCountdown()
            } else if (!isCameraReady) {
                Toast.makeText(this, "Colokkan webcam dulu!", Toast.LENGTH_SHORT).show()
            }
        }

        btnRetake.setOnClickListener {
            val idx = capturedPhotos.indexOfLast { it != null }
            if (idx >= 0) {
                capturedPhotos[idx] = null
                photoGridAdapter.notifyItemChanged(idx)
                updateButtonStates()
            }
        }

        btnOk.setOnClickListener {
            if (isComplete()) {
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra("filter", selectedFilter)
                intent.putExtra("gridCount", gridCount)
                intent.putStringArrayListExtra("photos", ArrayList(capturedPhotos.filterNotNull()))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Foto belum lengkap!", Toast.LENGTH_SHORT).show()
            }
        }

        btn4Foto.setOnClickListener { changeGrid(4) }
        btn6Foto.setOnClickListener { changeGrid(6) }
        updateGridButtons()
    }

    private fun changeGrid(count: Int) {
        gridCount = count
        capturedPhotos.clear()
        repeat(gridCount) { capturedPhotos.add(null) }
        photoGridAdapter.notifyDataSetChanged()
        updateGridButtons()
        updateButtonStates()
    }

    private fun updateGridButtons() {
        btn4Foto.setBackgroundResource(
            if (gridCount == 4) R.drawable.bg_grid_btn_active else R.drawable.bg_grid_btn_inactive
        )
        btn4Foto.setTextColor(if (gridCount == 4) 0xFFFFFFFF.toInt() else 0xFFAAAACC.toInt())
        btn6Foto.setBackgroundResource(
            if (gridCount == 6) R.drawable.bg_grid_btn_active else R.drawable.bg_grid_btn_inactive
        )
        btn6Foto.setTextColor(if (gridCount == 6) 0xFFFFFFFF.toInt() else 0xFFAAAACC.toInt())
    }

    private fun startCountdown() {
        isCountingDown = true
        layoutCountdown.visibility = View.VISIBLE
        updateButtonStates()
        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(ms: Long) {
                tvCountdown.text = ((ms / 1000) + 1).toString()
            }
            override fun onFinish() {
                layoutCountdown.visibility = View.GONE
                isCountingDown = false
                capturePhoto()
            }
        }.start()
    }

    private fun capturePhoto() {
        if (isCapturing) return
        isCapturing = true
        updateButtonStates()

        uvcCamera?.setFrameCallback({ frame ->
            if (frame != null) {
                val w = UVCCamera.DEFAULT_PREVIEW_WIDTH
                val h = UVCCamera.DEFAULT_PREVIEW_HEIGHT
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                bmp.copyPixelsFromBuffer(frame)

                // FIX FREEZE: matikan callback dari UI thread bukan dari callback thread
                runOnUiThread {
                    uvcCamera?.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_RGB565)
                    savePhoto(bmp)
                }
            }
        }, UVCCamera.PIXEL_FORMAT_RGB565)
    }

    private fun savePhoto(bitmap: Bitmap) {
        try {
            val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }

            val idx = capturedPhotos.indexOfFirst { it == null }
            if (idx >= 0) {
                capturedPhotos[idx] = file.absolutePath
                photoGridAdapter.notifyItemChanged(idx)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error simpan foto: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isCapturing = false
            updateButtonStates()
        }
    }

    private fun hasEmpty() = capturedPhotos.any { it == null }
    private fun isComplete() = capturedPhotos.none { it == null }

    private fun updateButtonStates() {
        val canCapture = hasEmpty() && !isCountingDown && !isCapturing && isCameraReady
        btnCapture.alpha = if (canCapture) 1f else 0.5f
        btnOk.alpha = if (isComplete()) 1f else 0.4f
    }

    // ─── USB Camera ───────────────────────────────────────────────────

    private fun setupUsbCamera() {
        usbMonitor = USBMonitor(this, object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) { usbMonitor?.requestPermission(device) }
            override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?, createNew: Boolean) { openCamera(ctrlBlock) }
            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) { closeCamera() }
            override fun onDettach(device: UsbDevice?) {
                runOnUiThread {
                    tvStatus.visibility = View.VISIBLE
                    isCameraReady = false
                    updateButtonStates()
                }
            }
            override fun onCancel(device: UsbDevice?) {}
        })
    }

    private fun openCamera(ctrlBlock: USBMonitor.UsbControlBlock?) {
        uvcCamera = UVCCamera()
        uvcCamera?.open(ctrlBlock)
        try {
            uvcCamera?.setPreviewSize(
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                UVCCamera.FRAME_FORMAT_MJPEG
            )
        } catch (e: Exception) {
            uvcCamera?.setPreviewSize(
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                UVCCamera.FRAME_FORMAT_YUYV
            )
        }
        uvcCamera?.setPreviewDisplay(surfaceView.holder)
        uvcCamera?.startPreview()
        runOnUiThread {
            isCameraReady = true
            tvStatus.visibility = View.GONE
            updateButtonStates()
        }
    }

    private fun closeCamera() {
        uvcCamera?.stopPreview()
        uvcCamera?.close()
        uvcCamera?.destroy()
        uvcCamera = null
        isCameraReady = false
    }

    override fun onStart() { super.onStart(); usbMonitor?.register() }
    override fun onStop() { super.onStop(); usbMonitor?.unregister() }
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        closeCamera()
        usbMonitor?.destroy()
    }
}