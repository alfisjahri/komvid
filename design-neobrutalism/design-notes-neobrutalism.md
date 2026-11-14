# Komvid UI Redesign — Eksplorasi Gaya Neobrutalism (Softened)

Dokumen ini merangkum eksplorasi arah visual alternatif untuk aplikasi kompresor video Android **Komvid** (`com.nade.komvid`, `v0.1.0`) dengan gaya **Softened Neobrutalism**. Dokumen ini merupakan **fase desain eksploratif saja** (tanpa perubahan kode aplikasi dan tanpa build APK) agar pemilik dapat membandingkan langsung tampilan *light neobrutalist utility* dengan desain implementasi saat ini (**Obsidian Emerald Utility**).

> **Catatan Ruang Lingkup**
> Seluruh fitur tetap 100% setia pada **Komvid v0.1.0** (Hardware Encoder H.265/H.264, Smart Downscale 720p–4K, Potong Durasi / Trim range slider, Bisukan Suara / Mute, Estimasi Ukuran, Foreground Service + Wakelock, dan Error Recovery). Tidak ada fitur fiktif yang ditambahkan.

---

## 1. Daftar File Desain

Seluruh aset disimpan terpisah di `/home/ubuntu/komvid-redesign/design-neobrutalism/` tanpa menimpa folder `design/` lama:

| File | Deskripsi | Dimensi / Format |
| :--- | :--- | :--- |
| `komvid-neobrutalism.figcraft` | File sumber native FigCraft (`471` node, `15` paint/text styles, `6` frame `360×800 dp`) | Native `.figcraft` |
| `01-idle.png` | State **Idle** — hero dropzone dashed well, badge visual, CTA galeri, 3-tile kapabilitas | `1080 × 2400` PNG (`@3x`) |
| `02-video-selected.png` | State **Video Dipilih** — metadata sumber, resolusi target, **POTONG DURASI (TRIM)** range slider, mute switch, estimasi & CTA | `1080 × 2400` PNG (`@3x`) |
| `03-compressing.png` | State **Compressing** — gauge progres `64%`, estimasi sisa, profil target, assurance banner, tombol batal | `1080 × 2400` PNG (`@3x`) |
| `04-done.png` | State **Selesai** — badge sukses, pill penghematan, bar perbandingan ukuran sebelum/sesudah, waktu & folder output, 3-level aksi | `1080 × 2400` PNG (`@3x`) |
| `05-error.png` | State **Error** — layar pemulihan error dengan `DETAIL ERROR` monospace, `SARAN PERBAIKAN`, dan aksi pemulihan | `1080 × 2400` PNG (`@3x`) |
| `06-variant-cobalt-tangerine.png` | **Varian Tema Alternatif** (*Blueprint Cobalt & Safety Tangerine*) pada layar `02-video-selected` untuk perbandingan arah warna | `1080 × 2400` PNG (`@3x`) |
| `design-notes-neobrutalism.md` | Spesifikasi token desain, alasan pilihan visual, dan perbandingan dengan *Obsidian Emerald* | Markdown |

---

## 2. Konsep Visual: *Softened Neobrutalism Utility*

Berbeda dari neobrutalism bertema hiburan atau *candy pop* yang penuh warna acak, desain ini mengambil pendekatan **Industrial / Studio Hardware Utility** (terinspirasi instrumen audio/perangkat keras modern seperti Teenage Engineering dan Braun):

1. **Tegas namun Tetap Profesional**: Latar kertas hangat (*Warm Cream* `#F5F0E4`) dipadukan kartu putih bersih (`#FFFFFF`), tinta hitam arang (`#141414`), serta aksen warna jenuh yang memiliki **peran semantik jelas** (hijau mint untuk aksi primer & penghematan, kuning volt untuk sorotan aktif, biru langit untuk telemetri & durasi trim, merah koral khusus error/batal).
2. **Sudut *Softened Rounded* (`8–12 dp`)**: Menghindari sudut tajam `0 px` yang terlalu kaku di layar ponsel modern, namun tetap menghindari lengkungan pill besar (`24 dp`) agar karakter geometris tegas neobrutalism tetap terasa.
3. **Permukaan Flat + Border Tegas + Hard Shadow Tanpa Blur**: Menggantikan *ambient glow* dan gradasi lembut dengan warna solid flat, garis tepi hitam konsisten (`1.5–2.5 dp`), dan bayangan jatuh keras (*offset hard shadow*, `blur = 0`) yang memberi kesan tombol fisik yang dapat ditekan.

