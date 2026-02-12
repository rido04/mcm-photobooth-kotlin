# Photo Print App - Aplikasi Android Kotlin

Aplikasi Android sederhana untuk mengambil foto dan print ke printer Canon PIXMA G1730 via USB.

## Fitur
- 📷 Ambil foto menggunakan kamera
- 🖼️ Pilih foto dari galeri
- 🖨️ Print foto ke printer USB (Canon PIXMA G1730)
- ✅ Support Android Print Framework

## Cara Setup Project

### 1. Install Android Studio
Download dan install Android Studio dari: https://developer.android.com/studio

### 2. Buka Project
1. Buka Android Studio
2. Pilih "Open" atau "Open an Existing Project"
3. Navigate ke folder `PhotoPrintApp` dan klik OK

### 3. Sync Gradle
- Android Studio akan otomatis sync Gradle
- Tunggu sampai proses sync selesai
- Jika ada error, klik "Sync Now"

### 4. Setup Android Device

#### Untuk Testing di HP Android:
1. **Enable Developer Options**:
   - Buka Settings → About Phone
   - Tap "Build Number" 7 kali
   
2. **Enable USB Debugging**:
   - Buka Settings → Developer Options
   - Aktifkan "USB Debugging"
   
3. **Hubungkan HP ke Komputer**:
   - Colok HP dengan kabel USB
   - Pilih "File Transfer" / "MTP" mode
   - Accept USB debugging prompt di HP

### 5. Build & Run
1. Di Android Studio, pilih device Anda di dropdown (atas)
2. Klik tombol Run (▶️) atau tekan Shift+F10
3. App akan terinstall dan buka otomatis di HP

## Cara Menggunakan App

### 1. Ambil Foto
- Tap tombol "📷 Ambil Foto"
- Izinkan akses kamera jika diminta
- Ambil foto
- Foto akan muncul di preview

### 2. Atau Pilih Foto dari Galeri
- Tap tombol "🖼️ Pilih Foto"
- Pilih foto dari galeri
- Foto akan muncul di preview

### 3. Print Foto
- Setelah ada foto di preview, tombol "🖨️ Print Foto" akan aktif
- Colok printer Canon PIXMA G1730 ke HP dengan **USB OTG adapter**
- Tap tombol "Print Foto"
- Dialog print akan muncul
- Pilih printer Canon PIXMA G1730
- Atur setting (ukuran kertas, orientasi, dll)
- Tap "Print"

## Catatan Penting untuk Printer USB

### Menggunakan Android Print Framework (RECOMMENDED)
Aplikasi ini menggunakan **Android Print Framework** yang:
- ✅ Lebih mudah dan reliable
- ✅ Support semua printer yang di-support Android
- ✅ Tidak butuh driver khusus
- ✅ UI print dari sistem Android

**Cara kerjanya:**
1. Android akan detect printer via USB OTG
2. Saat klik Print, dialog print system Android akan muncul
3. Pilih printer Canon PIXMA G1730
4. Android akan handle komunikasi dengan printer

### Requirement Hardware
- **USB OTG Adapter/Cable** diperlukan untuk colok printer ke HP Android
- Canon PIXMA G1730 harus ON dan tercolok via USB OTG

### Troubleshooting

**Printer tidak terdeteksi:**
1. Pastikan USB OTG adapter berfungsi
2. Pastikan printer ON
3. Coba cabut-colok USB
4. Restart app
5. Check di Settings → Printing → cari Canon printer service

**Print gagal:**
1. Pastikan kertas ada di printer
2. Pastikan tinta cukup
3. Coba print test page dari Settings HP
4. Restart printer dan app

**Permission kamera ditolak:**
1. Buka Settings HP → Apps → Photo Print App → Permissions
2. Berikan akses Camera

## Struktur Project

```
PhotoPrintApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/photoprintapp/
│   │       │   ├── MainActivity.kt
│   │       │   └── PhotoPrintDocumentAdapter.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── values/
│   │       │   │   └── strings.xml
│   │       │   └── xml/
│   │       │       ├── device_filter.xml
│   │       │       └── file_paths.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Teknologi
- Kotlin
- Android SDK 24+
- ViewBinding
- Android Print Framework
- USB Host API
- Camera2 API

## License
MIT License - Bebas digunakan untuk project pribadi maupun komersial

---
**Dibuat dengan ❤️ untuk testing Canon PIXMA G1730 USB printing**
