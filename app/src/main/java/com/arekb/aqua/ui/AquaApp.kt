package com.arekb.aqua.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.arekb.aqua.domain.model.AppTheme
import com.arekb.aqua.ui.components.UserMessageEffect
import com.arekb.aqua.ui.navigation.AquaDestination
import com.arekb.aqua.ui.navigation.rememberAquaNavigationState
import com.arekb.aqua.ui.screens.history.HistoryScreen
import com.arekb.aqua.ui.screens.history.HistoryViewModel
import com.arekb.aqua.ui.screens.settings.SettingsViewModel
import com.arekb.aqua.ui.screens.today.TodayScreen
import com.arekb.aqua.ui.screens.today.TodayViewModel
import com.arekb.aqua.ui.theme.AquaTheme
import java.time.LocalDate

@Composable
fun AquaRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val appTheme = settingsState.settings.appTheme
    val dynamicColor = settingsState.settings.isDynamicColorEnabled
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    AquaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        val navigationState = rememberAquaNavigationState()
        val snackbarHostState = remember { SnackbarHostState() }
        val selectedDestination = navigationState.topLevelRoute
        val layoutDirection = LocalLayoutDirection.current
        // Remembered so the provider (and the entry decoration keyed on it) isn't rebuilt on every
        // recomposition of the root, e.g. when the theme or window size changes.
        val entryProvider = remember(navigationState, snackbarHostState) {
            entryProvider<NavKey> {
                entry<AquaDestination> { destination ->
                    when (destination) {
                        AquaDestination.TODAY -> TodayRoute(
                            modifier = Modifier,
                            snackbarHostState = snackbarHostState
                        )
                        AquaDestination.HISTORY -> HistoryRoute(Modifier)
                        AquaDestination.SETTINGS -> SettingsRoute(
                            modifier = Modifier,
                            navigateTo = navigationState::navigateTo
                        )
                    }
                }
                entry<com.arekb.aqua.ui.navigation.SettingsPreferences> {
                    SettingsPreferencesRoute(
                        modifier = Modifier,
                        onBack = navigationState::goBack
                    )
                }
                entry<com.arekb.aqua.ui.navigation.SettingsAppearance> {
                    SettingsAppearanceRoute(
                        modifier = Modifier,
                        onBack = navigationState::goBack
                    )
                }
                entry<com.arekb.aqua.ui.navigation.SettingsDataManagement> {
                    SettingsDataManagementRoute(
                        modifier = Modifier,
                        snackbarHostState = snackbarHostState,
                        onBack = navigationState::goBack
                    )
                }
                entry<com.arekb.aqua.ui.navigation.SettingsAbout> {
                    SettingsAboutRoute(
                        modifier = Modifier,
                        onBack = navigationState::goBack
                    )
                }
            }
        }
        val entries = navigationState.toDecoratedEntries(entryProvider)

        val adaptiveInfo = currentWindowAdaptiveInfo()
        val customNavSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

        NavigationSuiteScaffold(
            layoutType = if (customNavSuiteType == NavigationSuiteType.NavigationBar) NavigationSuiteType.None else customNavSuiteType,
            navigationSuiteItems = {
                AquaDestination.entries.forEach { destination ->
                    val selected = selectedDestination == destination
                    val (selectedIcon, unselectedIcon) = when (destination) {
                        AquaDestination.TODAY -> Icons.Filled.WaterDrop to Icons.Outlined.WaterDrop
                        AquaDestination.HISTORY -> Icons.Filled.History to Icons.Outlined.History
                        AquaDestination.SETTINGS -> Icons.Filled.Settings to Icons.Outlined.Settings
                    }

                    item(
                        selected = selected,
                        onClick = { if (!selected) navigationState.navigateToTopLevel(destination) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = if (selected) selectedIcon else unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        }
                    )
                }
            }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (customNavSuiteType == NavigationSuiteType.NavigationBar) {
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
                                    val (selectedIcon, unselectedIcon) = when (destination) {
                                        AquaDestination.TODAY -> Icons.Filled.WaterDrop to Icons.Outlined.WaterDrop
                                        AquaDestination.HISTORY -> Icons.Filled.History to Icons.Outlined.History
                                        AquaDestination.SETTINGS -> Icons.Filled.Settings to Icons.Outlined.Settings
                                    }

                                    ToggleButton(
                                        checked = selected,
                                        colors = ToggleButtonDefaults.colors(),
                                        shapes = ToggleButtonShapes(
                                            CircleShape, CircleShape, CircleShape
                                        ),
                                        onCheckedChange = { checked ->
                                            if (checked && !selected) navigationState.navigateToTopLevel(destination)
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
    snackbarHostState: SnackbarHostState
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
    // ON_RESUME is delivered to a newly added observer when the lifecycle is already resumed, so
    // the observer above also covers the initial refresh without a separate LaunchedEffect.
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    HistoryScreen(state, modifier)
}

@Composable
private fun SettingsRoute(modifier: Modifier, navigateTo: (NavKey) -> Unit) {
    com.arekb.aqua.ui.screens.settings.SettingsMainScreen(
        modifier = modifier,
        onNavigateToPreferences = { navigateTo(com.arekb.aqua.ui.navigation.SettingsPreferences) },
        onNavigateToAppearance = { navigateTo(com.arekb.aqua.ui.navigation.SettingsAppearance) },
        onNavigateToDataManagement = { navigateTo(com.arekb.aqua.ui.navigation.SettingsDataManagement) },
        onNavigateToAbout = { navigateTo(com.arekb.aqua.ui.navigation.SettingsAbout) }
    )
}

@Composable
private fun SettingsAboutRoute(modifier: Modifier, onBack: () -> Unit) {
    com.arekb.aqua.ui.screens.settings.subscreens.SettingsAboutScreen(
        modifier = modifier,
        onBack = onBack
    )
}

@Composable
private fun SettingsPreferencesRoute(modifier: Modifier, onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    com.arekb.aqua.ui.screens.settings.subscreens.SettingsPreferencesScreen(
        settings = state.settings,
        modifier = modifier,
        onSave = viewModel::saveSettings,
        onBack = onBack
    )
}

@Composable
private fun SettingsAppearanceRoute(modifier: Modifier, onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    com.arekb.aqua.ui.screens.settings.subscreens.SettingsAppearanceScreen(
        settings = state.settings,
        modifier = modifier,
        onSave = viewModel::saveSettings,
        onBack = onBack
    )
}

@Composable
private fun SettingsDataManagementRoute(modifier: Modifier, snackbarHostState: SnackbarHostState, onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showWipeConfirmation by rememberSaveable { mutableStateOf(false) }
    var pendingImportUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(context.contentResolver, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    UserMessageEffect(state.message, snackbarHostState, onUndo = {}, onMessageShown = viewModel::messageShown)

    com.arekb.aqua.ui.screens.settings.subscreens.SettingsDataManagementScreen(
        modifier = modifier,
        onExportData = { exportLauncher.launch("aqua_backup_${LocalDate.now()}.json") },
        onImportData = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
        onWipeAllData = { showWipeConfirmation = true },
        onBack = onBack
    )

    if (showWipeConfirmation) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmation = false },
            title = { Text("Wipe all history?") },
            text = { Text("This will permanently delete all your hydration records and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.wipeAllData()
                    showWipeConfirmation = false
                }) { Text("Wipe") }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Import JSON Backup?") },
            text = { Text("Importing this backup will replace your current history and settings with the data from the backup file. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importData(context.contentResolver, uri)
                    pendingImportUri = null
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") }
            }
        )
    }
}














