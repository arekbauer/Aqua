package com.arekb.aqua.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arekb.aqua.ui.components.UserMessageEffect
import com.arekb.aqua.ui.screens.CustomEntryDialog
import com.arekb.aqua.ui.screens.DashboardScreen
import com.arekb.aqua.ui.screens.HistoryScreen
import com.arekb.aqua.ui.screens.SettingsScreen
import com.arekb.aqua.ui.theme.AquaTheme
import com.arekb.aqua.ui.viewmodel.DashboardViewModel
import com.arekb.aqua.ui.viewmodel.HistoryViewModel
import com.arekb.aqua.ui.viewmodel.SettingsViewModel

private enum class AppTab(val label: String, val symbol: String) {
    Dashboard("Today", "◉"), History("History", "▤"), Settings("Settings", "⚙")
}

@Composable
fun AquaRoot() {
    AquaTheme {
        var selectedTab by rememberSaveable { mutableStateOf(AppTab.Dashboard) }
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Text(tab.symbol, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            val contentModifier = Modifier.padding(padding)
            when (selectedTab) {
                AppTab.Dashboard -> DashboardRoute(contentModifier, snackbarHostState) {
                    selectedTab = AppTab.Settings
                }
                AppTab.History -> HistoryRoute(contentModifier)
                AppTab.Settings -> SettingsRoute(contentModifier, snackbarHostState)
            }
        }
    }
}

@Composable
private fun DashboardRoute(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomEntry by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(viewModel) { viewModel.refreshDate() }
    UserMessageEffect(state.message, snackbarHostState, viewModel::undo, viewModel::messageShown)

    DashboardScreen(
        state = state,
        modifier = modifier,
        onAdd = viewModel::logWater,
        onCustom = { showCustomEntry = true },
        onOpenSettings = onOpenSettings
    )

    if (showCustomEntry) {
        CustomEntryDialog(
            onDismiss = { showCustomEntry = false },
            onAdd = { amount ->
                showCustomEntry = false
                viewModel.logWater(amount)
            }
        )
    }
}

@Composable
private fun HistoryRoute(modifier: Modifier) {
    val viewModel: HistoryViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state.days, modifier)
}

@Composable
private fun SettingsRoute(modifier: Modifier, snackbarHostState: SnackbarHostState) {
    val viewModel: SettingsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetConfirmation by remember { mutableStateOf(false) }
    UserMessageEffect(state.message, snackbarHostState, onUndo = {}, onMessageShown = viewModel::messageShown)

    SettingsScreen(
        settings = state.settings,
        modifier = modifier,
        onSave = viewModel::saveSettings,
        onReset = { showResetConfirmation = true }
    )

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset today’s total?") },
            text = { Text("All of today’s entries will be removed. Past days will stay in your history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToday()
                    showResetConfirmation = false
                }) { Text("Reset today") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}
