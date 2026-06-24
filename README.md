# 🎬 Komvid (Video Compressor)

Komvid adalah aplikasi kompresi video Android super efisien yang dirancang khusus untuk memaksimalkan **Hardware Encoder (SoC/GPU)** bawaan *smartphone*. 

Berbeda dengan aplikasi kompresor tradisional yang menyiksa CPU hingga membuat perangkat panas (*thermal throttling*), Komvid sepenuhnya menggunakan **Google Media3 Transformer**. Hasilnya? Kompresi berjalan sangat cepat, hemat baterai, dan aman dijalankan di latar belakang sambil *multitasking*.

## ✨ Fitur Unggulan

* 🚀 **100% Hardware Accelerated:** Tidak menggunakan *software encode* (CPU). Perangkat tetap dingin dan baterai awet.
* 📉 **Smart Downscaling:** Mengurangi ukuran file secara drastis (bisa lebih dari 50%) dengan menurunkan resolusi (4K ➔ 2K ➔ 1080p ➔ 720p). Aplikasi cerdas mencegah *upscaling* agar video tidak buram/pixelated.
* 👻 **True Background Service:** Berjalan sebagai *Foreground Service* di Android. Bebas tekan tombol Home atau buka aplikasi lain (TikTok, IG, dll) tanpa takut proses kompresi dihentikan oleh OS.
* ⏱️ **Real-Time ETA & Progress:** Menampilkan estimasi sisa waktu penyelesaian kompresi langsung di layar aplikasi dan laci Notifikasi.
* 📂 **Direct MediaStore Export:** Video hasil kompresi langsung tersimpan rapi di folder publik (`Movies/Komvid`) dan otomatis muncul di Galeri utama HP.
* 🎨 **Modern Minimalist UI:** Dibangun sepenuhnya dengan Jetpack Compose (Material Design 3) yang *clean* dan responsif.

## 🛠️ Teknologi yang Digunakan

* **Bahasa:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Video Engine:** [AndroidX Media3 Transformer & Effect](https://developer.android.com/media/media3/transformer)
* **Concurrency:** Kotlin Coroutines & StateFlow
* **Background Task:** Android Foreground Services
* **Storage:** MediaStore API (Mendukung Scoped Storage Android 10 - 14+)
