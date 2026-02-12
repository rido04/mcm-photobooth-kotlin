# 📱 Tutorial Lengkap: Build Android App dari Scratch

## ✅ Langkah-Langkah Setup Project

### 1️⃣ Install Android Studio (Jika Belum)

```bash
# Download dari:
https://developer.android.com/studio

# Install dan buka Android Studio
# Pada first run, install:
- Android SDK
- Android SDK Platform
- Android Virtual Device (optional, kalau mau pakai emulator)
```

---

### 2️⃣ Import Project ke Android Studio

**Option A: Dari File Explorer**
1. Extract folder `PhotoPrintApp` (project ini)
2. Buka Android Studio
3. Pilih **File → Open**
4. Navigate ke folder `PhotoPrintApp`
5. Klik **OK**

**Option B: Dari Welcome Screen**
1. Buka Android Studio
2. Klik **Open**
3. Pilih folder `PhotoPrintApp`
4. Klik **OK**

---

### 3️⃣ Sync Gradle (Penting!)

Setelah project terbuka:

1. **Android Studio akan otomatis mulai sync Gradle**
   - Lihat progress bar di bawah
   - Tunggu sampai selesai (bisa 2-5 menit)

2. **Jika muncul error atau tidak auto-sync:**
   - Klik **File → Sync Project with Gradle Files**
   - Atau klik icon 🐘 (Gradle Sync) di toolbar

3. **Jika muncul "Gradle version not supported":**
   - Klik link yang disediakan untuk update
   - Atau edit file `gradle/wrapper/gradle-wrapper.properties`

---

### 4️⃣ Setup Device untuk Testing

#### A. Testing di HP Android Fisik (RECOMMENDED untuk USB Printer)

**1. Enable Developer Options:**
```
Settings → About Phone → Tap "Build Number" 7x
(Akan muncul "You are now a developer!")
```

**2. Enable USB Debugging:**
```
Settings → Developer Options → USB Debugging → ON
```

**3. Hubungkan HP ke Laptop:**
```
1. Colok HP dengan kabel USB
2. Di HP, pilih "File Transfer" atau "MTP"
3. Accept popup "Allow USB debugging?" → Allow
4. Centang "Always allow from this computer"
```

**4. Verifikasi Device Terdeteksi:**
```
Di Android Studio:
- Lihat dropdown di toolbar (sebelah tombol Run)
- Harusnya muncul nama HP kamu
```

#### B. Testing di Emulator (Tidak bisa untuk USB Printer)

⚠️ **CATATAN**: Emulator tidak support USB OTG, jadi tidak bisa test printer USB!

Jika tetap mau buat emulator:
```
Tools → Device Manager → Create Device
- Pilih device (misal: Pixel 4)
- Pilih system image (misal: Android 13)
- Finish
```

---

### 5️⃣ Build & Run App

**Cara 1: Pakai Tombol Run**
1. Pastikan device sudah terdeteksi di dropdown
2. Klik tombol **Run** (▶️) atau tekan **Shift + F10**
3. Tunggu build process (pertama kali bisa 5-10 menit)
4. App akan install dan buka otomatis di HP

**Cara 2: Build APK Manual**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)

APK akan ada di:
app/build/outputs/apk/debug/app-debug.apk

Transfer APK ini ke HP dan install manual
```

---

### 6️⃣ Testing Aplikasi

#### Test 1: Ambil Foto
```
1. Buka app
2. Tap "📷 Ambil Foto"
3. Izinkan akses kamera (jika diminta)
4. Ambil foto
5. Foto muncul di preview ✓
6. Button "Print Foto" jadi aktif ✓
```

#### Test 2: Pilih Foto dari Galeri
```
1. Tap "🖼️ Pilih Foto"
2. Pilih foto dari galeri
3. Foto muncul di preview ✓
```

#### Test 3: Print (Butuh Printer & USB OTG)
```
1. Siapkan:
   - Canon PIXMA G1730 (ON + ada kertas)
   - USB OTG adapter/cable
   
2. Colok printer ke HP via USB OTG

3. Di app, tap "🖨️ Print Foto"

4. Dialog print system Android akan muncul

5. Pilih printer "Canon PIXMA G1730"

6. Atur:
   - Paper size (A4, 4R, dll)
   - Orientation (Portrait/Landscape)
   - Copies
   
7. Tap "Print" ✓
```

---

## 🔧 Troubleshooting

### Problem: Gradle Sync Failed

**Solution:**
```bash
# 1. Check koneksi internet (Gradle download dependencies)

# 2. Invalidate Caches:
File → Invalidate Caches / Restart → Invalidate and Restart

# 3. Delete .gradle folder dan sync lagi:
# Tutup Android Studio
# Delete folder: PhotoPrintApp/.gradle
# Buka lagi dan sync
```

### Problem: Device Tidak Terdeteksi

**Solution:**
```bash
# 1. Check USB cable (harus data cable, bukan charging only)

