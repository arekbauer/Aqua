package com.arekb.aqua.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlin.math.roundToInt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import com.arekb.aqua.R

@Composable
fun DailyGoalSetting(
    value: Float,
    onValueChange: (Float) -> Unit,
    unitLabel: String,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 1000f..5000f,
    step: Float = 100f
) {
    val sliderState = remember(range) { 
        SliderState(value = value, trackRange = range)
    }
    
    LaunchedEffect(value) {
        if (sliderState.value != value) {
            sliderState.value = value
        }
    }

    SettingsCard("Daily goal", "Your target for each day") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
        ) {
            Text(
                text = "${value.toInt()} $unitLabel",
                style = MaterialTheme.typography.displaySmallEmphasized,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Slider(
                state = sliderState,
                onValueChange = { newValue -> 
                    val snappedValue = (newValue / step).roundToInt() * step
                    onValueChange(snappedValue)
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${range.start.toInt()} $unitLabel", style = MaterialTheme.typography.bodySmall)
                Text("${range.endInclusive.toInt()} $unitLabel", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsCategoryItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMediumEmphasized)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
fun SettingsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMediumEmphasized, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun QuickAddSettingsGrid(
    presetTexts: List<String>,
    unitLabel: String,
    onPresetChange: (index: Int, newValue: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presetTexts.forEachIndexed { index, textValue ->
            val icon = when (index) {
                0 -> R.drawable.icon_mug
                1 -> R.drawable.icon_glass_cup
                else -> R.drawable.icon_water_bottle
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    
                    BasicTextField(
                        value = textValue,
                        onValueChange = { onPresetChange(index, it.filter(Char::isDigit).take(4)) },
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
                                    Text("  $unitLabel", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(0.8f).padding(top = 2.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}