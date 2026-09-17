package com.example.klimata.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import java.util.Calendar

enum class DiurnalPhase { DAY, EVENING, NIGHT }

fun currentMinuteOfDay(): Int {
    val calendar = Calendar.getInstance()
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    return (hour * 60) + minute
}

fun currentDiurnalPhase(minuteOfDay: Int = currentMinuteOfDay()): DiurnalPhase = when (minuteOfDay) {
    in 420..989 -> DiurnalPhase.DAY       // 07:00 – 16:29
    in 990..1139 -> DiurnalPhase.EVENING  // 16:30 – 18:59
    else -> DiurnalPhase.NIGHT            // 19:00 – 06:59
}

private data class DiurnalKeyframe(
    val minute: Int,
    val stops: List<Color>,
    val celestialBloom: Color,
    val orbitAura: Color,
    val accentColor: Color,
    val accentGlow: Color,
    val deckSurface: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val dockBackground: Color,
    val frostedCardBackground: Color,
    val isDark: Boolean
)

private val DIURNAL_KEYFRAMES = listOf(
    // 00:00 (Midnight)
    DiurnalKeyframe(
        minute = 0,
        stops = listOf(Color(0xFF070B14), Color(0xFF0C1220), Color(0xFF10192C), Color(0xFF16233B), Color(0xFF1D2E4D)),
        celestialBloom = Color(0x3D818CF8),
        orbitAura = Color(0xFF818CF8),
        accentColor = Color(0xFFA5ACF0),
        accentGlow = Color(0xFFC7D2FE),
        deckSurface = Color(0xFF0B1120),
        cardSurface = Color(0xFF28304C),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xF2020617),
        frostedCardBackground = Color(0x24FFFFFF),
        isDark = true
    ),
    // 04:30 (Astronomical Twilight)
    DiurnalKeyframe(
        minute = 270,
        stops = listOf(Color(0xFF090E1B), Color(0xFF0E162B), Color(0xFF15223E), Color(0xFF1D3054), Color(0xFF263D6B)),
        celestialBloom = Color(0x35818CF8),
        orbitAura = Color(0xFF818CF8),
        accentColor = Color(0xFFA5B4FC),
        accentGlow = Color(0xFFC7D2FE),
        deckSurface = Color(0xFF0B1120),
        cardSurface = Color(0xFF28304C),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xF2020617),
        frostedCardBackground = Color(0x24FFFFFF),
        isDark = true
    ),
    // 05:45 (Civil Dawn)
    DiurnalKeyframe(
        minute = 345,
        stops = listOf(Color(0xFF101730), Color(0xFF1A2648), Color(0xFF2B3A65), Color(0xFF484F77), Color(0xFF756A82)),
        celestialBloom = Color(0x40A5B4FC),
        orbitAura = Color(0xFFA5B4FC),
        accentColor = Color(0xFF818CF8),
        accentGlow = Color(0xFFA5B4FC),
        deckSurface = Color(0xFF101730),
        cardSurface = Color(0xFF283452),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xEB101730),
        frostedCardBackground = Color(0x30FFFFFF),
        isDark = true
    ),
    // 06:30 (Sunrise / First Light)
    DiurnalKeyframe(
        minute = 390,
        stops = listOf(Color(0xFF1E3A68), Color(0xFF2E5288), Color(0xFF5A6E8C), Color(0xFFA67B73), Color(0xFFDF9B7A)),
        celestialBloom = Color(0x55FDBA74),
        orbitAura = Color(0xFFF59E0B),
        accentColor = Color(0xFFEA580C),
        accentGlow = Color(0xFFFB923C),
        deckSurface = Color(0xFFFAFAF9),
        cardSurface = Color(0xFFF5F5F4),
        cardBorder = Color(0x28FFFFFF),
        textPrimary = Color(0xFF1C1917),
        textSecondary = Color(0xFF78716C),
        textTertiary = Color(0xFFA8A29E),
        dockBackground = Color(0xEB1C1917),
        frostedCardBackground = Color(0x381C1917),
        isDark = false
    ),
    // 07:30 (Golden Morning)
    DiurnalKeyframe(
        minute = 450,
        stops = listOf(Color(0xFF1B4FA8), Color(0xFF2566C8), Color(0xFF3B81E2), Color(0xFF5D9EF0), Color(0xFF8CBEF4)),
        celestialBloom = Color(0x45FFEA79),
        orbitAura = Color(0xFF38BDF8),
        accentColor = Color(0xFF0284C7),
        accentGlow = Color(0xFF38BDF8),
        deckSurface = Color(0xFFFFFFFF),
        cardSurface = Color(0xFFF8FAFC),
        cardBorder = Color(0xFFEEF2F6),
        textPrimary = Color(0xFF090D16),
        textSecondary = Color(0xFF64748B),
        textTertiary = Color(0xFF94A3B8),
        dockBackground = Color(0xEB0F172A),
        frostedCardBackground = Color(0x4D0F172A),
        isDark = false
    ),
    // 11:30 (Solar Noon / Peak Day)
    DiurnalKeyframe(
        minute = 690,
        stops = listOf(Color(0xFF1550B8), Color(0xFF1E60CE), Color(0xFF3275E4), Color(0xFF4A8EF4), Color(0xFF6BA4F8)),
        celestialBloom = Color(0x3DFFEA79),
        orbitAura = Color(0xFF38BDF8),
        accentColor = Color(0xFF0284C7),
        accentGlow = Color(0xFF38BDF8),
        deckSurface = Color(0xFFFFFFFF),
        cardSurface = Color(0xFFF8FAFC),
        cardBorder = Color(0xFFEEF2F6),
        textPrimary = Color(0xFF090D16),
        textSecondary = Color(0xFF64748B),
        textTertiary = Color(0xFF94A3B8),
        dockBackground = Color(0xEB0F172A),
        frostedCardBackground = Color(0x4D0F172A),
        isDark = false
    ),
    // 15:30 (Afternoon Sun)
    DiurnalKeyframe(
        minute = 930,
        stops = listOf(Color(0xFF174CBE), Color(0xFF225ED0), Color(0xFF3878E4), Color(0xFF5292F4), Color(0xFF72A6F8)),
        celestialBloom = Color(0x3DFFEA79),
        orbitAura = Color(0xFF38BDF8),
        accentColor = Color(0xFF0284C7),
        accentGlow = Color(0xFF38BDF8),
        deckSurface = Color(0xFFFFFFFF),
        cardSurface = Color(0xFFF8FAFC),
        cardBorder = Color(0xFFEEF2F6),
        textPrimary = Color(0xFF090D16),
        textSecondary = Color(0xFF64748B),
        textTertiary = Color(0xFF94A3B8),
        dockBackground = Color(0xEB0F172A),
        frostedCardBackground = Color(0x4D0F172A),
        isDark = false
    ),
    // 16:45 (Pre-Sunset Warmth)
    DiurnalKeyframe(
        minute = 1005,
        stops = listOf(Color(0xFF173F94), Color(0xFF234EA8), Color(0xFF3B67B8), Color(0xFF607FB8), Color(0xFF9096A8)),
        celestialBloom = Color(0x45FBBF24),
        orbitAura = Color(0xFFF59E0B),
        accentColor = Color(0xFFEA580C),
        accentGlow = Color(0xFFF97316),
        deckSurface = Color(0xFFFAFAF9),
        cardSurface = Color(0xFFF5F5F4),
        cardBorder = Color(0x28FFFFFF),
        textPrimary = Color(0xFF1C1917),
        textSecondary = Color(0xFF78716C),
        textTertiary = Color(0xFFA8A29E),
        dockBackground = Color(0xEB1C1917),
        frostedCardBackground = Color(0x381C1917),
        isDark = false
    ),
    // 17:45 (Golden Hour / Sunset)
    DiurnalKeyframe(
        minute = 1065,
        stops = listOf(Color(0xFF1A1F52), Color(0xFF3D1F50), Color(0xFF782548), Color(0xFFC64222), Color(0xFFEA8610)),
        celestialBloom = Color(0x52FB923C),
        orbitAura = Color(0xFFF59E0B),
        accentColor = Color(0xFFF97316),
        accentGlow = Color(0xFFFB923C),
        deckSurface = Color(0xFFFAFAF9),
        cardSurface = Color(0xFFF5F5F4),
        cardBorder = Color(0x28FFFFFF),
        textPrimary = Color(0xFF1C1917),
        textSecondary = Color(0xFF78716C),
        textTertiary = Color(0xFFA8A29E),
        dockBackground = Color(0xEB1C1917),
        frostedCardBackground = Color(0x381C1917),
        isDark = false
    ),
    // 18:45 (Twilight / Blue Hour)
    DiurnalKeyframe(
        minute = 1125,
        stops = listOf(Color(0xFF121536), Color(0xFF1A1D4E), Color(0xFF262664), Color(0xFF373270), Color(0xFF4C3E68)),
        celestialBloom = Color(0x35818CF8),
        orbitAura = Color(0xFF818CF8),
        accentColor = Color(0xFFA5ACF0),
        accentGlow = Color(0xFFC7D2FE),
        deckSurface = Color(0xFF0B1120),
        cardSurface = Color(0xFF24263E),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xF2020617),
        frostedCardBackground = Color(0x28FFFFFF),
        isDark = true
    ),
    // 20:00 (Night Fall)
    DiurnalKeyframe(
        minute = 1200,
        stops = listOf(Color(0xFF0A0F1E), Color(0xFF0F172E), Color(0xFF15223E), Color(0xFF1C2C4E), Color(0xFF24365E)),
        celestialBloom = Color(0x3D818CF8),
        orbitAura = Color(0xFF818CF8),
        accentColor = Color(0xFFA5ACF0),
        accentGlow = Color(0xFFC7D2FE),
        deckSurface = Color(0xFF0B1120),
        cardSurface = Color(0xFF28304C),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xF2020617),
        frostedCardBackground = Color(0x24FFFFFF),
        isDark = true
    ),
    // 24:00 (Wraparound Midnight)
    DiurnalKeyframe(
        minute = 1440,
        stops = listOf(Color(0xFF070B14), Color(0xFF0C1220), Color(0xFF10192C), Color(0xFF16233B), Color(0xFF1D2E4D)),
        celestialBloom = Color(0x3D818CF8),
        orbitAura = Color(0xFF818CF8),
        accentColor = Color(0xFFA5ACF0),
        accentGlow = Color(0xFFC7D2FE),
        deckSurface = Color(0xFF0B1120),
        cardSurface = Color(0xFF28304C),
        cardBorder = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textTertiary = Color(0xFF64748B),
        dockBackground = Color(0xF2020617),
        frostedCardBackground = Color(0x24FFFFFF),
        isDark = true
    )
)

