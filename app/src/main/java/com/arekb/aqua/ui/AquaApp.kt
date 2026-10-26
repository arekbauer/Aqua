package com.arekb.aqua.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.arekb.aqua.ui.components.UserMessageEffect
import com.arekb.aqua.ui.navigation.AquaDestination
import com.arekb.aqua.ui.navigation.rememberAquaNavigationState
import com.arekb.aqua.ui.screens.CustomEntryDialog
import com.arekb.aqua.ui.screens.DashboardScreen
import com.arekb.aqua.ui.screens.HistoryScreen
import com.arekb.aqua.ui.screens.SettingsScreen
import com.arekb.aqua.ui.theme.AquaTheme
import com.arekb.aqua.ui.viewmodel.DashboardViewModel
import com.arekb.aqua.ui.viewmodel.HistoryViewModel
import com.arekb.aqua.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AquaRoot() {
    AquaTheme {
        val navigationState = rememberAquaNavigationState()
        val snackbarHostState = remember { SnackbarHostState() }
        val selectedDestination = navigationState.topLevelRoute
        val entryProvider = entryProvider<NavKey> {
            entry<AquaDestination> { destination ->
                when (destination) {
                    AquaDestination.TODAY -> DashboardRoute(
                        modifier = Modifier,
                        snackbarHostState = snackbarHostState,
                        onOpenSettings = dropUnlessResumed {
                            navigationState.navigateToTopLevel(AquaDestination.SETTINGS)
                        }
                    )
                    AquaDestination.HISTORY -> HistoryRoute(Modifier)
                    AquaDestination.SETTINGS -> SettingsRoute(Modifier, snackbarHostState)
                }
            }
        }
        val entries = navigationState.toDecoratedEntries(entryProvider)

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        content = {
                            AquaDestination.entries.forEach { destination ->
                                val selected = selectedDestination == destination
                                Surface(
                                    onClick = dropUnlessResumed {
                                        navigationState.navigateToTopLevel(destination)
                                    },
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                    modifier = Modifier.semantics { this.selected = selected }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = destination.symbol,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = destination.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavDisplay(
                entries = entries,
                modifier = Modifier.padding(padding),
                onBack = navigationState::goBack
            )
        }
    }
}

private val AquaDestination.label: String
    get() = when (this) {
        AquaDestination.TODAY -> "Today"
        AquaDestination.HISTORY -> "History"
        AquaDestination.SETTINGS -> "Settings"
    }

private val AquaDestination.symbol: String
    get() = when (this) {
        AquaDestination.TODAY -> "◉"
        AquaDestination.HISTORY -> "▤"
        AquaDestination.SETTINGS -> "⚙"
    }

@Composable
private fun DashboardRoute(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
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
    val viewModel: HistoryViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state.days, modifier)
}

@Composable
private fun SettingsRoute(modifier: Modifier, snackbarHostState: SnackbarHostState) {
    val viewModel: SettingsViewModel = hiltViewModel()
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




