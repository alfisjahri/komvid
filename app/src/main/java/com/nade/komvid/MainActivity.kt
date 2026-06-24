package com.nade.komvid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nade.komvid.ui.theme.KomvidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KomvidTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
    val currentProgress by CompressorState.progress.collectAsState()
    val currentEta by CompressorState.eta.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoMetadata by remember { mutableStateOf<VideoMetadata?>(null) }
    var estimateResult by remember { mutableStateOf<CompressionEstimate?>(null) }

    // State untuk menyimpan resolusi yang dipilih user (Default: 720p)
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

    // Update estimasi secara real-time saat user ganti opsi resolusi
    LaunchedEffect(selectedVideoUri, selectedResolution) {
        selectedVideoUri?.let { uri ->
            val meta = analyzer.extractVideoMetadata(uri)
            videoMetadata = meta
            meta?.let {
                // Pastikan target resolusi default tidak lebih besar dari video asli pertama kali di-load
                val shortSide = minOf(it.width, it.height)
                if (selectedResolution > shortSide) selectedResolution = shortSide

                estimateResult = analyzer.calculateEstimation(it, selectedResolution)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Komvid (Hardware Only)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (isCompressing) {
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(progress = currentProgress / 100f, modifier = Modifier.size(120.dp), strokeWidth = 8.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "$currentProgress%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(text = currentEta, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedButton(
                onClick = { (context as? Activity)?.moveTaskToBack(true) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Sembunyikan ke Latar Belakang")
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

                // Buat daftar resolusi yang aman (TIDAK BOLEH UPSCALE)
                val availableResolutions = listOf(
                    Pair(2160, "4K (2160p)"),
                    Pair(1440, "2K (1440p)"),
                    Pair(1080, "FHD (1080p)"),
                    Pair(720, "HD (720p)")
                ).filter { it.first <= shortSide }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pilih Kualitas / Resolusi", style = MaterialTheme.typography.titleMedium)
                        Text("Makin kecil, ukuran file makin hemat", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        availableResolutions.forEach { (resValue, resLabel) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = (selectedResolution == resValue),
                                    onClick = { selectedResolution = resValue }
                                )
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
                            Text("Mode: ${it.targetCodec}")
                            Text("Target Ukuran: ~${String.format("%.1f", it.estimatedSizeBytes.toDouble() / (1024 * 1024))} MB")
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
                            putExtra("TARGET_RES", selectedResolution) // Lempar resolusi pilihan
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