---

## 3. Sistem Token Desain

### 3.1 Palet Warna — Tema Utama: *"Drafting Cream & Signal Mint"* (`01`–`05`)

| Token | Hex | Peran & Alasan Pemilihan |
| :--- | :--- | :--- |
| `BG_PAGE` | `#F5F0E4` | Latar kanvas *Warm Cream*. Mengurangi silau putih murni dan menciptakan kontras bersih terhadap kartu `#FFFFFF`. |
| `BG_SURFACE` | `#FFFFFF` | Permukaan kartu utama, tile kapabilitas, dan tombol sekunder agar terangkat jelas dari kanvas krem. |
| `BG_RECESSED` | `#EDE6D6` | *Warm Sand* untuk sel metadata, track segmented control, track trim slider, track ring progres, dan box perbandingan hasil. |
| `BG_DROPZONE` | `#FAF7EE` | Sumur dropzone berbingkai garis putus-putus pada layar Idle untuk membedakan area target klik secara halus. |
| `INK` | `#141414` | Tinta utama untuk border, *hard shadow*, ikon vektor, judul, dan teks di atas warna aksen terang (kontras tajam). |
| `MINT` | `#2EE59D` | *Signal Mint* — CTA utama (`Mulai Kompresi Hardware`, `Putar Video Hasil`, `Coba Lagi`, `Pilih Video dari Galeri`), arc progres, badge sukses, dan active trim range. Menjaga DNA hijau Komvid tetap dikenali. |
| `YELLOW` | `#FFE14D` | *Volt Yellow* — logo mark, emblem Idle, segmen resolusi aktif (`1080p`), tombol `Ganti`, handle awal trim, pill `Hemat 83%`, dan tag `MENGOMPRESI`. |
| `CYAN` | `#56D4FF` | *Electric Sky* — pill status `• AKTIF`, badge durasi trim (`1m 30s`), handle akhir trim, dan ikon notice layanan latar. |
| `CORAL` | `#FF6B6B` | *Signal Coral* — badge peringatan pada layar Error. |
| `BG_MINT_TINT` | `#D8FCEB` | Latar kartu `TARGET ESTIMASI` agar blok ringkasan angka sebelum kompresi langsung menonjol. |
| `BG_CYAN_TINT` | `#DDF6FF` | Latar tile `ESTIMASI SISA`, `DISIMPAN KE`, dan tag `Smart Downscale`. |
| `BG_YELLOW_TINT` | `#FFF6C2` | Latar tile `PROFIL TARGET`, `WAKTU PROSES`, dan kotak `SARAN PERBAIKAN`. |
| `BG_CORAL_TINT` | `#FFE4E4` | Latar lembut tombol `Batal Kompresi` dan kotak `DETAIL ERROR` agar tidak terlalu agresif dibanding tombol utama. |
| `TEXT_SECONDARY` | `#44403C` | Teks deskripsi sekunder (*Warm Charcoal*). |
| `TEXT_TERTIARY` | `#57534E` | Micro-label UPPERCASE (`RESOLUSI`, `DURASI`, `ESTIMASI SISA`). |
| `TEXT_POSITIVE` | `#047857` | Teks status positif & angka hasil akhir di atas latar putih/krem (emerald gelap untuk keterbacaan WCAG AA). |
| `TEXT_TECH` | `#0369A1` | Teks label teknis (`H.265 (HEVC)`, `Durasi hasil`, `Movies/Komvid`) di atas permukaan terang. |
| `TEXT_DANGER` | `#B91C1C` | Label `DETAIL ERROR` dan teks tombol `Batal Kompresi`. |

