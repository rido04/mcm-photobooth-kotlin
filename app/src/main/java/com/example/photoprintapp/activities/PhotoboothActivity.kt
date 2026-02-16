package com.example.photoprintapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photoprintapp.R
import com.example.photoprintapp.models.FilterType
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import java.io.File
import java.io.FileOutputStream

class PhotoboothActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILTER = "extra_filter"
        const val EXTRA_PHOTOS = "extra_photos"
        private const val TAG = "PhotoboothActivity"
        const val MAX_GRID = 4
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var tvCountdown: TextView
    private lateinit var tvGridInfo: TextView
    private lateinit var btnCapture: Button
    private lateinit var btnRetake: Button
    private lateinit var btnNext: Button
    private lateinit var btnGrid1: Button
    private lateinit var btnGrid2: Button
    private lateinit var btnGrid3: Button
    private lateinit var btnGrid4: Button
    private val photoSlots = arrayOfNulls<ImageView>(MAX_GRID)

    private var usbMonitor: USBMonitor? = null
    private var uvcCamera: UVCCamera? = null
    private var isCameraOpen = false
    private var isCountingDown = false

    private var selectedFilter = FilterType.NONE
    private var gridCount = 2
    private val capturedPhotos = arrayOfNulls<String>(MAX_GRID)
    private var currentSlot = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photobooth)

        selectedFilter = FilterType.valueOf(
            intent.getStringExtra(EXTRA_FILTER) ?: FilterType.NONE.name
        )

        initViews()
        initUsbMonitor()
        updateGridUI()
    }

    private fun initViews() {
        surfaceView = findViewById(R.id.surfaceCamera)
        tvCountdown = findViewById(R.id.tvCountdown)
        tvGridInfo = findViewById(R.id.tvGridInfo)
        btnCapture = findViewById(R.id.btnCapture)
        btnRetake = findViewById(R.id.btnRetake)
        btnNext = findViewById(R.id.btnNext)
        btnGrid1 = findViewById(R.id.btnGrid1)
        btnGrid2 = findViewById(R.id.btnGrid2)
        btnGrid3 = findViewById(R.id.btnGrid3)
        btnGrid4 = findViewById(R.id.btnGrid4)

        photoSlots[0] = findViewById(R.id.imgSlot1)
        photoSlots[1] = findViewById(R.id.imgSlot2)
        photoSlots[2] = findViewById(R.id.imgSlot3)
        photoSlots[3] = findViewById(R.id.imgSlot4)

        btnCapture.setOnClickListener { startCountdown() }
        btnRetake.setOnClickListener { retakeLastPhoto() }
        btnNext.setOnClickListener { goToPreview() }

        btnGrid1.setOnClickListener { setGridCount(1) }
        btnGrid2.setOnClickListener { setGridCount(2) }
        btnGrid3.setOnClickListener { setGridCount(3) }
        btnGrid4.setOnClickListener { setGridCount(4) }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "Surface created")
                if (isCameraOpen) startPreview(holder.surface)
            }
            override fun surfaceChanged(holder: SurfaceHolder, f: Int, w: Int, h: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                uvcCamera?.stopPreview()
            }
        })
    }

    // ─────────────────────────────────────────────
    // USB / UVC Camera
    // ─────────────────────────────────────────────

    private fun initUsbMonitor() {
        usbMonitor = USBMonitor(this, object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
                Log.d(TAG, "USB attached: ${device?.deviceName}")
                runOnUiThread {
                    Toast.makeText(this@PhotoboothActivity,
                        "USB Camera terhubung", Toast.LENGTH_SHORT).show()
                }
                device?.let { usbMonitor?.requestPermission(it) }
            }

            override fun onDettach(device: UsbDevice?) {
                Log.d(TAG, "USB detached")
                runOnUiThread {
                    isCameraOpen = false
                    Toast.makeText(this@PhotoboothActivity,
                        "USB Camera terputus", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
                createNew: Boolean
            ) {
                Log.d(TAG, "USB connected, opening UVC camera")
                openUvcCamera(ctrlBlock)
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                Log.d(TAG, "USB disconnected")
                closeUvcCamera()
            }

            override fun onCancel(device: UsbDevice?) {
                Log.w(TAG, "USB permission cancelled")
                runOnUiThread {
                    Toast.makeText(this@PhotoboothActivity,
                        "Permission USB ditolak", Toast.LENGTH_SHORT).show()
                }
            }
        })

        usbMonitor?.register()
    }

    private fun openUvcCamera(ctrlBlock: USBMonitor.UsbControlBlock?) {
        try {
            val camera = UVCCamera()
            camera.open(ctrlBlock)

            // Set format MJPEG 640x480
            try {
                camera.setPreviewSize(
                    UVCCamera.DEFAULT_PREVIEW_WIDTH,
                    UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                    UVCCamera.FRAME_FORMAT_MJPEG
                )
            } catch (e: Exception) {
                // Fallback ke YUV
                camera.setPreviewSize(
                    UVCCamera.DEFAULT_PREVIEW_WIDTH,
                    UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                    UVCCamera.FRAME_FORMAT_YUYV
                )
                Log.w(TAG, "MJPEG not supported, using YUYV")
            }

            uvcCamera = camera
            isCameraOpen = true

            runOnUiThread {
                val surface = surfaceView.holder.surface
                if (surface.isValid) startPreview(surface)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening UVC camera: ${e.message}", e)
            runOnUiThread {
                Toast.makeText(this,
                    "Gagal buka kamera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startPreview(surface: Surface) {
        try {
            uvcCamera?.setPreviewDisplay(surface)
            uvcCamera?.startPreview()
            Log.d(TAG, "Preview started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting preview: ${e.message}")
        }
    }

    private fun closeUvcCamera() {
        try {
            uvcCamera?.stopPreview()
            uvcCamera?.close()
            uvcCamera?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing camera: ${e.message}")
        } finally {
            uvcCamera = null
            isCameraOpen = false
        }
    }

    // ─────────────────────────────────────────────
    // Capture & Countdown
    // ─────────────────────────────────────────────

    private fun startCountdown() {
        if (isCountingDown || currentSlot >= gridCount) return
        isCountingDown = true
        btnCapture.isEnabled = false

        tvCountdown.visibility = View.VISIBLE

        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000) + 1
                runOnUiThread { tvCountdown.text = sec.toString() }
            }

            override fun onFinish() {
                runOnUiThread {
                    tvCountdown.text = "📸"
                    capturePhoto()
                }
            }
        }.start()
    }

    private fun capturePhoto() {
        uvcCamera?.setFrameCallback({ frame ->
            // Frame datang sekali, langsung capture
            uvcCamera?.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_NV21)

            // FIX: Convert ByteBuffer to ByteArray
            val byteArray = ByteArray(frame.remaining())
            frame.get(byteArray)

            val bitmap = frameToBitmap(
                byteArray,
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT
            )

            bitmap?.let { bmp ->
                val path = saveBitmap(bmp, currentSlot)
                runOnUiThread {
                    capturedPhotos[currentSlot] = path
                    photoSlots[currentSlot]?.setImageBitmap(bmp)
                    photoSlots[currentSlot]?.visibility = View.VISIBLE
                    currentSlot++
                    tvCountdown.visibility = View.GONE
                    isCountingDown = false
                    updateGridUI()
                }
            } ?: runOnUiThread {
                Toast.makeText(this, "Gagal capture frame", Toast.LENGTH_SHORT).show()
                tvCountdown.visibility = View.GONE
                isCountingDown = false
                btnCapture.isEnabled = true
            }
        }, UVCCamera.PIXEL_FORMAT_NV21)
    }

    private fun frameToBitmap(frame: ByteArray, width: Int, height: Int): Bitmap? {
        return try {
            val yuv = android.graphics.YuvImage(
                frame,
                android.graphics.ImageFormat.NV21,
                width, height, null
            )
            val out = java.io.ByteArrayOutputStream()
            yuv.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
            val bytes = out.toByteArray()
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "frameToBitmap error: ${e.message}")
            null
        }
    }

    private fun saveBitmap(bitmap: Bitmap, index: Int): String {
        val file = File(cacheDir, "photo_$index.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    // ─────────────────────────────────────────────
    // Grid & UI
    // ─────────────────────────────────────────────

    private fun setGridCount(count: Int) {
        gridCount = count
        currentSlot = 0
        capturedPhotos.fill(null)
        photoSlots.forEach { it?.setImageDrawable(null); it?.visibility = View.INVISIBLE }
        updateGridUI()
    }

    private fun updateGridUI() {
        tvGridInfo.text = "Foto $currentSlot / $gridCount"

        val allDone = currentSlot >= gridCount
        btnCapture.isEnabled = !allDone && !isCountingDown && isCameraOpen
        btnCapture.text = if (isCountingDown) "WAIT..." else "📸 CAPTURE"
        btnNext.isEnabled = allDone
        btnRetake.isEnabled = currentSlot > 0

        // Tampilkan slot sesuai gridCount
        for (i in 0 until MAX_GRID) {
            photoSlots[i]?.visibility = if (i < gridCount) View.VISIBLE else View.GONE
        }

        // Update grid button selection
        listOf(btnGrid1, btnGrid2, btnGrid3, btnGrid4).forEachIndexed { i, btn ->
            btn.alpha = if (i + 1 == gridCount) 1f else 0.5f
        }
    }

    private fun retakeLastPhoto() {
        if (currentSlot <= 0) return
        currentSlot--
        capturedPhotos[currentSlot] = null
        photoSlots[currentSlot]?.setImageDrawable(null)
        updateGridUI()
    }

    private fun goToPreview() {
        val photos = capturedPhotos.filterNotNull().toTypedArray()
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra(PreviewActivity.EXTRA_PHOTOS, photos)
            putExtra(PreviewActivity.EXTRA_FILTER, selectedFilter.name)
            putExtra(PreviewActivity.EXTRA_GRID_COUNT, gridCount)
        }
        startActivity(intent)
    }

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        usbMonitor?.register()
    }

    override fun onPause() {
        super.onPause()
        usbMonitor?.unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeUvcCamera()
        usbMonitor?.destroy()
    }
}