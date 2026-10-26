package com.arekb.aqua.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
fun rememberAquaNavigationState(): AquaNavigationState {
    val todayBackStack = rememberNavBackStack(AquaDestination.TODAY)
    val historyBackStack = rememberNavBackStack(AquaDestination.HISTORY)
    val settingsBackStack = rememberNavBackStack(AquaDestination.SETTINGS)
    val backStacks = remember(todayBackStack, historyBackStack, settingsBackStack) {
        mapOf(
            AquaDestination.TODAY to todayBackStack,
            AquaDestination.HISTORY to historyBackStack,
            AquaDestination.SETTINGS to settingsBackStack
        )
    }
    val topLevelRoute = rememberSaveable { mutableStateOf(AquaDestination.TODAY) }

    return remember(topLevelRoute, backStacks) {
        AquaNavigationState(topLevelRoute, backStacks)
    }
}

class AquaNavigationState internal constructor(
    private val topLevelRouteState: MutableState<AquaDestination>,
    private val backStacks: Map<AquaDestination, NavBackStack<NavKey>>
) {
    val topLevelRoute: AquaDestination
        get() = topLevelRouteState.value

    private val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    fun navigateToTopLevel(destination: AquaDestination) {
        topLevelRouteState.value = destination
    }

    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        } else if (topLevelRoute != AquaDestination.TODAY) {
            navigateToTopLevel(AquaDestination.TODAY)
        }
    }

    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, backStack) ->
            rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator<NavKey>()
                ),
                entryProvider = entryProvider
            )
        }
        val visibleTopLevelRoutes = if (topLevelRoute == AquaDestination.TODAY) {
            listOf(AquaDestination.TODAY)
        } else {
            listOf(AquaDestination.TODAY, topLevelRoute)
        }
        return visibleTopLevelRoutes.flatMap { decoratedEntries.getValue(it) }
    }
}
