package com.arekb.aqua.ui.screens.today.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.aqua.ui.theme.AquaTheme
import kotlin.math.cos
import kotlin.math.sin

/** A restrained typographic version with a slim progress line. */
@Composable
fun MinimalProgressGauge(currentMl: Int, goalMl: Int, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "minimal hydration progress"
    )
    val percent = (currentMl * 100f / goalMl.coerceAtLeast(1)).toInt()
    val colors = MaterialTheme.colorScheme

    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = currentMl.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ml",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$percent%",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text("of $goalMl ml today", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().height(7.dp).clip(CircleShape)
                .background(colors.surfaceContainerHighest)
        ) {
            Box(
                Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(CircleShape)
                    .background(colors.primary)
            )
        }
    }
}

/** Minimal typography paired with a narrow vertical water level. */
@Composable
fun MinimalVerticalProgressGauge(currentMl: Int, goalMl: Int, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "minimal vertical hydration progress"
    )
    val colors = MaterialTheme.colorScheme
    val percent = (currentMl * 100f / goalMl.coerceAtLeast(1)).toInt()

    Row(
        modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("TODAY", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(currentMl.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text(" ml", modifier = Modifier.padding(start = 5.dp, bottom = 8.dp), color = colors.onSurfaceVariant)
            }
            Text("of $goalMl ml", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
        Box(
            Modifier.width(92.dp).height(164.dp).clip(RoundedCornerShape(32.dp))
                .background(colors.surfaceContainerHighest).padding(7.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().fillMaxHeight(fraction).align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(26.dp)).background(colors.primary)
            )
            Text(
                "$percent%",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                color = if (fraction > 0.48f) colors.onPrimary else colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Material cookie-shaped reservoir framed by a circular progress ring. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CookieReservoirProgressGauge(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier
) {
    val fraction by animateFloatAsState(
        (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(900), label = "cookie reservoir hydration progress"
    )
    val colors = MaterialTheme.colorScheme
    val remainingMl = (goalMl - currentMl).coerceAtLeast(0)
    val reservoirShape = MaterialShapes.Cookie7Sided.toShape()

    Box(
        contentAlignment = Alignment.Center
    ) {
        CookieReservoirLiquid(
            fraction = fraction,
            color = colors.primary,
            shape = reservoirShape
        )
        CookieReservoirReadout(
            currentMl = currentMl,
            remainingMl = remainingMl,
            goalMl = goalMl
        )
    }
}

@Composable
private fun CookieReservoirLiquid(
    fraction: Float,
    color: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape
) {
    Box(Modifier.size(220.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Canvas(Modifier.fillMaxSize()) {
            val fillY = size.height * (1f - fraction)
            val wave = Path().apply {
                moveTo(0f, fillY)
                cubicTo(size.width * .25f, fillY - 10.dp.toPx(), size.width * .35f, fillY + 10.dp.toPx(), size.width * .5f, fillY)
                cubicTo(size.width * .68f, fillY - 8.dp.toPx(), size.width * .82f, fillY + 8.dp.toPx(), size.width, fillY)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wave, color.copy(alpha = .28f))
        }
    }
}

@Composable
private fun CookieReservoirReadout(currentMl: Int, remainingMl: Int, goalMl: Int) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(currentMl.toString(), style = MaterialTheme.typography.displayMedium, color = colors.onSurface, fontWeight = FontWeight.Bold)
        Text("ml drank", style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text("$remainingMl ml left", style = MaterialTheme.typography.bodyMedium, color = colors.primary, fontWeight = FontWeight.Bold)
        Text("of $goalMl ml goal", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Preview(name = "Cookie reservoir · drank first", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CookieReservoirProgressGaugePreview() {
    AquaTheme {
        Surface {
            CookieReservoirProgressGauge(currentMl = 1_250, goalMl = 2_000)
        }
    }
}

@Preview(name = "Gauge · minimal", showBackground = true)
@Composable
private fun MinimalProgressGaugePreview() {
    AquaTheme {
        Surface(Modifier.padding(20.dp)) {
            MinimalProgressGauge(currentMl = 1_250, goalMl = 2_000)
        }
    }
}

@Preview(name = "Gauge hybrid · minimal vertical", showBackground = true)
@Composable
private fun MinimalVerticalProgressGaugePreview() {
    AquaTheme {
        Surface(Modifier.padding(20.dp)) {
            MinimalVerticalProgressGauge(currentMl = 1_250, goalMl = 2_000)
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

