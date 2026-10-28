package com.arekb.aqua.ui.screens.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.history.todayHeading
import com.arekb.aqua.ui.screens.today.components.CookieReservoirProgressGauge
import com.arekb.aqua.ui.screens.today.components.QuickAddButtonGroup
import com.arekb.aqua.ui.theme.AquaTheme

@Composable
fun TodayScreen(
    state: TodayUiState,
    modifier: Modifier = Modifier,
    onAdd: (Int) -> Unit,
    onCustom: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AquaTopAppBar(todayHeading(state.today)) },
    ) { innerPadding ->
        Column(
            modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            CookieReservoirProgressGauge(state.todayTotalMl, state.settings.dailyGoalMl)
            Spacer(Modifier.height(32.dp))
            QuickAddButtonGroup(
                presetsMl = state.settings.presetsMl,
                modifier = Modifier.fillMaxWidth(),
                onAdd = onAdd
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCustom,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("＋  Add custom amount")
            }
            Spacer(Modifier.height(22.dp))
        }
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


