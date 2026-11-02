package com.arekb.aqua.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCategoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToPreferences: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp)
        ) {
            item {
                SettingsCategoryItem(
                    title = "Preferences",
                    subtitle = "Daily goal, quick add presets, and units",
                    icon = Icons.Outlined.WaterDrop,
                    onClick = onNavigateToPreferences
                )
                Spacer(Modifier.height(14.dp))
            }

            item {
                SettingsCategoryItem(
                    title = "Appearance",
                    subtitle = "Light and dark theme",
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToAppearance
                )
                Spacer(Modifier.height(14.dp))
            }

            item {
                SettingsCategoryItem(
                    title = "Data Management",
                    subtitle = "Manage your history",
                    icon = Icons.Outlined.Storage,
                    onClick = onNavigateToDataManagement
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}