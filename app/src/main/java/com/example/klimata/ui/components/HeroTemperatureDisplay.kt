package com.example.klimata.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Degree symbol is decoupled from digits: a unified 108sp string sizes the degree glyph
        // to standard font ascender height, creating excessive visual mass and vertical offset.
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = temperature.toString(),
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 108.sp,
                    lineHeight = 112.sp,
                    letterSpacing = (-3).sp,
                    brush = reflectionGradient
                )
            )

            Text(
                text = "°",
                style = TextStyle(
                    fontFamily = JakartaFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 58.sp,
                    lineHeight = 62.sp,
                    brush = reflectionGradient
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val detailText = if ((highTemp != null) && (lowTemp != null)) {
            "$condition  $highTemp°/$lowTemp°"
        } else {
            condition
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
