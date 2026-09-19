package com.example.klimata.ui.components

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.R
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.StarColorCool
import com.example.klimata.ui.theme.StarColorWarm
import com.example.klimata.ui.theme.currentDiurnalPhase
import kotlin.math.sin
import kotlin.random.Random

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
 * Renders diurnal celestial bloom, starfield, and scroll-parallaxed distant cloud layer.
 * Continuous animation states are read exclusively inside graphicsLayer blocks to bypass
 * composition and layout passes, keeping the CPU in low-power C-states overnight.
 */
@Composable
fun AtmosphericSkyCanvas(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Overcast",
    backCloudRes: Int = when (phase) {
        DiurnalPhase.DAY -> R.drawable.cloud_layer_back_day
        DiurnalPhase.EVENING -> R.drawable.cloud_layer_back_sunset
        DiurnalPhase.NIGHT -> R.drawable.cloud_layer_back
    }
) {
    val currentRawScroll = scrollOffsetProvider()
    val animatedScroll by animateFloatAsState(
        targetValue = currentRawScroll,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessLow
        ),
        label = "CloudSpringParallax"
    )

    val isOvercast = true
    val targetCloudAlpha = when (phase) {
        DiurnalPhase.DAY -> 0.40f
        DiurnalPhase.EVENING -> 0.75f
        DiurnalPhase.NIGHT -> 0.90f
    }
    val starAlphaMultiplier = 0.0f

    val cloudBlurProgress = (animatedScroll / 260f).coerceIn(0f, 1f)
    val bloomColors = remember(phase, isOvercast) {
        val bloomColor = when (phase) {
            DiurnalPhase.DAY -> Color(0x28FFFFFF)
            DiurnalPhase.EVENING -> Color(0x35F97316)
            DiurnalPhase.NIGHT -> if (isOvercast) Color(0x151E293B) else Color(0x1EA5B4FC)
        }
        listOf(bloomColor, Color.Transparent)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphericBackDriftTransition")
    val backDriftProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AtmosphericBackDrift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val blurPx = cloudBlurProgress * 14.dp.toPx()
                    renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
                }
        ) {
            val width = size.width
            val height = size.height

            val bloomCenter = Offset(width * 0.68f, height * 0.18f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = bloomColors,
                    center = bloomCenter,
                    radius = width * 0.70f
                ),
                center = bloomCenter,
                radius = width * 0.70f
            )

            if (phase == DiurnalPhase.NIGHT && starAlphaMultiplier > 0f) {
                drawCalmStarfield(
                    width,
                    height,
                    CalmNightStars,
                    alphaMultiplier = starAlphaMultiplier * (1f - cloudBlurProgress * 0.4f)
                )
            }
        }

        // Single-pass continuous horizontal drift using GPU hardware texture wrapping
        // Eliminates abutting quad cracks, subpixel gaps, and visible seam lines between tiles
        val context = LocalContext.current
        val backBitmap = remember(backCloudRes) {
            val opts = BitmapFactory.Options().apply { inScaled = false }
            BitmapFactory.decodeResource(context.resources, backCloudRes, opts)
        }
        val backShader = remember(backBitmap) {
            BitmapShader(backBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        }
        val backMatrix = remember { Matrix() }
        val backPaint = remember {
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
        }

        val cloudHeight = 740.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(cloudHeight)
                .align(Alignment.TopStart)
                .graphicsLayer {
                    translationY = -60.dp.toPx() - animatedScroll * 0.10f
                    alpha = (targetCloudAlpha - cloudBlurProgress * 0.20f).coerceAtLeast(0.0f)
                    val blurPx = cloudBlurProgress * 14.dp.toPx()
                    renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
                }
        ) {
            val w = size.width
            val h = size.height
            val bmW = backBitmap.width.toFloat()
            val bmH = backBitmap.height.toFloat()

            val scaleX = w / bmW
            val scaleY = h / bmH
            val progress = backDriftProgress.value

            backMatrix.reset()
            backMatrix.postScale(scaleX, scaleY)
            backMatrix.postTranslate(-progress * w, 0f)
            backShader.setLocalMatrix(backMatrix)

            backPaint.shader = backShader
            backPaint.alpha = 255

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRect(0f, 0f, w, h, backPaint)
            }
        }
    }
}

/**
 * Translucent foreground cloud veil.
 * Drifts directly across the hero temperature readout with smooth alpha blending
 * to establish authentic visual depth.
 * Fades out with vertical scroll to clear visibility for lower control cards.
 */
@Composable
fun CloudForegroundVeil(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Overcast",
    baseAlpha: Float = 0.38f,
    frontCloudRes: Int = R.drawable.cloud_layer_front
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CloudForegroundDriftTransition")
    val frontDriftProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudForegroundDrift"
    )

    val context = LocalContext.current
    val frontBitmap = remember(frontCloudRes) {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        BitmapFactory.decodeResource(context.resources, frontCloudRes, opts)
    }
    val frontShader = remember(frontBitmap) {
        BitmapShader(frontBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
    }
    val frontMatrix = remember { Matrix() }
    val frontPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            val scroll = scrollOffsetProvider()
            val scrollFade = (1f - scroll / 160f).coerceIn(0f, 1f)
            alpha = scrollFade
            val blurProgress = (scroll / 260f).coerceIn(0f, 1f)
            val blurPx = blurProgress * 14.dp.toPx()
            renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
        }
    ) {
        val progress = frontDriftProgress.value
        val w = size.width
        val h = size.height
        val bmW = frontBitmap.width.toFloat()
        val bmH = frontBitmap.height.toFloat()

        val scaleX = w / bmW
        val scaleY = h / bmH
        val verticalOffset = sin(progress * 2f * Math.PI.toFloat()) * 4f.dp.toPx()

        frontMatrix.reset()
        frontMatrix.postScale(scaleX, scaleY)
        frontMatrix.postTranslate(-progress * w, verticalOffset)
        frontShader.setLocalMatrix(frontMatrix)

        val effectiveAlpha = if (phase == DiurnalPhase.DAY) 0.16f else baseAlpha
        frontPaint.alpha = (effectiveAlpha * 255).toInt().coerceIn(0, 255)
        if (phase == DiurnalPhase.EVENING) {
            frontPaint.colorFilter = PorterDuffColorFilter(0xFFFFD1A4.toInt(), PorterDuff.Mode.MULTIPLY)
        } else {
            frontPaint.colorFilter = null
        }

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, w, h, frontPaint)
        }
    }
}

@Composable
fun AtmosphericSkyCanvas(
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Clear Night"
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = { scrollOffset },
        phase = phase,
        weatherCondition = weatherCondition
    )
}

/**
 * Drop-in backwards-compatible alias.
 */
@Composable
fun SmokeyCloudCanvas(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Clear Night"
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = scrollOffsetProvider,
        phase = phase,
        weatherCondition = weatherCondition
    )
}

@Composable
fun SmokeyCloudCanvas(
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Clear Night"
) {
    AtmosphericSkyCanvas(
        modifier = modifier,
        scrollOffsetProvider = { scrollOffset },
        phase = phase,
        weatherCondition = weatherCondition
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
