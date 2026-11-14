package com.nade.komvid.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nade.komvid.R
import com.nade.komvid.ui.theme.DisplayFontFamily
import com.nade.komvid.ui.theme.InkBlack
import com.nade.komvid.ui.theme.KomvidThemeTokens
import com.nade.komvid.ui.theme.TabularMetricStyle
import kotlin.math.abs
import kotlin.math.roundToLong

fun Modifier.hardShadow(
    cornerRadius: Dp,
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    shadowColor: Color = InkBlack
): Modifier = this.drawBehind {
    val radiusPx = cornerRadius.toPx()
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size,
        cornerRadius = CornerRadius(radiusPx, radiusPx)
    )
}

@Composable
fun NeobrutalistCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    containerColor: Color = KomvidThemeTokens.colors.bgSurface,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = KomvidThemeTokens.colors
    Surface(
        modifier = modifier.hardShadow(
            cornerRadius = cornerRadius,
            offsetX = shadowOffset,
            offsetY = shadowOffset,
            shadowColor = colors.ink
        ),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(borderWidth, colors.ink),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(content = content)
    }
}

@Composable
fun NeobrutalistIconTile(
    @DrawableRes iconRes: Int,
    size: Dp,
    cornerRadius: Dp,
    iconSize: Dp,
    containerColor: Color,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.5.dp,
    shadowOffset: Dp = 0.dp,
    iconTint: Color = KomvidThemeTokens.colors.ink
) {
    val colors = KomvidThemeTokens.colors
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = if (shadowOffset > 0.dp) {
        modifier
            .size(size)
            .hardShadow(
                cornerRadius = cornerRadius,
                offsetX = shadowOffset,
                offsetY = shadowOffset,
                shadowColor = colors.ink
            )
    } else {
        modifier.size(size)
    }
    Box(
        modifier = baseModifier
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, colors.ink, shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun NeobrutalistProgressBar(
    fraction: Float,
    fillColor: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = KomvidThemeTokens.colors.bgSurface,
    showCapLine: Boolean = true
) {
    val colors = KomvidThemeTokens.colors
    val clamped = fraction.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(8.dp)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(shape)
            .background(trackColor)
            .border(2.dp, colors.ink, shape)
    ) {
        val fillWidth = size.width * clamped
        if (fillWidth > 0f) {
            drawRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(fillWidth, size.height)
            )
            if (showCapLine && clamped < 0.99f) {
                val capWidth = 2.dp.toPx()
                val capX = (fillWidth - capWidth / 2f).coerceIn(0f, size.width - capWidth)
                drawRect(
                    color = colors.ink,
                    topLeft = Offset(capX, 0f),
                    size = Size(capWidth, size.height)
                )
            }
        }
    }
}

@Composable
fun AppHeader(
    isCompressing: Boolean,
    versionName: String,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .hardShadow(cornerRadius = 10.dp, offsetX = 2.5.dp, offsetY = 2.5.dp, shadowColor = colors.ink)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.yellow)
                    .border(2.2.dp, colors.ink, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_brand_compress),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Komvid",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.4).sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = "Hardware Video Compressor",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = colors.textTertiary
                )
            }
        }

        if (isCompressing) {
            Surface(
                modifier = Modifier.hardShadow(
                    cornerRadius = 8.dp,
                    offsetX = 2.5.dp,
                    offsetY = 2.5.dp,
                    shadowColor = colors.ink
                ),
                shape = RoundedCornerShape(8.dp),
                color = colors.cyan,
                border = BorderStroke(2.dp, colors.ink),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.ink)
                    )
                    Text(
                        text = "AKTIF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            lineHeight = 13.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = colors.ink
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.hardShadow(
                    cornerRadius = 8.dp,
                    offsetX = 2.dp,
                    offsetY = 2.dp,
                    shadowColor = colors.ink
                ),
                shape = RoundedCornerShape(8.dp),
                color = colors.bgSurface,
                border = BorderStroke(2.dp, colors.ink),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = "v$versionName",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium.merge(TabularMetricStyle).copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.ink
                )
            }
        }
    }
}

