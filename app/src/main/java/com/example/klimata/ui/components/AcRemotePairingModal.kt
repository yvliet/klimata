package com.example.klimata.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.X
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AcRemotePairingModal(
    brand: String,
    currentCodeSetId: String?,
    onCodeSetSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val diurnal = LocalDiurnalColors.current
    val coroutineScope = rememberCoroutineScope()
    val irBlaster = remember { IrBlasterService(context) }
    val codeSets = remember(brand) { irBlaster.getCodeSetsForBrand(brand) }

    var selectedIndex by remember {
        val initial = codeSets.indexOfFirst { it.id == currentCodeSetId }
        mutableIntStateOf(if (initial >= 0) initial else 0)
    }

    var isFiringPulse by remember { mutableStateOf(false) }
    val activeCodeSet = codeSets.getOrNull(selectedIndex) ?: codeSets.first()

    // Finite single-shot optical pulse animation; avoid infinite transitions to permit SoC low-power C-states
    val pulseRingScale = remember { Animatable(1.0f) }
    val pulseRingAlpha = remember { Animatable(0.0f) }
    val pulseGlowAlpha = remember { Animatable(0.0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        com.example.klimata.SyncDialogStatusBar()
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(DetailBlackBackground)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Broadcast,
                            contentDescription = null,
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Pair AC Remote",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Text(
                                text = "$brand Split AC",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = diurnal.accentColor
                                )
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .bouncyClickable(shape = CircleShape, onClick = onDismiss)
                            .clip(CircleShape)
                            .background(DetailCardSurface)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.X,
                            contentDescription = "Close",
                            tint = DetailTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (codeSets.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(codeSets) { index, _ ->
                            val isSelected = index == selectedIndex
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) diurnal.accentColor.copy(alpha = 0.18f) else DetailCardSurface,
                                label = "ChipBg"
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .bouncyClickable(
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = { selectedIndex = index }
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Signal ${index + 1}",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) diurnal.accentColor else DetailTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(116.dp)
                ) {
                    if (pulseRingAlpha.value > 0.001f) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .graphicsLayer {
                                    scaleX = pulseRingScale.value
                                    scaleY = pulseRingScale.value
                                    alpha = pulseRingAlpha.value
                                }
                                .border(
                                    width = 2.dp,
                                    color = diurnal.accentColor,
                                    shape = CircleShape
                                )
                        )
                    }

                    if (pulseGlowAlpha.value > 0.001f) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .graphicsLayer {
                                    scaleX = pulseRingScale.value * 0.95f
                                    scaleY = pulseRingScale.value * 0.95f
                                    alpha = pulseGlowAlpha.value
                                }
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            diurnal.accentColor.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(84.dp)
                            .bouncyClickable(
                                shape = CircleShape,
                                onClick = {
                                    coroutineScope.launch {
                                        if (isFiringPulse) return@launch
                                        isFiringPulse = true
                                        irBlaster.dispatchTestPulse(
                                            brand = brand,
                                            codeSetId = activeCodeSet.id,
                                            power = true
                                        )
                                        launch {
                                            pulseRingScale.snapTo(1.0f)
                                            pulseRingScale.animateTo(
                                                targetValue = 1.38f,
                                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        launch {
                                            pulseRingAlpha.snapTo(0.70f)
                                            pulseRingAlpha.animateTo(
                                                targetValue = 0.0f,
                                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        launch {
                                            pulseGlowAlpha.snapTo(0.40f)
                                            pulseGlowAlpha.animateTo(
                                                targetValue = 0.0f,
                                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        delay(450)
                                        isFiringPulse = false
                                    }
                                }
                            )
                            .clip(CircleShape)
                            .background(
                                if (isFiringPulse) diurnal.accentColor else DetailCardSurfaceElevated
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Power,
                                contentDescription = "Test Signal",
                                tint = if (isFiringPulse) Color.Black else diurnal.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isFiringPulse) "SENT" else "TEST",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    color = if (isFiringPulse) Color.Black else DetailTextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.Broadcast,
                        contentDescription = null,
                        tint = if (isFiringPulse) diurnal.accentColor else DetailTextSecondary.copy(alpha = 0.60f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isFiringPulse) "Transmitting infrared command..." else "Point phone top at AC and tap",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            color = if (isFiringPulse) diurnal.accentColor else DetailTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeCodeSet.displayName,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = DetailTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (activeCodeSet.id == currentCodeSetId) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Light.Check,
                                    contentDescription = null,
                                    tint = MineralMintActive,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Active",
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = MineralMintActive
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "Compatible: ${activeCodeSet.sampleModels}",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = DetailTextMuted
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = activeCodeSet.description,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = DetailTextMuted
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .bouncyClickable(
                            shape = RoundedCornerShape(16.dp),
                            onClick = {
                                onCodeSetSelected(activeCodeSet.id)
                                onDismiss()
                            }
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(diurnal.accentColor)
                ) {
                    Text(
                        text = "Save Remote",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.Black
                        )
                    )
                }

                if (codeSets.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .bouncyClickable(
                                shape = RoundedCornerShape(14.dp),
                                onClick = { selectedIndex = (selectedIndex + 1) % codeSets.size }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(DetailCardSurface)
                    ) {
                        Text(
                            text = "Try Next Signal (${selectedIndex + 1} / ${codeSets.size})",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = DetailTextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}