fun dynamicDiurnalColors(minuteOfDay: Int): DiurnalColors {
    val m = ((minuteOfDay % 1440) + 1440) % 1440
    val kf = DIURNAL_KEYFRAMES
    var i = 0
    while (i < kf.size - 1 && kf[i + 1].minute <= m) {
        i++
    }
    val k1 = kf[i]
    val k2 = if (i + 1 < kf.size) kf[i + 1] else kf.first()
    val span = if (k2.minute > k1.minute) (k2.minute - k1.minute) else (1440 - k1.minute)
    val fraction = if (span > 0) ((m - k1.minute).toFloat() / span.toFloat()).coerceIn(0f, 1f) else 0f

    val interpolatedStops = (0 until 5).map { idx ->
        lerp(k1.stops[idx], k2.stops[idx], fraction)
    }

    val accent = lerp(k1.accentColor, k2.accentColor, fraction)
    val accentGlow = lerp(k1.accentGlow, k2.accentGlow, fraction)
    val bloom = lerp(k1.celestialBloom, k2.celestialBloom, fraction)
    val orbitAura = lerp(k1.orbitAura, k2.orbitAura, fraction)
    val deckSurface = lerp(k1.deckSurface, k2.deckSurface, fraction)
    val cardSurface = lerp(k1.cardSurface, k2.cardSurface, fraction)
    val cardBorder = lerp(k1.cardBorder, k2.cardBorder, fraction)
    val textPrimary = lerp(k1.textPrimary, k2.textPrimary, fraction)
    val textSecondary = lerp(k1.textSecondary, k2.textSecondary, fraction)
    val textTertiary = lerp(k1.textTertiary, k2.textTertiary, fraction)
    val dockBackground = lerp(k1.dockBackground, k2.dockBackground, fraction)
    val frostedCard = lerp(k1.frostedCardBackground, k2.frostedCardBackground, fraction)
    val onAccent = if (k1.isDark || k2.isDark) Color(0xFF0F172A) else Color.White

    return DiurnalColors(
        skyGradient = Brush.verticalGradient(interpolatedStops),
        celestialBloom = bloom,
        celestialCoreColor = lerp(k1.stops[4], Color.White, 0.7f),
        celestialCoronaColor = bloom,
        celestialOuterHalo = bloom.copy(alpha = bloom.alpha * 0.5f),
        showsStars = k1.isDark || k2.isDark,
        cloudTint = if (k1.isDark || k2.isDark) Color(0xFF94A3B8) else Color.White,
        orbitAura = orbitAura,
        accentColor = accent,
        accentGlow = accentGlow,
        accentMuted = accent.copy(alpha = 0.16f),
        accentGradient = Brush.horizontalGradient(listOf(accent, accentGlow)),
        deckSurface = deckSurface,
        cardSurface = cardSurface,
        cardBorder = cardBorder,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        dockBackground = dockBackground,
        dockBorder = if (k1.isDark || k2.isDark) darkGlassBorderBrush() else glassBorderBrush(),
        frostedCardBackground = frostedCard,
        heroTempGradient = HeroTempGradientBrush,
        onSkyPrimary = OnSkyPrimary,
        onSkySecondary = OnSkySecondary,
        onSkyMuted = OnSkyMuted,
        onAccent = onAccent
    )
}

