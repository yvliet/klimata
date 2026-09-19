package com.example.klimata.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Large ambient temperature readout.
 * Uses hardware-accelerated [TextStyle.brush] vertical gradients to reflect the sky's
 * chromatic tone without offscreen layer allocations.
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
    tempScale: Float = 1f,
    tempAlpha: Float = 1f,
) {
    val reflectionGradient = when (phase) {
        DiurnalPhase.DAY -> Brush.verticalGradient(
            0.0f to Color.White,
            0.45f to Color(0xFFF0F7FF),
            1.0f to Color(0xFF8AC5F8),
        )
        DiurnalPhase.EVENING -> Brush.verticalGradient(
            0.0f to Color.White,
            0.50f to Color(0xFFFFF8F0),
            1.0f to Color(0xFFFFB74D),
        )
        DiurnalPhase.NIGHT -> Brush.verticalGradient(
            0.0f to Color.White,
            0.50f to Color(0xFFF1F5F9),
            1.0f to Color(0xFFA5B4FC),
        )
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
                    scaleX = tempScale
                    scaleY = tempScale
                    alpha = tempAlpha
                    transformOrigin = TransformOrigin.Center
                }
            ) {
                Text(
                    text = animTemp.toString(),
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 122.sp,
                        lineHeight = 126.sp,
                        letterSpacing = (-3.5).sp,
                        brush = reflectionGradient
                    )
                )

                Text(
                    text = "°",
                    modifier = Modifier.padding(top = 10.dp),
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 64.sp,
                        lineHeight = 68.sp,
                        brush = reflectionGradient
                    )
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
                color = OnSkySecondary
            )
        }
    }
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
