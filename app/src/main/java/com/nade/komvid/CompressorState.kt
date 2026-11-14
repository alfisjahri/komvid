package com.nade.komvid

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

object CompressorState {
    val isCompressing = MutableStateFlow(false)
    val progress = MutableStateFlow(0)
    val eta = MutableStateFlow("")
    val targetProfile = MutableStateFlow("")

    val isFinished = MutableStateFlow(false)
    val timeTaken = MutableStateFlow("")
    val originalSizeMb = MutableStateFlow(0.0)
    val finalSizeMb = MutableStateFlow(0.0)
    val savedVideoUri = MutableStateFlow<Uri?>(null)

    val errorMessage = MutableStateFlow<String?>(null)

    fun clearError() {
        errorMessage.value = null
    }

    fun reportError(message: String) {
        isCompressing.value = false
        isFinished.value = false
        errorMessage.value = message
    }

    fun reset() {
        isCompressing.value = false
        progress.value = 0
        eta.value = ""
        targetProfile.value = ""
        isFinished.value = false
        timeTaken.value = ""
        originalSizeMb.value = 0.0
        finalSizeMb.value = 0.0
        savedVideoUri.value = null
        errorMessage.value = null
    }
}
