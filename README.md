# Komvid (Video Compressor)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.1-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Stable-success?style=flat-square)

**Komvid** ini aplikasi kompresi video Android super efisien yang saya racik khusus buat memaksimalkan **Hardware Encoder (SoC/GPU)** bawaan HP. Beda sama kompresor tradisional yang nyiksa CPU sampai bikin perangkat panas (*thermal throttling*), Komvid sepenuhnya pakai **Google Media3 Transformer**. Hasilnya? Kompresi berjalan sangat cepat, hemat baterai, dan aman banget dijalankan di latar belakang sambil *multitasking*!

## 🔥 Fitur Unggulan

*   **100% Hardware Accelerated:** Tidak menggunakan *software encode* (CPU). Perangkat tetap dingin dan baterai awet.
*   **Smart Downscaling:** Mengurangi ukuran file secara drastis (bisa lebih dari 50%) dengan menurunkan resolusi (4K -> 2K -> 1080p -> 720p). Aplikasi cerdas mencegah *upscaling* agar video tidak buram atau *pixelated*.
*   **True Background Service & Wakelock:** Berjalan sebagai *Foreground Service* di Android dengan proteksi *Wakelock*. Bebas tekan tombol Home atau buka aplikasi lain (TikTok, IG, dll) tanpa takut proses kompresi dihentikan oleh OS.
*   **Real-Time ETA & Progress:** Menampilkan estimasi sisa waktu penyelesaian kompresi langsung di layar aplikasi dan laci Notifikasi.
*   **Direct MediaStore Export:** Video hasil kompresi langsung tersimpan rapi di folder publik (`Movies/Komvid`) dan otomatis muncul di Galeri utama HP.
*   **Modern Minimalist UI:** Dibangun sepenuhnya dengan Jetpack Compose (Material Design 3) yang *clean* dan responsif.

## 🛠 Tech Stack & Library

*   **Language:** Kotlin
*   **Minimum SDK:** API 24 (Android 7.0)
*   **Target SDK:** API 37
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Video Engine:** AndroidX Media3 Transformer & Effect
*   **Concurrency:** Kotlin Coroutines & StateFlow
*   **Background Task:** Android Foreground Services
*   **Storage:** MediaStore API (Mendukung Scoped Storage Android 10 - 14+)