# 2. Check USB debugging sudah ON

# 3. Coba cabut-colok USB

# 4. Coba port USB lain di laptop

# 5. Check di terminal:
# Buka Terminal di Android Studio
adb devices

# Harusnya muncul:
# List of devices attached
# ABC123456789    device

# Jika muncul "unauthorized", unlock HP dan allow debugging
```

### Problem: Build Failed - SDK Not Found

**Solution:**
```bash
# 1. Install SDK yang diperlukan:
Tools → SDK Manager
→ SDK Platforms: Install Android 13.0 (API 33) atau 14.0 (API 34)
→ SDK Tools: Install "Android SDK Build-Tools"

# 2. Check file local.properties (auto-generated):
# Harusnya berisi path ke Android SDK
sdk.dir=/Users/yourname/Library/Android/sdk
```

### Problem: App Crash saat Ambil Foto

**Solution:**
```bash
# 1. Check permissions di AndroidManifest.xml (sudah ada ✓)

# 2. Grant permission manual:
Settings HP → Apps → Photo Print App → Permissions
→ Camera: Allow
→ Files: Allow

# 3. Check logcat di Android Studio:
View → Tool Windows → Logcat
# Lihat error message
```

### Problem: Printer Tidak Terdeteksi

**Solution:**
```bash
# 1. Check USB OTG adapter berfungsi
# Test dengan flash disk

# 2. Check printer ON dan ready

# 3. Check di Settings HP:
Settings → Connected devices → USB
# Harusnya muncul printer

# 4. Install Canon Print Service (optional):
# Buka Play Store
# Search "Canon Print Service"
# Install
# Enable di Settings → Printing

# 5. Restart HP dan printer
```

---

## 📂 Struktur Project Explained

```
PhotoPrintApp/
│
├── app/
│   ├── build.gradle.kts        # Dependencies & config app
│   │
│   └── src/main/
│       ├── AndroidManifest.xml  # Permissions & config
│       │
│       ├── java/.../photoprintapp/
│       │   ├── MainActivity.kt              # Logic utama
│       │   └── PhotoPrintDocumentAdapter.kt # Logic print
│       │
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml  # UI layout
│           │
│           ├── values/
│           │   └── strings.xml        # Text resources
│           │
│           └── xml/
│               ├── device_filter.xml  # USB device filter
│               └── file_paths.xml     # FileProvider paths
│
├── build.gradle.kts      # Config Gradle project level
├── settings.gradle.kts   # Project settings
└── gradle.properties     # Gradle properties
```

---

## 🚀 Command Line Build (Advanced)

Jika lebih suka pakai terminal:

```bash
# 1. Navigate ke project folder
cd PhotoPrintApp

# 2. Make gradlew executable (Mac/Linux)
chmod +x gradlew

# 3. Build debug APK
./gradlew assembleDebug

# Windows:
gradlew.bat assembleDebug

# APK ada di:
# app/build/outputs/apk/debug/app-debug.apk

# 4. Install ke device (harus connect via USB)
./gradlew installDebug

# 5. Clean build (jika ada masalah)
./gradlew clean
./gradlew assembleDebug
```

---

## 📝 Modifikasi App

### Ganti Nama App
```xml
<!-- File: app/src/main/res/values/strings.xml -->
<string name="app_name">Nama App Baru</string>
```

### Ganti Package Name
1. Klik kanan folder `com.example.photoprintapp`
2. Refactor → Rename
3. Update di `AndroidManifest.xml` dan `build.gradle.kts`

### Ganti Icon
1. Klik kanan folder `res`
2. New → Image Asset
3. Pilih icon/image
4. Generate

### Add New Features
Edit file `MainActivity.kt` dan tambahkan function baru

---

## ❓ FAQ

**Q: Minimal Android version?**
A: Android 7.0 (API 24) ke atas

**Q: Bisa print tanpa USB OTG?**
A: Tidak. Canon PIXMA G1730 cuma support USB. Butuh USB OTG adapter.

**Q: Bisa pakai WiFi/Bluetooth print?**
A: Tidak. G1730 tidak punya WiFi/Bluetooth. Cuma USB.

**Q: Kenapa pakai Android Print Framework, bukan direct USB?**
A: Karena:
- Lebih mudah & reliable
- Tidak butuh implementasi protokol printer
- Support otomatis semua printer yang Android support
- UI print dari sistem (profesional)

**Q: Bisa pakai printer lain?**
A: Ya! App ini harusnya work dengan printer manapun yang support USB OTG dan Android Print Framework.

---

**Happy Coding! 🎉**

Jika ada pertanyaan atau error, check logcat di Android Studio untuk detail error.
