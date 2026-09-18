package com.example.klimata.ui.components

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klimata.R
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.KlimataTheme
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.LocalMinuteOfDay
import com.example.klimata.ui.theme.StarColorCool
import com.example.klimata.ui.theme.StarColorWarm
import com.example.klimata.ui.theme.currentDiurnalPhase
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

data class CloudCrossfadeState(
    val baseIndex: Int,
    val overlayIndex: Int,
    val overlayAlpha: Float,
    val skyAlpha: Float,
    val heroAlpha: Float
)

fun computeCloudCrossfade(minuteOfDay: Int): CloudCrossfadeState {
    val m = minuteOfDay.coerceIn(0, 1439)
    return when {
        // Deep Night: 00:00 -> 05:00
        m < 300 -> CloudCrossfadeState(
            baseIndex = 2,
            overlayIndex = 2,
            overlayAlpha = 0f,
            skyAlpha = 0.88f,
            heroAlpha = 0.46f
        )
        // Night -> Dawn / Sunset Tint: 05:00 -> 06:15
        m < 375 -> {
            val progress = (m - 300) / 75f
            CloudCrossfadeState(
                baseIndex = 2,
                overlayIndex = 1,
                overlayAlpha = progress,
                skyAlpha = androidx.compose.ui.util.lerp(0.88f, 0.70f, progress),
                heroAlpha = androidx.compose.ui.util.lerp(0.46f, 0.42f, progress)
            )
        }
        // Dawn / Sunset Tint -> Day: 06:15 -> 07:15
        m < 435 -> {
            val progress = (m - 375) / 60f
            CloudCrossfadeState(
                baseIndex = 1,
                overlayIndex = 0,
                overlayAlpha = progress,
                skyAlpha = androidx.compose.ui.util.lerp(0.70f, 0.42f, progress),
                heroAlpha = androidx.compose.ui.util.lerp(0.42f, 0.30f, progress)
            )
        }
        // Peak Day: 07:15 -> 16:30
        m < 990 -> CloudCrossfadeState(
            baseIndex = 0,
            overlayIndex = 0,
            overlayAlpha = 0f,
            skyAlpha = 0.42f,
            heroAlpha = 0.30f
        )
        // Day -> Sunset: 16:30 -> 18:00
        m < 1080 -> {
            val progress = (m - 990) / 90f
            CloudCrossfadeState(
                baseIndex = 0,
                overlayIndex = 1,
                overlayAlpha = progress,
                skyAlpha = androidx.compose.ui.util.lerp(0.42f, 0.75f, progress),
                heroAlpha = androidx.compose.ui.util.lerp(0.30f, 0.42f, progress)
            )
        }
        // Sunset -> Twilight / Night: 18:00 -> 19:30
        m < 1170 -> {
            val progress = (m - 1080) / 90f
            CloudCrossfadeState(
                baseIndex = 1,
                overlayIndex = 2,
                overlayAlpha = progress,
                skyAlpha = androidx.compose.ui.util.lerp(0.75f, 0.88f, progress),
                heroAlpha = androidx.compose.ui.util.lerp(0.42f, 0.46f, progress)
            )
        }
        // Night: 19:30 -> 24:00
        else -> CloudCrossfadeState(
            baseIndex = 2,
            overlayIndex = 2,
            overlayAlpha = 0f,
            skyAlpha = 0.88f,
            heroAlpha = 0.46f
        )
    }
}

/**
 * Atmospheric sky backdrop.
 * Renders diurnal celestial bloom, starfield, and scroll-parallaxed distant cloud layer.
 * Pre-decodes day, sunset, and night cloud textures to eliminate runtime GC churn and main-thread
 * bitmap decoding during dynamic diurnal transitions.
 */
