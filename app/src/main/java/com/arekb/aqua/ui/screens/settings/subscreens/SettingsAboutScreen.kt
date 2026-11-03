package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.settings.components.SettingsCard
import com.arekb.aqua.ui.theme.AquaTheme
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val versionName = rememberVersionName(context)
    val appIconBitmap = rememberAppIconBitmap(context)
    var showLicensesSheet by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = "About",
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = "Aqua App Icon",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Aqua",
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Clean, private, local-first hydration tracking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Version $versionName",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(
                    title = "Developer",
                    subtitle = "Created & maintained"
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AB",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arek Bauer",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(
                    title = "Source Code & Feedback",
                    subtitle = "Explore the code or submit feature requests and bug reports"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AboutLinkItem(
                            title = "GitHub Repository",
                            subtitle = "View source code and release history",
                            icon = Icons.Default.Code,
                            onClick = { uriHandler.openUri("https://github.com/arekbauer/Aqua") }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        AboutLinkItem(
                            title = "Report Bugs & Suggest Features",
                            subtitle = "Open an issue or idea on GitHub",
                            icon = Icons.Default.BugReport,
                            onClick = { uriHandler.openUri("https://github.com/arekbauer/Aqua/issues") }
                        )
                    }
                }
            }

            item {
                SettingsCard(
                    title = "Open Source & Architecture",
                    subtitle = "Licensing and tech stack"
                ) {
                    Column {
                        Text(
                            text = "Aqua is free and open-source software.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Licensed under GNU GPLv3",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showLicensesSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("View Third-Party Open Source Licenses")
                        }
                    }
                }
            }
        }
    }

    if (showLicensesSheet) {
        val libraries by produceLibraries()
        ModalBottomSheet(
            onDismissRequest = { showLicensesSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Open Source Licenses",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LibrariesContainer(
                        libraries = libraries,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutLinkItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun rememberVersionName(context: android.content.Context): String {
    return androidx.compose.runtime.remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }
}

@Composable
private fun rememberAppIconBitmap(context: android.content.Context): ImageBitmap? {
    return remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(width = 128, height = 128).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

@Preview(name = "Settings About Screen · Light", showBackground = true)
@Composable
private fun SettingsAboutScreenLightPreview() {
    AquaTheme(darkTheme = false) {
        SettingsAboutScreen(onBack = {})
    }
}

@Preview(name = "Settings About Screen · Dark", showBackground = true)
@Composable
private fun SettingsAboutScreenDarkPreview() {
    AquaTheme(darkTheme = true) {
        SettingsAboutScreen(onBack = {})
    }
}
