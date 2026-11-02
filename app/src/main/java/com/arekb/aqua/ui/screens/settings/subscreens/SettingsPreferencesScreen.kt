package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.label
import com.arekb.aqua.domain.model.toSystemMl
import com.arekb.aqua.domain.model.toUserUnit
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPreferencesScreen(
    settings: HydrationSettings,
    modifier: Modifier = Modifier,
    onSave: (HydrationSettings) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var unit by remember(settings.liquidUnit) { mutableStateOf(settings.liquidUnit) }
    var goalText by remember(settings.dailyGoalMl, settings.liquidUnit) { mutableStateOf(settings.dailyGoalMl.toUserUnit(settings.liquidUnit).toString()) }
    var presetTexts by remember(settings.presetsMl, settings.liquidUnit) { mutableStateOf(settings.presetsMl.map { it.toUserUnit(settings.liquidUnit).toString() }) }

    val goal = goalText.toIntOrNull()
    val presets = presetTexts.mapNotNull(String::toIntOrNull)
    val valid = goal != null && goal > 0 && presets.size == 3 && presets.all { it > 0 }
    val unitLabel = unit.label()

    fun handleUnitChange(newUnit: LiquidUnit) {
        if (newUnit != unit) {
            goalText = goalText.toIntOrNull()?.toSystemMl(unit)?.toUserUnit(newUnit)?.toString() ?: ""
            presetTexts = presetTexts.map { it.toIntOrNull()?.toSystemMl(unit)?.toUserUnit(newUnit)?.toString() ?: "" }
            unit = newUnit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = "Preferences",
                scrollBehavior = scrollBehavior,
                onNavIconPressed = onBack
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp)
        ) {
            item {
                SettingsCard("Daily goal", "Your target for each day") {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it.filter(Char::isDigit).take(5) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Daily target") }, suffix = { Text(unitLabel) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                SettingsCard("Quick add", "Choose three amounts for your buttons") {
                    presetTexts.forEachIndexed { index, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { updated -> presetTexts = presetTexts.toMutableList().also { it[index] = updated.filter(Char::isDigit).take(5) } },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
                            label = { Text("Preset ${index + 1}") }, suffix = { Text(unitLabel) }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                SettingsCard("Units", "Choose your preferred unit") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = unit == LiquidUnit.ML,
                            onClick = { handleUnitChange(LiquidUnit.ML) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Milliliters (ml)")
                        }
                        SegmentedButton(
                            selected = unit == LiquidUnit.OZ,
                            onClick = { handleUnitChange(LiquidUnit.OZ) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Fluid Ounces (oz)")
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                Button(
                    onClick = {
                        val sysGoal = goal!!.toSystemMl(unit)
                        val sysPresets = presets.map { it.toSystemMl(unit) }
                        onSave(settings.copy(dailyGoalMl = sysGoal, presetsMl = sysPresets, liquidUnit = unit))
                        onBack()
                    },
                    enabled = valid, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)
                ) { Text("Save changes") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
