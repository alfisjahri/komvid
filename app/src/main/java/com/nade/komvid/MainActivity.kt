package com.nade.komvid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nade.komvid.ui.components.AppHeader
import com.nade.komvid.ui.screens.CompressingScreen
import com.nade.komvid.ui.screens.DoneScreen
import com.nade.komvid.ui.screens.ErrorScreen
import com.nade.komvid.ui.screens.IdleScreen
import com.nade.komvid.ui.screens.VideoSelectedScreen
import com.nade.komvid.ui.theme.KomvidTheme
import com.nade.komvid.ui.theme.KomvidThemeTokens

private val PRESET_SHORT_SIDES = setOf(720, 1080, 1440, 2160)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            KomvidTheme(darkTheme = false, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KomvidThemeTokens.colors.bgPage
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
    val codecSupport = remember { analyzer.detectHardwareCodecSupport() }
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.2.0"
        }.getOrDefault("0.2.0")
    }

    val isCompressing by CompressorState.isCompressing.collectAsState()
    val isFinished by CompressorState.isFinished.collectAsState()
    val errorMessage by CompressorState.errorMessage.collectAsState()

    val currentProgress by CompressorState.progress.collectAsState()
    val currentEta by CompressorState.eta.collectAsState()
    val targetProfile by CompressorState.targetProfile.collectAsState()
    val timeTaken by CompressorState.timeTaken.collectAsState()
    val origSize by CompressorState.originalSizeMb.collectAsState()
    val finSize by CompressorState.finalSizeMb.collectAsState()
    val savedUri by CompressorState.savedVideoUri.collectAsState()

    var selectedVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var initializedTrimUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var videoMetadata by remember { mutableStateOf<VideoMetadata?>(null) }
    var estimateResult by remember { mutableStateOf<CompressionEstimate?>(null) }
    var selectedResolution by rememberSaveable { mutableIntStateOf(1080) }
    var selectedCodec by rememberSaveable { mutableStateOf(codecSupport.defaultCodecMimeType()) }
    var muteAudio by rememberSaveable { mutableStateOf(false) }
    var trimStartMs by rememberSaveable { mutableLongStateOf(0L) }
    var trimEndMs by rememberSaveable { mutableLongStateOf(0L) }
    var metadataRetryNonce by remember { mutableIntStateOf(0) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                CompressorState.reset()
                initializedTrimUri = null
                videoMetadata = null
                estimateResult = null
                selectedVideoUri = uri
            }
        }
    )

    val launchVideoPicker = {
        videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    val startCompressionService = {
        if (!codecSupport.isSupported(selectedCodec)) {
            Toast.makeText(
                context,
                "SoC tidak mendukung hardware encoder video.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            selectedVideoUri?.let { uri ->
                val meta = videoMetadata
                val isTrimmed = meta != null && (trimStartMs > 0L || trimEndMs < meta.durationMs)
                val intent = Intent(context, VideoCompressorService::class.java).apply {
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(VideoCompressorService.EXTRA_VIDEO_URI, uri.toString())
                    putExtra(VideoCompressorService.EXTRA_TARGET_RES, selectedResolution)
                    putExtra(VideoCompressorService.EXTRA_VIDEO_CODEC, selectedCodec)
                    putExtra(VideoCompressorService.EXTRA_MUTE_AUDIO, muteAudio)
                    if (isTrimmed) {
                        putExtra(VideoCompressorService.EXTRA_TRIM_START_MS, trimStartMs)
                        putExtra(VideoCompressorService.EXTRA_TRIM_END_MS, trimEndMs)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
        Unit
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startCompressionService()
            } else {
                Toast.makeText(context, "Izin notifikasi dibutuhkan!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(selectedVideoUri, metadataRetryNonce) {
        val uri = selectedVideoUri ?: return@LaunchedEffect
        val result = analyzer.extractVideoMetadataWithError(uri)
        val meta = result.metadata
        if (meta != null) {
            videoMetadata = meta
            if (initializedTrimUri != uri || trimEndMs <= trimStartMs || trimEndMs > meta.durationMs) {
                trimStartMs = 0L
                trimEndMs = meta.durationMs
                initializedTrimUri = uri
            }
            val shortSide = minOf(meta.width, meta.height)
            selectedResolution = when {
                selectedResolution in PRESET_SHORT_SIDES && selectedResolution <= shortSide -> selectedResolution
                shortSide >= 1080 -> 1080
                shortSide >= 720 -> 720
                else -> shortSide
            }
        } else {
            videoMetadata = null
            estimateResult = null
            CompressorState.reportError(
                result.errorMessage ?: "MetadataException: Gagal membaca metadata video."
            )
        }
    }

    LaunchedEffect(videoMetadata, selectedResolution, selectedCodec, muteAudio, trimStartMs, trimEndMs) {
        val meta = videoMetadata ?: return@LaunchedEffect
        val trimmedDurationMs = (trimEndMs - trimStartMs).coerceIn(1L, meta.durationMs)
        estimateResult = analyzer.calculateEstimation(
            metadata = meta,
            targetShortSide = selectedResolution,
            codecMimeType = selectedCodec,
            muteAudio = muteAudio,
            trimmedDurationMs = trimmedDurationMs
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Vertical padding = 24dp, header ~58dp, spacer = 10dp -> remaining body height
        val minBodyHeight = (maxHeight - 92.dp).coerceAtLeast(420.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AppHeader(
                isCompressing = isCompressing,
                versionName = versionName
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isFinished -> {
                    DoneScreen(
                        minContentHeight = minBodyHeight,
                        origSizeMb = origSize,
                        finalSizeMb = finSize,
                        timeTaken = timeTaken,
                        onPlayVideo = {
                            savedUri?.let { uri ->
                                val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "video/mp4")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(playIntent, "Putar Video"))
                                }.onFailure {
                                    Toast.makeText(context, "Tidak ada pemutar video ditemukan.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onShareVideo = {
                            savedUri?.let { uri ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Video"))
                                }.onFailure {
                                    Toast.makeText(context, "Gagal membuka menu bagikan.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onCompressAnother = {
                            CompressorState.reset()
                            selectedVideoUri = null
                            initializedTrimUri = null
                            videoMetadata = null
                            estimateResult = null
                            trimStartMs = 0L
                            trimEndMs = 0L
                        }
                    )
                }

                isCompressing -> {
                    CompressingScreen(
                        minContentHeight = minBodyHeight,
                        progress = currentProgress,
                        eta = currentEta,
                        targetProfile = targetProfile,
                        onCancel = {
                            val cancelIntent = Intent(context, VideoCompressorService::class.java).apply {
                                action = VideoCompressorService.ACTION_CANCEL
                            }
                            context.startService(cancelIntent)
                        }
                    )
                }

                errorMessage != null -> {
                    ErrorScreen(
                        minContentHeight = minBodyHeight,
                        errorMessage = errorMessage ?: "Terjadi kesalahan tidak diketahui.",
                        onRetry = {
                            CompressorState.clearError()
                            if (videoMetadata == null && selectedVideoUri != null) {
                                metadataRetryNonce += 1
                            }
                        },
                        onPickAnother = {
                            CompressorState.reset()
                            selectedVideoUri = null
                            initializedTrimUri = null
                            videoMetadata = null
                            estimateResult = null
                            launchVideoPicker()
                        }
                    )
                }

                selectedVideoUri != null && videoMetadata != null -> {
                    VideoSelectedScreen(
                        minContentHeight = minBodyHeight,
                        metadata = videoMetadata!!,
                        estimate = estimateResult,
                        selectedResolution = selectedResolution,
                        selectedCodec = selectedCodec,
                        codecSupport = codecSupport,
                        muteAudio = muteAudio,
                        trimStartMs = trimStartMs,
                        trimEndMs = trimEndMs,
                        onChangeVideo = launchVideoPicker,
                        onSelectResolution = { res -> selectedResolution = res },
                        onSelectCodec = { codec ->
                            if (codecSupport.isSupported(codec)) {
                                selectedCodec = codec
                            }
                        },
                        onTrimChange = { newStart, newEnd ->
                            trimStartMs = newStart
                            trimEndMs = newEnd
                        },
                        onToggleMute = { muteAudio = it },
                        onStartCompression = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@VideoSelectedScreen
                                }
                            }
                            startCompressionService()
                        }
                    )
                }

                else -> {
                    IdleScreen(
                        minContentHeight = minBodyHeight,
                        onPickVideo = launchVideoPicker
                    )
                }
            }
        }
    }
}
