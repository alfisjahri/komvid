package com.nade.komvid.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nade.komvid.CompressionEstimate
import com.nade.komvid.HardwareCodecSupport
import com.nade.komvid.MIME_AVC
import com.nade.komvid.MIME_HEVC
import com.nade.komvid.R
import com.nade.komvid.VideoMetadata
import com.nade.komvid.formatDurationCompact
import com.nade.komvid.formatMbOrGb
import com.nade.komvid.formatMmSs
import com.nade.komvid.ui.components.NeobrutalistCard
import com.nade.komvid.ui.components.NeobrutalistIconTile
import com.nade.komvid.ui.components.NeobrutalistProgressBar
import com.nade.komvid.ui.components.NeobrutalistSwitch
import com.nade.komvid.ui.components.PrimaryActionButton
import com.nade.komvid.ui.components.ProgressRing
import com.nade.komvid.ui.components.SecondaryActionButton
import com.nade.komvid.ui.components.SoftDangerButton
import com.nade.komvid.ui.components.TrimRangeSlider
import com.nade.komvid.ui.components.hardShadow
import com.nade.komvid.ui.theme.DisplayFontFamily
import com.nade.komvid.ui.theme.KomvidThemeTokens
import com.nade.komvid.ui.theme.MonoErrorStyle
import com.nade.komvid.ui.theme.TabularMetricStyle

