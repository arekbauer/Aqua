package com.arekb.aqua.ui.screens.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.history.todayHeading
import com.arekb.aqua.ui.screens.today.components.CookieReservoirProgressGauge
import com.arekb.aqua.ui.screens.today.components.QuickAddButtonGroup
import com.arekb.aqua.ui.theme.AquaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayUiState,
    modifier: Modifier = Modifier,
    onAdd: (Int) -> Unit,
    onCustom: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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

            if (state.settings.dailyGoalMl > 0 && state.todayTotalMl >= state.settings.dailyGoalMl) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Daily goal reached - well done!",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

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
                onClick = onCustom,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Icon(Icons.Outlined.WaterDrop, contentDescription = null)
                Text("Add custom amount", modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(20.dp))
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
            onCustom = {}
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
            onCustom = {}
        )
    }
}


