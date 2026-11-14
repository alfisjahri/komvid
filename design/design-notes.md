# Komvid UI Redesign — Design Notes (Fase 1)

Dokumen ini menjelaskan spesifikasi desain lengkap hasil redesign UI aplikasi Android **Komvid** (`com.nade.komvid`, v1.1 / `versionCode = 2`), beserta analisis UI eksisting, token desain (*color*, *typography*, *spacing*, *radius*, *iconography*), perubahan per layar, dan rekomendasi teknis untuk Fase 2 (implementasi Jetpack Compose).

> **Catatan Ruang Lingkup (Fase 1)**
> Redesign ini berfokus murni pada peningkatan hierarki visual, kejelasan status proses, keterbacaan angka (*tabular clarity*), efisiensi ruang layar HP (*mobile-first utility*), dan penampakan state error yang sebelumnya sunyi di kode. **Tidak ada penambahan fitur baru** dan **tidak ada perubahan kode Komvid / build APK** pada tahap ini.

---

## 1. Daftar File Desain

Semua file desain berada di `/home/ubuntu/komvid-redesign/design/`:

| File | Deskripsi | Dimensi / Format |
| :--- | :--- | :--- |
| `komvid-redesign.figcraft` | File sumber native FigCraft (`Page: Komvid Redesign v2`, 324 node, 15 styles) | Native `.figcraft` (360×800 dp canvas) |
| `01-idle.png` | State **Idle** — belum ada video dipilih | `1080 × 2400` PNG (`@3x`) |
| `02-video-selected.png` | State **Video Dipilih** — metadata video, kontrol resolusi, mute switch, estimasi & CTA | `1080 × 2400` PNG (`@3x`) |
| `03-compressing.png` | State **Compressing** — ring progres real-time, ETA, profil target, assurance banner, tombol batal | `1080 × 2400` PNG (`@3x`) |
| `04-done.png` | State **Selesai** — badge penghematan, bar perbandingan ukuran sebelum/sesudah, durasi, folder output, aksi hasil | `1080 × 2400` PNG (`@3x`) |
| `05-error.png` | State **Error** — penanganan kegagalan encoding (`ExportException`) atau pembacaan metadata yang sebelumnya tidak tampil di UI | `1080 × 2400` PNG (`@3x`) |

---

## 2. Audit UI Komvid v1.1 (Sebelum Redesign)

Berdasarkan pembacaan `MainActivity.kt`, `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`, `CompressorState.kt`, `VideoAnalyzer.kt`, `VideoCompressorService.kt`, `VideoTranscoder.kt`, `res/values/themes.xml`, dan `app/build.gradle.kts`:

1. **Identitas warna tertimpa oleh Dynamic Color & pemakaian `surfaceVariant` merata**:
   - `Theme.kt` menetapkan `dynamicColor: Boolean = true`, sehingga di Android 12+ palet Emerald/Cyan/Slate sering tertimpa warna wallpaper (*Material You*).
   - `DarkSurface` (`#1E293B`) didefinisikan di `Color.kt`, tetapi seluruh `Card` di `MainActivity.kt` memaksa `containerColor = MaterialTheme.colorScheme.surfaceVariant` (`#334155`), membuat seluruh kartu terlihat abu-abu terlalu terang dan datar.
   - `CyanAccent` (`#06B6D4`) didefinisikan di `Color.kt`/`Theme.kt` namun tidak pernah dipakai di `MainActivity.kt`.
2. **Ketergantungan pada emoji mentah**:
   - Seluruh ikon UI memakai emoji Unicode (`➕`, `🎬`, `ℹ️`, `⚙️`, `🔇`, `📊`, `⚡`, `🎉`, `▶`, `📤`, `🔄`, `✖`), yang tampilannya berubah-ubah antar perangkat/vendor Android dan terasa kurang menyatu dengan tema gelap utilitas.
