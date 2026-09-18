package com.example.klimata.ui.components

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.klimata.R
import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.currentDiurnalPhase

import com.example.klimata.ui.theme.LocalMinuteOfDay

/**
 * Foreground pass of the background sky cloud layer.
 * Anchored directly to the background sky coordinate space and parallax rate rather than the text,
 * so the cloud remains permanently attached to the sky as the user scrolls, while physically
 * layering in front of the hero temperature digits in 3D.
 */
@Composable
fun HeroForegroundClouds(
    modifier: Modifier = Modifier,
    scrollOffsetProvider: () -> Float = { 0f },
    phase: DiurnalPhase = currentDiurnalPhase(),
    cloudRes: Int? = null
) {
    val minuteOfDay = LocalMinuteOfDay.current ?: when (phase) {
        DiurnalPhase.DAY -> 12 * 60
        DiurnalPhase.EVENING -> 17 * 60 + 45
        DiurnalPhase.NIGHT -> 22 * 60
    }
    val crossfade = computeCloudCrossfade(minuteOfDay)

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

    val explicitBitmap = remember(cloudRes) {
        cloudRes?.let { resId ->
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
    val topFadePx = with(density) { 145.dp.toPx() }
    val bottomFadePx = with(density) { 330.dp.toPx() }
    val featherMask = remember(density) {
        LinearGradient(
            0f, topFadePx, 0f, bottomFadePx,
            intArrayOf(
                android.graphics.Color.argb(0, 0, 0, 0),
                android.graphics.Color.argb(255, 0, 0, 0),
                android.graphics.Color.argb(255, 0, 0, 0),
                android.graphics.Color.argb(0, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.18f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
    }

    val shaderMatrix = remember { Matrix() }

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

    val layerPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "HeroSkyCloudDrift")
    val driftProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 65000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HeroSkyDriftX"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .graphicsLayer {
                val rawScroll = scrollOffsetProvider()
                val cloudBlurProgress = (rawScroll / 260f).coerceIn(0f, 1f)
                translationY = -60.dp.toPx() - rawScroll * 0.10f
                val blurPx = cloudBlurProgress * 14.dp.toPx()
                renderEffect = if (blurPx > 0.5f) BlurEffect(blurPx, blurPx) else null
            }
    ) {
        val w = size.width
        val h = size.height
        val driftX = driftProgress * w
        val alignY = 0f

        layerPaint.alpha = (crossfade.heroAlpha * 255f).toInt().coerceIn(0, 255)

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val checkpoint = nativeCanvas.saveLayer(0f, 0f, w, h, layerPaint)

            if (explicitBitmap != null && explicitShader != null) {
                val bmW = explicitBitmap.width.toFloat()
                val bmH = explicitBitmap.height.toFloat()
                shaderMatrix.reset()
                shaderMatrix.postScale(w / bmW, 740.dp.toPx() / bmH)
                shaderMatrix.postTranslate(driftX, alignY)
                explicitShader.setLocalMatrix(shaderMatrix)
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

                shaderMatrix.reset()
                shaderMatrix.postScale(w / baseW, 740.dp.toPx() / baseH)
                shaderMatrix.postTranslate(driftX, alignY)
                baseShader.setLocalMatrix(shaderMatrix)

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

                    shaderMatrix.reset()
                    shaderMatrix.postScale(w / overlayW, 740.dp.toPx() / overlayH)
                    shaderMatrix.postTranslate(driftX, alignY)
                    overlayShader.setLocalMatrix(shaderMatrix)

                    overlayPaint.shader = overlayShader
                    overlayPaint.alpha = (crossfade.overlayAlpha * 255f).toInt().coerceIn(0, 255)
                    nativeCanvas.drawRect(0f, 0f, w, h, overlayPaint)
                }
            }

            maskPaint.shader = featherMask
            nativeCanvas.drawRect(0f, 0f, w, h, maskPaint)

            nativeCanvas.restoreToCount(checkpoint)
        }
    }
}
