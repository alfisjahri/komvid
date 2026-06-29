package com.nade.komvid

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

object CompressorState {
    val isCompressing = MutableStateFlow(false)
    val progress = MutableStateFlow(0)
    val eta = MutableStateFlow("")

    // Tambahan State untuk Halaman Statistik
    val isFinished = MutableStateFlow(false)
    val timeTaken = MutableStateFlow("")
    val originalSizeMb = MutableStateFlow(0.0)
    val finalSizeMb = MutableStateFlow(0.0)
    val savedVideoUri = MutableStateFlow<Uri?>(null)

    fun reset() {
        isCompressing.value = false
        progress.value = 0
        eta.value = ""
        isFinished.value = false
        timeTaken.value = ""
        originalSizeMb.value = 0.0
        finalSizeMb.value = 0.0
        savedVideoUri.value = null
    }
}