3. **Hierarki & kepadatan layar belum seimbang**:
   - **State Idle**: Kartu picker kecil di atas menyisakan ~70% ruang kosong di bagian bawah layar.
   - **State Video Dipilih**: Empat kartu bertumpuk (`Picker Card` + `Info Video Asli` + `Pengaturan Kompresi` + `Target Estimasi`) dengan daftar `RadioButton` vertikal memakan tinggi layar sehingga memaksa scroll di layar kompak.
   - **State Selesai**: Badge `"Tersimpan $savedRatio% Ruang HP | Durasi $timeTaken"` menggabungkan dua metrik berbeda dalam satu baris sempit tanpa visualisasi perbandingan proporsional dan tanpa info folder tujuan (`Movies/Komvid`).
   - **State Error hilang dari UI**: Jika `extractVideoMetadata()` mengembalikan `null` atau `VideoTranscoder` mengalami `ExportException`, `VideoCompressorService` hanya mengirim pesan lewat notifikasi dan mengembalikan UI secara diam-diam tanpa feedback di layar utama.
4. **Kejelasan status saat proses**:
   - State Compressing menampilkan ring progres dan ETA, namun belum menampilkan profil target aktif (mis. resolusi target & codec hardware yang dipilih) secara ringkas.

---

## 3. Sistem Desain: *"Obsidian Emerald Utility"*

Konsep desain mempertahankan tema gelap khas Komvid (Slate gelap + aksen Emerald & Cyan) dengan kedalaman permukaan (*surface layering*) yang tenang, kontras tinggi untuk metrik numerik, dan bobot visual minimal agar cocok untuk aplikasi utilitas ringan (~1.4 MB).

### 3.1 Palet Warna (Color Tokens)

| Token | Hex | Peran di UI |
| :--- | :--- | :--- |
| `BG_PAGE` (`DarkBackground`) | `#0F172A` | Latar kanvas utama layar (*window/scaffold background*) |
| `BG_SURFACE` (`DarkSurface`) | `#1E293B` | Permukaan kartu utama (*primary elevated card*) & tombol sekunder |
| `BG_SURFACE_ALT` | `#162033` | Kartu hero *Idle dropzone* & kotak tips sekunder agar lebih halus dari `#1E293B` |
| `BG_RECESSED` | `#0B1221` | Kotak metrik di dalam kartu, track segmented control, track ring/bar progres |
| `BG_EMERALD_TINT` | `#102826` | Latar ikon badge bertema sukses / siap kompres |
| `BG_CYAN_TINT` | `#112430` | Kartu *Target Estimasi* & banner jaminan *background service* |
| `EMERALD` (`EmeraldPrimary`) | `#10B981` | Aksen primer: CTA utama, segmen aktif, gradien awal progress arc |
| `EMERALD_LIGHT` (`EmeraldSecondary`) | `#34D399` | Teks aksen penghematan, sub-label status positif, angka estimasi |
| `CYAN` (`CyanAccent`) | `#06B6D4` | Aksen telemetri proses: badge `• AKTIF`, nilai `ESTIMASI SISA`, label `Smart Downscale`, codec H.265, jalur `Movies/Komvid` |
| `TEXT_PRIMARY` (`TextWhite`) | `#F8FAFC` | Judul layar, angka metrik utama, label kontrol primer |
| `TEXT_BODY_SOFT` | `#CBD5E1` | Teks isi paragraf teknis / banner / saran |
| `TEXT_SECONDARY` (`TextMuted`) | `#94A3B8` | Subjudul, deskripsi opsi, label metrik sekunder |
| `TEXT_TERTIARY` | `#64748B` | Micro-label UPPERCASE (`RESOLUSI`, `DURASI`, `ESTIMASI SISA`) & hint opsi non-aktif |
| `TEXT_ON_EMERALD` | `#052019` | Teks & ikon di atas tombol/pill Emerald (`#10B981`) untuk kontras gelap tajam |
| `ERROR_TEXT` | `#F87171` | Teks/ikon tombol `Batal Kompresi`, ikon peringatan, header `DETAIL ERROR` |
| `ERROR_FILL_SUBTLE` | `#EF4444` (`10%` alpha, `#EF444418`) | Latar lembut tombol `Batal Kompresi` dan lingkaran ikon error |
| `BORDER_SUBTLE` | `#283548` | Garis tepi kartu `#1E293B` (`1dp`) |
| `BORDER_MUTED` | `#334155` | Garis tepi tombol sekunder & track switch OFF |
| `BORDER_EMERALD` | `#10B981` (`33%` alpha, `#10B98155`) | Garis tepi aksen kartu estimasi/progres/hasil dan garis putus-putus *dropzone* |
| `BORDER_CYAN_SOFT` | `#06B6D4` (`27%` alpha, `#06B6D444`) | Garis tepi pill `• AKTIF` dan banner layanan latar |
| `BORDER_ERROR` | `#EF4444` (`33%` alpha, `#EF444455`) | Garis tepi tombol batal & kartu error |

