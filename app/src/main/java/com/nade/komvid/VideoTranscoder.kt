package com.nade.komvid

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

class VideoTranscoder(private val context: Context) {

    suspend fun startTranscode(
        videoUri: Uri,
        mimeType: String,
        targetBitrateBps: Int,
        targetHeightAbs: Int,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {

        suspendCancellableCoroutine { continuation ->
            val tempFile = File(context.cacheDir, "temp_komvid_${System.currentTimeMillis()}.mp4")

            val videoEncoderSettings = VideoEncoderSettings.Builder()
                .setBitrate(targetBitrateBps)
                .build()

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(videoEncoderSettings)
                .build()

            var progressJob: Job? = null

            // Transformer murni tanpa TransformationRequest
            val transformer = Transformer.Builder(context)
                .setVideoMimeType(mimeType)
                .setEncoderFactory(encoderFactory)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        progressJob?.cancel()
                        val success = saveToPublicGallery(tempFile, "Komvid_${System.currentTimeMillis()}.mp4")
                        tempFile.delete()
                        if (continuation.isActive) continuation.resume(success)
                    }

                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        progressJob?.cancel()
                        tempFile.delete()
                        exportException.printStackTrace()
                        if (continuation.isActive) continuation.resume(false)
                    }
                })
                .build()

            // SISTEM BARU: Menggunakan Effects dan EditedMediaItem untuk Downscale
            val mediaItem = MediaItem.fromUri(videoUri)
            val presentation = Presentation.createForHeight(targetHeightAbs)
            val effects = Effects(emptyList(), listOf(presentation))
            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setEffects(effects)
                .build()

            // Mulai eksekusi dengan item yang sudah diberi efek "Resize"
            transformer.start(editedMediaItem, tempFile.absolutePath)

            val startTime = System.currentTimeMillis()

            progressJob = launch {
                val progressHolder = ProgressHolder()
                while (isActive) {
                    val progressState = transformer.getProgress(progressHolder)
                    if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val p = progressHolder.progress
                        if (p > 0) {
                            val elapsedSec = (System.currentTimeMillis() - startTime) / 1000
                            val totalEstSec = (elapsedSec * 100) / p
                            val remainSec = totalEstSec - elapsedSec
                            val mins = remainSec / 60
                            val secs = remainSec % 60
                            val etaStr = String.format("Estimasi: %02d:%02d", mins, secs)
                            onProgress(p, etaStr)
                        } else {
                            onProgress(0, "Menghitung estimasi...")
                        }
                    }
                    delay(1000)
                }
            }

            continuation.invokeOnCancellation {
                transformer.cancel()
                progressJob?.cancel()
                tempFile.delete()
            }
        }
    }

    private fun saveToPublicGallery(tempFile: File, fileName: String): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Komvid")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Komvid")
                if (!dir.exists()) dir.mkdirs()
                put(MediaStore.Video.Media.DATA, File(dir, fileName).absolutePath)
            }
        }

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return false

        return try {
            resolver.openOutputStream(uri).use { outStream ->
                tempFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream!!)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}