@Immutable
data class DiurnalColors(
    val skyGradient: Brush,
    val celestialBloom: Color,
    val celestialCoreColor: Color,
    val celestialCoronaColor: Color,
    val celestialOuterHalo: Color,
    val showsStars: Boolean,
    val cloudTint: Color,
    val orbitAura: Color,
    val accentColor: Color,
    val accentGlow: Color,
    val accentMuted: Color,
    val accentGradient: Brush,
    val deckSurface: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val dockBackground: Color,
    val dockBorder: Brush,
    val frostedCardBackground: Color = FrostedCardBackground,
    val heroTempGradient: Brush = HeroTempGradientBrush,
    val onSkyPrimary: Color = OnSkyPrimary,
    val onSkySecondary: Color = OnSkySecondary,
    val onSkyMuted: Color = OnSkyMuted,
    val onAccent: Color = Color.White,
)

val LocalDiurnalColors = staticCompositionLocalOf {
    dynamicDiurnalColors(12 * 60)
}

val LocalMinuteOfDay = staticCompositionLocalOf<Int?> {
    null
}

fun diurnalColorsFor(phase: DiurnalPhase): DiurnalColors = when (phase) {
    DiurnalPhase.DAY -> dynamicDiurnalColors(12 * 60)
    DiurnalPhase.EVENING -> dynamicDiurnalColors(17 * 60 + 45)
    DiurnalPhase.NIGHT -> dynamicDiurnalColors(22 * 60)
}

