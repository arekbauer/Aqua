package com.arekb.aqua.ui.screens.today.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.R
import com.arekb.aqua.ui.theme.AquaTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickAddButtonGroup(
    presetsMl: List<Int>,
    modifier: Modifier = Modifier,
    onAdd: (Int) -> Unit
) {
    val presets = presetsMl.take(3)
    val interactionSources = remember(presets.size) {
        List(presets.size) { MutableInteractionSource() }
    }

    ButtonGroup(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = modifier.fillMaxWidth(),
        expandedRatio = 0.25f,
    ) {
        presets.forEachIndexed { index, amountMl ->
            val buttonIcon = when (index) {
                0 -> R.drawable.icon_mug
                1 -> R.drawable.icon_glass_cup
                else -> R.drawable.icon_water_bottle
            }
            customItem(
                buttonGroupContent = {
                    val contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    val layoutDirection = LocalLayoutDirection.current
                    val colorScheme = MaterialTheme.colorScheme
                    val (containerColor, contentColor) = when (index) {
                        0 -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
                        1 -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
                        else -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
                    }
                    val buttonShapes = when (index) {
                        0 -> ButtonDefaults.shapes(
                            shape = RoundedCornerShape(
                                topStart = 30.dp,
                                bottomStart = 30.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp
                            ),
                            pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape
                        )
                        presets.lastIndex -> ButtonDefaults.shapes(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 30.dp,
                                bottomEnd = 30.dp
                            ),
                            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape
                        )
                        else -> ButtonDefaults.shapes(
                            shape = RoundedCornerShape(18.dp),
                            pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape
                        )
                    }
                    Button(
                        onClick = { onAdd(amountMl) },
                        shapes = buttonShapes,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        ),
                        contentPadding = contentPadding,
                        interactionSource = interactionSources[index],
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(
                                interactionSource = interactionSources[index],
                                compressionLimit = contentPadding.calculateEndPadding(layoutDirection)
                            )
                            .semantics {
                                contentDescription = "Add $amountMl millilitres of water"
                            }
                            .height(92.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(buttonIcon),
                                contentDescription = null
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "$amountMl ml",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                menuContent = {
                    DropdownMenuItem(
                        leadingIcon = { Icon(painter = painterResource(buttonIcon), contentDescription = null) },
                        text = { Text("$amountMl ml") },
                        onClick = { onAdd(amountMl) },
                        interactionSource = interactionSources[index]
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickAddButtonGroupPreview() {
    AquaTheme {
        QuickAddButtonGroup(
            presetsMl = listOf(200, 350, 500),
            modifier = Modifier.fillMaxWidth(),
            onAdd = {}
        )
    }
}

