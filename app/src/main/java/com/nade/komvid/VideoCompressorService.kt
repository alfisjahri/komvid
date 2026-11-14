package com.nade.komvid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class VideoCompressorService : Service() {

    companion object {
        const val ACTION_CANCEL = "com.nade.komvid.ACTION_CANCEL"
        const val CHANNEL_ID = "KomvidChannel"
        const val NOTIF_ID = 1
        const val EXTRA_VIDEO_URI = "VIDEO_URI"
        const val EXTRA_TARGET_RES = "TARGET_RES"
        const val EXTRA_VIDEO_CODEC = "VIDEO_CODEC"
        const val EXTRA_MUTE_AUDIO = "MUTE_AUDIO"
        const val EXTRA_TRIM_START_MS = "TRIM_START_MS"
        const val EXTRA_TRIM_END_MS = "TRIM_END_MS"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var transcodeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            cancelCompression()
            return START_NOT_STICKY
        }

        val videoUriStr = intent?.getStringExtra(EXTRA_VIDEO_URI) ?: return START_NOT_STICKY
        val targetShortSide = intent.getIntExtra(EXTRA_TARGET_RES, 720)
        val requestedCodec = intent.getStringExtra(EXTRA_VIDEO_CODEC)
        val muteAudio = intent.getBooleanExtra(EXTRA_MUTE_AUDIO, false)
        val requestedTrimStartMs = intent.getLongExtra(EXTRA_TRIM_START_MS, -1L)
        val requestedTrimEndMs = intent.getLongExtra(EXTRA_TRIM_END_MS, -1L)

        val notification = buildNotification(0, "Mempersiapkan mesin kompresi...")
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING)
        } else {
            startForeground(NOTIF_ID, notification)
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Komvid::CompressWakelock")
        wakeLock.acquire(30 * 60 * 1000L)

        transcodeJob?.cancel()
        transcodeJob = serviceScope.launch {
            try {
                CompressorState.reset()
                CompressorState.isCompressing.value = true
                CompressorState.eta.value = "--:--"

                val uri = Uri.parse(videoUriStr)
                val analyzer = VideoAnalyzer(this@VideoCompressorService)
                val transcoder = VideoTranscoder(this@VideoCompressorService)

                val metadataResult = analyzer.extractVideoMetadataWithError(uri)
                val metadata = metadataResult.metadata
                if (metadata == null) {
                    val errMsg = metadataResult.errorMessage
                        ?: "MetadataException: Gagal membaca metadata video sumber."
                    CompressorState.reportError(errMsg)
                    updateNotification(0, "Gagal membaca berkas video.")
                    return@launch
                }

                val hasCustomTrim = requestedTrimStartMs >= 0L &&
                    requestedTrimEndMs > requestedTrimStartMs &&
                    (requestedTrimStartMs > 0L || requestedTrimEndMs < metadata.durationMs)
                val effectiveTrimStartMs = if (hasCustomTrim) requestedTrimStartMs.coerceAtLeast(0L) else -1L
                val effectiveTrimEndMs = if (hasCustomTrim) requestedTrimEndMs.coerceAtMost(metadata.durationMs) else -1L
                val effectiveDurationMs = if (hasCustomTrim) effectiveTrimEndMs - effectiveTrimStartMs else metadata.durationMs

                val codecSupport = analyzer.detectHardwareCodecSupport()
                val mimeType = if (requestedCodec != null && codecSupport.isSupported(requestedCodec)) {
                    requestedCodec
                } else {
                    codecSupport.defaultCodecMimeType()
                }
                val estimate = analyzer.calculateEstimation(
                    metadata = metadata,
                    targetShortSide = targetShortSide,
                    codecMimeType = mimeType,
                    muteAudio = muteAudio,
                    trimmedDurationMs = effectiveDurationMs
                )
                val targetBitrateBps = (estimate.targetBitrateMbps * 1024.0 * 1024.0).toInt()

                CompressorState.targetProfile.value = "${formatResolutionLabel(targetShortSide)} · ${estimate.codecTag}"

                val isPortrait = metadata.height > metadata.width
                var targetHeightAbs = if (isPortrait) {
                    (metadata.height * (targetShortSide.toFloat() / metadata.width)).toInt()
                } else {
                    targetShortSide
                }
                if (targetHeightAbs % 2 != 0) targetHeightAbs += 1

                val startTimeMs = System.currentTimeMillis()

                when (
                    val result = transcoder.startTranscode(
                        videoUri = uri,
                        mimeType = mimeType,
                        targetBitrateBps = targetBitrateBps,
                        targetHeightAbs = targetHeightAbs,
                        muteAudio = muteAudio,
                        trimStartMs = effectiveTrimStartMs,
                        trimEndMs = effectiveTrimEndMs
                    ) { progress, etaString ->
                        val notifEta = if (etaString == "--:--") "Menghitung..." else "Sisa $etaString"
                        updateNotification(progress, "Kompresi $progress% | $notifEta")
                        CompressorState.progress.value = progress
                        CompressorState.eta.value = etaString
                    }
                ) {
                    is TranscodeResult.Success -> {
                        val timeTakenMs = System.currentTimeMillis() - startTimeMs
                        val mins = (timeTakenMs / 1000L) / 60L
                        val secs = (timeTakenMs / 1000L) % 60L

                        CompressorState.timeTaken.value = String.format(Locale.US, "%02dm %02ds", mins, secs)
                        CompressorState.originalSizeMb.value = metadata.sizeBytes.toDouble() / (1024.0 * 1024.0)
                        CompressorState.finalSizeMb.value = result.finalSizeBytes.toDouble() / (1024.0 * 1024.0)
                        CompressorState.savedVideoUri.value = result.savedUri

                        CompressorState.isFinished.value = true
                        updateNotification(100, "Selesai! Video tersimpan di Movies/Komvid.")
                    }

                    is TranscodeResult.Failure -> {
                        CompressorState.reportError(result.errorMessage)
                        updateNotification(0, "Gagal mengompresi video.")
                    }
                }
            } catch (_: CancellationException) {
                updateNotification(0, "Kompresi dibatalkan.")
            } catch (e: Exception) {
                CompressorState.reportError(
                    "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Terjadi kesalahan saat memproses video."}"
                )
                updateNotification(0, "Error: ${e.localizedMessage}")
            } finally {
                CompressorState.isCompressing.value = false
                if (wakeLock.isHeld) wakeLock.release()
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun cancelCompression() {
        transcodeJob?.cancel()
        CompressorState.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Status Kompresi Komvid", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(progress: Int, text: String): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, VideoCompressorService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Komvid")
            .setContentText(text)
            .setContentIntent(contentPendingIntent)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, progress, progress == 0)
            .setOngoing(progress < 100)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Batal", cancelPendingIntent)
            .build()
    }

    private fun updateNotification(progress: Int, text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(progress, text))
    }
}
