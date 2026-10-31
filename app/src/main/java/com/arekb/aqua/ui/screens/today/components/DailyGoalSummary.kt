package com.arekb.aqua.ui.screens.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.ui.theme.AquaTheme

/** C1 is the primary quiet text treatment; C2 is retained as a compact chip alternative. */
enum class DailyGoalSummaryStyle {
    CENTERED_TEXT,
    GOAL_CHIP
}

@Composable
fun DailyGoalSummary(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    style: DailyGoalSummaryStyle = DailyGoalSummaryStyle.CENTERED_TEXT
) {
    val colors = MaterialTheme.colorScheme
    val remainingMl = (goalMl - currentMl).coerceAtLeast(0)
    val goalReached = goalMl in 1..currentMl
    val remainingLabel = if (goalReached) "Daily goal reached" else "$remainingMl ml to go"

    when (style) {
        DailyGoalSummaryStyle.CENTERED_TEXT -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Daily goal · $goalMl ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }

        DailyGoalSummaryStyle.GOAL_CHIP -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = remainingLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(12.dp))
                Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                    Text(
                        text = "Goal · $goalMl ml",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Daily goal summary · C1 and C2", showBackground = true)
@Composable
private fun DailyGoalSummaryPreview() {
    AquaTheme {
        Surface {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                DailyGoalSummary(currentMl = 1_250, goalMl = 2_000)
                DailyGoalSummary(
                    currentMl = 1_250,
                    goalMl = 2_000,
                    style = DailyGoalSummaryStyle.GOAL_CHIP
                )
            }
        }
    }
}
