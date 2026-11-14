package com.nade.komvid

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

const val MIME_HEVC = "video/hevc"
const val MIME_AVC = "video/avc"

const val AUDIO_BITRATE_MBPS = 0.128

fun formatDurationCompact(durationMs: Long): String {
    val totalSec = (durationMs.coerceAtLeast(0L) / 1000L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return if (mins > 0L) {
        String.format(Locale.US, "%dm %02ds", mins, secs)
    } else {
        String.format(Locale.US, "%ds", secs)
    }
}

fun formatMmSs(durationMs: Long): String {
    val totalSec = (durationMs.coerceAtLeast(0L) / 1000L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

fun formatMbOrGb(mb: Double): String {
    val safeMb = mb.coerceAtLeast(0.0)
    return if (safeMb >= 1024.0) {
        String.format(Locale.US, "%.2f GB", safeMb / 1024.0)
    } else {
        String.format(Locale.US, "%.1f MB", safeMb)
    }
}

fun formatResolutionLabel(targetShortSide: Int): String =
    if (targetShortSide >= 2160) "4K" else "${targetShortSide}p"

/**
 * Target video bitrate requested from MediaCodec's hardware encoder.
 * H.264 uses a separate higher target table (~35% higher than H.265) to maintain comparable quality.
 */
fun targetVideoBitrateMbpsFor(
    targetShortSide: Int,
    codecMimeType: String = MIME_HEVC
): Double = if (codecMimeType == MIME_AVC) {
    when {
        targetShortSide >= 2160 -> 16.2
        targetShortSide >= 1440 -> 8.1
        targetShortSide >= 1080 -> 4.75
        targetShortSide >= 720 -> 2.05
        else -> 1.35
    }
} else {
    when {
        targetShortSide >= 2160 -> 12.0
        targetShortSide >= 1440 -> 6.0
        targetShortSide >= 1080 -> 3.5
        targetShortSide >= 720 -> 1.5
        else -> 1.0
    }
}

/**
 * Calibrated real-world output video bitrate per resolution and codec
 * (accounting for hardware VBR rate-control overshoot and MP4 muxing overhead).
 */
fun calibratedBaseOutputBitrateMbpsFor(
    targetShortSide: Int,
    codecMimeType: String = MIME_HEVC
): Double = if (codecMimeType == MIME_AVC) {
    when {
        targetShortSide >= 2160 -> 20.25
        targetShortSide >= 1440 -> 10.15
        targetShortSide >= 1080 -> 5.7
        targetShortSide >= 720 -> 2.55
        else -> 1.7
    }
} else {
    when {
        targetShortSide >= 2160 -> 15.0
        targetShortSide >= 1440 -> 7.5
        targetShortSide >= 1080 -> 4.2
        targetShortSide >= 720 -> 1.9
        else -> 1.25
    }
}

fun referenceSourceBitrateMbpsFor(targetShortSide: Int): Double = when {
    targetShortSide >= 2160 -> 35.0
    targetShortSide >= 1440 -> 22.0
    targetShortSide >= 1080 -> 15.0
    targetShortSide >= 720 -> 8.0
    else -> 4.0
}

fun sourceBitrateFactor(sourceBitrateBps: Long, targetShortSide: Int): Double {
    if (sourceBitrateBps <= 0L) return 1.0
    val sourceMbps = sourceBitrateBps.toDouble() / (1024.0 * 1024.0)
    val refMbps = referenceSourceBitrateMbpsFor(targetShortSide)
    val ratio = sourceMbps / refMbps
    return (0.94 + 0.06 * ratio).coerceIn(0.88, 1.12)
}

fun estimatedTotalBitrateMbpsFor(
    targetShortSide: Int,
    muteAudio: Boolean,
    codecMimeType: String = MIME_HEVC,
    sourceBitrateBps: Long = 0L
): Double {
    val baseVideoMbps = calibratedBaseOutputBitrateMbpsFor(targetShortSide, codecMimeType)
    val sourceFactor = sourceBitrateFactor(sourceBitrateBps, targetShortSide)
    val audioMbps = if (muteAudio) 0.0 else AUDIO_BITRATE_MBPS
    return (baseVideoMbps * sourceFactor) + audioMbps
}

fun estimateOutputSizeBytes(
    targetShortSide: Int,
    muteAudio: Boolean,
    durationMs: Long,
    codecMimeType: String = MIME_HEVC,
    sourceBitrateBps: Long = 0L
): Long {
    val totalBitrateMbps = estimatedTotalBitrateMbpsFor(
        targetShortSide = targetShortSide,
        muteAudio = muteAudio,
        codecMimeType = codecMimeType,
        sourceBitrateBps = sourceBitrateBps
    )
    val durationSec = (durationMs.coerceAtLeast(1L).toDouble()) / 1000.0
    val estimatedSizeMb = (totalBitrateMbps * durationSec) / 8.0
    return (estimatedSizeMb * 1024.0 * 1024.0).toLong()
}

data class HardwareCodecSupport(
    val supportsH265: Boolean,
    val supportsH264: Boolean
) {
    fun isSupported(mimeType: String): Boolean = when (mimeType) {
        MIME_HEVC -> supportsH265
        MIME_AVC -> supportsH264
        else -> false
    }

    fun hasAnySupportedCodec(): Boolean = supportsH265 || supportsH264

    fun defaultCodecMimeType(): String = when {
        supportsH265 -> MIME_HEVC
        supportsH264 -> MIME_AVC
        else -> MIME_HEVC
    }
}

data class VideoMetadata(
    val width: Int,
    val height: Int,
    val durationSec: Long,
    val bitRateBps: Long,
    val sizeBytes: Long,
    val durationMs: Long = durationSec * 1000L
) {
    fun formattedDuration(): String = formatDurationCompact(durationMs)

    fun formattedSize(): String = formatMbOrGb(sizeBytes.toDouble() / (1024.0 * 1024.0))
}

data class VideoMetadataResult(
    val metadata: VideoMetadata?,
    val errorMessage: String? = null
)

data class CompressionEstimate(
    val codecMimeType: String,
    val targetCodec: String,
    val shortCodecName: String,
    val codecTag: String,
    val targetBitrateMbps: Double,
    val estimatedBitrateMbps: Double,
    val estimatedSizeBytes: Long,
    val compressionPercentage: Int,
    val trimmedDurationMs: Long
) {
    fun formattedEstimatedSize(): String = formatMbOrGb(estimatedSizeBytes.toDouble() / (1024.0 * 1024.0))
}

class VideoAnalyzer(private val context: Context) {

    suspend fun extractVideoMetadata(videoUri: Uri): VideoMetadata? =
        extractVideoMetadataWithError(videoUri).metadata

    suspend fun extractVideoMetadataWithError(videoUri: Uri): VideoMetadataResult = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var sizeBytes = 0L

        runCatching {
            context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }
        if (sizeBytes <= 0L) {
            runCatching {
                context.contentResolver.openAssetFileDescriptor(videoUri, "r")?.use { afd ->
                    if (afd.length > 0L) sizeBytes = afd.length
                }
            }
        }

        return@withContext try {
            retriever.setDataSource(context, videoUri)
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val r = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

            val trueWidth = if (r == 90 || r == 270) h else w
            val trueHeight = if (r == 90 || r == 270) w else h

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            var bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L

            if (bitrate <= 0L && durationMs > 0L && sizeBytes > 0L) {
                bitrate = (sizeBytes * 8L * 1000L) / durationMs
            }

            if (trueWidth <= 0 || trueHeight <= 0 || durationMs <= 0L) {
                VideoMetadataResult(
                    metadata = null,
                    errorMessage = "MetadataException: Video tidak memiliki stream visual atau durasi valid (dimensi=${trueWidth}x${trueHeight}, durasi=${durationMs}ms)."
                )
            } else {
                VideoMetadataResult(
                    metadata = VideoMetadata(
                        width = trueWidth,
                        height = trueHeight,
                        durationSec = durationMs / 1000L,
                        bitRateBps = bitrate,
                        sizeBytes = sizeBytes,
                        durationMs = durationMs
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            VideoMetadataResult(
                metadata = null,
                errorMessage = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Gagal membaca metadata berkas video."}"
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun isHardwareEncoder(codecInfo: MediaCodecInfo): Boolean {
        if (!codecInfo.isEncoder) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            codecInfo.isHardwareAccelerated
        } else {
            val name = codecInfo.name.lowercase(Locale.US)
            !name.startsWith("omx.google.") &&
                !name.startsWith("c2.android.") &&
                !name.contains("google") &&
                !name.contains("android") &&
                !name.contains(".sw")
        }
    }

    fun detectHardwareCodecSupport(): HardwareCodecSupport {
        var supportsHevc = false
        var supportsAvc = false
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (!isHardwareEncoder(codecInfo)) continue
            for (type in codecInfo.supportedTypes) {
                if (type.equals(MIME_HEVC, ignoreCase = true)) {
                    supportsHevc = true
                } else if (type.equals(MIME_AVC, ignoreCase = true)) {
                    supportsAvc = true
                }
            }
            if (supportsHevc && supportsAvc) break
        }
        return HardwareCodecSupport(
            supportsH265 = supportsHevc,
            supportsH264 = supportsAvc
        )
    }

    fun checkHardwareCodecSupport(): String = detectHardwareCodecSupport().defaultCodecMimeType()

    fun calculateEstimation(
        metadata: VideoMetadata,
        targetShortSide: Int,
        codecMimeType: String = checkHardwareCodecSupport(),
        muteAudio: Boolean = false,
        trimmedDurationMs: Long = metadata.durationMs
    ): CompressionEstimate {
        val targetEncoderBitrateMbps = targetVideoBitrateMbpsFor(targetShortSide, codecMimeType)
        val effectiveSourceBitrateBps = when {
            metadata.bitRateBps > 0L -> metadata.bitRateBps
            metadata.sizeBytes > 0L && metadata.durationMs > 0L ->
                ((metadata.sizeBytes.toDouble() * 8.0 * 1000.0) / metadata.durationMs.toDouble()).toLong()
            else -> 0L
        }

        val effectiveDurationMs = if (metadata.durationMs > 0L) {
            trimmedDurationMs.coerceIn(1L, metadata.durationMs)
        } else {
            (metadata.durationSec * 1000L).coerceAtLeast(1000L)
        }

        val estimatedBitrateMbps = estimatedTotalBitrateMbpsFor(
            targetShortSide = targetShortSide,
            muteAudio = muteAudio,
            codecMimeType = codecMimeType,
            sourceBitrateBps = effectiveSourceBitrateBps
        )
        val estimatedSizeBytes = estimateOutputSizeBytes(
            targetShortSide = targetShortSide,
            muteAudio = muteAudio,
            durationMs = effectiveDurationMs,
            codecMimeType = codecMimeType,
            sourceBitrateBps = effectiveSourceBitrateBps
        )

        val percentage = if (metadata.sizeBytes > 0L) {
            val reduction = metadata.sizeBytes - estimatedSizeBytes
            ((reduction.toDouble() / metadata.sizeBytes) * 100.0).coerceIn(0.0, 99.0).toInt()
        } else {
            0
        }

        val isHevc = codecMimeType == MIME_HEVC
        return CompressionEstimate(
            codecMimeType = codecMimeType,
            targetCodec = if (isHevc) "H.265 (HEVC Hardware)" else "H.264 (AVC Hardware)",
            shortCodecName = if (isHevc) "H.265 (HEVC)" else "H.264 (AVC)",
            codecTag = if (isHevc) "H.265" else "H.264",
            targetBitrateMbps = targetEncoderBitrateMbps,
            estimatedBitrateMbps = estimatedBitrateMbps,
            estimatedSizeBytes = estimatedSizeBytes,
            compressionPercentage = percentage,
            trimmedDurationMs = effectiveDurationMs
        )
    }
}
