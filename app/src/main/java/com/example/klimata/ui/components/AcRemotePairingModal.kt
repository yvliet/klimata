package com.example.klimata.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Power
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardBorder
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

/**
 * Universal Remote Control Smart Pairing & Signal Testing Modal.
 *
 * Cycles through brand-specific infrared protocol code sets (e.g. Sharp Inverter vs
 * Sharp Gree OEM AH-A5UCY/A9UCY) allowing users to fire test pulses and verify
 * audible/operational response before locking in the profile.
 */
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
    var hasFiredAtLeastOnce by remember { mutableStateOf(false) }

    val activeCodeSet = codeSets.getOrNull(selectedIndex) ?: codeSets.first()

    // Pulsing animation when user taps the test button
    val infiniteTransition = rememberInfiniteTransition(label = "IrPulseGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(DetailBlackBackground)
                    .border(1.dp, DetailCardBorder.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(diurnal.accentColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Broadcast,
                                contentDescription = null,
                                tint = diurnal.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Pair $brand Remote",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Text(
                                text = "Smart Code Finder (Signal ${selectedIndex + 1} of ${codeSets.size})",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = diurnal.accentColor
                                )
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DetailCardSurface)
                            .bouncyClickable(shape = CircleShape, onClick = onDismiss)
                    ) {
                        Text(
                            text = "✕",
                            color = DetailTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Signal selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(codeSets) { index, codeSet ->
                        val isSelected = index == selectedIndex
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) diurnal.accentColor.copy(alpha = 0.22f) else DetailCardSurface,
                            label = "ChipBg"
                        )
                        val chipBorder by animateColorAsState(
                            targetValue = if (isSelected) diurnal.accentColor else DetailCardBorder.copy(alpha = 0.5f),
                            label = "ChipBorder"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                .bouncyClickable(
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { selectedIndex = index }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Signal ${index + 1}",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) diurnal.accentColor else DetailTextSecondary
                                )
                            )
                        }
                    }
                }

                // Protocol detail card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DetailCardSurface)
                        .border(1.dp, DetailCardBorder.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeCodeSet.displayName,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DetailTextPrimary
                            )
                        )

                        if (activeCodeSet.id == currentCodeSetId) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Light.Check,
                                    contentDescription = null,
                                    tint = MineralMintActive,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Currently Active",
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
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = diurnal.accentColor
                        )
                    )

                    Text(
                        text = activeCodeSet.description,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = DetailTextMuted
                        )
                    )
                }

                // Central interactive "Test Power" button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(92.dp)
                            .scale(if (isFiringPulse) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (isFiringPulse) diurnal.accentColor else DetailCardSurfaceElevated,
                                        DetailCardSurface
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (isFiringPulse) diurnal.accentColor else DetailCardBorder,
                                shape = CircleShape
                            )
                            .bouncyClickable(
                                shape = CircleShape,
                                onClick = {
                                    coroutineScope.launch {
                                        isFiringPulse = true
                                        hasFiredAtLeastOnce = true
                                        irBlaster.dispatchTestPulse(
                                            brand = brand,
                                            codeSetId = activeCodeSet.id,
                                            power = true
                                        )
                                        delay(550)
                                        isFiringPulse = false
                                    }
                                }
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Light.Power,
                                contentDescription = "Test IR Pulse",
                                tint = if (isFiringPulse) Color.White else diurnal.accentColor,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = if (isFiringPulse) "EMITTING..." else "TEST POWER",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isFiringPulse) Color.White else DetailTextPrimary
                                )
                            )
                        }
                    }

                    Text(
                        text = if (isFiringPulse) "Transmitting 38 kHz infrared burst..." else "Point top of phone at AC unit & tap button",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = if (isFiringPulse) diurnal.accentColor else DetailTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                // Response & Verification Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Did your air conditioner beep or turn ON/OFF?",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = DetailTextPrimary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // "No / Next Signal" button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DetailCardSurface)
                                .border(1.dp, DetailCardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .bouncyClickable(
                                    shape = RoundedCornerShape(14.dp),
                                    onClick = {
                                        selectedIndex = (selectedIndex + 1) % codeSets.size
                                    }
                                )
                        ) {
                            Text(
                                text = if (selectedIndex < codeSets.size - 1) "Try Next Signal" else "Back to Signal 1",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = DetailTextSecondary
                                )
                            )
                        }

                        // "Yes, AC Responded!" button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(diurnal.accentColor)
                                .bouncyClickable(
                                    shape = RoundedCornerShape(14.dp),
                                    onClick = {
                                        onCodeSetSelected(activeCodeSet.id)
                                        onDismiss()
                                    }
                                )
                        ) {
                            Text(
                                text = "Yes, Save Remote",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
