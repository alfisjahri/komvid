package com.nade.komvid

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoMetadata(
    val width: Int,
    val height: Int,
    val durationSec: Long,
    val bitRateBps: Long,
    val sizeBytes: Long
)

data class CompressionEstimate(
    val targetCodec: String,
    val targetBitrateMbps: Double,
    val estimatedSizeBytes: Long,
    val compressionPercentage: Int
)

class VideoAnalyzer(private val context: Context) {

    // Ditambah suspend & Dispatchers.IO biar UI nggak freeze/delay
    suspend fun extractVideoMetadata(videoUri: Uri): VideoMetadata? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var sizeBytes: Long = 0

        context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) { sizeBytes = cursor.getLong(sizeIndex) }
        }

        return@withContext try {
            retriever.setDataSource(context, videoUri)
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            val r = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0

            // FIX PORTRAIT: Jika HP nge-rotate video, kita tukar panjang x lebarnya
            val trueWidth = if (r == 90 || r == 270) h else w
            val trueHeight = if (r == 90 || r == 270) w else h

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            var bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLong() ?: 0

            if (bitrate <= 0 && durationMs > 0) {
                bitrate = (sizeBytes * 8) / (durationMs / 1000)
            }

            VideoMetadata(trueWidth, trueHeight, durationMs / 1000, bitrate, sizeBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    fun checkHardwareCodecSupport(): String {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (!codecInfo.isEncoder) continue
            for (type in codecInfo.supportedTypes) {
                if (type.equals("video/hevc", ignoreCase = true)) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        if (codecInfo.isHardwareAccelerated) return "video/hevc"
                    } else {
                        val name = codecInfo.name.lowercase()
                        if (!name.contains("google") && !name.contains("android") && !name.contains("sw")) {
                            return "video/hevc"
                        }
                    }
                }
            }
        }
        return "video/avc"
    }

    fun calculateEstimation(metadata: VideoMetadata, targetShortSide: Int): CompressionEstimate {
        val codec = checkHardwareCodecSupport()
        val targetVideoBitrateMbps = when {
            targetShortSide >= 2160 -> 12.0
            targetShortSide >= 1440 -> 6.0
            targetShortSide >= 1080 -> 3.5
            else -> 1.5
        }
        val targetAudioBitrateMbps = 0.096
        val totalBitrateMbps = targetVideoBitrateMbps + targetAudioBitrateMbps
        val estimatedSizeMb = (totalBitrateMbps * metadata.durationSec) / 8
        val estimatedSizeBytes = (estimatedSizeMb * 1024 * 1024).toLong()

        val percentage = if (metadata.sizeBytes > 0) {
            val reduction = metadata.sizeBytes - estimatedSizeBytes
            ((reduction.toDouble() / metadata.sizeBytes) * 100).coerceIn(0.0, 99.0).toInt()
        } else { 0 }

        return CompressionEstimate(
            targetCodec = if (codec == "video/hevc") "H.265 (HEVC Hardware)" else "H.264 (AVC Hardware)",
            targetBitrateMbps = targetVideoBitrateMbps,
            estimatedSizeBytes = estimatedSizeBytes,
            compressionPercentage = percentage
        )
    }
}