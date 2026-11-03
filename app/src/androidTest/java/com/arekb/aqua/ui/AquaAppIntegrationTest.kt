package com.arekb.aqua.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.MainActivity
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.usecase.ClearAllHistoryUseCase
import com.arekb.aqua.domain.usecase.SaveHydrationSettingsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AquaAppIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var clearAllHistoryUseCase: ClearAllHistoryUseCase

    @Inject
    lateinit var saveHydrationSettingsUseCase: SaveHydrationSettingsUseCase

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            clearAllHistoryUseCase()
            saveHydrationSettingsUseCase(HydrationSettings())
        }
    }

    @Test
    fun changingGoalAndPresetsUpdatesTodayScreen() {
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Preferences").performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasTestTag("PresetInput_0")).fetchSemanticsNodes().isNotEmpty()
        }

        // Modify the first preset from 200 ml to 450 ml
        composeRule.onNode(hasTestTag("PresetInput_0")).performTextReplacement("450")

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.onNodeWithContentDescription("Today").performClick()

        // The preset button on Today Screen should now say 450 ml
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasTestTag("QuickAddButton_450")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasTestTag("QuickAddButton_450")).assertIsDisplayed()
    }

    @Test
    fun changingUnitsUpdatesTodayScreen() {
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Preferences").performClick()

        // Switch to oz (fluid ounces)
        composeRule.onNodeWithText("oz").performClick()

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.onNodeWithContentDescription("Today").performClick()

        // 2000 ml default goal converts to approx 68 oz
        composeRule.onNodeWithText("68 oz to go").assertIsDisplayed()

        // Drink some water (200 ml default preset converts to 7 oz)
        composeRule.onNodeWithContentDescription("Add 7 oz of water").performClick()

        // The day's total should update to 7 oz and remaining to 61 oz
        composeRule.onNodeWithText("7").assertIsDisplayed()
        composeRule.onNodeWithText("61 oz to go").assertIsDisplayed()
    }

    @Test
    fun loggingWaterAppearsInHistory() {
        // Wait for the Today screen to settle
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasTestTag("QuickAddButton_200")).fetchSemanticsNodes().isNotEmpty()
        }

        // Log 200 ml of water
        composeRule.onNode(hasTestTag("QuickAddButton_200")).performClick()

        // Navigate to History
        composeRule.onNodeWithContentDescription("History").performClick()

        // The daily log row should display "200 ml"
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("200 ml", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("200 ml", substring = true).assertIsDisplayed()
    }
}