### 3.2 Tipografi & Tabular Figures

- **Font Keluarga**: Mockup menggunakan `Inter` (dan `Liberation Mono` untuk log teknis error). Di implementasi Compose Android, `FontFamily.SansSerif` (Roboto sistem) atau variabel `Inter` bawaan menjaga ukuran APK tetap ~1.4 MB tanpa harus membundel file font besar.
- **Angka Tabular (`fontFeatureSettings = "tnum"`)**: Diterapkan pada semua angka resolusi (`3840 × 2160`), ukuran file (`248.4 MB`, `~59.2 MB`, `42.8 MB`), persentase (`64%`), countdown (`00:34`), dan jam agar angka tidak bergeser (*jitter*) ketika diperbarui tiap 800 ms.
- **Skala Tipografi**:
  - **Display Percentage**: `38sp` Bold, `lineHeight = 44sp` (`64%` di layar Compressing)
  - **Screen Hero Heading**: `20–21sp` Bold, `lineHeight = 25–26sp` (`Kompresi Selesai!`, `Kompresi Gagal Diproses`)
  - **Card Heading / App Brand**: `19sp` Bold (`Komvid`, `Pilih Video untuk Dikompres`)
  - **Primary Metric Value**: `17–22sp` Bold (`~59.2 MB`, `42.8 MB`, `00:34`)
  - **CTA Label**: `15sp` Bold (`Mulai Kompresi Hardware`, `Putar Video Hasil`, `Batal Kompresi`)
  - **Section Title**: `14sp` Bold (`Video Terpilih`, tombol sekunder)
  - **Body / Copy**: `13sp` Regular, `lineHeight = 19sp`
  - **Technical Mono Detail**: `11.5sp` Regular Monospace, `lineHeight = 16.5sp`
  - **Micro Uppercase Label**: `9.5–10.5sp` SemiBold, `letterSpacing = 0.6–0.8sp` (`RESOLUSI TARGET`, `TARGET ESTIMASI`)

### 3.3 Spacing, Layout Grid & Shape

- **Kanvas Acuan**: `360 × 800 dp` (di-export pada `@3x` menjadi `1080 × 2400 px`).
- **Margin Horizontal Layar**: `20dp` kiri dan kanan.
- **Gap Antar Seksi / Kartu**: `12–16dp` vertikal.
- **Padding Internal Kartu**: `14–22dp` (kartu konfigurasi ringkas `14dp`; kartu hero Idle/Compressing/Done/Error `20–26dp`).
- **Corner Radius Hierarchy**:
  - Kartu utama / Hero container: `18dp` (multi-card) dan `24dp` (single hero card)
  - Kotak metrik internal (`BG_RECESSED`), banner, tombol CTA utama & sekunder: `14dp` (grid metrik & sub-box `12dp`)
  - Segmented pill aktif: `9dp` di dalam container `12dp`
  - Logo container: `10dp` (`36×36dp`)
  - Badge status / pill penghematan / progress track: `999dp` (*full pill*)
