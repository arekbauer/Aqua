package com.arekb.aqua.ui.screens.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.screens.history.todayHeading
import com.arekb.aqua.ui.theme.AquaTheme
import com.arekb.aqua.ui.screens.today.components.ProgressGauge
import com.arekb.aqua.ui.screens.today.components.QuickAddButtonGroup

@Composable
fun TodayScreen(
    state: TodayUiState,
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
        Spacer(Modifier.height(32.dp))
        QuickAddButtonGroup(
            presetsMl = state.settings.presetsMl,
            modifier = Modifier.fillMaxWidth(),
            onAdd = onAdd
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onCustom, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)) {
            Text("＋  Add custom amount")
        }
        Spacer(Modifier.height(22.dp))
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
            onAdd = {},
            onCustom = {},
            onOpenSettings = {}
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
            onAdd = {},
            onCustom = {},
            onOpenSettings = {}
        )
    }
}