### 3.2 Palet Varian Alternatif (`06-variant-cobalt-tangerine.png`): *"Blueprint Cobalt & Safety Tangerine"*

Sebagai opsi kedua bagi pemilik apabila ingin membandingkan nuansa hangat-hijau dengan estetika studio perangkat keras yang lebih dingin dan teknis:

| Token | Hex | Peran di Varian 2 |
| :--- | :--- | :--- |
| `BG_PAGE` | `#EEF2FA` | *Cool Blueprint Paper* — kanvas abu-biru sangat muda ala cetak biru teknis. |
| `INK` | `#111827` | Tinta biru-hitam pekat untuk border dan *hard shadow*. |
| `CTA_PRIMARY` | `#FF6B2C` | *Safety Tangerine* — tombol CTA utama yang mencolok seperti tombol *record/export* perangkat keras. |
| `ACCENT_COBALT` | `#2B59FF` | *Electric Cobalt* — brand mark, segmen resolusi aktif (`1080p Standar`, teks putih), durasi trim, handle akhir, dan bar estimasi. |
| `ACCENT_AMBER` | `#FFD633` | *Signal Amber* — thumbnail video, tombol `Ganti`, handle awal trim, dan badge `83% Lebih Kecil`. |
| `BG_EST_TINT` | `#E6EEFF` | Latar kartu `TARGET ESTIMASI` bernuansa biru es lembut. |

### 3.3 Tipografi & Keterbacaan Angka (`tnum`)

Mockup dirender dengan font bawaan FigCraft (`--no-system-fonts`) agar konsisten dan dapat direproduksi:

- **Display & Headings (`Montserrat`, Variable `wght = 800` / `700`)**:
  - Dipakai pada merek `Komvid` (`20 sp`), judul hero (`19–21 sp`), persentase progres (`36 sp`), angka metrik hero (`~40.5 MB`, `42.8 MB`, `00:34`), label CTA (`15 sp`), dan judul seksi (`10.5–14 sp`).
  - Karakter geometris tebal memberi bobot visual khas neobrutalism tanpa terlihat kekanak-kanakan seperti font komik/display membulat berlebihan.
- **UI Body & Angka Tabular (`Inter`, `Bold` / `SemiBold`, `tnum = true`)**:
  - Dipakai pada teks instruksi (`11–13 sp`), label mikro, serta semua angka dinamis (`3840 × 2160`, `2m 15s`, `248.4 MB`, `00:15`, `01:45`, `00m 38d`) dengan fitur *tabular figures* agar angka tetap sejajar vertikal dan tidak bergeser saat progres berubah.
- **Technical Monospace (`Liberation Mono`, `Bold`, `11.5 sp`)**:
  - Dipakai khusus di blok `DETAIL ERROR` (`ExportException (4001): ...`) untuk memisahkan pesan diagnostik dari teks bantuan pengguna.

### 3.4 Corner Radius, Border & Hard Shadow

| Elemen UI | Corner Radius | Border (`strokeAlign: center`) | Hard Shadow (`blur = 0`) |
| :--- | :--- | :--- | :--- |
| Kartu Hero Single-State (`01`, `03`, `04`, `05`) | **`12 dp`** | `2.5 dp` `#141414` | Offset `(5, 5)` solid `#141414` |
| Kartu Multi-Card (`02`, `06`) | **`12 dp`** | `2.5 dp` `#141414` | Offset `(4, 4)` solid `#141414` |
| Tombol CTA Primer, Sekunder & Batal | **`10 dp`** | `2.5 dp` `#141414` | Offset `(4, 4)` solid `#141414` |
| Sub-kartu / Banner / Kotak Info / Tile | **`10 dp`** | `2.0 dp` `#141414` | Offset `(2.5–3, 2.5–3)` solid `#141414` |
| Segmented Control Container | **`10 dp`** | `2.0 dp` `#141414` | Tanpa shadow luar (permukaan recessed) |
| Segmen Aktif, Pill, Badge, Thumb Trim, Metadata Cell, Switch Track | **`8 dp`** | `1.5–2.0 dp` `#141414` | Offset `(2, 2)` pada elemen interaktif / badge |
| Thumb Switch (inner nested element) | **`6 dp`** | `2.0 dp` `#141414` | — (konsentris di dalam track `8 dp`) |

