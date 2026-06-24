package com.nade.komvid

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns

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

    fun extractVideoMetadata(videoUri: Uri): VideoMetadata? {
        val retriever = MediaMetadataRetriever()
        var sizeBytes: Long = 0

        context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) { sizeBytes = cursor.getLong(sizeIndex) }
        }

        return try {
            retriever.setDataSource(context, videoUri)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

            var bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLong() ?: 0
            if (bitrate <= 0 && durationMs > 0) {
                bitrate = (sizeBytes * 8) / (durationMs / 1000)
            }

            VideoMetadata(width, height, durationMs / 1000, bitrate, sizeBytes)
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

    // Parameter isHardwareMode dihapus, diganti targetShortSide (misal: 720, 1080)
    fun calculateEstimation(metadata: VideoMetadata, targetShortSide: Int): CompressionEstimate {
        val codec = checkHardwareCodecSupport()

        // Bitrate dinamis ngikutin resolusi yang dipilih user
        val targetVideoBitrateMbps = when {
            targetShortSide >= 2160 -> 12.0 // 4K
            targetShortSide >= 1440 -> 6.0  // 2K
            targetShortSide >= 1080 -> 3.5  // FHD
            else -> 1.5                     // 720p HD
        }

        val targetAudioBitrateMbps = 0.096 // AAC 96 Kbps
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