@Composable
fun IdleScreen(
    minContentHeight: Dp,
    onPickVideo: () -> Unit
) {
    val colors = KomvidThemeTokens.colors
    val heroMinHeight = (minContentHeight - 106.dp).coerceAtLeast(360.dp)
    val wellMinHeight = (heroMinHeight - 28.dp).coerceAtLeast(332.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minContentHeight),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Hero Picker Card with Dashed Dropzone Well
        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = heroMinHeight),
            cornerRadius = 12.dp,
            containerColor = colors.bgSurface,
            borderWidth = 2.5.dp,
            shadowOffset = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = heroMinHeight)
                    .padding(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = wellMinHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.bgDropzone)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val inset = strokeWidth / 2f
                            drawRoundRect(
                                color = colors.ink,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(7.dp.toPx(), 5.dp.toPx()),
                                        phase = 0f
                                    )
                                )
                            )
                        }
                        .clickable(onClick = onPickVideo)
                        .padding(horizontal = 18.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        NeobrutalistIconTile(
                            iconRes = R.drawable.ic_video_plus,
                            size = 72.dp,
                            cornerRadius = 12.dp,
                            iconSize = 32.dp,
                            containerColor = colors.yellow,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Pilih Video untuk Dikompres",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    lineHeight = 24.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = colors.ink,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Mendukung format MP4, MKV, & MOV.\n100% lokal via Hardware Encoder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        PrimaryActionButton(
                            label = "Pilih Video dari Galeri",
                            iconRes = R.drawable.ic_plus,
                            onClick = onPickVideo
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom 3-Tile Capability Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CapabilityTile(
                iconRes = R.drawable.ic_chip,
                title = "Hardware",
                subtitle = "H.265 / H.264",
                accent = colors.mint,
                modifier = Modifier.weight(1f)
            )
            CapabilityTile(
                iconRes = R.drawable.ic_shield_check,
                title = "Latar Aman",
                subtitle = "Wakelock Aktif",
                accent = colors.cyan,
                modifier = Modifier.weight(1f)
            )
            CapabilityTile(
                iconRes = R.drawable.ic_folder,
                title = "Output",
                subtitle = "Movies/Komvid",
                accent = colors.yellow,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CapabilityTile(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    NeobrutalistCard(
        modifier = modifier,
        cornerRadius = 10.dp,
        containerColor = colors.bgSurface,
        borderWidth = 2.dp,
        shadowOffset = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NeobrutalistIconTile(
                iconRes = iconRes,
                size = 28.dp,
                cornerRadius = 8.dp,
                iconSize = 16.dp,
                containerColor = accent,
                borderWidth = 1.5.dp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp
                ),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                ),
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class ResolutionPreset(
    val shortSide: Int,
    val label: String,
    val badge: String,
    val description: String
)

private val RESOLUTION_PRESETS = listOf(
    ResolutionPreset(720, "720p", "Hemat", "HD (720p) — Hemat ruang optimal & cepat"),
    ResolutionPreset(1080, "1080p", "Standar", "FHD (1080p) — Standar jelas & hemat ruang"),
    ResolutionPreset(1440, "1440p", "Tinggi", "2K (1440p) — Ketajaman tinggi & seimbang"),
    ResolutionPreset(2160, "4K", "Maks", "4K (2160p) — Kualitas maksimal")
)

private data class CodecOption(
    val mimeType: String,
    val title: String,
    val supportedSubtitle: String
)

private val CODEC_OPTIONS = listOf(
    CodecOption(MIME_HEVC, "H.265 (HEVC)", "Lebih hemat ~30%"),
    CodecOption(MIME_AVC, "H.264 (AVC)", "Kompatibel luas")
)

@Composable
fun VideoSelectedScreen(
    minContentHeight: Dp,
    metadata: VideoMetadata,
    estimate: CompressionEstimate?,
    selectedResolution: Int,
    selectedCodec: String,
    codecSupport: HardwareCodecSupport,
    muteAudio: Boolean,
    trimStartMs: Long,
    trimEndMs: Long,
    onChangeVideo: () -> Unit,
    onSelectResolution: (Int) -> Unit,
    onSelectCodec: (String) -> Unit,
    onTrimChange: (Long, Long) -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onStartCompression: () -> Unit
) {
    val colors = KomvidThemeTokens.colors
    val shortSide = minOf(metadata.width, metadata.height)
    val trimmedDurationMs = (trimEndMs - trimStartMs).coerceAtLeast(1000L.coerceAtMost(metadata.durationMs))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minContentHeight),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Card 1: Source Video & Metadata
            NeobrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                containerColor = colors.bgSurface,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        NeobrutalistIconTile(
                            iconRes = R.drawable.ic_film,
                            size = 38.dp,
                            cornerRadius = 8.dp,
                            iconSize = 18.dp,
                            containerColor = colors.mint,
                            borderWidth = 2.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Video Terpilih",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                ),
                                color = colors.ink
                            )
                            Text(
                                text = "Siap untuk dikompres",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPositive
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .hardShadow(
                                    cornerRadius = 8.dp,
                                    offsetX = 2.dp,
                                    offsetY = 2.dp,
                                    shadowColor = colors.ink
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onChangeVideo),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.yellow,
                            border = BorderStroke(2.dp, colors.ink),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = "Ganti",
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                ),
                                color = colors.ink
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.5.dp, color = colors.divider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetadataCell(
                            label = "RESOLUSI",
                            value = "${metadata.width} × ${metadata.height}",
                            modifier = Modifier.weight(1f)
                        )
                        MetadataCell(
                            label = "DURASI",
                            value = metadata.formattedDuration(),
                            modifier = Modifier.weight(1f)
                        )
                        MetadataCell(
                            label = "UKURAN ASLI",
                            value = metadata.formattedSize(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card 2: Resolution, Codec Selector, Trim & Audio Settings
            NeobrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                containerColor = colors.bgSurface,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Resolution Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESOLUSI TARGET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.6.sp
                            ),
                            color = colors.ink
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.bgCyanTint,
                            border = BorderStroke(1.5.dp, colors.ink),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = "Smart Downscale",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.sp
                                ),
                                color = colors.textTech
                            )
                        }
                    }

                    // Resolution Segmented Control
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.bgRecessed,
                        border = BorderStroke(2.dp, colors.ink),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RESOLUTION_PRESETS.forEach { preset ->
                                val isAvailable = preset.shortSide <= shortSide
                                val isSelected = selectedResolution == preset.shortSide
                                val segShape = RoundedCornerShape(8.dp)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .hardShadow(
                                                        cornerRadius = 8.dp,
                                                        offsetX = 2.dp,
                                                        offsetY = 2.dp,
                                                        shadowColor = colors.ink
                                                    )
                                                    .clip(segShape)
                                                    .background(colors.yellow)
                                                    .border(2.dp, colors.ink, segShape)
                                            } else {
                                                Modifier.clip(segShape)
                                            }
                                        )
                                        .then(
                                            if (isAvailable) {
                                                Modifier.clickable { onSelectResolution(preset.shortSide) }
                                            } else {
                                                Modifier.alpha(0.38f)
                                            }
                                        )
                                        .padding(horizontal = 4.dp, vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.labelLarge.merge(TabularMetricStyle).copy(
                                            fontSize = 12.5.sp,
                                            lineHeight = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected || isAvailable) colors.ink else colors.textTertiary
                                    )
                                    Text(
                                        text = preset.badge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            lineHeight = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        ),
                                        color = if (isSelected) colors.textSecondary else colors.textTertiary
                                    )
                                }
                            }
                        }
                    }

                    val activeDescription = RESOLUTION_PRESETS
                        .firstOrNull { it.shortSide == selectedResolution }
                        ?.description
                        ?: "Resolusi Asli (${shortSide}p) — Pertahankan resolusi sumber"
                    Text(
                        text = activeDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        ),
                        color = colors.textSecondary
                    )

                    HorizontalDivider(thickness = 1.5.dp, color = colors.divider)

                    // Codec Selector Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CODEC VIDEO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = colors.ink
                            )
                            val bothSupported = codecSupport.supportsH265 && codecSupport.supportsH264
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (bothSupported) colors.bgYellowTint else colors.bgCoralTint,
                                border = BorderStroke(1.5.dp, colors.ink),
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = if (bothSupported) "Hardware SoC" else "Dukungan SoC Terbatas",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = if (bothSupported) colors.ink else colors.textDanger
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgRecessed,
                            border = BorderStroke(2.dp, colors.ink),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CODEC_OPTIONS.forEach { option ->
                                    val isSupported = codecSupport.isSupported(option.mimeType)
                                    val isSelected = isSupported && selectedCodec == option.mimeType
                                    val segShape = RoundedCornerShape(8.dp)

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .then(
                                                if (isSelected) {
                                                    Modifier
                                                        .hardShadow(
                                                            cornerRadius = 8.dp,
                                                            offsetX = 2.dp,
                                                            offsetY = 2.dp,
                                                            shadowColor = colors.ink
                                                        )
                                                        .clip(segShape)
                                                        .background(colors.yellow)
                                                        .border(2.dp, colors.ink, segShape)
                                                } else {
                                                    Modifier.clip(segShape)
                                                }
                                            )
                                            .then(
                                                if (isSupported) {
                                                    Modifier.clickable { onSelectCodec(option.mimeType) }
                                                } else {
                                                    Modifier.alpha(0.68f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 5.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.labelLarge.merge(TabularMetricStyle).copy(
                                                fontSize = 12.sp,
                                                lineHeight = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isSupported) colors.ink else colors.textTertiary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isSupported) option.supportedSubtitle else "SoC tidak support",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                lineHeight = 12.sp,
                                                fontWeight = if (isSelected || !isSupported) FontWeight.Bold else FontWeight.SemiBold,
                                                letterSpacing = 0.sp
                                            ),
                                            color = when {
                                                !isSupported -> colors.textDanger
                                                isSelected -> colors.textSecondary
                                                else -> colors.textTertiary
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.5.dp, color = colors.divider)

                    // Trimming Control Section
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_trim),
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "POTONG DURASI (TRIM)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = colors.ink
                                )
                            }

                            Surface(
                                modifier = Modifier.hardShadow(
                                    cornerRadius = 8.dp,
                                    offsetX = 2.dp,
                                    offsetY = 2.dp,
                                    shadowColor = colors.ink
                                ),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.cyan,
                                border = BorderStroke(1.5.dp, colors.ink),
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = formatDurationCompact(trimmedDurationMs),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.merge(TabularMetricStyle).copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = colors.ink
                                )
                            }
                        }

                        TrimRangeSlider(
                            durationMs = metadata.durationMs,
                            startMs = trimStartMs,
                            endMs = trimEndMs,
                            onRangeChange = onTrimChange
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatMmSs(trimStartMs),
                                style = MaterialTheme.typography.labelMedium.merge(TabularMetricStyle).copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPositive
                            )
                            Text(
                                text = "Rentang video penuh ${formatMmSs(metadata.durationMs)}",
                                style = MaterialTheme.typography.labelSmall.merge(TabularMetricStyle).copy(
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.sp
                                ),
                                color = colors.textTertiary
                            )
                            Text(
                                text = formatMmSs(trimEndMs),
                                style = MaterialTheme.typography.labelMedium.merge(TabularMetricStyle).copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textTech
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.5.dp, color = colors.divider)

                    // Mute Audio Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleMute(!muteAudio) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeobrutalistIconTile(
                            iconRes = R.drawable.ic_volume_off,
                            size = 34.dp,
                            cornerRadius = 8.dp,
                            iconSize = 17.dp,
                            containerColor = colors.bgRecessed,
                            borderWidth = 1.5.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Bisukan Suara (Mute)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = colors.ink
                            )
                            Text(
                                text = "Hapus audio untuk ukuran lebih kecil",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                ),
                                color = colors.textTertiary
                            )
                        }

                        NeobrutalistSwitch(
                            checked = muteAudio,
                            onCheckedChange = onToggleMute
                        )
                    }
                }
            }

            // Card 3: Target Estimation
            estimate?.let { est ->
                val estRatio = if (metadata.sizeBytes > 0L) {
                    (est.estimatedSizeBytes.toFloat() / metadata.sizeBytes.toFloat()).coerceIn(0.03f, 1f)
                } else {
                    0.25f
                }

                NeobrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp,
                    containerColor = colors.bgMintTint,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TARGET ESTIMASI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = colors.ink
                            )
                            Surface(
                                modifier = Modifier.hardShadow(
                                    cornerRadius = 8.dp,
                                    offsetX = 2.dp,
                                    offsetY = 2.dp,
                                    shadowColor = colors.ink
                                ),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.mint,
                                border = BorderStroke(1.5.dp, colors.ink),
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = "${est.compressionPercentage}% Lebih Kecil",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.merge(TabularMetricStyle).copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = colors.ink
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "~${est.formattedEstimatedSize()}",
                                        style = MaterialTheme.typography.headlineSmall.merge(TabularMetricStyle).copy(
                                            fontFamily = DisplayFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            lineHeight = 25.sp
                                        ),
                                        color = colors.ink
                                    )
                                    Text(
                                        text = "dari ${metadata.formattedSize()}",
                                        style = MaterialTheme.typography.labelMedium.merge(TabularMetricStyle).copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Durasi hasil: ${formatDurationCompact(est.trimmedDurationMs)}",
                                    style = MaterialTheme.typography.labelMedium.merge(TabularMetricStyle),
                                    color = colors.textTech
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = est.shortCodecName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp
                                    ),
                                    color = colors.textTech
                                )
                                Text(
                                    text = "Hardware Encoder",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        NeobrutalistProgressBar(
                            fraction = estRatio,
                            fillColor = colors.mint,
                            trackColor = colors.bgSurface,
                            showCapLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryActionButton(
            label = "Mulai Kompresi Hardware",
            iconRes = R.drawable.ic_bolt,
            onClick = onStartCompression,
            enabled = codecSupport.isSupported(selectedCodec)
        )
    }
}

