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
import kotlinx.coroutines.*

class VideoCompressorService : Service() {

    companion object {
        const val ACTION_CANCEL = "com.nade.komvid.ACTION_CANCEL"
        const val CHANNEL_ID = "KomvidChannel"
        const val NOTIF_ID = 1
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

        val videoUriStr = intent?.getStringExtra("VIDEO_URI") ?: return START_NOT_STICKY
        val targetShortSide = intent.getIntExtra("TARGET_RES", 720)
        val muteAudio = intent.getBooleanExtra("MUTE_AUDIO", false)

        val notification = buildNotification(0, "Mempersiapkan mesin kompresi...")
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING)
        } else {
            startForeground(NOTIF_ID, notification)
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Komvid::CompressWakelock")
        wakeLock.acquire(30 * 60 * 1000L)

        transcodeJob = serviceScope.launch {
            try {
                CompressorState.reset()
                CompressorState.isCompressing.value = true
                CompressorState.eta.value = "Memulai..."

                val uri = Uri.parse(videoUriStr)
                val analyzer = VideoAnalyzer(this@VideoCompressorService)
                val transcoder = VideoTranscoder(this@VideoCompressorService)

                val metadata = analyzer.extractVideoMetadata(uri)
                if (metadata != null) {
                    val estimate = analyzer.calculateEstimation(metadata, targetShortSide, muteAudio)
                    val mimeType = analyzer.checkHardwareCodecSupport()
                    val targetBitrateBps = (estimate.targetBitrateMbps * 1024 * 1024).toInt()

                    val isPortrait = metadata.height > metadata.width
                    var targetHeightAbs = if (isPortrait) {
                        (metadata.height * (targetShortSide.toFloat() / metadata.width)).toInt()
                    } else {
                        targetShortSide
                    }
                    if (targetHeightAbs % 2 != 0) targetHeightAbs += 1

                    val startTimeMs = System.currentTimeMillis()

                    val (savedUri, finalSizeBytes) = transcoder.startTranscode(uri, mimeType, targetBitrateBps, targetHeightAbs, muteAudio) { progress, etaString ->
                        updateNotification(progress, "Kompresi $progress% | $etaString")
                        CompressorState.progress.value = progress
                        CompressorState.eta.value = etaString
                    }

                    if (savedUri != null) {
                        val timeTakenMs = System.currentTimeMillis() - startTimeMs
                        val mins = (timeTakenMs / 1000) / 60
                        val secs = (timeTakenMs / 1000) % 60

                        CompressorState.timeTaken.value = String.format("%02d menit %02d detik", mins, secs)
                        CompressorState.originalSizeMb.value = metadata.sizeBytes.toDouble() / (1024 * 1024)
                        CompressorState.finalSizeMb.value = finalSizeBytes.toDouble() / (1024 * 1024)
                        CompressorState.savedVideoUri.value = savedUri

                        CompressorState.isFinished.value = true
                        updateNotification(100, "Selesai! Video tersimpan di Galeri.")
                    } else {
                        updateNotification(0, "Gagal mengompresi video.")
                    }
                }
            } catch (e: CancellationException) {
                updateNotification(0, "Kompresi dibatalkan.")
            } catch (e: Exception) {
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
        val cancelIntent = Intent(this, VideoCompressorService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, 0, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("nade komvid")
            .setContentText(text)
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