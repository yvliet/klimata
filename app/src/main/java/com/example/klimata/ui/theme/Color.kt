package com.example.klimata.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Diurnal Sky Gradient Stops
val DaySkyStop1 = Color(0xFF1956CE)
val DaySkyStop2 = Color(0xFF2568E2)
val DaySkyStop3 = Color(0xFF3B7EF0)
val DaySkyStop4 = Color(0xFF5294F7)
val DaySkyStop5 = Color(0xFF68A4F8)

val EveningSkyStop1 = Color(0xFF1A237E)
val EveningSkyStop2 = Color(0xFFAD1457)
val EveningSkyStop3 = Color(0xFFE65100)
val EveningSkyStop4 = Color(0xFFFFB74D)
val EveningSkyStop5 = Color(0xFFFFB74D)

val NightSkyStop1 = Color(0xFF0D1B2A)
val NightSkyStop2 = Color(0xFF1B2838)
val NightSkyStop3 = Color(0xFF1E3A5F)
val NightSkyStop4 = Color(0xFF2C5282)
val NightSkyStop5 = Color(0xFF2C5282)

// Overcast / Stormy Sky Gradient Stops
val OvercastSkyStop1 = Color(0xFF0F1322)
val OvercastSkyStop2 = Color(0xFF141829)
val OvercastSkyStop3 = Color(0xFF1A1F33)
val OvercastSkyStop4 = Color(0xFF222842)

// Celestial Light Blooms
val DaySunBloom = Color(0x3DFFEA79)
val DaySunCore = Color(0xFFFFFDF5)
val DaySunCoronaInner = Color(0x70FFE58F)
val DaySunCoronaOuter = Color(0x28FFC107)
val DaySunRayBloom = Color(0x18FFE082)

val EveningSunsetBloom = Color(0x52FB923C)
val EveningSunsetCore = Color(0xFFFFF1E6)
val EveningSunsetCoronaInner = Color(0x80F97316)
val EveningSunsetHorizonWash = Color(0x40EA580C)

val NightMoonBloom = Color(0x3D818CF8)
val NightMoonCore = Color(0xFFF8FAFC)
val NightMoonEarthshine = Color(0x221E293B)
val NightMoonCoronaInner = Color(0x40A5B4FC)
val NightMoonCoronaOuter = Color(0x18818CF8)
val StarColorCool = Color(0xFFE0E7FF)
val StarColorWarm = Color(0xFFFEF3C7)

// Translucent Frosted Panels
// Contextual contrast rule:
// On light daytime skies, cards use a translucent dark slate-navy ("dark on light")
// providing tangible elevation and crisp contrast for white typography and charts.
// On dark nocturnal skies, cards use pure translucent white ("light on dark")
// naturally yielding a soft, luminous frosted indigo surface.
val DayFrostedCardBackground = Color(0x4D0F172A)
val EveningFrostedCardBackground = Color(0x381C1917)
val NightFrostedCardBackground = Color(0x24FFFFFF)
val FrostedCardBackground = NightFrostedCardBackground

val FrostedSubCardBackground = FrostedCardBackground
val FrostedButtonBackground = Color(0x18FFFFFF)

// Sky Legibility Tokens
val OnSkyPrimary = Color(0xFFFFFFFF)
val OnSkySecondary = Color(0xCCFFFFFF)
val OnSkyMuted = Color(0x8AFFFFFF)

// Hero Temperature Sky Reflection Gradient
val HeroTempGradientBrush: Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFF90CAF9),
    ),
)

fun heroTempGradientBrush(
    topColor: Color = Color.White,
    bottomColor: Color = Color(0xFF90CAF9)
): Brush = Brush.verticalGradient(listOf(topColor, bottomColor))

// Surfaces & Cards
val DayDeckSurface = Color(0xFFFFFFFF)
val DayCardSurface = Color(0xFFF8FAFC)
val DayCardBorder = Color(0xFFEEF2F6)

val EveningDeckSurface = Color(0xFFFAFAF9)
val EveningCardSurface = Color(0xFFF5F5F4)
val EveningCardBorder = Color(0x28FFFFFF)

val NightDeckSurface = Color(0xFF0B1120)
val NightCardSurface = Color(0xFF28304C)
val NightCardBorder = Color(0x33FFFFFF)

// Text Colors
val DayTextPrimary = Color(0xFF090D16)
val DayTextSecondary = Color(0xFF64748B)
val DayTextTertiary = Color(0xFF94A3B8)

val EveningTextPrimary = Color(0xFF1C1917)
val EveningTextSecondary = Color(0xFF78716C)
val EveningTextTertiary = Color(0xFFA8A29E)

val NightTextPrimary = Color(0xFFF8FAFC)
val NightTextSecondary = Color(0xFF94A3B8)
val NightTextTertiary = Color(0xFF64748B)

// Accents & Indicators
val MineralMint = Color(0xFF6EE7B7)
val MineralMintActive = Color(0xFF34D399)
val MineralMintGlow = Color(0x3D34D399)
val AccentBlue = Color(0xFF2563EB)
val AccentCyan = Color(0xFF06B6D4)
val EcoGreen = Color(0xFF34D399)
val EcoGreenDark = Color(0xFF059669)
val CarbonBlue = Color(0xFF0284C7)
val WarmAmber = Color(0xFFF59E0B)
val ActivePulse = Color(0xFF34D399)

val DockDay = Color(0xEB0F172A)
val DockEvening = Color(0xEB1C1917)
val DockNight = Color(0xF2020617)

val DayOrbitAura = Color(0xFF38BDF8)
val EveningOrbitAura = Color(0xFFF59E0B)
val NightOrbitAura = Color(0xFF818CF8)

val GlassWhiteBackdrop = Color(0x1AFFFFFF)
val GlassWhiteSurfaceTop = Color(0x33FFFFFF)
val GlassWhiteSurfaceBottom = Color(0x0DFFFFFF)
val GlassDarkBackdrop = Color(0x4D0F172A)
val GlassDarkSurfaceTop = Color(0x33334155)
val GlassBorderHighlight = Color(0x59FFFFFF)
val GlassBorderShadow = Color(0x1AFFFFFF)

fun glassSurfaceBrush(): Brush = Brush.linearGradient(
    colors = listOf(GlassWhiteSurfaceTop, GlassWhiteSurfaceBottom)
)

fun glassBorderBrush(): Brush = Brush.verticalGradient(
    colors = listOf(GlassBorderHighlight, GlassBorderShadow)
)

fun darkGlassBorderBrush(): Brush = Brush.verticalGradient(
    colors = listOf(Color(0x66475569), Color(0x1A1E293B))
)