@Composable
private fun MetadataCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.bgRecessed,
        border = BorderStroke(1.5.dp, colors.ink),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = colors.textTertiary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.merge(TabularMetricStyle).copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CompressingScreen(
    minContentHeight: Dp,
    progress: Int,
    eta: String,
    targetProfile: String,
    onCancel: () -> Unit
) {
    val colors = KomvidThemeTokens.colors
    val cardMinHeight = (minContentHeight - 72.dp).coerceAtLeast(360.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minContentHeight),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardMinHeight),
            cornerRadius = 12.dp,
            containerColor = colors.bgSurface,
            borderWidth = 2.5.dp,
            shadowOffset = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = cardMinHeight)
                    .padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ProgressRing(progress = progress)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeobrutalistCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        containerColor = colors.bgCyanTint,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_clock),
                                    contentDescription = null,
                                    tint = colors.textTech,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ESTIMASI SISA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = colors.textTertiary,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = eta.ifBlank { "--:--" },
                                style = MaterialTheme.typography.titleLarge.merge(TabularMetricStyle).copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp
                                ),
                                color = colors.ink
                            )
                        }
                    }

                    NeobrutalistCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        containerColor = colors.bgYellowTint,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chip),
                                    contentDescription = null,
                                    tint = colors.ink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "PROFIL TARGET",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = colors.textTertiary,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = targetProfile.ifBlank { "Hardware" },
                                style = MaterialTheme.typography.titleSmall.merge(TabularMetricStyle).copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    lineHeight = 24.sp
                                ),
                                color = colors.ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.bgRecessed,
                    border = BorderStroke(2.dp, colors.ink),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeobrutalistIconTile(
                            iconRes = R.drawable.ic_shield_check,
                            size = 32.dp,
                            cornerRadius = 8.dp,
                            iconSize = 18.dp,
                            containerColor = colors.cyan,
                            borderWidth = 1.5.dp
                        )
                        Text(
                            text = "Hardware Encoder aktif di latar belakang. Aman usap ke Home atau kunci layar.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.5.sp
                            ),
                            color = colors.ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SoftDangerButton(
            label = "Batal Kompresi",
            iconRes = R.drawable.ic_close,
            onClick = onCancel
        )
    }
}

