package com.arekb.aqua.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class AquaDestination : NavKey {
    TODAY,
    HISTORY,
    SETTINGS
}
