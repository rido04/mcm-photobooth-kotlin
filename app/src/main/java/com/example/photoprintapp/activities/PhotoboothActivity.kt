package com.example.photoprintapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import com.example.photoprintapp.adapters.PhotoGridAdapter
import com.example.photoprintapp.camera.HostCameraConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class PhotoboothActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PhotoboothRTSP"
    }

    private var selectedFilter = "NONE"
    private var gridCount = 4
    private val capturedPhotos = mutableListOf<String?>()

    private var isCameraReady = false
    private var isCountingDown = false
    private var isCapturing = false
    private var shouldAutoReconnect = false
    private var reconnectAttempt = 0
    private var countDownTimer: CountDownTimer? = null

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val reconnectRunnable = Runnable {
        if (shouldAutoReconnect && player == null) {
            Log.i(TAG, "retrying RTSP connection attempt=${reconnectAttempt + 1}")
            startPreview()
        }
    }

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

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "player state=BUFFERING url=${HostCameraConfig.RTSP_URL}")
                    showStatus("Buffering preview host camera...")
                }

                Player.STATE_READY -> {
                    reconnectAttempt = 0
                    isCameraReady = true
                    tvStatus.visibility = View.GONE
                    updateButtonStates()
                    Log.i(TAG, "player state=READY url=${HostCameraConfig.RTSP_URL}")
                }

                Player.STATE_ENDED -> {
                    isCameraReady = false
                    showStatus("Preview host selesai")
                    Log.w(TAG, "player state=ENDED, scheduling reconnect")
                    scheduleReconnect("Playback ended")
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            isCameraReady = false
            val shortMessage = error.message ?: error.errorCodeName
            Log.e(TAG, "RTSP playback error for ${HostCameraConfig.RTSP_URL}: $shortMessage", error)
            showStatus("Preview gagal: $shortMessage")
            scheduleReconnect(shortMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photobooth)

        selectedFilter = intent.getStringExtra("filter") ?: "NONE"

        playerView = findViewById(R.id.playerView)
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
        playerView.setKeepContentOnPlayerReset(true)

        initPhotoGrid()
        setupButtons()
        showStatus("Menghubungkan ke ${HostCameraConfig.RTSP_URL}")
    }

    private fun initPhotoGrid() {
        capturedPhotos.clear()
        repeat(gridCount) { capturedPhotos.add(null) }

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
                Toast.makeText(this, "Preview host belum siap", Toast.LENGTH_SHORT).show()
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
        showStatus("Mengambil foto dari host...")

        Thread {
            try {
                val request = Request.Builder()
                    .url(HostCameraConfig.CAPTURE_URL)
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("HTTP ${response.code}")
                    }

                    val body = response.body ?: throw IllegalStateException("Body kosong")
                    val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                        ?: throw IllegalStateException("Folder output tidak tersedia")
                    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")

                    body.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }

                    runOnUiThread {
                        Log.i(TAG, "capture success from ${HostCameraConfig.CAPTURE_URL}")
                        val idx = capturedPhotos.indexOfFirst { it == null }
                        if (idx >= 0) {
                            capturedPhotos[idx] = file.absolutePath
                            photoGridAdapter.notifyItemChanged(idx)
                        }
                        isCapturing = false
                        tvStatus.visibility = View.GONE
                        updateButtonStates()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    isCapturing = false
                    Log.e(TAG, "capture failed from ${HostCameraConfig.CAPTURE_URL}: ${e.message}", e)
                    showStatus("Capture host gagal: ${e.message}")
                    Toast.makeText(this, "Gagal capture: ${e.message}", Toast.LENGTH_SHORT).show()
                    updateButtonStates()
                }
            }
        }.start()
    }

    private fun hasEmpty() = capturedPhotos.any { it == null }

    private fun isComplete() = capturedPhotos.none { it == null }

    private fun updateButtonStates() {
        val canCapture = hasEmpty() && !isCountingDown && !isCapturing && isCameraReady
        btnCapture.alpha = if (canCapture) 1f else 0.5f
        btnOk.alpha = if (isComplete()) 1f else 0.4f
    }

    private fun startPreview() {
        if (player != null) return
        cancelReconnect()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                250,
                750,
                100,
                150
            )
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
        player = exoPlayer
        playerView.player = exoPlayer
        exoPlayer.addListener(playerListener)
        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        exoPlayer.playWhenReady = true

        val mediaSource = RtspMediaSource.Factory()
            .setForceUseRtpTcp(true)
            .createMediaSource(MediaItem.fromUri(HostCameraConfig.RTSP_URL))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        Log.i(TAG, "opening RTSP preview url=${HostCameraConfig.RTSP_URL}")
        showStatus("Membuka preview RTSP...")
    }

    private fun stopPreview() {
        cancelReconnect()
        player?.removeListener(playerListener)
        player?.stop()
        player?.release()
        player = null
        playerView.player = null
        isCameraReady = false
        Log.i(TAG, "preview stopped")
    }

    private fun scheduleReconnect(reason: String) {
        if (!shouldAutoReconnect) return

        reconnectAttempt += 1
        cancelReconnect()
        Log.w(
            TAG,
            "scheduling RTSP reconnect in ${HostCameraConfig.RECONNECT_DELAY_MS}ms " +
                "attempt=$reconnectAttempt reason=$reason"
        )
        showStatus("Preview putus, reconnect...")
        mainHandler.postDelayed(reconnectRunnable, HostCameraConfig.RECONNECT_DELAY_MS)
    }

    private fun cancelReconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
    }

    private fun showStatus(message: String) {
        tvStatus.text = message
        tvStatus.visibility = View.VISIBLE
        updateButtonStates()
    }

    override fun onStart() {
        super.onStart()
        shouldAutoReconnect = true
        startPreview()
    }

    override fun onStop() {
        shouldAutoReconnect = false
        stopPreview()
        super.onStop()
    }

    override fun onDestroy() {
        shouldAutoReconnect = false
        cancelReconnect()
        countDownTimer?.cancel()
        stopPreview()
        super.onDestroy()
    }
}
