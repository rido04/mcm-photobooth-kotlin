package com.example.photoprintapp

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.photoprintapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentPhotoPath: String = ""
    private var currentPhotoBitmap: Bitmap? = null
    private lateinit var usbManager: UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbConnection: UsbDeviceConnection? = null

    private val ACTION_USB_PERMISSION = "com.example.photoprintapp.USB_PERMISSION"

    // Launcher untuk mengambil foto dengan kamera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            loadPhotoFromPath()
        } else {
            updateStatus("Gagal mengambil foto")
        }
    }

    // Launcher untuk memilih foto dari galeri
    private val selectPictureLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                currentPhotoBitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageView.setImageBitmap(currentPhotoBitmap)
                binding.btnPrint.isEnabled = true
                updateStatus("Foto berhasil dipilih. Siap untuk print!")
            } catch (e: Exception) {
                updateStatus("Gagal memuat foto: ${e.message}")
            }
        }
    }

    // Launcher untuk request permission kamera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permission kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // USB Permission receiver
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            updateStatus("USB Permission granted untuk ${it.deviceName}")
                            usbDevice = it
                            connectToUsbDevice(it)
                        }
                    } else {
                        updateStatus("USB Permission ditolak")
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
                device?.let {
                    if (it == usbDevice) {
                        updateStatus("Printer USB terputus")
                        usbConnection?.close()
                        usbConnection = null
                        usbDevice = null
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

            // Register USB receiver
            val filter = IntentFilter().apply {
                addAction(ACTION_USB_PERMISSION)
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(usbReceiver, filter)
            }

            // Setup button listeners
            binding.btnTakePhoto.setOnClickListener {
                checkCameraPermissionAndTakePhoto()
            }

            binding.btnSelectPhoto.setOnClickListener {
                selectPictureLauncher.launch("image/*")
            }

            binding.btnPrint.setOnClickListener {
                printPhoto()
            }

            // Check USB devices saat startup (dengan delay kecil)
            binding.root.post {
                checkUsbDevices()
            }

            // Handle jika dibuka dari USB intent
            handleUsbIntent(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Error saat membuka app: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleUsbIntent(it) }
    }

    private fun handleUsbIntent(intent: Intent) {
        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }

            device?.let {
                if (it.vendorId == 1193) { // Canon vendor ID
                    updateStatus("Canon printer terdeteksi via USB")
                    requestUsbPermission(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            // Receiver mungkin belum registered
        }
        usbConnection?.close()
    }

    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.photoprintapp.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoURI)
        } catch (ex: IOException) {
            updateStatus("Error membuat file foto: ${ex.message}")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PHOTO_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun loadPhotoFromPath() {
        try {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            currentPhotoBitmap = bitmap
            binding.imageView.setImageBitmap(bitmap)
            binding.btnPrint.isEnabled = true
            updateStatus("Foto berhasil diambil. Siap untuk print!")
        } catch (e: Exception) {
            updateStatus("Gagal memuat foto: ${e.message}")
        }
    }

    private fun checkUsbDevices() {
        try {
            val deviceList = usbManager.deviceList
            updateStatus("Ditemukan ${deviceList.size} USB device(s)")

            deviceList.values.forEach { device ->
                // Canon vendor ID = 0x04a9 (1193 decimal)
                if (device.vendorId == 1193) {
                    updateStatus("Canon printer terdeteksi: ${device.deviceName}")
                    requestUsbPermission(device)
                    return
                }
            }

            if (deviceList.isEmpty()) {
                updateStatus("Tidak ada USB device terdeteksi. Colokkan printer Canon PIXMA G1730.")
            }
        } catch (e: Exception) {
            updateStatus("Error checking USB: ${e.message}")
        }
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun connectToUsbDevice(device: UsbDevice) {
        try {
            usbConnection = usbManager.openDevice(device)
            if (usbConnection != null) {
                updateStatus("Terhubung ke printer: ${device.deviceName}")
                usbDevice = device
            } else {
                updateStatus("Gagal membuka koneksi USB")
            }
        } catch (e: Exception) {
            updateStatus("Error koneksi USB: ${e.message}")
        }
    }

    private fun printPhoto() {
        currentPhotoBitmap?.let { bitmap ->
            // Menggunakan Android Print Framework (kompatibel dengan printer manapun)
            printUsingAndroidPrintFramework(bitmap)
        } ?: run {
            updateStatus("Tidak ada foto untuk di-print")
        }
    }

    private fun printUsingAndroidPrintFramework(bitmap: Bitmap) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "PhotoPrintApp - Photo Print"

        val printAdapter = PhotoPrintDocumentAdapter(this, bitmap)

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        updateStatus("Print job dikirim ke sistem Android")
    }

    private fun updateStatus(message: String) {
        try {
            runOnUiThread {
                if (::binding.isInitialized) {
                    binding.tvStatus.text = message
                }
            }
        } catch (e: Exception) {
            // Ignore jika UI belum siap
        }
    }
}