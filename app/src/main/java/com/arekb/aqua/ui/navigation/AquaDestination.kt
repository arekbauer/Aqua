package com.arekb.aqua.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class AquaDestination : NavKey {
    TODAY,
    HISTORY,
    SETTINGS
}

@Serializable
data object SettingsPreferences : NavKey

@Serializable
data object SettingsAppearance : NavKey

@Serializable
data object SettingsDataManagement : NavKey

@Serializable
data object SettingsAbout : NavKey
