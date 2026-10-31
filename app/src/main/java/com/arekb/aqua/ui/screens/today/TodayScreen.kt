package com.arekb.aqua.ui.screens.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.history.todayHeading
import com.arekb.aqua.ui.screens.today.components.CookieReservoirProgressGauge
import com.arekb.aqua.ui.screens.today.components.DailyGoalSummary
import com.arekb.aqua.ui.screens.today.components.DailyGoalSummaryStyle
import com.arekb.aqua.ui.screens.today.components.DrinkPickerBottomSheet
import com.arekb.aqua.ui.screens.today.components.IntakeLogRow
import com.arekb.aqua.ui.screens.today.components.QuickAddButtonGroup
import com.arekb.aqua.ui.theme.AquaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayUiState,
    modifier: Modifier = Modifier,
    onAdd: (Int) -> Unit,
    onEdit: (WaterIntake, Int) -> Unit = { _, _ -> },
    onDelete: (WaterIntake) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showDrinkPicker by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WaterIntake?>(null) }
    var editAmount by remember { mutableStateOf("") }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = todayHeading(state.today),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            CookieReservoirProgressGauge(state.todayTotalMl, state.settings.dailyGoalMl)
            Spacer(Modifier.height(12.dp))
            DailyGoalSummary(
                currentMl = state.todayTotalMl,
                goalMl = state.settings.dailyGoalMl,
                style = DailyGoalSummaryStyle.CENTERED_TEXT
            )
            Spacer(Modifier.height(28.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Quick add",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose an amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            QuickAddButtonGroup(
                presetsMl = state.settings.presetsMl,
                modifier = Modifier.fillMaxWidth(),
                onAdd = onAdd
            )

            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = { showDrinkPicker = true },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Icon(Icons.Outlined.WaterDrop, contentDescription = null)
                Text("Add a drink", modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Today’s logs", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                Text("${state.entries.size} entries", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            if (state.entries.isEmpty()) {
                Text("Your drinks will appear here.", Modifier.fillMaxWidth().padding(vertical = 16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.entries.forEach { entry ->
                    IntakeLogRow(
                        entry = entry,
                        onEdit = { editingEntry = entry; editAmount = entry.amountMl.toString() },
                        onDelete = { onDelete(entry) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showDrinkPicker) {
        ModalBottomSheet(
            onDismissRequest = { showDrinkPicker = false },
            sheetState = sheetState
        ) {
            DrinkPickerBottomSheet(
                onAdd = { amount ->
                    showDrinkPicker = false
                    onAdd(amount)
                },
            )
        }
    }

    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Edit water log") },
            text = {
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { editAmount = it.filter(Char::isDigit).take(5) },
                    label = { Text("Amount in ml") },
                    suffix = { Text("ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editAmount.toIntOrNull()?.takeIf { it > 0 }?.let { onEdit(entry, it) }
                    editingEntry = null
                }, enabled = editAmount.toIntOrNull()?.let { it > 0 } == true) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingEntry = null }) { Text("Cancel") } }
        )
    }
}

@Preview(name = "Today · light", showBackground = true)
@Composable
private fun TodayScreenPreview() {
    AquaTheme(darkTheme = false) {
        TodayScreen(
            state = TodayUiState(
                settings = HydrationSettings(),
                today = "2026-10-27",
                todayTotalMl = 1_250
            ),
            onAdd = {}
        )
    }
}

@Preview(name = "Today · dark", showBackground = true)
@Composable
private fun TodayScreenDarkPreview() {
    AquaTheme(darkTheme = true) {
        TodayScreen(
            state = TodayUiState(
                settings = HydrationSettings(),
                today = "2026-10-27",
                todayTotalMl = 2_250
            ),
            onAdd = {}
        )
    }
}


