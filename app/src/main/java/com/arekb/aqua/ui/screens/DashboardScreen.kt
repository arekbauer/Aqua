package com.arekb.aqua.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.aqua.ui.viewmodel.DashboardUiState

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    modifier: Modifier = Modifier,
    onAdd: (Int) -> Unit,
    onCustom: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, bottom = 112.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aqua", style = MaterialTheme.typography.headlineMedium)
                Text(todayHeading(state.today), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onOpenSettings) { Text("Goal · ${state.settings.dailyGoalMl} ml") }
        }
        Spacer(Modifier.height(18.dp))
        ProgressGauge(state.todayTotalMl, state.settings.dailyGoalMl)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Quick add", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Tap to log", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        state.settings.presetsMl.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { amount ->
                    Button(
                        onClick = { onAdd(amount) },
                        modifier = Modifier.weight(1f).height(74.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("+${amount}ml", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("water", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
        OutlinedButton(onClick = onCustom, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)) {
            Text("＋  Add custom amount", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(22.dp))
        Text("Small sips add up.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ProgressGauge(currentMl: Int, goalMl: Int) {
    val fraction by animateFloatAsState(
        (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(850), label = "hydration progress"
    )
    val water = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val progressPercent = (currentMl * 100f / goalMl.coerceAtLeast(1)).toInt()
    Box(Modifier.size(264.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 13.dp.toPx()
            drawArc(track, -90f, 360f, false, style = Stroke(strokeWidth))
            drawArc(water, -90f, 360f * fraction, false, style = Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            val diameter = size.minDimension * .75f
            val left = (size.width - diameter) / 2f
            val top = (size.height - diameter) / 2f
            val bottom = top + diameter
            val fillY = bottom - diameter * fraction
            val clip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(left, top, left + diameter, bottom)) }
            clipPath(clip) {
                val wave = Path().apply {
                    moveTo(left, fillY)
                    cubicTo(left + diameter * .2f, fillY - 9.dp.toPx(), left + diameter * .34f, fillY + 9.dp.toPx(), left + diameter * .5f, fillY)
                    cubicTo(left + diameter * .68f, fillY - 9.dp.toPx(), left + diameter * .82f, fillY + 9.dp.toPx(), left + diameter, fillY)
                    lineTo(left + diameter, bottom)
                    lineTo(left, bottom)
                    close()
                }
                drawPath(wave, water.copy(alpha = .16f))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TODAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(currentMl.toString(), fontSize = 44.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold)
                Text(" ml", fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("of $goalMl ml", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("$progressPercent%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CustomEntryDialog(onDismiss: () -> Unit, onAdd: (Int) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    val parsed = amount.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom amount") },
        text = {
            Column {
                Text("How much water did you drink?")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit).take(5) },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Amount in ml") }, suffix = { Text("ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                if (amount.isNotEmpty() && (parsed == null || parsed <= 0)) {
                    Text("Enter an amount greater than 0 ml", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(enabled = parsed != null && parsed > 0, onClick = { parsed?.let(onAdd) }) { Text("Add water") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}



