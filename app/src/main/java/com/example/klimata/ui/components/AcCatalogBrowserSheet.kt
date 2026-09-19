package com.example.klimata.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klimata.data.models.AcDatabase
import com.example.klimata.data.models.AcModelInfo
import com.example.klimata.ui.theme.DetailBlackBackground
import com.example.klimata.ui.theme.DetailCardBorder
import com.example.klimata.ui.theme.DetailCardSurface
import com.example.klimata.ui.theme.DetailTextMuted
import com.example.klimata.ui.theme.DetailTextPrimary
import com.example.klimata.ui.theme.DetailTextSecondary
import com.example.klimata.ui.theme.JakartaFamily

/**
 * Bottom-sheet AC model catalog extracted as a shared composable so both the
 * provisioning wizard and the AC unit detail screen can reference the same browsing UI.
 * Caller is responsible for the overlay animation and BackHandler registration.
 */
@Composable
fun AcCatalogBrowserSheet(
    onDismiss: () -> Unit,
    onModelSelected: (AcModelInfo) -> Unit,
    accentColor: Color,
) {
    BackHandler { onDismiss() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("All") }
    val brands = remember { AcDatabase.getBrands() }
    val filteredModels = remember(selectedBrand, searchQuery) {
        AcDatabase.filter(selectedBrand, searchQuery)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(DetailBlackBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(DetailCardBorder)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AC Model Catalog",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DetailTextPrimary
                        )
                    )
                    Text(
                        text = "40+ residential split units with pre-mapped IR codes",
                        style = TextStyle(
                            fontFamily = JakartaFamily,
                            fontSize = 12.sp,
                            color = DetailTextSecondary
                        )
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .bouncyClickable(shape = CircleShape, onClick = onDismiss)
                        .clip(CircleShape)
                        .background(DetailCardSurface)
                ) {
                    Text("✕", color = DetailTextSecondary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search model (e.g. AH-A9, FTKF, Plasmacluster...)",
                        style = TextStyle(fontFamily = JakartaFamily, fontSize = 13.sp, color = DetailTextMuted)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DetailCardSurface,
                    unfocusedContainerColor = DetailCardSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = DetailTextPrimary,
                    unfocusedTextColor = DetailTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(brands, key = { it }) { brand ->
                    val isSelected = brand.equals(selectedBrand, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .bouncyClickable(
                                shape = RoundedCornerShape(10.dp),
                                onClick = { selectedBrand = brand }
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.20f) else DetailCardSurface)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = brand,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) accentColor else DetailTextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredModels, key = { "${it.brand}_${it.modelCode}" }) { modelInfo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable(
                                shape = RoundedCornerShape(16.dp),
                                onClick = { onModelSelected(modelInfo) }
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(DetailCardSurface)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${modelInfo.brand} ${modelInfo.modelCode}",
                                style = TextStyle(
                                    fontFamily = JakartaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DetailTextPrimary
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = modelInfo.defaultCapacity,
                                    style = TextStyle(
                                        fontFamily = JakartaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                )
                            }
                        }

                        Text(
                            text = modelInfo.series,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = DetailTextSecondary
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = modelInfo.inverterType,
                                style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = DetailTextMuted)
                            )
                            Text("·", style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = DetailTextMuted.copy(alpha = 0.50f)))
                            Text(
                                text = modelInfo.irProtocol.replace("_", " "),
                                style = TextStyle(fontFamily = JakartaFamily, fontSize = 11.sp, color = DetailTextMuted)
                            )
                        }

                        Text(
                            text = modelInfo.notes,
                            style = TextStyle(
                                fontFamily = JakartaFamily,
                                fontSize = 11.sp,
                                color = DetailTextMuted.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }
    }
}
