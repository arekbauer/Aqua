package com.arekb.aqua.ui.screens.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.domain.model.AppTheme
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.ui.screens.settings.subscreens.SettingsAppearanceScreen
import com.arekb.aqua.ui.screens.settings.subscreens.SettingsPreferencesScreen
import com.arekb.aqua.ui.theme.AquaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mainScreenShowsAllCategoriesAndNavigates() {
        var navigatedToPreferences = false
        var navigatedToAppearance = false
        var navigatedToData = false
        var navigatedToAbout = false

        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsMainScreen(
                    onNavigateToPreferences = { navigatedToPreferences = true },
                    onNavigateToAppearance = { navigatedToAppearance = true },
                    onNavigateToDataManagement = { navigatedToData = true },
                    onNavigateToAbout = { navigatedToAbout = true }
                )
            }
        }

        composeRule.onNodeWithText("Preferences").assertIsDisplayed()
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Data Management").assertIsDisplayed()
        composeRule.onNodeWithText("About").assertIsDisplayed()

        composeRule.onNodeWithText("Preferences").performClick()
        assertEquals(true, navigatedToPreferences)

        composeRule.onNodeWithText("Appearance").performClick()
        assertEquals(true, navigatedToAppearance)

        composeRule.onNodeWithText("Data Management").performClick()
        assertEquals(true, navigatedToData)

        composeRule.onNodeWithText("About").performClick()
        assertEquals(true, navigatedToAbout)
    }

    @Test
    fun appearanceScreenThemeSelectionUpdatesState() {
        var savedTheme: AppTheme? = null
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsAppearanceScreen(
                    settings = HydrationSettings(appTheme = AppTheme.LIGHT),
                    onSave = { savedTheme = it.appTheme },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Dark").performClick()
        assertEquals(AppTheme.DARK, savedTheme)
    }

    @Test
    fun preferencesScreenPresetChangeTriggersSave() {
        var savedSettings: HydrationSettings? = null
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsPreferencesScreen(
                    settings = HydrationSettings(presetsMl = listOf(200, 300, 400)),
                    onSave = { savedSettings = it },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("300").performTextReplacement("350")

        composeRule.waitForIdle()

        assertEquals(listOf(200, 350, 400), savedSettings?.presetsMl)
    }

    @Test
    fun preferencesScreenPreservesUserInputWhenSettingsEmitsUpdates() {
        var currentSettings by mutableStateOf(HydrationSettings(presetsMl = listOf(200, 300, 400)))

        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsPreferencesScreen(
                    settings = currentSettings,
                    onSave = { updated -> currentSettings = updated },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("300").performTextReplacement("350")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("350").assertIsDisplayed()
        assertEquals(listOf(200, 350, 400), currentSettings.presetsMl)
    }

    @Test
    fun preferencesScreenTogglingUnitsPreservesValuesWithoutDrift() {
        var currentSettings by mutableStateOf(HydrationSettings(dailyGoalMl = 2000, presetsMl = listOf(200, 350, 500)))

        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsPreferencesScreen(
                    settings = currentSettings,
                    onSave = { updated -> currentSettings = updated },
                    onBack = {}
                )
            }
        }

        // Toggle to oz
        composeRule.onNodeWithText("oz").performClick()
        composeRule.waitForIdle()

        // Toggle back to ml
        composeRule.onNodeWithText("ml").performClick()
        composeRule.waitForIdle()

        // Assert original ml values remain intact without precision drift
        assertEquals(2000, currentSettings.dailyGoalMl)
        assertEquals(listOf(200, 350, 500), currentSettings.presetsMl)
    }
}
