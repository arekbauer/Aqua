package com.arekb.aqua.ui.screens.settings.subscreens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.ui.theme.AquaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDataManagementScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysDataManagementSections() {
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsDataManagementScreen(
                    onExportData = {},
                    onImportData = {},
                    onWipeAllData = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("100% On-Device Storage").assertIsDisplayed()
        composeRule.onNodeWithText("Backup & Restore").assertIsDisplayed()
        composeRule.onNodeWithText("Danger Zone").assertIsDisplayed()
        composeRule.onNodeWithText("Export to JSON").assertIsDisplayed()
        composeRule.onNodeWithText("Import JSON Backup").assertIsDisplayed()
        composeRule.onNodeWithText("Wipe all history").assertIsDisplayed()
    }

    @Test
    fun buttonClicksTriggerCallbacks() {
        var exported = false
        var imported = false
        var wiped = false
        var backed = false

        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsDataManagementScreen(
                    onExportData = { exported = true },
                    onImportData = { imported = true },
                    onWipeAllData = { wiped = true },
                    onBack = { backed = true }
                )
            }
        }

        composeRule.onNodeWithText("Export to JSON").performClick()
        assertEquals(true, exported)

        composeRule.onNodeWithText("Import JSON Backup").performClick()
        assertEquals(true, imported)

        composeRule.onNodeWithText("Wipe all history").performClick()
        assertEquals(true, wiped)

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        assertEquals(true, backed)
    }
}
