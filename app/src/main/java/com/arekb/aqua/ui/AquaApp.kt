package com.arekb.aqua.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.arekb.aqua.ui.components.UserMessageEffect
import com.arekb.aqua.ui.navigation.AquaDestination
import com.arekb.aqua.ui.navigation.rememberAquaNavigationState
import com.arekb.aqua.ui.screens.today.TodayScreen
import com.arekb.aqua.ui.screens.history.HistoryScreen
import com.arekb.aqua.ui.screens.settings.SettingsScreen
import com.arekb.aqua.ui.theme.AquaTheme
import com.arekb.aqua.ui.screens.today.TodayViewModel
import com.arekb.aqua.ui.screens.history.HistoryViewModel
import com.arekb.aqua.ui.screens.settings.SettingsViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

@Composable
fun AquaRoot() {
    AquaTheme {
        val navigationState = rememberAquaNavigationState()
        val snackbarHostState = remember { SnackbarHostState() }
        val selectedDestination = navigationState.topLevelRoute
        val layoutDirection = LocalLayoutDirection.current
        val entryProvider = entryProvider<NavKey> {
            entry<AquaDestination> { destination ->
                when (destination) {
                    AquaDestination.TODAY -> TodayRoute(
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
                        colors = FloatingToolbarDefaults.standardFloatingToolbarColors()
                    ) {
                        AquaDestination.entries.forEach { destination ->
                            val selected = selectedDestination == destination
                            val selectDestination = dropUnlessResumed {
                                navigationState.navigateToTopLevel(destination)
                            }
                            val (selectedIcon, unselectedIcon) = when (destination) {
                                AquaDestination.TODAY -> Icons.Filled.WaterDrop to Icons.Outlined.WaterDrop
                                AquaDestination.HISTORY -> Icons.Filled.History to Icons.Outlined.History
                                AquaDestination.SETTINGS -> Icons.Filled.Settings to Icons.Outlined.Settings
                            }

                            ToggleButton(
                                checked = selected,
                                colors = ToggleButtonDefaults.colors(),
                                shapes = ToggleButtonShapes(
                                    CircleShape,CircleShape, CircleShape
                                ),
                                onCheckedChange = { checked ->
                                    if (checked && !selected) selectDestination()
                                },
                                modifier = Modifier
                                    .height(56.dp)
                                    .semantics { contentDescription = destination.label },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = if (selected) selectedIcon else unselectedIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = expandHorizontally(
                                            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                        ),
                                        exit = shrinkHorizontally(
                                            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                        )
                                    ) {
                                        Text(
                                            text = destination.label,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavDisplay(
                entries = entries,
                modifier = Modifier
                    .padding(
                        start = padding.calculateStartPadding(layoutDirection),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(layoutDirection)
                    )
                    .navigationBarsPadding(),
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

@Composable
private fun TodayRoute(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit
) {
    val viewModel: TodayViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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

    TodayScreen(
        state = state,
        modifier = modifier,
        onAdd = viewModel::logWater,
        onEdit = viewModel::updateAmount,
        onDelete = viewModel::delete
    )
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














