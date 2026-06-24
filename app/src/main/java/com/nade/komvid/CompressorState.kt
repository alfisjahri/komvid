package com.nade.komvid

import kotlinx.coroutines.flow.MutableStateFlow

object CompressorState {
    val isCompressing = MutableStateFlow(false)
    val progress = MutableStateFlow(0)
    val eta = MutableStateFlow("")

    fun reset() {
        isCompressing.value = false
        progress.value = 0
        eta.value = ""
    }
}