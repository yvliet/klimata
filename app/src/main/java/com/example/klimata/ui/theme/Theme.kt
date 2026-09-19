package com.example.klimata.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class DiurnalPhase { DAY, EVENING, NIGHT }

fun currentDiurnalPhase(): DiurnalPhase {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar[java.util.Calendar.HOUR_OF_DAY]
    val minute = calendar[java.util.Calendar.MINUTE]
    return when ((hour * 60) + minute) {
        in 420..989 -> DiurnalPhase.DAY       // 07:00 – 16:29
        in 990..1139 -> DiurnalPhase.EVENING  // 16:30 – 18:59
        else -> DiurnalPhase.NIGHT            // 19:00 – 06:59
    }
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
)

val LocalDiurnalColors = staticCompositionLocalOf {
    diurnalColorsFor(DiurnalPhase.DAY)
}

fun diurnalColorsFor(phase: DiurnalPhase): DiurnalColors = when (phase) {
    DiurnalPhase.DAY -> DiurnalColors(
        skyGradient = Brush.verticalGradient(
            listOf(DaySkyStop1, DaySkyStop2, DaySkyStop3, DaySkyStop4, DaySkyStop5),
        ),
        celestialBloom = DaySunBloom,
        celestialCoreColor = DaySunCore,
        celestialCoronaColor = DaySunCoronaInner,
        celestialOuterHalo = DaySunCoronaOuter,
        showsStars = false,
        cloudTint = Color.White,
        orbitAura = DayOrbitAura,
        accentColor = Color(0xFF0284C7),
        accentGlow = Color(0xFF38BDF8),
        accentMuted = Color(0xFF0284C7).copy(alpha = 0.14f),
        accentGradient = Brush.horizontalGradient(
            listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
        ),
        deckSurface = DayDeckSurface,
        cardSurface = DayCardSurface,
        cardBorder = DayCardBorder,
        textPrimary = DayTextPrimary,
        textSecondary = DayTextSecondary,
        textTertiary = DayTextTertiary,
        dockBackground = DockDay,
        dockBorder = glassBorderBrush(),
        frostedCardBackground = DayFrostedCardBackground,
        heroTempGradient = HeroTempGradientBrush,
        onSkyPrimary = OnSkyPrimary,
        onSkySecondary = OnSkySecondary,
        onSkyMuted = OnSkyMuted,
    )
    DiurnalPhase.EVENING -> DiurnalColors(
        skyGradient = Brush.verticalGradient(
            listOf(EveningSkyStop1, EveningSkyStop2, EveningSkyStop3, EveningSkyStop4)
        ),
        celestialBloom = EveningSunsetBloom,
        celestialCoreColor = EveningSunsetCore,
        celestialCoronaColor = EveningSunsetCoronaInner,
        celestialOuterHalo = EveningSunsetHorizonWash,
        showsStars = true,
        cloudTint = Color(0xFFFFEDD5),
        orbitAura = EveningOrbitAura,
        accentColor = Color(0xFFF97316),
        accentGlow = Color(0xFFFB923C),
        accentMuted = Color(0xFFF97316).copy(alpha = 0.16f),
        accentGradient = Brush.horizontalGradient(
            listOf(Color(0xFFEA580C), Color(0xFFFBBF24))
        ),
        deckSurface = EveningDeckSurface,
        cardSurface = EveningCardSurface,
        cardBorder = EveningCardBorder,
        textPrimary = EveningTextPrimary,
        textSecondary = EveningTextSecondary,
        textTertiary = EveningTextTertiary,
        dockBackground = DockEvening,
        dockBorder = glassBorderBrush(),
        frostedCardBackground = EveningFrostedCardBackground,
        heroTempGradient = HeroTempGradientBrush,
        onSkyPrimary = OnSkyPrimary,
        onSkySecondary = OnSkySecondary,
        onSkyMuted = OnSkyMuted,
    )
    DiurnalPhase.NIGHT -> DiurnalColors(
        skyGradient = Brush.verticalGradient(
            listOf(NightSkyStop1, NightSkyStop2, NightSkyStop3, NightSkyStop4)
        ),
        celestialBloom = NightMoonBloom,
        celestialCoreColor = NightMoonCore,
        celestialCoronaColor = NightMoonCoronaInner,
        celestialOuterHalo = NightMoonCoronaOuter,
        showsStars = true,
        cloudTint = Color(0xFF94A3B8),
        orbitAura = Color(0xFFA5ACF0),
        accentColor = Color(0xFFA5ACF0),
        accentGlow = Color(0xFFC7D2FE),
        accentMuted = Color(0xFFA5ACF0).copy(alpha = 0.18f),
        accentGradient = Brush.horizontalGradient(
            listOf(Color(0xFFA5ACF0), Color(0xFF818CF8))
        ),
        deckSurface = NightDeckSurface,
        cardSurface = NightCardSurface,
        cardBorder = NightCardBorder,
        textPrimary = NightTextPrimary,
        textSecondary = NightTextSecondary,
        textTertiary = NightTextTertiary,
        dockBackground = DockNight,
        dockBorder = darkGlassBorderBrush(),
        frostedCardBackground = NightFrostedCardBackground,
        heroTempGradient = HeroTempGradientBrush,
        onSkyPrimary = OnSkyPrimary,
        onSkySecondary = OnSkySecondary,
        onSkyMuted = OnSkyMuted,
    )
}

private fun makeLightKlimataScheme(diurnal: DiurnalColors) = lightColorScheme(
    primary = diurnal.accentColor,
    onPrimary = Color.White,
    surface = diurnal.deckSurface,
    onSurface = diurnal.textPrimary,
    surfaceVariant = diurnal.cardSurface,
    onSurfaceVariant = diurnal.textSecondary,
)

private fun makeDarkKlimataScheme(diurnal: DiurnalColors) = darkColorScheme(
    primary = diurnal.accentColor,
    onPrimary = Color.White,
    surface = diurnal.deckSurface,
    onSurface = diurnal.textPrimary,
    surfaceVariant = diurnal.cardSurface,
    onSurfaceVariant = diurnal.textSecondary,
)

@Composable
fun KlimataTheme(
    phase: DiurnalPhase = currentDiurnalPhase(),
    content: @Composable () -> Unit
) {
    val diurnalColors = diurnalColorsFor(phase)
    val colorScheme = when (phase) {
        DiurnalPhase.NIGHT -> makeDarkKlimataScheme(diurnalColors)
        else -> makeLightKlimataScheme(diurnalColors)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalDiurnalColors provides diurnalColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}