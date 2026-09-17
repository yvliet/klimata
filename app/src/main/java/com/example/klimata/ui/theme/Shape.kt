package com.example.klimata.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardCornerRadius = 24.dp
val SubCardCornerRadius = 16.dp
val ModalCornerRadius = 24.dp
val ButtonCornerRadius = 14.dp
val ChipCornerRadius = 8.dp
val ScaffoldCornerRadius = 28.dp

val CardShape = RoundedCornerShape(CardCornerRadius)
val SubCardShape = RoundedCornerShape(SubCardCornerRadius)
val ModalShape = RoundedCornerShape(ModalCornerRadius)
val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
val ChipShape = RoundedCornerShape(ChipCornerRadius)
val ScaffoldShape = RoundedCornerShape(topStart = ScaffoldCornerRadius, topEnd = ScaffoldCornerRadius)

val KlimataShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(ChipCornerRadius),
    medium = RoundedCornerShape(ButtonCornerRadius),
    large = RoundedCornerShape(CardCornerRadius),
    extraLarge = RoundedCornerShape(ScaffoldCornerRadius)
)
