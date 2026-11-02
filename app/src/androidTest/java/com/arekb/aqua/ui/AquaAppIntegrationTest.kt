package com.arekb.aqua.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.arekb.aqua.MainActivity

// Note: Hilt Android UI testing requires specific setup not present in this module's Gradle build
// (dagger.hilt.android.testing dependency is missing). Since AquaApp is a Hilt app,
// starting MainActivity implicitly launches the Hilt app.

@RunWith(AndroidJUnit4::class)
class AquaAppIntegrationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loggingWaterUpdatesHistoryAutomatically() {
        // Find and click the default 200 ml quick add button on Today screen
        composeRule.onNodeWithContentDescription("Add 200 millilitres of water").performClick()

        // Wait for it to reflect in Today total
        composeRule.onNodeWithText("200").assertIsDisplayed()

        // Navigate to History screen
        composeRule.onNodeWithContentDescription("History").performClick()

        // Wait for History screen to load and show the 200 ml in the daily log
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("200 ml"))
        composeRule.onNodeWithText("200 ml").assertIsDisplayed()
    }
}