> **Alasan teknis `strokeAlign: center` + `showBehind: true` pada FigCraft**:
> Pada kombinasi border hitam solid + *hard shadow* hitam solid di atas latar putih/berwarna, `strokeAlign: inside` dapat menyisakan garis tipis anti-aliasing di tepi luar kartu. Menggunakan `center` + `showBehind: true` menghasilkan pertemuan border dan bayangan yang sepenuhnya rapat dan tajam pada ekspor `@3x`.

---

## 4. Ringkasan Pilihan Desain per Layar

1. **`01-idle.png`**:
   - Kartu hero putih berbayangan keras membungkus *dashed dropzone well* krem hangat (`#FAF7EE`), ikon film-plus di kotak kuning `72×72 dp` (`12 dp` radius), dan CTA utama `Signal Mint` (`52 dp`).
   - 3 tile kapabilitas di bagian bawah (`Hardware`, `Latar Aman`, `Output`) diberi kotak ikon berkode warna (`Mint`, `Sky`, `Yellow`) agar seimbang dengan bobot kartu hero di atasnya.
2. **`02-video-selected.png`**:
   - Seluruh alur konfigurasi (`Video Terpilih` + `Pengaturan` + `Target Estimasi` + CTA) muat dalam satu layar `360×800 dp` dengan jarak bersih di antara tiap kartu sehingga bayangan `4 dp` tidak pernah saling bertumpukan.
   - **Segmented Control Resolusi**: Pilihan aktif `1080p Standar` memakai blok *Volt Yellow* berborder & berbayangan mini sehingga menyerupai tombol mekanik yang tertekan/terpilih.
   - **Kontrol `POTONG DURASI (TRIM)`**: Track recessed `10 dp` berborder hitam, rentang aktif solid *Signal Mint*, dan dua handle taktil berbentuk persegi membulat (`20×22 dp`, radius `8 dp`) dengan warna berbeda (**Kuning** untuk titik awal `00:15`, **Biru Langit** untuk titik akhir `01:45`) beserta garis grip vertikal di tengah thumb serta badge durasi `1m 30s`.
   - **Switch Mute**: Didesain ulang menjadi *chunky rounded-rectangle switch* (`46×26 dp`, radius `8 dp`) agar serasi dengan bahasa visual kotak-bulat.
   - **Target Estimasi**: Menggunakan latar *pale mint* (`#D8FCEB`) dan progress gauge dengan garis pemisah hitam vertikal di ujung porsi `~40.5 MB` (`16%`).
3. **`03-compressing.png`**:
   - Pill header berubah menjadi `• AKTIF` (*Electric Sky* berbayangan keras).
   - **Gauge Progres Neobrutalist**: Menggantikan ring gradien & *ambient glow* dengan cincin solid berborder luar-dalam hitam `2.5 dp` + bayangan offset `(5, 5)`, angka `64%` tebal di tengah, dan pill tag kuning `MENGOMPRESI`.
   - Dua tile metrik (`ESTIMASI SISA` biru muda & `PROFIL TARGET` kuning muda) serta banner keamanan *background service* tetap terbaca cepat. Tombol `Batal Kompresi` memakai tint koral lembut `#FFE4E4` dengan tinta `#B91C1C` agar tetap tenang saat kompresi berlangsung.
4. **`04-done.png`**:
   - Emblem centang kotak `56×56 dp` (*Signal Mint*), pill kuning `Hemat 83% Ruang (205.6 MB)`, kotak perbandingan `UKURAN AWAL → HASIL AKHIR` dengan dua gauge proporsional berborder tegas, tile `WAKTU PROSES` (`00m 38d`) & `DISIMPAN KE` (`Movies/Komvid`), dan hierarki 3 aksi (`Putar Video Hasil` → `Bagikan Video` → tautan bergaris bawah `Kompres Video Lainnya`).
