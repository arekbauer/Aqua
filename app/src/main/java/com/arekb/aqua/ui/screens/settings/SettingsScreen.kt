package com.arekb.aqua.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCategoryItem

@Composable
fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToPreferences: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = "Settings",
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        // Four fixed categories: a plain scrollable Column avoids lazy-list overhead.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp)
        ) {
            SettingsCategoryItem(
                title = "Preferences",
                subtitle = "Daily goal, quick add presets, and units",
                icon = Icons.Outlined.WaterDrop,
                onClick = onNavigateToPreferences
            )
            Spacer(Modifier.height(14.dp))

            SettingsCategoryItem(
                title = "Appearance",
                subtitle = "Light and dark theme",
                icon = Icons.Outlined.Palette,
                onClick = onNavigateToAppearance
            )
            Spacer(Modifier.height(14.dp))

            SettingsCategoryItem(
                title = "Data Management",
                subtitle = "Manage your history",
                icon = Icons.Outlined.Storage,
                onClick = onNavigateToDataManagement
            )
            Spacer(Modifier.height(14.dp))

            SettingsCategoryItem(
                title = "About",
                subtitle = "App version, developer info, and open source links",
                icon = Icons.Outlined.Info,
                onClick = onNavigateToAbout
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}
