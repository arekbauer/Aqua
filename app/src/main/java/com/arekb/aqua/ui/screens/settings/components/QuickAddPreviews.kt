package com.arekb.aqua.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.R
import com.arekb.aqua.ui.theme.AquaTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicTextField

@Preview(showBackground = true, name = "4. Button Grid (Editable)")
@Composable
fun QuickAddStyle4VisualEditablePreview() {
    AquaTheme {
        SettingsCard("Quick add", "Choose three amounts for your buttons") {
            val presets = remember { mutableStateListOf("200", "350", "500") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEachIndexed { index, textValue ->
                    val icon = when (index) {
                        0 -> R.drawable.icon_mug
                        1 -> R.drawable.icon_glass_cup
                        else -> R.drawable.icon_water_bottle
                    }
                    val value = textValue.toIntOrNull() ?: 0
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            
                            // Editable Text Field
                            BasicTextField(
                                value = textValue,
                                onValueChange = { presets[index] = it.filter(Char::isDigit).take(4) },
                                textStyle = MaterialTheme.typography.titleMediumEmphasized.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                                                innerTextField()
                                            }
                                            Text(" ml", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        HorizontalDivider(
                                            modifier = Modifier.fillMaxWidth(0.6f).padding(top = 2.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledIconButton(
                                    onClick = { presets[index] = (value - 50).coerceAtLeast(10).toString() },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(16.dp))
                                }
                                FilledIconButton(
                                    onClick = { presets[index] = (value + 50).coerceAtMost(5000).toString() },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "1. Sliders (Like Daily Goal)")
@Composable
fun QuickAddStyle1SlidersPreview() {
    AquaTheme {
        SettingsCard("Quick add", "Choose three amounts for your buttons") {
            val presets = remember { mutableStateListOf(200f, 350f, 500f) }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                presets.forEachIndexed { index, value ->
                    val icon = when (index) {
                        0 -> R.drawable.icon_mug
                        1 -> R.drawable.icon_glass_cup
                        else -> R.drawable.icon_water_bottle
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Preset ${index + 1}", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.weight(1f))
                            Text("${value.toInt()} ml", style = MaterialTheme.typography.titleMediumEmphasized, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = value,
                            onValueChange = { presets[index] = it },
                            valueRange = 50f..1000f
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "2. Compact Steppers")
@Composable
fun QuickAddStyle2SteppersPreview() {
    AquaTheme {
        SettingsCard("Quick add", "Choose three amounts for your buttons") {
            val presets = remember { mutableStateListOf(200, 350, 500) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEachIndexed { index, value ->
                    val icon = when (index) {
                        0 -> R.drawable.icon_mug
                        1 -> R.drawable.icon_glass_cup
                        else -> R.drawable.icon_water_bottle
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Text("${value} ml", style = MaterialTheme.typography.titleMediumEmphasized)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { presets[index] = (value - 50).coerceAtLeast(50) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            IconButton(onClick = { presets[index] = (value + 50).coerceAtMost(1000) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                    if (index < 2) HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "3. Button Grid (Visual)")
@Composable
fun QuickAddStyle3VisualPreview() {
    AquaTheme {
        SettingsCard("Quick add", "Choose three amounts for your buttons") {
            val presets = remember { mutableStateListOf(200, 350, 500) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEachIndexed { index, value ->
                    val icon = when (index) {
                        0 -> R.drawable.icon_mug
                        1 -> R.drawable.icon_glass_cup
                        else -> R.drawable.icon_water_bottle
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text("${value} ml", style = MaterialTheme.typography.labelLargeEmphasized)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledIconButton(
                                    onClick = { presets[index] = (value - 50).coerceAtLeast(50) },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(16.dp))
                                }
                                FilledIconButton(
                                    onClick = { presets[index] = (value + 50).coerceAtMost(1000) },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
