package com.nade.komvid.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Softened Neobrutalism "Drafting Cream & Signal Mint" palette tokens
val BgPageCream = Color(0xFFF5F0E4)
val BgSurfaceWhite = Color(0xFFFFFFFF)
val BgRecessedSand = Color(0xFFEDE6D6)
val BgDropzoneCream = Color(0xFFFAF7EE)
val DividerCream = Color(0xFFD5CCB8)
val BarTrackOriginal = Color(0xFFD8D0BE)

val InkBlack = Color(0xFF141414)

val SignalMint = Color(0xFF2EE59D)
val VoltYellow = Color(0xFFFFE14D)
val ElectricSky = Color(0xFF56D4FF)
val SignalCoral = Color(0xFFFF6B6B)

val BgMintTint = Color(0xFFD8FCEB)
val BgCyanTint = Color(0xFFDDF6FF)
val BgYellowTint = Color(0xFFFFF6C2)
val BgCoralTint = Color(0xFFFFE4E4)

val TextPrimaryInk = Color(0xFF141414)
val TextSecondaryCharcoal = Color(0xFF44403C)
val TextTertiaryStone = Color(0xFF57534E)
val TextPositiveEmerald = Color(0xFF047857)
val TextTechBlue = Color(0xFF0369A1)
val TextDangerRed = Color(0xFFB91C1C)

@Immutable
data class KomvidExtendedColors(
    val bgPage: Color = BgPageCream,
    val bgSurface: Color = BgSurfaceWhite,
    val bgRecessed: Color = BgRecessedSand,
    val bgDropzone: Color = BgDropzoneCream,
    val divider: Color = DividerCream,
    val barTrackOriginal: Color = BarTrackOriginal,
    val ink: Color = InkBlack,
    val mint: Color = SignalMint,
    val yellow: Color = VoltYellow,
    val cyan: Color = ElectricSky,
    val coral: Color = SignalCoral,
    val bgMintTint: Color = BgMintTint,
    val bgCyanTint: Color = BgCyanTint,
    val bgYellowTint: Color = BgYellowTint,
    val bgCoralTint: Color = BgCoralTint,
    val textPrimary: Color = TextPrimaryInk,
    val textSecondary: Color = TextSecondaryCharcoal,
    val textTertiary: Color = TextTertiaryStone,
    val textPositive: Color = TextPositiveEmerald,
    val textTech: Color = TextTechBlue,
    val textDanger: Color = TextDangerRed
)

val LocalKomvidColors = staticCompositionLocalOf { KomvidExtendedColors() }