@Composable
private fun NeobrutalistButton(
    label: String,
    @DrawableRes iconRes: Int,
    containerColor: Color,
    contentColor: Color,
    height: Dp,
    textStyle: androidx.compose.ui.text.TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = KomvidThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressShift by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 70),
        label = "neoButtonPress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(if (enabled) 1f else 0.52f)
            .hardShadow(
                cornerRadius = 10.dp,
                offsetX = 4.dp,
                offsetY = 4.dp,
                shadowColor = colors.ink
            )
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(x = pressShift, y = pressShift),
            shape = RoundedCornerShape(10.dp),
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(2.5.dp, colors.ink),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = label,
                    style = textStyle,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PrimaryActionButton(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = KomvidThemeTokens.colors
    NeobrutalistButton(
        label = label,
        iconRes = iconRes,
        containerColor = colors.mint,
        contentColor = colors.ink,
        height = 52.dp,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.ExtraBold
        ),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun SecondaryActionButton(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    NeobrutalistButton(
        label = label,
        iconRes = iconRes,
        containerColor = colors.bgSurface,
        contentColor = colors.ink,
        height = 48.dp,
        textStyle = MaterialTheme.typography.titleSmall.copy(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold
        ),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun SoftDangerButton(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    NeobrutalistButton(
        label = label,
        iconRes = iconRes,
        containerColor = colors.bgCoralTint,
        contentColor = colors.textDanger,
        height = 52.dp,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.ExtraBold
        ),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun NeobrutalistSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.mint else colors.bgRecessed,
        animationSpec = tween(durationMillis = 160),
        label = "switchTrackColor"
    )
    val thumbOffsetX by animateDpAsState(
        targetValue = if (checked) 25.dp else 5.dp,
        animationSpec = tween(durationMillis = 160),
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(trackColor)
            .border(2.dp, colors.ink, RoundedCornerShape(8.dp))
            .clickable(role = Role.Switch) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffsetX)
                .size(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.bgSurface)
                .border(2.dp, colors.ink, RoundedCornerShape(6.dp))
        )
    }
}