- **Ikonografi Vektor**: Mengganti seluruh emoji dengan ikon vektor `24×24dp` bergaya *stroke* konsisten (`1.9–2.4dp`, *round cap/join*) serta ikon petir/play solid untuk tombol CTA. Dapat ditulis langsung sebagai Compose `ImageVector` / Android `VectorDrawable` XML (hanya beberapa KB) tanpa library `material-icons-extended`.

---

## 4. Perubahan Desain per Layar

### 4.1 `01-idle.png` — Layar Awal (Idle)
- **Masalah lama**: Kartu pilih video kecil di atas menyisakan ~70% ruang kosong tanpa panduan visual atau ringkasan kapabilitas utilitas.
- **Perubahan desain**:
  1. **App Header Terstruktur**: Ikon merek gradien Emerald→Cyan (`36×36dp`) bergaya panah kompresi `[→|←]`, judul `Komvid`, subjudul `Hardware Video Compressor`, dan pill versi `v1.1` (menyelaraskan ketidaksinkronan nama `nade komvid` vs `Komvid`).
  2. **Hero Dropzone Full-Height**: Kartu utama `#162033` dengan border putus-putus (`#10B98155`) mengisi area tengah layar agar langsung mengarahkan perhatian ke aksi tunggal: *pilih video*.
  3. **Visual Icon Halo + CTA Primer**: Ikon film-plus dengan *ambient emerald glow* tipis, heading tegas `"Pilih Video untuk Dikompres"`, teks 2 baris `"Mendukung format MP4, MKV, & MOV. 100% lokal via Hardware Encoder."`, dan tombol solid Emerald penuh `"Pilih Video dari Galeri"` (`52dp` tinggi).
  4. **3-Tile Hardware Capability Strip di Bawah**: Menampilkan 3 fakta nyata Komvid yang sudah ada (bukan fitur baru): `Hardware` (`H.265 / H.264`), `Latar Aman` (`Wakelock Aktif`), dan `Output` (`Movies/Komvid`).

### 4.2 `02-video-selected.png` — Video Dipilih & Pengaturan
- **Masalah lama**: 4 kartu berbobot visual sama memakai `surfaceVariant`, `RadioButton` vertikal memakan ruang tinggi, memicu scrolling, dan card picker tetap besar setelah video terpilih.
- **Perubahan desain**:
  1. **Source Video Card Ringkas + Tombol `"Ganti"`**: Setelah video dipilih, area picker menyusut menjadi header sumber video (`Video Terpilih · Siap untuk dikompres` + tombol `"Ganti"` di kanan atas) diikuti grid 3 kolom metrik recessed (`RESOLUSI`, `DURASI`, `UKURAN ASLI`) dengan angka tabular.
  2. **Segmented Control 4-Opsi untuk `RESOLUSI TARGET`**: Daftar `RadioButton` vertikal diganti kontrol segmen horizontal (`720p Hemat`, `1080p Standar`, `1440p Tinggi`, `4K Maks`) dalam satu baris dengan pill aktif Emerald solid + label penjelas 1 baris (`"FHD (1080p) — Standar jelas & hemat ruang"`) dan tag `Smart Downscale` di kanan header. *(Catatan: pada video di bawah 4K/1440p, segmen di atas resolusi sumber tetap disembunyikan/dinonaktifkan mengikuti logika smart downscaling Komvid saat ini.)*
  3. **Toggle Audio Rapi**: Baris `Bisukan Suara (Mute)` memakai container ikon tersendiri dan Switch M3 bertrack gelap yang jelas status OFF/ON-nya.
  4. **Target Estimasi dengan Warna Cyan-Tint & Proportional Bar**: Kartu estimasi dibedakan memakai `BG_CYAN_TINT` (`#112430`) + border Emerald, pill `"76% Lebih Kecil"`, angka besar `~59.2 MB dari 248.4 MB`, badge codec `H.265 (HEVC) · Hardware Encoder` di kanan, serta bar visual mini yang menunjukkan rasio ukuran estimasi terhadap ukuran asli.
  5. **CTA Utama di Zona Jempol (Bottom)**: Tombol solid Emerald `52dp` `"Mulai Kompresi Hardware"` terpisah rapi di bagian bawah.