5. **`05-error.png`**:
   - Emblem peringatan *Signal Coral* `#FF6B6B`, kotak `DETAIL ERROR` (`Liberation Mono` di atas tint koral `#FFE4E4`), kotak `SARAN PERBAIKAN` di atas tint kuning `#FFF6C2`, serta dua tombol pemulihan jelas (`Coba Lagi` dan `Pilih Video Lain`).
6. **`06-variant-cobalt-tangerine.png`**:
   - Memperlihatkan layar `02-video-selected` dengan palet *Blueprint Cobalt & Safety Tangerine* untuk memudahkan pemilik memilih antara nuansa **Krem + Hijau Mint** (dekat dengan identitas Komvid lama) atau nuansa **Biru Kertas + Oranye Industri** (lebih teknikal & netral).

---

## 5. Perbandingan Singkat: *Obsidian Emerald Utility* vs *Softened Neobrutalism*

| Aspek | *Obsidian Emerald Utility* (v0.1.0 Saat Ini) | *Softened Neobrutalism* (Eksplorasi Baru) |
| :--- | :--- | :--- |
| **Tema Dasar** | Dark mode (`#0F172A` Slate gelap + `#1E293B`) | Light mode hangat (`#F5F0E4` Cream + `#FFFFFF` putih) / opsi Cool Ivory (`#EEF2FA`) |
| **Aksen & Kedalaman** | Gradien Emerald→Cyan, *ambient glow* radial, border tipis `1 dp` transparan | Warna solid flat kontras tinggi, border tegas `1.5–2.5 dp` (`#141414`), *hard offset shadow* tanpa blur |
| **Sudut (Corner Radius)** | Besar & lembut (`14–24 dp`, pill penuh `999 dp`) | *Softened brutalist* (`8–12 dp` di seluruh kartu, tombol, badge, slider thumb, dan switch) |
| **Tipografi** | `Inter` Bold/Regular (santai & teknis) | `Montserrat` ExtraBold (`wght 800`) untuk judul/CTA/metrik + `Inter` (`tnum`) + `Liberation Mono` |
| **Komponen Khusus** | Progress ring gradien halus; trim thumb lingkaran; switch Material 3 | Progress gauge cincin berborder ganda + bayangan; trim thumb persegi-bulat bertanda grip; switch kotak-bulat |
| **Yang Dipertahankan** | Struktur 5 state, seluruh fitur v0.1.0 (termasuk Trimming), hierarki informasi & alur satu layar tanpa scroll, ikon vektor, `tnum` | Seluruhnya dipertahankan utuh |

---

## 6. Catatan Teknis Jika Dipilih untuk Implementasi Compose

Apabila pemilik memilih gaya Neobrutalism ini pada fase berikutnya:
1. **Hard Offset Shadow di Jetpack Compose**: Hindari `Modifier.shadow(...)` bawaan Material (karena menghasilkan *blur shadow* abu-abu berbasis `RenderNode` elevasi). Gunakan `Modifier.drawBehind { drawRoundRect(color = ink, topLeft = Offset(offsetPx, offsetPx), size = size, cornerRadius = CornerRadius(radiusPx)) }` sebelum menggambar latar & `BorderStroke` kartu agar bayangan tajam `blur = 0` bekerja identik mulai Android 7 (`minSdk 24`) ke atas.
2. **Font Display**: Untuk menjaga ukuran APK tetap ~1.4–1.6 MB, dapat membundel **satu** subset/bobot static `Montserrat-ExtraBold.ttf` (~35–50 KB) khusus untuk display/CTA sambil mempertahankan `FontFamily.SansSerif` / Roboto untuk body teks.
3. **Tema Splash & Status Bar**: Karena latar utama menjadi terang (`#F5F0E4`), `enableEdgeToEdge` perlu memakai `SystemBarStyle.light(...)` dengan ikon status bar gelap, dan `windowSplashScreenBackground` disesuaikan ke `#F5F0E4`.
