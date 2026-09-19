package com.example.klimata.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Broadcast
import com.adamglin.phosphoricons.light.Lightning
import com.adamglin.phosphoricons.light.MapPin
import com.adamglin.phosphoricons.light.Power
import com.adamglin.phosphoricons.light.Sparkle
import com.example.klimata.data.RoomFactory
import com.example.klimata.data.RoomState
import com.example.klimata.data.ir.IrBlasterService
import com.example.klimata.ui.components.AcCatalogBrowserSheet
import com.example.klimata.ui.components.AcRemotePairingModal
import com.example.klimata.ui.components.DetailPageScaffold
import com.example.klimata.ui.components.bouncyClickable
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailCardSurfaceElevated
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily
import com.example.klimata.ui.theme.LocalDiurnalColors
import com.example.klimata.ui.theme.MineralMintActive
import kotlinx.coroutines.launch

@Composable
fun AcUnitDetailScreen(
    room: RoomState,
    onBackClick: () -> Unit = {},
    onModelSelected: (brand: String, model: String, capacity: String, inverterType: String, codeSetId: String?) -> Unit = { _, _, _, _, _ -> },
    onCodeSetSelected: (codeSetId: String) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val diurnal = LocalDiurnalColors.current

    var showCatalog by remember { mutableStateOf(false) }
    var showPairingModal by remember { mutableStateOf(false) }
    var isTestingIr by remember { mutableStateOf(false) }

    val irBlaster = remember { IrBlasterService(context) }
    val effectiveCodeSetId = room.profile.irCodeSet
        ?: RoomFactory.resolveDefaultCodeSet(room.profile.brand, room.profile.model)
    val brandCodeSets = remember(room.profile.brand) { irBlaster.getCodeSetsForBrand(room.profile.brand) }
    val activeCodeSet = brandCodeSets.find { it.id == effectiveCodeSetId } ?: brandCodeSets.firstOrNull()
    val assessment = remember(room.profile.capacity, room.areaSquareMeters) {
        RoomFactory.assessAcMatch(room.profile.capacity, room.areaSquareMeters)
    }

    if (showPairingModal) {
        AcRemotePairingModal(
            brand = room.profile.brand,
            currentCodeSetId = effectiveCodeSetId,
            onCodeSetSelected = { codeSetId ->
                onCodeSetSelected(codeSetId)
                showPairingModal = false
            },
            onDismiss = { showPairingModal = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DetailPageScaffold(
            title = "AC Unit",
            subtitle = room.name,
            onBackClick = onBackClick,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DetailCardSurface)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Hardware",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = DetailTextMuted
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${room.profile.brand} ${room.profile.model}",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.3).sp,
                            color = DetailTextPrimary
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = room.profile.capacity,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = DetailTextSecondary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(9.dp)
                                .background(DetailCardBorder)
                        )
                        Text(
                            text = room.profile.inverterType,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = DetailTextSecondary
                            )
                        )
                    }
                }

                val btnShape = RoundedCornerShape(14.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .bouncyClickable(shape = btnShape, onClick = { showCatalog = true })
                        .clip(btnShape)
                        .background(diurnal.accentColor.copy(alpha = 0.14f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Sparkle,
                            contentDescription = null,
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Change AC Model",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = diurnal.accentColor
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DetailCardSurface)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IR Dispatch",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = DetailTextMuted
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Broadcast,
                            contentDescription = null,
                            tint = diurnal.accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = room.dispatchState.dispatchMethod.ifEmpty { "IR Blaster" },
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = diurnal.accentColor
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DetailCardSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = activeCodeSet?.displayName ?: "Signal 1",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = DetailTextPrimary
                        )
                    )
                    Text(
                        text = activeCodeSet?.description ?: "Verified protocol timing for this hardware.",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = DetailTextSecondary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rowBtnShape = RoundedCornerShape(14.dp)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .bouncyClickable(
                                enabled = !isTestingIr,
                                shape = rowBtnShape,
                                onClick = {
                                    isTestingIr = true
                                    coroutineScope.launch {
                                        irBlaster.dispatchAcCommand(
                                            brand = room.profile.brand,
                                            power = true,
                                            temp = room.profile.currentSetpoint,
                                            mode = room.profile.mode,
                                            fanSpeed = room.profile.fanSpeed,
                                            isEco = room.isEcoEnabled,
                                            swing = room.profile.swing,
                                            codeSetId = effectiveCodeSetId
                                        )
                                        isTestingIr = false
                                    }
                                }
                            )
                            .clip(rowBtnShape)
                            .background(DetailCardSurfaceElevated)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isTestingIr) {
                                CircularProgressIndicator(
                                    color = diurnal.accentColor,
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = PhosphorIcons.Light.Power,
                                    contentDescription = null,
                                    tint = diurnal.accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "Test Power",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = DetailTextPrimary
                                )
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .bouncyClickable(shape = rowBtnShape, onClick = { showPairingModal = true })
                            .clip(rowBtnShape)
                            .background(diurnal.accentColor.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = "Pair Remote (${brandCodeSets.size})",
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = diurnal.accentColor
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DetailCardSurface)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Specifications",
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = DetailTextMuted
                    )
                )

                val specsShape = RoundedCornerShape(14.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(specsShape)
                        .background(DetailCardSurfaceElevated)
                ) {
                    AcSpecRow(label = "Capacity", value = room.profile.capacity)
                    AcSpecDivider()
                    AcSpecRow(label = "Inverter", value = room.profile.inverterType)
                    AcSpecDivider()
                    AcSpecRow(label = "Cooling Load", value = "${room.coolingLoadBtu} BTU/h")
                    AcSpecDivider()
                    AcSpecRow(label = "Room Area", value = "${room.areaSquareMeters} m²")
                }

                val assessmentBg = if (assessment.isOptimal) {
                    MineralMintActive.copy(alpha = 0.12f)
                } else {
                    Color(0xFFFBBF24).copy(alpha = 0.10f)
                }
                val assessmentTint = if (assessment.isOptimal) MineralMintActive else Color(0xFFFBBF24)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(specsShape)
                        .background(assessmentBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.Lightning,
                            contentDescription = null,
                            tint = assessmentTint,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = assessment.description,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = DetailTextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DetailCardSurface)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Light.MapPin,
                        contentDescription = null,
                        tint = DetailTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = room.name,
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = DetailTextPrimary
                        )
                    )
                }
                Text(
                    text = room.location,
                    style = TextStyle(
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = DetailTextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        AnimatedVisibility(
            visible = showCatalog,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            AcCatalogBrowserSheet(
                onDismiss = { showCatalog = false },
                onModelSelected = { selected ->
                    val codeSetId = RoomFactory.resolveDefaultCodeSet(selected.brand, selected.modelCode)
                    onModelSelected(
                        selected.brand,
                        selected.modelCode,
                        selected.defaultCapacity,
                        selected.inverterType,
                        codeSetId
                    )
                    showCatalog = false
                },
                accentColor = diurnal.accentColor
            )
        }
    }
}

@Composable
private fun AcSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = DetailTextSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DetailTextPrimary
            )
        )
    }
}

@Composable
private fun AcSpecDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(DetailCardBorder)
    )
}