@Composable
fun DoneScreen(
    minContentHeight: Dp,
    origSizeMb: Double,
    finalSizeMb: Double,
    timeTaken: String,
    onPlayVideo: () -> Unit,
    onShareVideo: () -> Unit,
    onCompressAnother: () -> Unit
) {
    val colors = KomvidThemeTokens.colors
    val cardMinHeight = (minContentHeight - 178.dp).coerceAtLeast(360.dp)
    val savedMb = (origSizeMb - finalSizeMb).coerceAtLeast(0.0)
    val savedRatio = if (origSizeMb > 0.0) {
        ((savedMb / origSizeMb) * 100.0).coerceIn(0.0, 99.0).toInt()
    } else {
        0
    }
    val compressedFraction = if (origSizeMb > 0.0) {
        (finalSizeMb / origSizeMb).toFloat().coerceIn(0.03f, 1f)
    } else {
        0.2f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minContentHeight),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardMinHeight),
            cornerRadius = 12.dp,
            containerColor = colors.bgSurface,
            borderWidth = 2.5.dp,
            shadowOffset = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = cardMinHeight)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NeobrutalistIconTile(
                    iconRes = R.drawable.ic_check,
                    size = 56.dp,
                    cornerRadius = 12.dp,
                    iconSize = 28.dp,
                    containerColor = colors.mint,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kompresi Selesai!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.hardShadow(
                        cornerRadius = 8.dp,
                        offsetX = 2.5.dp,
                        offsetY = 2.5.dp,
                        shadowColor = colors.ink
                    ),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.yellow,
                    border = BorderStroke(2.dp, colors.ink),
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = "Hemat $savedRatio% Ruang (${formatMbOrGb(savedMb)})",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge.merge(TabularMetricStyle).copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.ink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Before -> After Comparison Box with Dual Proportional Bars
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.bgRecessed,
                    border = BorderStroke(2.dp, colors.ink),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "UKURAN AWAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary
                                )
                                Text(
                                    text = formatMbOrGb(origSizeMb),
                                    style = MaterialTheme.typography.titleMedium.merge(TabularMetricStyle).copy(
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        lineHeight = 22.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }

                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_right),
                                contentDescription = null,
                                tint = colors.ink,
                                modifier = Modifier.size(20.dp)
                            )

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "HASIL AKHIR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textPositive
                                )
                                Text(
                                    text = formatMbOrGb(finalSizeMb),
                                    style = MaterialTheme.typography.titleLarge.merge(TabularMetricStyle).copy(
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        lineHeight = 24.sp
                                    ),
                                    color = colors.textPositive
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NeobrutalistProgressBar(
                                fraction = 1f,
                                fillColor = colors.barTrackOriginal,
                                trackColor = colors.barTrackOriginal,
                                showCapLine = false
                            )
                            NeobrutalistProgressBar(
                                fraction = compressedFraction,
                                fillColor = colors.mint,
                                trackColor = colors.bgSurface,
                                showCapLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Grid: WAKTU PROSES & DISIMPAN KE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeobrutalistCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        containerColor = colors.bgYellowTint,
                        borderWidth = 2.dp,
                        shadowOffset = 2.5.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "WAKTU PROSES",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary
                            )
                            Text(
                                text = timeTaken.ifBlank { "00m 00s" },
                                style = MaterialTheme.typography.titleSmall.merge(TabularMetricStyle).copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    lineHeight = 17.sp
                                ),
                                color = colors.ink
                            )
                        }
                    }

                    NeobrutalistCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        containerColor = colors.bgCyanTint,
                        borderWidth = 2.dp,
                        shadowOffset = 2.5.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "DISIMPAN KE",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary
                            )
                            Text(
                                text = "Movies/Komvid",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                ),
                                color = colors.textTech,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-Tier Action Stack
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryActionButton(
                label = "Putar Video Hasil",
                iconRes = R.drawable.ic_play,
                onClick = onPlayVideo
            )
            SecondaryActionButton(
                label = "Bagikan Video",
                iconRes = R.drawable.ic_share,
                onClick = onShareVideo
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCompressAnother)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kompres Video Lainnya",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = colors.ink
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    minContentHeight: Dp,
    errorMessage: String,
    onRetry: () -> Unit,
    onPickAnother: () -> Unit
) {
    val colors = KomvidThemeTokens.colors
    val cardMinHeight = (minContentHeight - 130.dp).coerceAtLeast(360.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minContentHeight),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardMinHeight),
            cornerRadius = 12.dp,
            containerColor = colors.bgSurface,
            borderWidth = 2.5.dp,
            shadowOffset = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = cardMinHeight)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NeobrutalistIconTile(
                    iconRes = R.drawable.ic_alert,
                    size = 58.dp,
                    cornerRadius = 12.dp,
                    iconSize = 28.dp,
                    containerColor = colors.coral,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Kompresi Gagal Diproses",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Hardware encoder gagal memproses\nberkas video yang dipilih.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Technical Detail Error Monospace Box
                NeobrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    containerColor = colors.bgCoralTint,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DETAIL ERROR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = colors.textDanger
                        )
                        Text(
                            text = errorMessage,
                            style = MonoErrorStyle,
                            color = colors.ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Recovery Suggestions Box
                NeobrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    containerColor = colors.bgYellowTint,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SARAN PERBAIKAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = colors.ink
                        )
                        Text(
                            text = "• Turunkan target resolusi ke 720p HD atau coba codec H.264 lalu coba lagi.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = colors.ink
                        )
                        Text(
                            text = "• Pastikan ruang penyimpanan internal masih mencukupi.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = colors.ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryActionButton(
                label = "Coba Lagi",
                iconRes = R.drawable.ic_refresh,
                onClick = onRetry
            )
            SecondaryActionButton(
                label = "Pilih Video Lain",
                iconRes = R.drawable.ic_folder,
                onClick = onPickAnother
            )
        }
    }
}
