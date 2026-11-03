package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCard
import com.arekb.aqua.ui.theme.AquaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDataManagementScreen(
    modifier: Modifier = Modifier,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onWipeAllData: () -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = "Data Management",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "100% On-Device Storage",
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Your data stays private on your phone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(Modifier.height(14.dp))

                        val privacyPoints = listOf(
                            "Local Storage" to "All intake history, goals, and settings are saved locally on your device.",
                            "Zero Cloud Servers" to "No external cloud sync, account registration, or remote databases.",
                            "Absolute Privacy" to "No telemetry, tracking, or data sharing. Your personal data is never sold."
                        )

                        privacyPoints.forEachIndexed { index, (title, description) ->
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            if (index < privacyPoints.lastIndex) {
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            item {
                SettingsCard(
                    title = "Backup & Restore",
                    subtitle = "Export your data to a JSON backup file or restore from a previous export"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onExportData,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Export to JSON")
                        }

                        OutlinedButton(
                            onClick = onImportData,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Import JSON Backup")
                        }
                    }
                }
            }

            item {
                SettingsCard(
                    title = "Danger Zone",
                    subtitle = "Permanently remove your hydration records"
                ) {
                    OutlinedButton(
                        onClick = onWipeAllData,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Wipe all history")
                    }
                }
            }
        }
    }
}

@Preview(name = "Settings Data Management · Light", showBackground = true)
@Composable
private fun SettingsDataManagementScreenLightPreview() {
    AquaTheme(darkTheme = false) {
        SettingsDataManagementScreen(
            onExportData = {},
            onImportData = {},
            onWipeAllData = {},
            onBack = {}
        )
    }
}

@Preview(name = "Settings Data Management · Dark", showBackground = true)
@Composable
private fun SettingsDataManagementScreenDarkPreview() {
    AquaTheme(darkTheme = true) {
        SettingsDataManagementScreen(
            onExportData = {},
            onImportData = {},
            onWipeAllData = {},
            onBack = {}
        )
    }
}