@Composable
fun ProgressRing(
    progress: Int,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    val clampedProgress = progress.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress / 100f,
        animationSpec = tween(durationMillis = 350),
        label = "compressProgress"
    )

    Box(
        modifier = modifier.size(182.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(182.dp)) {
            val borderStrokeWidth = 25.2.dp.toPx()
            val trackStrokeWidth = 20.4.dp.toPx()
            val centerlineRadius = 71.3.dp.toPx()
            val shadowOffset = Offset(5.dp.toPx(), 5.dp.toPx())
            val arcDiameter = centerlineRadius * 2f
            val arcTopLeft = Offset(
                center.x - centerlineRadius,
                center.y - centerlineRadius
            )
            val arcSize = Size(arcDiameter, arcDiameter)

            // Offset hard shadow ring (blur = 0)
            drawCircle(
                color = colors.ink,
                radius = centerlineRadius,
                center = center + shadowOffset,
                style = Stroke(width = borderStrokeWidth)
            )

            // Outer + inner ink border ring
            drawCircle(
                color = colors.ink,
                radius = centerlineRadius,
                center = center,
                style = Stroke(width = borderStrokeWidth)
            )

            // Recessed sand track ring
            drawCircle(
                color = colors.bgRecessed,
                radius = centerlineRadius,
                center = center,
                style = Stroke(width = trackStrokeWidth)
            )

            // Solid Signal Mint progress arc starting at 12 o'clock
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    color = colors.mint,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = trackStrokeWidth, cap = StrokeCap.Butt)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$clampedProgress%",
                style = MaterialTheme.typography.displaySmall,
                color = colors.ink,
                textAlign = TextAlign.Center
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.yellow,
                border = BorderStroke(1.5.dp, colors.ink),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = "MENGOMPRESI",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TrimRangeSlider(
    durationMs: Long,
    startMs: Long,
    endMs: Long,
    onRangeChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KomvidThemeTokens.colors
    val safeDuration = durationMs.coerceAtLeast(1L)
    val minGapMs = minOf(1000L, safeDuration)

    val latestDuration by rememberUpdatedState(safeDuration)
    val latestStart by rememberUpdatedState(startMs.coerceIn(0L, safeDuration))
    val latestEnd by rememberUpdatedState(endMs.coerceIn(0L, safeDuration))
    val latestCallback by rememberUpdatedState(onRangeChange)

    val enabled = safeDuration > 1000L

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .pointerInput(enabled, safeDuration) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val thumbPad = 10.dp.toPx()
                    val trackWidth = (size.width - thumbPad * 2f).coerceAtLeast(1f)

                    fun xToMs(x: Float): Long {
                        val frac = ((x - thumbPad) / trackWidth).coerceIn(0f, 1f)
                        val raw = (frac * latestDuration).roundToLong()
                        return when {
                            frac <= 0.015f || raw <= 400L -> 0L
                            frac >= 0.985f || raw >= latestDuration - 400L -> latestDuration
                            else -> ((raw + 500L) / 1000L) * 1000L
                        }.coerceIn(0L, latestDuration)
                    }

                    val startFrac = latestStart.toFloat() / latestDuration.toFloat()
                    val endFrac = latestEnd.toFloat() / latestDuration.toFloat()
                    val startX = thumbPad + startFrac * trackWidth
                    val endX = thumbPad + endFrac * trackWidth

                    var draggingStart = abs(down.position.x - startX) <= abs(down.position.x - endX)
                    if (abs(startX - endX) < 4f && down.position.x > endX) {
                        draggingStart = false
                    }

                    val initialTarget = xToMs(down.position.x)
                    if (draggingStart) {
                        val clampedStart = initialTarget.coerceIn(0L, (latestEnd - minGapMs).coerceAtLeast(0L))
                        latestCallback(clampedStart, latestEnd)
                    } else {
                        val clampedEnd = initialTarget.coerceIn(
                            (latestStart + minGapMs).coerceAtMost(latestDuration),
                            latestDuration
                        )
                        latestCallback(latestStart, clampedEnd)
                    }
                    down.consume()

                    drag(down.id) { change ->
                        change.consume()
                        val targetMs = xToMs(change.position.x)
                        if (draggingStart) {
                            val clampedStart = targetMs.coerceIn(0L, (latestEnd - minGapMs).coerceAtLeast(0L))
                            latestCallback(clampedStart, latestEnd)
                        } else {
                            val clampedEnd = targetMs.coerceIn(
                                (latestStart + minGapMs).coerceAtMost(latestDuration),
                                latestDuration
                            )
                            latestCallback(latestStart, clampedEnd)
                        }
                    }
                }
            }
    ) {
        val thumbW = 20.dp.toPx()
        val thumbH = 22.dp.toPx()
        val thumbRadius = 8.dp.toPx()
        val borderPx = 2.dp.toPx()
        val shadowOffset = 2.dp.toPx()

        val trackHeight = 10.dp.toPx()
        val trackCorner = 6.dp.toPx()
        val centerY = size.height / 2f
        val trackTop = centerY - trackHeight / 2f
        val trackLeft = thumbW / 2f
        val trackRight = (size.width - thumbW / 2f).coerceAtLeast(trackLeft + 1f)
        val trackWidth = trackRight - trackLeft

        val startFraction = (startMs.coerceIn(0L, safeDuration).toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
        val endFraction = (endMs.coerceIn(0L, safeDuration).toFloat() / safeDuration.toFloat()).coerceIn(startFraction, 1f)

        val startCenterX = trackLeft + startFraction * trackWidth
        val endCenterX = trackLeft + endFraction * trackWidth

        // Recessed Warm Sand Track with Ink Border
        drawRoundRect(
            color = colors.bgRecessed,
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackCorner, trackCorner)
        )
        drawRoundRect(
            color = colors.ink,
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackCorner, trackCorner),
            style = Stroke(width = borderPx)
        )

        // Active Signal Mint Range
        if (endCenterX > startCenterX) {
            drawRect(
                color = colors.mint,
                topLeft = Offset(startCenterX, trackTop),
                size = Size(endCenterX - startCenterX, trackHeight)
            )
            drawLine(
                color = colors.ink,
                start = Offset(startCenterX, trackTop),
                end = Offset(endCenterX, trackTop),
                strokeWidth = borderPx
            )
            drawLine(
                color = colors.ink,
                start = Offset(startCenterX, trackTop + trackHeight),
                end = Offset(endCenterX, trackTop + trackHeight),
                strokeWidth = borderPx
            )
        }

        fun drawHandle(centerX: Float, fillColor: Color) {
            val left = centerX - thumbW / 2f
            val top = centerY - thumbH / 2f
            val thumbSize = Size(thumbW, thumbH)
            val corner = CornerRadius(thumbRadius, thumbRadius)

            // Hard shadow offset (2, 2)
            drawRoundRect(
                color = colors.ink,
                topLeft = Offset(left + shadowOffset, top + shadowOffset),
                size = thumbSize,
                cornerRadius = corner
            )
            // Solid fill
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(left, top),
                size = thumbSize,
                cornerRadius = corner
            )
            // Crisp border
            drawRoundRect(
                color = colors.ink,
                topLeft = Offset(left, top),
                size = thumbSize,
                cornerRadius = corner,
                style = Stroke(width = borderPx)
            )
            // Vertical tactile grip bar
            val gripW = 2.4.dp.toPx()
            val gripH = 8.dp.toPx()
            drawRoundRect(
                color = colors.ink,
                topLeft = Offset(centerX - gripW / 2f, centerY - gripH / 2f),
                size = Size(gripW, gripH),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }

        drawHandle(startCenterX, colors.yellow)
        drawHandle(endCenterX, colors.cyan)
    }
}