private fun makeLightKlimataScheme(diurnal: DiurnalColors) = lightColorScheme(
    primary = diurnal.accentColor,
    onPrimary = diurnal.onAccent,
    surface = diurnal.deckSurface,
    onSurface = diurnal.textPrimary,
    surfaceVariant = diurnal.cardSurface,
    onSurfaceVariant = diurnal.textSecondary,
)

private fun makeDarkKlimataScheme(diurnal: DiurnalColors) = darkColorScheme(
    primary = diurnal.accentColor,
    onPrimary = diurnal.onAccent,
    surface = diurnal.deckSurface,
    onSurface = diurnal.textPrimary,
    surfaceVariant = diurnal.cardSurface,
    onSurfaceVariant = diurnal.textSecondary,
)

@Composable
fun KlimataTheme(
    phase: DiurnalPhase = currentDiurnalPhase(),
    minuteOfDay: Int? = null,
    content: @Composable () -> Unit
) {
    val effectiveMinute = minuteOfDay ?: when (phase) {
        DiurnalPhase.DAY -> if (currentDiurnalPhase() == DiurnalPhase.DAY) currentMinuteOfDay() else 12 * 60
        DiurnalPhase.EVENING -> if (currentDiurnalPhase() == DiurnalPhase.EVENING) currentMinuteOfDay() else 17 * 60 + 45
        DiurnalPhase.NIGHT -> if (currentDiurnalPhase() == DiurnalPhase.NIGHT) currentMinuteOfDay() else 22 * 60
    }
    val diurnalColors = remember(effectiveMinute) {
        dynamicDiurnalColors(effectiveMinute)
    }
    val effectivePhase = currentDiurnalPhase(effectiveMinute)
    val colorScheme = when (effectivePhase) {
        DiurnalPhase.NIGHT -> makeDarkKlimataScheme(diurnalColors)
        else -> makeLightKlimataScheme(diurnalColors)
    }

    CompositionLocalProvider(
        LocalDiurnalColors provides diurnalColors,
        LocalMinuteOfDay provides effectiveMinute
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = KlimataShapes,
            content = content
        )
    }
}