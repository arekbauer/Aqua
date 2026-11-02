package com.arekb.aqua.ui.screens.settings.subscreens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.AppTheme
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCard
import com.arekb.aqua.ui.theme.AquaTheme

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    title = "Theme",
                    subtitle = "Choose your preferred application theme"
                ) {
                    ThemePickerIconCards(
                        selectedTheme = theme,
                        onThemeSelected = ::handleThemeChange
                    )
                }
            }

            item {
                val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                val isEnabled = isDynamicColorSupported && settings.isDynamicColorEnabled

                SettingsCard(
                    title = "Dynamic Color",
                    subtitle = "Adapt app colors to match your device wallpaper"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isEnabled)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (isEnabled)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use wallpaper colors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isDynamicColorSupported)
                                    "Dynamic colors applied across the application"
                                else
                                    "Requires Android 12 (API 31) or higher",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { onSave(settings.copy(isDynamicColorEnabled = it)) },
                            enabled = isDynamicColorSupported
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemePickerIconCards(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val themes = listOf(
            Triple(AppTheme.SYSTEM, "System", Icons.Default.AutoMode),
            Triple(AppTheme.LIGHT, "Light", Icons.Default.LightMode),
            Triple(AppTheme.DARK, "Dark", Icons.Default.DarkMode)
        )

        themes.forEach { (appTheme, label, icon) ->
            val isSelected = selectedTheme == appTheme
            Card(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = isSelected,
                        onClick = { onThemeSelected(appTheme) },
                        role = Role.RadioButton
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainer
                ),
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Settings Appearance Screen · Light", showBackground = true)
@Composable
private fun SettingsAppearanceScreenLightPreview() {
    AquaTheme(darkTheme = false) {
        SettingsAppearanceScreen(
            settings = HydrationSettings(appTheme = AppTheme.SYSTEM, isDynamicColorEnabled = true),
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(name = "Settings Appearance Screen · Dark", showBackground = true)
@Composable
private fun SettingsAppearanceScreenDarkPreview() {
    AquaTheme(darkTheme = true) {
        SettingsAppearanceScreen(
            settings = HydrationSettings(appTheme = AppTheme.DARK, isDynamicColorEnabled = false),
            onSave = {},
            onBack = {}
        )
    }
}
