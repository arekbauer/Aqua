package com.arekb.aqua.ui.screens.today.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ProgressGauge(currentMl: Int, goalMl: Int) {
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
            drawArc(water, -90f, 360f * fraction, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            val diameter = size.minDimension * .75f
            val left = (size.width - diameter) / 2f
            val top = (size.height - diameter) / 2f
            val bottom = top + diameter
            val fillY = bottom - diameter * fraction
            val clip = Path().apply { addOval(Rect(left, top, left + diameter, bottom)) }
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