@Composable
fun AtmosphericSkyCanvas(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase(),
    weatherCondition: String = "Overcast",
    backCloudRes: Int? = null
) {
    val minuteOfDay = LocalMinuteOfDay.current ?: when (phase) {
        DiurnalPhase.DAY -> 12 * 60
        DiurnalPhase.EVENING -> 17 * 60 + 45
        DiurnalPhase.NIGHT -> 22 * 60
    }
    val crossfade = computeCloudCrossfade(minuteOfDay)
    val starAlphaMultiplier = 0.0f

    val diurnal = LocalDiurnalColors.current
    val bloomColors = remember(diurnal.celestialBloom) {
        listOf(diurnal.celestialBloom, Color.Transparent)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val rawScroll = scrollOffsetProvider()
                    val cloudBlurProgress = (rawScroll / 260f).coerceIn(0f, 1f)
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
                    alphaMultiplier = starAlphaMultiplier
                )
            }
        }

        val context = LocalContext.current
        val dayBitmap = remember {
            val opts = BitmapFactory.Options().apply { inScaled = false }
            BitmapFactory.decodeResource(context.resources, R.drawable.cloud_layer_back_day, opts)
        }
        val sunsetBitmap = remember {
            val opts = BitmapFactory.Options().apply { inScaled = false }
            BitmapFactory.decodeResource(context.resources, R.drawable.cloud_layer_back_sunset, opts)
        }
        val nightBitmap = remember {
            val opts = BitmapFactory.Options().apply { inScaled = false }
            BitmapFactory.decodeResource(context.resources, R.drawable.cloud_layer_back, opts)
        }

        val explicitBitmap = remember(backCloudRes) {
            backCloudRes?.let { resId ->
                val opts = BitmapFactory.Options().apply { inScaled = false }
                BitmapFactory.decodeResource(context.resources, resId, opts)
            }
        }

        val shaders = remember(dayBitmap, sunsetBitmap, nightBitmap) {
            listOf(
                BitmapShader(dayBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP),
                BitmapShader(sunsetBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP),
                BitmapShader(nightBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
            )
        }
        val explicitShader = remember(explicitBitmap) {
            explicitBitmap?.let { BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP) }
        }

        val density = LocalDensity.current
        val backMatrix = remember { Matrix() }
        val basePaint = remember {
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
        }
        val overlayPaint = remember {
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
            }
        }
        val maskPaint = remember {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
        }

        val cloudHeight = 740.dp
        val fadeStartPx = with(density) { 380.dp.toPx() }
        val fadeEndPx = with(density) { 740.dp.toPx() }
        val bottomFeatherMask = remember(density) {
            LinearGradient(
                0f, fadeStartPx, 0f, fadeEndPx,
                intArrayOf(
                    android.graphics.Color.BLACK,
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        val infiniteTransition = rememberInfiniteTransition(label = "SkyCloudDrift")
        val skyDriftProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 65000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SkyDriftX"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(cloudHeight)
                .align(Alignment.TopStart)
                .graphicsLayer {
                    val rawScroll = scrollOffsetProvider()
                    val cloudBlurProgress = (rawScroll / 260f).coerceIn(0f, 1f)
                    translationY = -60.dp.toPx() - rawScroll * 0.10f
                    alpha = (crossfade.skyAlpha - cloudBlurProgress * 0.20f).coerceAtLeast(0.0f)
                    val blurPx = cloudBlurProgress * 14.dp.toPx()
                    renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
                }
        ) {
            val w = size.width
            val h = size.height
            val driftX = skyDriftProgress * w

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val checkpoint = nativeCanvas.saveLayer(0f, 0f, w, h, null)

                if (explicitBitmap != null && explicitShader != null) {
                    val bmW = explicitBitmap.width.toFloat()
                    val bmH = explicitBitmap.height.toFloat()
                    backMatrix.reset()
                    backMatrix.postScale(w / bmW, h / bmH)
                    backMatrix.postTranslate(driftX, 0f)
                    explicitShader.setLocalMatrix(backMatrix)

                    basePaint.shader = explicitShader
                    basePaint.alpha = 255
                    nativeCanvas.drawRect(0f, 0f, w, h, basePaint)
                } else {
                    val isCrossfading = crossfade.overlayAlpha > 0.005f && crossfade.overlayIndex != crossfade.baseIndex
                    val baseAlphaInt = if (isCrossfading) {
                        ((1f - crossfade.overlayAlpha) * 255f).toInt().coerceIn(0, 255)
                    } else {
                        255
                    }

                    val baseBm = when (crossfade.baseIndex) {
                        0 -> dayBitmap
                        1 -> sunsetBitmap
                        else -> nightBitmap
                    }
                    val baseShader = shaders[crossfade.baseIndex]
                    val baseW = baseBm.width.toFloat()
                    val baseH = baseBm.height.toFloat()

                    backMatrix.reset()
                    backMatrix.postScale(w / baseW, h / baseH)
                    backMatrix.postTranslate(driftX, 0f)
                    baseShader.setLocalMatrix(backMatrix)

                    basePaint.shader = baseShader
                    basePaint.alpha = baseAlphaInt
                    nativeCanvas.drawRect(0f, 0f, w, h, basePaint)

                    if (isCrossfading) {
                        val overlayBm = when (crossfade.overlayIndex) {
                            0 -> dayBitmap
                            1 -> sunsetBitmap
                            else -> nightBitmap
                        }
                        val overlayShader = shaders[crossfade.overlayIndex]
                        val overlayW = overlayBm.width.toFloat()
                        val overlayH = overlayBm.height.toFloat()

                        backMatrix.reset()
                        backMatrix.postScale(w / overlayW, h / overlayH)
                        backMatrix.postTranslate(driftX, 0f)
                        overlayShader.setLocalMatrix(backMatrix)

                        overlayPaint.shader = overlayShader
                        overlayPaint.alpha = (crossfade.overlayAlpha * 255f).toInt().coerceIn(0, 255)
                        nativeCanvas.drawRect(0f, 0f, w, h, overlayPaint)
                    }
                }

                maskPaint.shader = bottomFeatherMask
                nativeCanvas.drawRect(0f, 0f, w, h, maskPaint)

                nativeCanvas.restoreToCount(checkpoint)
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
    val minuteOfDay = LocalMinuteOfDay.current ?: when (phase) {
        DiurnalPhase.DAY -> 12 * 60
        DiurnalPhase.EVENING -> 17 * 60 + 45
        DiurnalPhase.NIGHT -> 22 * 60
    }
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
    val eveningFilter = remember {
        PorterDuffColorFilter(0xFFFFD1A4.toInt(), PorterDuff.Mode.MULTIPLY)
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
        val w = size.width
        val h = size.height
        val bmW = frontBitmap.width.toFloat()
        val bmH = frontBitmap.height.toFloat()

        val scaleX = w / bmW
        val scaleY = h / bmH

        frontMatrix.reset()
        frontMatrix.postScale(scaleX, scaleY)
        frontShader.setLocalMatrix(frontMatrix)

        val isDay = minuteOfDay in 435..990
        val isSunset = minuteOfDay in 990..1125 || minuteOfDay in 345..420
        val effectiveAlpha = when {
            isDay -> 0.16f
            isSunset -> androidx.compose.ui.util.lerp(0.16f, baseAlpha, 0.7f)
            else -> baseAlpha
        }
        frontPaint.alpha = (effectiveAlpha * 255).toInt().coerceIn(0, 255)
        frontPaint.colorFilter = if (isSunset) eveningFilter else null

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
