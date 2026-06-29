package com.nade.komvid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nade.komvid.ui.theme.KomvidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KomvidTheme {
                // PERBAIKAN 1: Surface dibiarkan Full Size tanpa padding,
                // supaya warna gelapnya nembus sampai ke ujung layar (belakang jam/baterai).
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoCompressorScreen()
                }
            }
        }
    }
}

@Composable
fun VideoCompressorScreen() {
    val context = LocalContext.current
    val analyzer = remember { VideoAnalyzer(context) }

    val isCompressing by CompressorState.isCompressing.collectAsState()
    val isFinished by CompressorState.isFinished.collectAsState()

    val currentProgress by CompressorState.progress.collectAsState()
    val currentEta by CompressorState.eta.collectAsState()
    val timeTaken by CompressorState.timeTaken.collectAsState()
    val origSize by CompressorState.originalSizeMb.collectAsState()
    val finSize by CompressorState.finalSizeMb.collectAsState()
    val savedUri by CompressorState.savedVideoUri.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoMetadata by remember { mutableStateOf<VideoMetadata?>(null) }
    var estimateResult by remember { mutableStateOf<CompressionEstimate?>(null) }
    var selectedResolution by remember { mutableStateOf(720) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedVideoUri = uri }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Izin notifikasi dibutuhkan!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(selectedVideoUri, selectedResolution) {
        selectedVideoUri?.let { uri ->
            val meta = analyzer.extractVideoMetadata(uri)
            videoMetadata = meta
            meta?.let {
                val shortSide = minOf(it.width, it.height)
                if (selectedResolution > shortSide) selectedResolution = shortSide
                estimateResult = analyzer.calculateEstimation(it, selectedResolution)
            }
        }
    }

    // PERBAIKAN 2: systemBarsPadding() dipindah ke Column.
    // Jadi yang menghindari Status Bar cuma teks dan tombolnya aja, background tetap full!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "nade komvid", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (isFinished) {
            val savedRatio = if (origSize > 0) ((origSize - finSize) / origSize * 100).toInt() else 0

            Text("Selesai! 🎉", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Statistik Kompresi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Waktu Pengerjaan: $timeTaken")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ukuran Awal: ${String.format("%.1f", origSize)} MB")
                    Text("Ukuran Akhir: ${String.format("%.1f", finSize)} MB")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tersimpan: $savedRatio%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    savedUri?.let { uri ->
                        val playIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "video/mp4")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(playIntent, "Putar Video"))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Putar Video")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    savedUri?.let { uri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Video"))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Bagikan")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { CompressorState.reset() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Kompres Video Lainnya")
            }

        } else if (isCompressing) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = currentProgress / 100f, modifier = Modifier.size(140.dp), strokeWidth = 10.dp)
                Text(text = "$currentProgress%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = currentEta, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Silakan usap layar / tekan tombol Home HP Anda.\nProses 100% aman di latar belakang!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Aman ditinggal (Usap ke Home)")
            }

        } else {
            OutlinedButton(
                onClick = { videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (selectedVideoUri != null) "Ganti Video ✓" else "+ Pilih Video dari Galeri")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedVideoUri != null && videoMetadata != null) {
                val meta = videoMetadata!!
                val shortSide = minOf(meta.width, meta.height)
                val availableResolutions = listOf(
                    Pair(2160, "4K (2160p)"),
                    Pair(1440, "2K (1440p)"),
                    Pair(1080, "FHD (1080p)"),
                    Pair(720, "HD (720p)")
                ).filter { it.first <= shortSide }

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pilih Kualitas Resolusi", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        availableResolutions.forEach { (resValue, resLabel) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (selectedResolution == resValue), onClick = { selectedResolution = resValue })
                                Text(resLabel)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                estimateResult?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📊 Target Kompresi", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Target Ukuran: ~${String.format("%.1f", it.estimatedSizeBytes.toDouble() / (1024 * 1024))} MB")
                            Text("Penghematan Ruang: ${it.compressionPercentage}% Lebih Kecil", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@Button
                            }
                        }
                        val intent = Intent(context, VideoCompressorService::class.java).apply {
                            data = selectedVideoUri
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            putExtra("VIDEO_URI", selectedVideoUri.toString())
                            putExtra("TARGET_RES", selectedResolution)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Eksekusi Kompresi")
                }
            }
        }
    }
}