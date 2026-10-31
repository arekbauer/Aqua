package com.arekb.aqua.ui.screens.today.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.arekb.aqua.ui.theme.AquaTheme
import androidx.graphics.shapes.Morph
import com.arekb.aqua.domain.model.WaterIntake
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    var previousMl by remember { mutableIntStateOf(currentMl) }
    var shapeIndex by remember { mutableIntStateOf(0) }
    var shapeStartIndex by remember { mutableIntStateOf(0) }
    val shapeMorphProgress = remember { Animatable(1f) }
    val reservoirBounce = remember { Animatable(1f) }

    LaunchedEffect(currentMl) {
        if (previousMl != currentMl) {
            previousMl = currentMl
            shapeStartIndex = shapeIndex
            shapeIndex = (shapeIndex + 1) % RESERVOIR_SHAPE_STAGES.size
            shapeMorphProgress.snapTo(0f)
            reservoirBounce.snapTo(0.94f)
            launch {
                reservoirBounce.animateTo(
                    targetValue = 1.10f,
                    animationSpec = spring(dampingRatio = 0.95f, stiffness = 700f)
                )
                reservoirBounce.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.58f, stiffness = 500f)
                )
            }
            shapeMorphProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900)
            )
        }
    }
    val shapeMorph = remember(shapeStartIndex, shapeIndex) {
        Morph(RESERVOIR_SHAPE_STAGES[shapeStartIndex], RESERVOIR_SHAPE_STAGES[shapeIndex])
    }
    val reservoirShape = ReservoirMorphShape(shapeMorph, shapeMorphProgress.value)

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = reservoirBounce.value
            scaleY = reservoirBounce.value
        },
        contentAlignment = Alignment.Center
    ) {
        CookieReservoirLiquid(
            fraction = fraction,
            color = colors.primary,
            shape = reservoirShape
        )
        CookieReservoirReadout(
            currentMl = currentMl
        )
    }
}

/**
 * Edit this list to choose the silhouettes and cycle order. Each change to currentMl advances
 * to the next shape, regardless of the daily goal or the amount added.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val RESERVOIR_SHAPE_STAGES = listOf(
    //MaterialShapes.Clover4Leaf,
    MaterialShapes.Sunny,
    MaterialShapes.Cookie7Sided,
    MaterialShapes.SoftBurst,
    MaterialShapes.Flower,
)

// Adjust this value to resize the reservoir outline and its liquid together.
private val RESERVOIR_SIZE = 256.dp

/** A Material 3 polygon morph rendered as a Compose clip shape. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class ReservoirMorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress)
        val scale = Matrix().apply { scale(x = size.width, y = size.height) }
        path.transform(scale)
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }
}

@Composable
private fun CookieReservoirLiquid(
    fraction: Float,
    color: androidx.compose.ui.graphics.Color,
    shape: Shape
) {
    Box(Modifier.size(RESERVOIR_SIZE).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
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
private fun CookieReservoirReadout(
    currentMl: Int
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            currentMl.toString(),
            style = MaterialTheme.typography.displayLargeEmphasized,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
        Text("ml today", style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
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
fun DrinkPickerBottomSheet(onAdd: (Int) -> Unit) {
    var customAmount by rememberSaveable { mutableStateOf("") }
    val customMl = customAmount.toIntOrNull()?.takeIf { it > 0 }
    val servings = listOf(
        ServingOption("Juice Box", 200, Icons.Filled.LocalDrink),
        ServingOption("Glass", 250, Icons.Filled.WaterDrop),
        ServingOption("Can", 330, Icons.Filled.LocalDrink),
        ServingOption("Coffee Cup ", 350, Icons.Filled.Coffee),
        ServingOption("Bottle", 500, Icons.Filled.WaterDrop),
        ServingOption("Pint", 568, Icons.Filled.LocalBar)
    )

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Add a drink", style = MaterialTheme.typography.titleLargeEmphasized)
            Text("Choose a typical serving size", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        servings.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { serving ->
                    FilledTonalButton(
                        onClick = { onAdd(serving.amountMl) },
                        modifier = Modifier.weight(1f).aspectRatio(1.25f),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Icon(serving.icon, contentDescription = null, modifier = Modifier.sizeIn(maxWidth = 22.dp, maxHeight = 22.dp))
                            Text(serving.label,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Text("${serving.amountMl} ml", style = MaterialTheme.typography.labelLargeEmphasized)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text("Custom amount", style = MaterialTheme.typography.titleLargeEmphasized)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customAmount,
                onValueChange = { customAmount = it.filter(Char::isDigit).take(5) },
                modifier = Modifier.weight(1f),
                label = { Text("Amount in ml") },
                shape = RoundedCornerShape(100.dp),
                suffix = { Text("ml") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
            OutlinedButton(onClick = { customMl?.let(onAdd) }, enabled = customMl != null, modifier = Modifier.height(56.dp)) {
                Text("Add")
            }
        }
    }
}

@Composable
fun IntakeLogRow(entry: WaterIntake, onEdit: () -> Unit, onDelete: () -> Unit) {
    val time = remember(entry.timestampMillis) {
        Instant.ofEpochMilli(entry.timestampMillis).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${entry.amountMl} ml", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("$time", modifier = Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit ${entry.amountMl} ml log", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${entry.amountMl} ml log", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class ServingOption(
    val label: String,
    val amountMl: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