### 4.3 `03-compressing.png` — Proses Kompresi Berjalan
- **Masalah lama**: Tidak ada indikator hidup di header, tidak terlihat target resolusi/codec yang sedang dijalankan, dan banner latar kurang dibedakan dari kartu.
- **Perubahan desain**:
  1. **Header Status Pill `• AKTIF`**: Menggantikan pill versi `v1.1` selama kompresi berlangsung dengan indikator dot Cyan agar user langsung tahu encoder sedang bekerja.
  2. **Progress Ring Gradien + Glow Halus**: Lingkaran `164dp` dengan track `#0B1221` dan arc gradien Emerald→Cyan searah jarum jam dari jam 12, menaungi angka tabular `64%` (`38sp` Bold) dan label mikro `MENGOMPRESI`.
  3. **2 Kartu Telemetri Real-Time**: Berdampingan di bawah ring: `ESTIMASI SISA` (`00:34`, beraksen Cyan) dan `PROFIL TARGET` (`1080p · H.265`).
  4. **Assurance Notice Terstruktur**: Banner berikon perisai Cyan menjelaskan layanan foreground & wakelock (`"Hardware Encoder aktif di latar belakang. Aman usap ke Home atau kunci layar."`).
  5. **Tombol Batal Destruktif Terkendali**: Tombol `Batal Kompresi` (`52dp`) di bagian bawah memakai tint merah lembut (`#EF444418`), border merah transparan (`#EF444455`), dan teks/ikon `#F87171` — mudah ditekan jika sengaja, namun tidak bersaing secara visual dengan ring progres.

### 4.4 `04-done.png` — Kompresi Selesai
- **Masalah lama**: Memakai emoji pesta (`🎉`), metrik perbandingan sebelum/sesudah dan durasi terhimpit dalam teks panjang satu baris, serta lokasi penyimpanan `Movies/Komvid` tidak ditampilkan di layar selesai.
- **Perubahan desain**:
  1. **Badge Sukses Vektor + Pill Penghematan**: Lingkaran ganda Emerald dengan centang tajam, judul `"Kompresi Selesai!"`, dan pill `"Hemat 83% Ruang (205.6 MB)"`.
  2. **Comparison Box dengan Dual Proportional Bar**: Menampilkan `UKURAN AWAL` (`248.4 MB`) `→` `HASIL AKHIR` (`42.8 MB`) disertai **dua bar proporsional bertumpuk** (bar atas penuh `#334155` mewakili ukuran awal; bar bawah gradien Emerald→Cyan sepanjang ~17% mewakili hasil kompresi) agar dampak penghematan langsung terbaca dalam 1 detik.
  3. **Grid Metadata Output**: Menampilkan `WAKTU PROSES` (`00m 38d`) dan `DISIMPAN KE` (`Movies/Komvid`) secara jelas.
  4. **Hierarki 3 Level Aksi**: Primer solid Emerald (`Putar Video Hasil`), Sekunder Surface (`Bagikan Video`), dan Ghost link (`Kompres Video Lainnya`) di bawah agar alur klik jelas dan tidak penuh garis kotak.

### 4.5 `05-error.png` — Penanganan Error (State Baru yang Melengkapi Alur)
- **Masalah lama**: Belum ada UI state error di `MainActivity.kt` / `CompressorState.kt`. Ketika `extractVideoMetadata()` gagal (`null`) atau `VideoTranscoder` menerima `ExportException`, UI kembali diam-diam tanpa informasi ke pengguna.
- **Perubahan desain**:
  1. **Error Hero Card Tenang & Informatif**: Menggunakan ikon segitiga peringatan merah lembut (`#EF444418` + `#EF444455`), judul `"Kompresi Gagal Diproses"`, dan ringkasan sebab yang ramah pengguna.
  2. **`DETAIL ERROR` Monospace Box**: Menampilkan kode/pesan teknis secara ringkas di kotak gelap (`ExportException (4001): ...` sebagai contoh kasus kegagalan codec Media3) agar memudahkan diagnosa tanpa membanjiri layar.
  3. **`SARAN PERBAIKAN` Kontekstual**: Memberikan langkah solusi yang sesuai kemampuan Komvid saat ini (mis. turunkan resolusi ke `720p` atau cek ruang penyimpanan internal).
  4. **Aksi Pemulihan Jelas**: Tombol primer Emerald `"Coba Lagi"` (mengulang atau kembali ke konfigurasi video yang sama) dan tombol sekunder `"Pilih Video Lain"`.

