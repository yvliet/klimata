package com.example.klimata.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.OnSkySecondary
import com.example.klimata.ui.theme.currentDiurnalPhase

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity

/**
 * Large ambient temperature readout.
 * Uses hardware-accelerated [TextStyle.brush] dynamic linear gradients that glide in lockstep
 * with the moving sky clouds, creating an authentic luminous light reflection across the letterforms.
 */
@Composable
fun HeroTemperatureDisplay(
    temperature: Int,
    condition: String,
    modifier: Modifier = Modifier,
    highTemp: Int? = null,
    lowTemp: Int? = null,
    phase: DiurnalPhase = currentDiurnalPhase(),
    roomIndex: Int = 0,
    tempScaleProvider: () -> Float = { 1f },
    tempAlphaProvider: () -> Float = { 1f },
    scrollOffsetProvider: () -> Float = { 0f },
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "HeroReflectionDrift")
    val reflectionProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 65000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReflectionProgress"
    )

    val reflectionGradient = remember(phase, reflectionProgress, density) {
        val shiftX = with(density) { 140.dp.toPx() } * (reflectionProgress - 0.5f)
        val widthPx = with(density) { 70.dp.toPx() }
        val heightPx = with(density) { 150.dp.toPx() }
        val startOffset = Offset(shiftX, 0f)
        val endOffset = Offset(shiftX + widthPx, heightPx)

        when (phase) {
            DiurnalPhase.DAY -> Brush.linearGradient(
                0.0f to Color.White,
                0.40f to Color(0xFFFFFFFF),
                0.75f to Color(0xFFF1F5F9),
                1.0f to Color(0xFFE2E8F0).copy(alpha = 0.86f),
                start = startOffset,
                end = endOffset
            )
            DiurnalPhase.EVENING -> Brush.linearGradient(
                0.0f to Color.White,
                0.40f to Color(0xFFFFFFFF),
                0.75f to Color(0xFFFFFBEB),
                1.0f to Color(0xFFFEF3C7).copy(alpha = 0.86f),
                start = startOffset,
                end = endOffset
            )
            DiurnalPhase.NIGHT -> Brush.linearGradient(
                0.0f to Color.White,
                0.40f to Color(0xFFFFFFFF),
                0.75f to Color(0xFFF1F5F9),
                1.0f to Color(0xFFE2E8F0).copy(alpha = 0.86f),
                start = startOffset,
                end = endOffset
            )
        }
    }

    Crossfade(
        targetState = Pair(temperature, condition),
        animationSpec = tween(220),
        label = "HeroTempCrossfade",
        modifier = modifier.fillMaxWidth()
    ) { (animTemp, animCondition) ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Scale and fade applied exclusively to the temperature digits and degree symbol,
            // zooming in to invisibility at midpoint while preserving condition text stability.
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.graphicsLayer {
                    val scale = tempScaleProvider()
                    scaleX = scale
                    scaleY = scale
                    alpha = tempAlphaProvider()
                    transformOrigin = TransformOrigin.Center
                }
            ) {
                Text(
                    text = animTemp.toString(),
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 140.sp,
                        lineHeight = 144.sp,
                        letterSpacing = (-4).sp,
                        brush = reflectionGradient
                    )
                )

                Text(
                    text = "°",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 74.sp,
                        lineHeight = 78.sp,
                        brush = reflectionGradient
                    ),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val detailText = if ((highTemp != null) && (lowTemp != null)) {
                "$animCondition  $highTemp°/$lowTemp°"
            } else {
                animCondition
            }

            Text(
                text = detailText,
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp
                ),
                color = if (phase == DiurnalPhase.DAY) Color.White else OnSkySecondary
            )
        }
    }
}

@Composable
fun HeroTemperatureDisplay(
    temperature: Int,
    condition: String,
    modifier: Modifier = Modifier,
    highTemp: Int? = null,
    lowTemp: Int? = null,
    phase: DiurnalPhase = currentDiurnalPhase(),
    roomIndex: Int = 0,
    tempScale: Float,
    tempAlpha: Float = 1f,
    scrollOffset: Float = 0f,
) {
    HeroTemperatureDisplay(
        temperature = temperature,
        condition = condition,
        modifier = modifier,
        highTemp = highTemp,
        lowTemp = lowTemp,
        phase = phase,
        roomIndex = roomIndex,
        tempScaleProvider = { tempScale },
        tempAlphaProvider = { tempAlpha },
        scrollOffsetProvider = { scrollOffset }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1976D2)
@Composable
private fun HeroTemperatureDisplayDayPreview() {
    KlimataTheme(phase = DiurnalPhase.DAY) {
        HeroTemperatureDisplay(
            temperature = 32,
            condition = "Clear",
            highTemp = 34,
            lowTemp = 24,
            phase = DiurnalPhase.DAY
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF9E4812)
@Composable
private fun HeroTemperatureDisplayEveningPreview() {
    KlimataTheme(phase = DiurnalPhase.EVENING) {
        HeroTemperatureDisplay(
            temperature = 29,
            condition = "Golden Hour",
            highTemp = 33,
            lowTemp = 25,
            phase = DiurnalPhase.EVENING
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1128)
@Composable
private fun HeroTemperatureDisplayNightPreview() {
    KlimataTheme(phase = DiurnalPhase.NIGHT) {
        HeroTemperatureDisplay(
            temperature = 26,
            condition = "Clear",
            highTemp = 32,
            lowTemp = 23,
            phase = DiurnalPhase.NIGHT
        )
    }
}
