package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.AppTheme
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(
    settings: HydrationSettings,
    modifier: Modifier = Modifier,
    onSave: (HydrationSettings) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var theme by remember(settings.appTheme) { mutableStateOf(settings.appTheme) }

    fun handleThemeChange(newTheme: AppTheme) {
        if (newTheme != theme) {
            theme = newTheme
            onSave(settings.copy(appTheme = newTheme))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = "Appearance",
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
                SettingsCard("Theme", "Choose application theme") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = theme == AppTheme.SYSTEM,
                            onClick = { handleThemeChange(AppTheme.SYSTEM) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("System")
                        }
                        SegmentedButton(
                            selected = theme == AppTheme.LIGHT,
                            onClick = { handleThemeChange(AppTheme.LIGHT) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Light")
                        }
                        SegmentedButton(
                            selected = theme == AppTheme.DARK,
                            onClick = { handleThemeChange(AppTheme.DARK) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("Dark")
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}