---

## 5. Catatan Penggunaan FigCraft (Tooling)

- **Cara eksekusi**: Menggunakan binary **`figcraft-cli`** yang dibangun langsung dari source upstream Rust `https://github.com/storytold/figcraft` (beserta aset font `https://github.com/storytold/craft-fonts`) di direktori scratch.
- **Workflow headless**: `figcraft-cli run komvid-commands.json --out design/komvid-redesign.figcraft` menghasilkan file native `.figcraft` terstruktur dengan **324 node**, **15 reusable paint/text styles**, **auto-layout**, dan **vector paths**, lalu di-render ke PNG resolusi `1080×2400 px` menggunakan `figcraft-cli export --scale 3` tanpa memerlukan interaksi GUI manual atau display Xvfb.
- **Pembaruan manual selanjutnya**: File `design/komvid-redesign.figcraft` dapat dibuka langsung di aplikasi desktop `figcraft` maupun dimodifikasi kembali lewat `figcraft-cli`.

---

## 6. Saran Implementasi Fase 2 (Jetpack Compose)

Rekomendasi teknis agar transisi ke kode Komvid berjalan mulus dan ukuran APK tetap ~1.4 MB:

1. **Perbarui `ui/theme/Theme.kt` & `Color.kt`**:
   - Ubah default `dynamicColor: Boolean = false` pada `KomvidTheme` (atau hapus cabang `dynamicDarkColorScheme`) agar tema *Obsidian Emerald* konsisten di Android 12+.
   - Tambahkan token warna kustom (via `CompositionLocalProvider` / objek `KomvidColors`) untuk `surfaceAlt` (`#162033`), `surfaceRecessed` (`#0B1221`), `cyanTint` (`#112430`), `emeraldTint` (`#102826`), dan `errorSoft` (`#EF444418`) agar semua `Card` memakai `#1E293B` (bukan lagi `surfaceVariant` `#334155`).
2. **Perbarui `ui/theme/Type.kt` & `res/values/themes.xml`**:
   - Lengkapi skala `Typography` (`headlineSmall`, `titleMedium`, `bodyMedium`, `labelSmall`) dan terapkan `fontFeatureSettings = "tnum"` pada komponen teks angka progres, ETA, ukuran file, dan resolusi.
   - Pertimbangkan menyesuaikan `windowSplashScreenBackground` di `themes.xml` (`Theme.App.Starting`) dari `#FFFFFF` ke `#0F172A` agar tidak terjadi kilatan putih saat aplikasi dibuka di mode gelap.
   - Samakan `strings.xml` (`app_name`) atau biarkan `"Komvid"` sesuai keinginan pemilik.
3. **Tambahkan `errorMessage: StateFlow<String?>` ke `CompressorState.kt`**:
   - Update `VideoCompressorService.kt` dan `VideoTranscoder.kt` untuk mengisi `CompressorState.errorMessage` saat `ExportException` atau kegagalan ekstraksi metadata terjadi (termasuk membedakan kasus cancel vs error di `VideoCompressorService`), serta reset `errorMessage` di `CompressorState.reset()`.
4. **Ganti Emoji dengan Custom `VectorDrawable` XML (Bukan `material-icons-extended`)**:
   - Menyertakan hanya ~15 file XML kecil di `res/drawable/ic_*.xml` menjaga APK tetap ~1.4 MB, berbeda jauh dengan `androidx.compose.material:material-icons-extended` yang bisa membengkakkan APK bila R8 tidak memangkas sempurna.
