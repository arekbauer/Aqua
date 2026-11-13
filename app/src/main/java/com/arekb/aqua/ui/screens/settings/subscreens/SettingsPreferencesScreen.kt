package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.label
import com.arekb.aqua.domain.model.toSystemMl
import com.arekb.aqua.domain.model.toUserUnit
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.DailyGoalSetting
import com.arekb.aqua.ui.screens.settings.components.QuickAddSettingsGrid
import com.arekb.aqua.ui.screens.settings.components.SettingsCard
import com.arekb.aqua.ui.screens.settings.components.UnitsPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPreferencesScreen(
    settings: HydrationSettings,
    modifier: Modifier = Modifier,
    onSave: (HydrationSettings) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Settings this screen has already pushed to storage. Their echoes coming back through
    // `settings` must not overwrite what the user is currently typing.
    val sentSettings = remember { mutableSetOf<HydrationSettings>() }

    var unit by remember { mutableStateOf(settings.liquidUnit) }
    var goalValue by remember { mutableFloatStateOf(settings.dailyGoalMl.toUserUnit(settings.liquidUnit).toFloat()) }
    var presetTexts by remember {
        mutableStateOf(settings.presetsMl.map { it.toUserUnit(settings.liquidUnit).toString() })
    }

    // Adopt settings that changed from outside this screen (e.g. the stored value finishing
    // loading after the screen was first composed with defaults, or a data import).
    LaunchedEffect(settings) {
        if (settings !in sentSettings) {
            unit = settings.liquidUnit
            goalValue = settings.dailyGoalMl.toUserUnit(settings.liquidUnit).toFloat()
            presetTexts = settings.presetsMl.map { it.toUserUnit(settings.liquidUnit).toString() }
        }
    }

    val goal = goalValue.toInt()
    val presets = presetTexts.mapNotNull(String::toIntOrNull)
    val valid = goal > 0 && presets.size == 3 && presets.all { it > 0 }
    val unitLabel = unit.label()
    
    val sliderRange = if (unit == LiquidUnit.ML) 1000f..5000f else 35f..175f
    val sliderStep = if (unit == LiquidUnit.ML) 100f else 5f

    LaunchedEffect(unit, goalValue, presetTexts) {
        if (valid) {
            val sysGoal = if (goal == settings.dailyGoalMl.toUserUnit(unit)) {
                settings.dailyGoalMl
            } else {
                goal.toSystemMl(unit)
            }
            val currentPresetsUser = settings.presetsMl.map { it.toUserUnit(unit) }
            val sysPresets = if (presets == currentPresetsUser) {
                settings.presetsMl
            } else {
                presets.map { it.toSystemMl(unit) }
            }
            val updated = settings.copy(
                dailyGoalMl = sysGoal,
                presetsMl = sysPresets,
                liquidUnit = unit
            )
            if (updated != settings) {
                sentSettings += updated
                onSave(updated)
            }
        }
    }

    fun handleUnitChange(newUnit: LiquidUnit) {
        if (newUnit != unit) {
            unit = newUnit
            goalValue = settings.dailyGoalMl.toUserUnit(newUnit).toFloat()
            presetTexts = settings.presetsMl.map { it.toUserUnit(newUnit).toString() }
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
        // Three fixed cards: a plain scrollable Column avoids lazy-list overhead.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp)
        ) {
            DailyGoalSetting(
                value = goalValue,
                onValueChange = { goalValue = it },
                unitLabel = unitLabel,
                range = sliderRange,
                step = sliderStep
            )
            Spacer(Modifier.height(14.dp))

            SettingsCard("Quick add", "Choose three amounts for your buttons") {
                QuickAddSettingsGrid(
                    presetTexts = presetTexts,
                    unitLabel = unitLabel,
                    onPresetChange = { index, newValue -> 
                        presetTexts = presetTexts.toMutableList().also { it[index] = newValue }
                    }
                )
            }
            Spacer(Modifier.height(14.dp))

            SettingsCard("Units", "Choose your preferred unit") {
                UnitsPicker(
                    unit = unit,
                    onUnitChange = { handleUnitChange(it) }
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}
