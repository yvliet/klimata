package com.example.klimata.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.R
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.StarColorCool
import com.example.klimata.ui.theme.StarColorWarm
import com.example.klimata.ui.theme.currentDiurnalPhase
import kotlin.random.Random

data class CirrusWisp(
    val xPercent: Float,
    val yPercent: Float,
    val widthRatio: Float,
    val heightPercent: Float,
    val alpha: Float,
)

// Fixed PRNG seed guarantees deterministic wisp placement across recompositions
// and configuration changes without requiring database persistence.
val StaticCirrusWisps: List<CirrusWisp> by lazy {
    val random = Random(42)
    val list = mutableListOf<CirrusWisp>()
    repeat(5) {
        list.add(
            CirrusWisp(
                xPercent = (random.nextFloat() * 1.1f) - 0.05f,
                yPercent = (random.nextFloat() * 0.35f) + 0.05f,
                widthRatio = (random.nextFloat() * 4.0f) + 4.0f,
                heightPercent = (random.nextFloat() * 0.08f) + 0.04f,
                alpha = (random.nextFloat() * 0.06f) + 0.06f
            )
        )
    }
    list
}

data class StaticStar(
    val xPercent: Float,
    val yPercent: Float,
    val radiusDp: Float,
    val alpha: Float,
    val isWarm: Boolean,
)

// Fixed seed avoids starfield jitter across activity recreation
val CalmNightStars: List<StaticStar> by lazy {
    val random = Random(1337)
    val list = mutableListOf<StaticStar>()
    repeat(45) {
        val y = (random.nextFloat() * 0.45f) + 0.03f
        val x = (random.nextFloat() * 0.94f) + 0.03f
        val radius = (random.nextFloat() * 0.7f) + 0.6f
        val alpha = (random.nextFloat() * 0.45f) + 0.35f
        list.add(StaticStar(x, y, radius, alpha, isWarm = random.nextFloat() < 0.15f))
    }
    list
}

private fun DrawScope.drawCalmStarfield(
    canvasWidth: Float,
    canvasHeight: Float,
    stars: List<StaticStar>,
    alphaMultiplier: Float
) {
    if (alphaMultiplier <= 0f) return
    stars.forEach { star ->
        val cx = canvasWidth * star.xPercent
        val cy = canvasHeight * star.yPercent
        val radiusPx = star.radiusDp.dp.toPx()
        val color = if (star.isWarm) StarColorWarm else StarColorCool
        drawCircle(
            color = color.copy(alpha = star.alpha * alphaMultiplier),
            center = Offset(cx, cy),
            radius = radiusPx
        )
    }
}

/**
 * Atmospheric sky backdrop.
 * Renders diurnal celestial bloom, starfield, and scroll-parallaxed cirrus layers.
 * Continuous infinite transitions are deliberately avoided to allow the SoC to enter
 * low-power C-states during overnight climate monitoring.
 */
@Composable
fun AtmosphericSkyCanvas(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase()
) {
    val topCloudRes = when (phase) {
        DiurnalPhase.DAY -> R.drawable.clouds_top_right_day
        DiurnalPhase.EVENING -> R.drawable.clouds_top_right_sunset
        DiurnalPhase.NIGHT -> R.drawable.clouds_top_right_night
    }

    val currentRawScroll = scrollOffsetProvider()
    val animatedScroll by animateFloatAsState(
        targetValue = currentRawScroll,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessLow
        ),
        label = "CloudSpringParallax"
    )

    val cloudBlurProgress = (animatedScroll / 260f).coerceIn(0f, 1f)
    val cloudBlurRadius = (cloudBlurProgress * 16f).dp

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = (cloudBlurProgress * 8f).dp)
        ) {
            val width = size.width
            val height = size.height

            val bloomCenter = Offset(width * 0.68f, height * 0.18f)
            val bloomColor = when (phase) {
                DiurnalPhase.DAY -> Color(0x28FFFFFF)
                DiurnalPhase.EVENING -> Color(0x35F97316)
                DiurnalPhase.NIGHT -> Color(0x1EA5B4FC)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bloomColor, Color.Transparent),
                    center = bloomCenter,
                    radius = width * 0.70f
                ),
                center = bloomCenter,
                radius = width * 0.70f
            )

            if (phase == DiurnalPhase.NIGHT) {
                drawCalmStarfield(width, height, CalmNightStars, alphaMultiplier = 0.85f * (1f - cloudBlurProgress * 0.4f))
            }

            StaticCirrusWisps.forEach { wisp ->
                val wispWidth = width * wisp.heightPercent * wisp.widthRatio
                val wispHeight = height * wisp.heightPercent
                val cx = width * wisp.xPercent
                val cy = height * wisp.yPercent - animatedScroll * 0.08f

                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = wisp.alpha),
                        Color.White.copy(alpha = wisp.alpha * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(wispWidth / 2f, wispHeight / 2f),
                    radius = wispWidth / 2f
                )

                withTransform(
                    transformBlock = {
                        translate(left = cx - wispWidth / 2f, top = cy - wispHeight / 2f)
                        scale(
                            scaleX = 1f,
                            scaleY = wispHeight / wispWidth,
                            pivot = Offset(wispWidth / 2f, wispHeight / 2f)
                        )
                    }
                ) {
                    drawCircle(
                        brush = brush,
                        center = Offset(wispWidth / 2f, wispHeight / 2f),
                        radius = wispWidth / 2f
                    )
                }
            }
        }

        // Reading animatedScroll inside graphicsLayer and blur confines invalidations
        Image(
            painter = painterResource(id = topCloudRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .align(Alignment.TopEnd)
                .blur(radius = cloudBlurRadius)
                .graphicsLayer {
                    translationY = -animatedScroll * 0.14f
                    alpha = (0.88f - cloudBlurProgress * 0.25f).coerceAtLeast(0.40f)
                }
        )
    }
}

@Composable
fun AtmosphericSkyCanvas(
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase()
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = { scrollOffset },
        phase = phase
    )
}

/**
 * Drop-in backwards-compatible alias.
 */
@Composable
fun SmokeyCloudCanvas(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase()
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = scrollOffsetProvider,
        phase = phase
    )
}

@Composable
fun SmokeyCloudCanvas(
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase()
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = { scrollOffset },
        phase = phase
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D47A1)
@Composable
private fun AtmosphericSkyCanvasDayPreview() {
    KlimataTheme(phase = DiurnalPhase.DAY) {
        AtmosphericSkyCanvas(phase = DiurnalPhase.DAY)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020617)
@Composable
private fun AtmosphericSkyCanvasNightPreview() {
    KlimataTheme(phase = DiurnalPhase.NIGHT) {
        AtmosphericSkyCanvas(phase = DiurnalPhase.NIGHT)
    }
}
