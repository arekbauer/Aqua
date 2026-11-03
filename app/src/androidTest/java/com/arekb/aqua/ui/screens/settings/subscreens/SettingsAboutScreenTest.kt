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
class SettingsAboutScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysAboutAppInformation() {
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsAboutScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("Aqua").assertIsDisplayed()
        composeRule.onNodeWithText("Clean, private, local-first hydration tracking").assertIsDisplayed()
        composeRule.onNodeWithText("Developer").assertIsDisplayed()
        composeRule.onNodeWithText("Arek Bauer").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub Repository").assertIsDisplayed()
        composeRule.onNodeWithText("Report Bugs & Suggest Features").assertIsDisplayed()
        composeRule.onNodeWithText("Licensed under GNU GPLv3").assertIsDisplayed()
        composeRule.onNodeWithText("View Third-Party Open Source Licenses").assertIsDisplayed()
    }

    @Test
    fun backButtonTriggersCallback() {
        var backed = false
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsAboutScreen(onBack = { backed = true })
            }
        }

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        assertEquals(true, backed)
    }

    @Test
    fun openLicensesSheetDisplaysBottomSheet() {
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                SettingsAboutScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("View Third-Party Open Source Licenses").performClick()
        composeRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()
    }
}
