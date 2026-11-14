package com.nade.komvid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class KomvidUnitTest {

    @Test
    fun formatDurationCompact_formatsMinutesAndSeconds() {
        assertEquals("0s", formatDurationCompact(0L))
        assertEquals("45s", formatDurationCompact(45_000L))
        assertEquals("1m 30s", formatDurationCompact(90_000L))
        assertEquals("2m 15s", formatDurationCompact(135_000L))
    }

    @Test
    fun formatMmSs_formatsZeroPaddedMinutesAndSeconds() {
        assertEquals("00:00", formatMmSs(0L))
        assertEquals("00:15", formatMmSs(15_000L))
        assertEquals("01:45", formatMmSs(105_000L))
        assertEquals("02:15", formatMmSs(135_000L))
    }

    @Test
    fun estimateOutputSizeBytes_calibratedWithinFivePercentOfReal4kTo1080pTestCase() {
        val sourceSizeBytes = (879.6 * 1024.0 * 1024.0).toLong()
        val durationMs = 291_000L
        val sourceBitrateBps = (sourceSizeBytes * 8L * 1000L) / durationMs

        val estimatedBytes = estimateOutputSizeBytes(
            targetShortSide = 1080,
            muteAudio = false,
            durationMs = durationMs,
            codecMimeType = MIME_HEVC,
            sourceBitrateBps = sourceBitrateBps
        )
        val estimatedMb = estimatedBytes.toDouble() / (1024.0 * 1024.0)
        val actualMb = 162.3
        val relativeError = abs(estimatedMb - actualMb) / actualMb

        assertTrue(
            "Expected <5% error vs 162.3 MB, got ${"%.2f".format(estimatedMb)} MB (${"%.2f".format(relativeError * 100)}%)",
            relativeError < 0.05
        )
    }

    @Test
    fun estimateOutputSizeBytes_h264IsThirtyToFortyPercentLargerThanH265AcrossResolutions() {
        val resolutions = listOf(720, 1080, 1440, 2160)
        for (res in resolutions) {
            val h265Bytes = estimateOutputSizeBytes(
                targetShortSide = res,
                muteAudio = false,
                durationMs = 120_000L,
                codecMimeType = MIME_HEVC
            )
            val h264Bytes = estimateOutputSizeBytes(
                targetShortSide = res,
                muteAudio = false,
                durationMs = 120_000L,
                codecMimeType = MIME_AVC
            )
            val ratio = h264Bytes.toDouble() / h265Bytes.toDouble()
            assertTrue(
                "H.264/H.265 ratio at ${res}p should be in 1.28..1.42, got $ratio",
                ratio in 1.28..1.42
            )
        }
    }

    @Test
    fun estimateOutputSizeBytes_scalesProportionallyWithTrimmedDuration() {
        val fullBytes = estimateOutputSizeBytes(targetShortSide = 1080, muteAudio = false, durationMs = 135_000L)
        val trimmedBytes = estimateOutputSizeBytes(targetShortSide = 1080, muteAudio = false, durationMs = 90_000L)
        val ratio = trimmedBytes.toDouble() / fullBytes.toDouble()
        assertEquals(90.0 / 135.0, ratio, 0.001)
    }

    @Test
    fun estimateOutputSizeBytes_muteReducesEstimatedSize() {
        val withAudio = estimateOutputSizeBytes(targetShortSide = 720, muteAudio = false, durationMs = 60_000L)
        val muted = estimateOutputSizeBytes(targetShortSide = 720, muteAudio = true, durationMs = 60_000L)
        assertTrue(muted < withAudio)
    }

    @Test
    fun estimateOutputSizeBytes_higherSourceBitrateIncreasesEstimatedOutput() {
        val lowSrc = estimateOutputSizeBytes(
            targetShortSide = 1080,
            muteAudio = false,
            durationMs = 120_000L,
            codecMimeType = MIME_HEVC,
            sourceBitrateBps = 8_000_000L
        )
        val highSrc = estimateOutputSizeBytes(
            targetShortSide = 1080,
            muteAudio = false,
            durationMs = 120_000L,
            codecMimeType = MIME_HEVC,
            sourceBitrateBps = 35_000_000L
        )
        assertTrue(highSrc > lowSrc)
    }

    @Test
    fun hardwareCodecSupport_selectsDefaultAndChecksSupport() {
        val both = HardwareCodecSupport(supportsH265 = true, supportsH264 = true)
        assertEquals(MIME_HEVC, both.defaultCodecMimeType())
        assertTrue(both.hasAnySupportedCodec())
        assertTrue(both.isSupported(MIME_HEVC))
        assertTrue(both.isSupported(MIME_AVC))

        val onlyAvc = HardwareCodecSupport(supportsH265 = false, supportsH264 = true)
        assertEquals(MIME_AVC, onlyAvc.defaultCodecMimeType())
        assertTrue(onlyAvc.hasAnySupportedCodec())
        assertFalse(onlyAvc.isSupported(MIME_HEVC))
        assertTrue(onlyAvc.isSupported(MIME_AVC))

        val none = HardwareCodecSupport(supportsH265 = false, supportsH264 = false)
        assertFalse(none.hasAnySupportedCodec())
        assertFalse(none.isSupported(MIME_HEVC))
        assertFalse(none.isSupported(MIME_AVC))
    }

    @Test
    fun compressorState_reportErrorAndReset_clearsAllFields() {
        CompressorState.reset()
        CompressorState.isCompressing.value = true
        CompressorState.progress.value = 50
        CompressorState.eta.value = "00:20"
        CompressorState.targetProfile.value = "1080p · H.265"

        CompressorState.reportError("ExportException (4001): Encoder init failed")
        assertFalse(CompressorState.isCompressing.value)
        assertFalse(CompressorState.isFinished.value)
        assertEquals("ExportException (4001): Encoder init failed", CompressorState.errorMessage.value)

        CompressorState.clearError()
        assertNull(CompressorState.errorMessage.value)

        CompressorState.reportError("Another error")
        CompressorState.reset()
        assertNull(CompressorState.errorMessage.value)
        assertEquals(0, CompressorState.progress.value)
        assertEquals("", CompressorState.eta.value)
        assertEquals("", CompressorState.targetProfile.value)
    }
}
