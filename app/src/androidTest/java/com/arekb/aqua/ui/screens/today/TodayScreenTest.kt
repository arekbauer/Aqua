package com.arekb.aqua.ui.screens.today

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.ui.theme.AquaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun quickAddLogsConfiguredAmountAndShowsTodaySummary() {
        var addedAmount = 0
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                TodayScreen(
                    state = TodayUiState(
                        settings = HydrationSettings(dailyGoalMl = 2_000, presetsMl = listOf(200, 350, 500)),
                        today = "2026-10-31",
                        todayTotalMl = 1_250
                    ),
                    onAdd = { addedAmount = it }
                )
            }
        }

        composeRule.onNodeWithText("1250").assertIsDisplayed()
        composeRule.onNodeWithText("750 ml to go").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Add 200 ml of water").performClick()

        assertEquals(200, addedAmount)
    }

    @Test
    fun customDrinkAmountCanBeAddedFromBottomSheet() {
        var addedAmount = 0
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                TodayScreen(
                    state = TodayUiState(settings = HydrationSettings(), today = "2026-10-31"),
                    onAdd = { addedAmount = it }
                )
            }
        }

        composeRule.onNodeWithText("Add a drink").performClick()
        composeRule.onNodeWithText("Amount in ml").performTextReplacement("425")
        composeRule.onNodeWithText("Add").performClick()

        assertEquals(425, addedAmount)
    }

    @Test
    fun logCanBeEditedThroughTheDialog() {
        var editedAmount = 0
        val entry = WaterIntake(1, 150, 1_793_442_000_000, "2026-10-31")
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                TodayScreen(
                    state = TodayUiState(entries = listOf(entry), today = "2026-10-31"),
                    onAdd = {},
                    onEdit = { _, amount -> editedAmount = amount }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Edit 150 ml log").performClick()
        composeRule.onNodeWithText("Amount in ml").performTextReplacement("275")
        composeRule.onNodeWithText("Save").performClick()

        assertEquals(275, editedAmount)
    }
}
