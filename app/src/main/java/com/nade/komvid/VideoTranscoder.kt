package com.nade.komvid

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume

sealed interface TranscodeResult {
    data class Success(val savedUri: Uri, val finalSizeBytes: Long) : TranscodeResult
    data class Failure(val errorMessage: String) : TranscodeResult
}

class VideoTranscoder(private val context: Context) {

    suspend fun startTranscode(
        videoUri: Uri,
        mimeType: String,
        targetBitrateBps: Int,
        targetHeightAbs: Int,
        muteAudio: Boolean = false,
        trimStartMs: Long = -1L,
        trimEndMs: Long = -1L,
        onProgress: (Int, String) -> Unit
    ): TranscodeResult = withContext(Dispatchers.Main) {

        suspendCancellableCoroutine { continuation ->
            val tempFile = File(context.cacheDir, "temp_komvid_${System.currentTimeMillis()}.mp4")

            val videoEncoderSettings = VideoEncoderSettings.Builder()
                .setBitrate(targetBitrateBps)
                .build()

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(videoEncoderSettings)
                .build()

            var progressJob: Job? = null

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(mimeType)
                .setEncoderFactory(encoderFactory)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        progressJob?.cancel()
                        val finalSize = tempFile.length()
                        val savedUri = saveToPublicGallery(tempFile, "Komvid_${System.currentTimeMillis()}.mp4")
                        tempFile.delete()

                        if (continuation.isActive) {
                            if (savedUri != null) {
                                continuation.resume(TranscodeResult.Success(savedUri, finalSize))
                            } else {
                                continuation.resume(
                                    TranscodeResult.Failure(
                                        "MediaStoreError: Gagal menyimpan hasil kompresi ke folder Movies/Komvid. Pastikan ruang penyimpanan internal mencukupi."
                                    )
                                )
                            }
                        }
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        progressJob?.cancel()
                        tempFile.delete()
                        exportException.printStackTrace()
                        val causeMsg = exportException.cause?.localizedMessage
                            ?: exportException.localizedMessage
                            ?: "Hardware encoder gagal memproses video."
                        val errorText = "ExportException (${exportException.errorCode}): ${exportException.getErrorCodeName()} — $causeMsg"
                        if (continuation.isActive) {
                            continuation.resume(TranscodeResult.Failure(errorText))
                        }
                    }
                })
                .build()

            val mediaItemBuilder = MediaItem.Builder().setUri(videoUri)
            if (trimStartMs >= 0L && trimEndMs > trimStartMs) {
                val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(trimStartMs)
                    .setEndPositionMs(trimEndMs)
                    .build()
                mediaItemBuilder.setClippingConfiguration(clippingConfig)
            }
            val mediaItem = mediaItemBuilder.build()

            val presentation = Presentation.createForHeight(targetHeightAbs)
            val effects = Effects(emptyList(), listOf(presentation))
            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setEffects(effects)
                .setRemoveAudio(muteAudio)
                .build()

            transformer.start(editedMediaItem, tempFile.absolutePath)

            val startTime = System.currentTimeMillis()

            progressJob = launch {
                val progressHolder = ProgressHolder()
                while (isActive) {
                    val progressState = transformer.getProgress(progressHolder)
                    if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val p = progressHolder.progress.coerceIn(0, 100)
                        if (p > 0) {
                            val elapsedSec = (System.currentTimeMillis() - startTime) / 1000L
                            val totalEstSec = (elapsedSec * 100L) / p
                            val remainSec = (totalEstSec - elapsedSec).coerceAtLeast(0L)
                            val mins = remainSec / 60L
                            val secs = remainSec % 60L
                            val etaStr = String.format(Locale.US, "%02d:%02d", mins, secs)
                            onProgress(p, etaStr)
                        } else {
                            onProgress(0, "--:--")
                        }
                    }
                    delay(800)
                }
            }

            continuation.invokeOnCancellation {
                progressJob?.cancel()
                val cancelAction = {
                    runCatching { transformer.cancel() }
                    tempFile.delete()
                    Unit
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    cancelAction()
                } else {
                    Handler(Looper.getMainLooper()).post { cancelAction() }
                }
            }
        }
    }

    private fun saveToPublicGallery(tempFile: File, fileName: String): Uri? {
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

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        return try {
            resolver.openOutputStream(uri).use { outStream ->
                if (outStream == null) throw IllegalStateException("Output stream null")
                tempFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            runCatching { resolver.delete(uri, null, null) }
            null
        }
    }
}