5. **Komponen Progress Ring & Proportional Bar Ringan**:
   - Ring progres `03-compressing` dapat dibuat murni dengan Compose `Canvas` (`drawArc` dengan `Brush.sweepGradient` / `linearGradient`), tanpa dependensi pihak ketiga.
   - Bar perbandingan `02-video-selected` dan `04-done` cukup memakai 2 `Box` bertumpuk dengan `fillMaxWidth(fraction)`.
6. **Penyelarasan Format Durasi (Opsional Halus)**:
   - Pertimbangkan menyelaraskan format `timeTaken` di `VideoCompressorService.kt` (`"%02d menit %02d detik"`) atau menampilkannya ringkas (`"00m 38d"` / `"2m 15s"`) agar konsisten dengan format durasi di `VideoAnalyzer.formattedDuration()`.

---

## 7. Pembaruan Fase 2: Penambahan Fitur Trimming (`02-video-selected.png`)

Pada Fase 2 ditambahkan fitur **Trimming Video** (pemotongan rentang waktu awal–akhir sebelum transcoding). Perubahan pada file desain `design/komvid-redesign.figcraft` dan `design/02-video-selected.png` (`360 × 800 dp` / `1080 × 2400 px`):

1. **Kontrol Trimming di `Settings Card` (`02-video-selected.png`)**:
   - Ditempatkan di dalam kartu pengaturan di antara `RESOLUSI TARGET` dan baris `Bisukan Suara (Mute)` agar ritme 3 kartu (`Video Terpilih` → `Pengaturan` → `Target Estimasi`) tetap ringkas dan muat dalam satu layar penuh `360 × 800 dp` tanpa scroll.
   - **Header Bagian Trim**: Ikon gunting vektor `24×24dp` (`Cyan` `#06B6D4`) + label mikro `POTONG DURASI (TRIM)` (`10.5sp` SemiBold, `letterSpacing = 0.8sp`) dan pill durasi hasil trim (`1m 30s`, `CYAN_FILL_SUBTLE` `#06B6D41A`, border `#06B6D444`, angka tabular `tnum`).
   - **Range Slider Dua Handle**: Track latar `BG_RECESSED` (`#0B1221`, tinggi `6dp`, full pill) dengan rentang aktif gradien `Emerald → Cyan` (`#10B981 → #06B6D4`) dan dua thumb bundar `18dp` (`#F8FAFC` dengan stroke `3dp` Emerald untuk start dan Cyan untuk end).
   - **Label Waktu `mm:ss` di Kedua Ujung**: Waktu mulai (`00:15`, `EMERALD_LIGHT`, `tnum`) di ujung kiri, petunjuk rentang penuh (`Rentang video penuh 02:15`, `TEXT_TERTIARY`, `tnum`) di tengah, dan waktu akhir (`01:45`, `CYAN`, `tnum`) di ujung kanan.
2. **Pembaruan Kartu `TARGET ESTIMASI` untuk Mencerminkan Durasi Trim**:
   - Angka estimasi ukuran dan persentase penghematan dihitung berdasarkan durasi hasil trim (`1m 30s` / `90s`, bukan durasi penuh `2m 15s` / `135s`): contoh pada `1080p H.265` menghasilkan `~40.5 MB dari 248.4 MB` (`83% Lebih Kecil`, bar proporsional ~16%).
   - Ditambahkan sub-label eksplisit `Durasi hasil: 1m 30s` (`11sp` SemiBold `CYAN`, `tnum`) tepat di bawah estimasi ukuran agar pengguna langsung melihat durasi output hasil pemotongan sebelum memulai kompresi.
3. **Catatan Implementasi Media3**:
   - Trimming bersifat opsional: nilai awal slider mencakup rentang penuh video (`00:00` s/d durasi penuh) dan hanya memasang `MediaItem.ClippingConfiguration` saat pengguna mengubah titik awal/akhir dari rentang penuh.

