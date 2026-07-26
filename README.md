# Komvid (Video Compressor)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.1-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Stable-success?style=flat-square)

**Komvid** adalah aplikasi kompresi video Android super efisien yang dirancang khusus untuk memaksimalkan **Hardware Encoder (SoC/GPU)** bawaan HP. Menggunakan **Google Media3 Transformer**, Komvid menghasilkan kompresi video ultra cepat, hemat baterai, dan beroperasi penuh di latar belakang secara aman.

---

## 🔥 Fitur Unggulan v1.1

* ⚡ **100% Hardware Accelerated:** Menggunakan chip akselerasi hardware HP. Perangkat tetap dingin dan baterai hemat.
* 🔇 **Mute Audio Option (Baru):** Pilihan untuk membisukan/menghapus track suara video agar ukuran file menyusut lebih drastis.
* ✖ **Batal Kompresi Kapan Saja (Baru):** Kendali penuh untuk membatalkan proses kompresi secara instan melalui aplikasi atau laci Notifikasi.
* ℹ️ **Detail Metadata Video (Baru):** Menampilkan resolusi asli, durasi terformat, ukuran file, dan estimasi hasil kompresi secara presisi.
* 📉 **Smart Downscaling & Bitrate Control:** Penyesuaian otomatis resolusi (4K ➔ 2K ➔ 1080p ➔ 720p) tanpa membuat video kabur.
* 🛡️ **True Background Service & Wakelock:** Aman ditinggal multitasking (buka TikTok, IG, dll) karena berjalan di *Foreground Service* dengan proteksi *Wakelock*.
* ⏳ **Real-Time ETA & Progress:** Estimasi waktu tersisa dan persentase progress terpusat dengan indikator visual modern.
* 📁 **Direct MediaStore Export:** Otomatis menyimpan hasil kompresi di folder `Movies/Komvid` dan langsung muncul di Galeri HP.
* 🎨 **Modern Material 3 Dark UI:** Desain antarmuka gelap modern (*Emerald & Cyan Theme*) dengan tata letak presisi di semua rasio layar.
* 📦 **Super Lightweight APK:** Ukuran APK Release hanya **~1.4 MB** berkat optimasi R8 Code & Resource Shrinking.

---

## 🛠 Tech Stack & Library

* **Language:** Kotlin
* **Minimum SDK:** API 24 (Android 7.0)
* **Target SDK:** API 37
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Video Engine:** AndroidX Media3 Transformer & Effect
* **Concurrency:** Kotlin Coroutines & StateFlow
* **Background Task:** Android Foreground Services
* **Storage:** MediaStore API (Scoped Storage Compatible)

---

## 📦 Cara Build APK

1. Clone repositori ini:
   ```bash
   git clone https://github.com/alfisjahri/komvid.git
   cd komvid
   ```
2. Pastikan file `local.properties` menunjuk ke lokasi Android SDK Anda:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```
3. Build **Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
4. File APK dapat ditemukan di `app/build/outputs/apk/release/app-release.apk`.
