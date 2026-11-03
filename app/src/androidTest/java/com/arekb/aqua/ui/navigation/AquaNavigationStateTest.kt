package com.arekb.aqua.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AquaNavigationStateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun defaultDestinationIsToday() {
        lateinit var state: AquaNavigationState
        composeRule.setContent {
            state = rememberAquaNavigationState()
        }

        assertEquals(AquaDestination.TODAY, state.topLevelRoute)
    }

    @Test
    fun navigateToTopLevel_changesTopLevelRoute() {
        lateinit var state: AquaNavigationState
        composeRule.setContent {
            state = rememberAquaNavigationState()
        }

        composeRule.runOnIdle {
            state.navigateToTopLevel(AquaDestination.SETTINGS)
        }

        assertEquals(AquaDestination.SETTINGS, state.topLevelRoute)
    }

    @Test
    fun navigateToSubscreenAndGoBack_popsSubscreen() {
        lateinit var state: AquaNavigationState
        composeRule.setContent {
            state = rememberAquaNavigationState()
        }

        composeRule.runOnIdle {
            state.navigateToTopLevel(AquaDestination.SETTINGS)
            state.navigateTo(SettingsPreferences)
        }

        assertEquals(AquaDestination.SETTINGS, state.topLevelRoute)

        composeRule.runOnIdle {
            state.goBack()
        }

        assertEquals(AquaDestination.SETTINGS, state.topLevelRoute)
    }

    @Test
    fun goBackFromTopLevelNonHomeDestination_navigatesToToday() {
        lateinit var state: AquaNavigationState
        composeRule.setContent {
            state = rememberAquaNavigationState()
        }

        composeRule.runOnIdle {
            state.navigateToTopLevel(AquaDestination.HISTORY)
        }

        assertEquals(AquaDestination.HISTORY, state.topLevelRoute)

        composeRule.runOnIdle {
            state.goBack()
        }

        assertEquals(AquaDestination.TODAY, state.topLevelRoute)
    }
}
