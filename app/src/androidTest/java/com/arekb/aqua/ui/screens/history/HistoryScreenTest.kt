package com.arekb.aqua.ui.screens.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.ui.theme.AquaTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsWeeklySummaryNoLogMarkerDailyGoalStatusAndPatterns() {
        val today = LocalDate.parse("2026-10-31")
        val week = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            HistoryDay(date.toString(), if (offset == 2) 0 else 1_500, 2_000, hasLogs = offset != 2)
        }
        val insights = HistoryInsights(
            today = today.toString(),
            week = week,
            days = listOf(
                HistoryDay(today.toString(), 1_500, 2_000, true),
                HistoryDay(today.minusDays(1).toString(), 2_100, 2_000, true)
            ),
            averageCompletedDayMl = 1_250,
            completedDays = 6,
            goalMetDays = 2,
            goalAvailableDays = 6,
            patterns = HistoryPatternInsights(8, 9 * 60, DayPeriod.AFTERNOON, 7_200_000, today.minusDays(2).toString())
        )
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                HistoryScreen(HistoryUiState(isLoading = false, insights = insights))
            }
        }

        composeRule.onNodeWithText("1,250 ml").assertIsDisplayed()
        composeRule.onNodeWithText("2/6 days").assertIsDisplayed()
        composeRule.onNodeWithText("No logs").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Goal met"))
        composeRule.onNodeWithText("Goal met").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("In progress"))
        composeRule.onNodeWithText("In progress").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("9:00 AM", ignoreCase = true))
        composeRule.onNodeWithText("9:00 AM", ignoreCase = true).assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Afternoon"))
        composeRule.onNodeWithText("Afternoon").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("2h"))
        composeRule.onNodeWithText("2h").assertIsDisplayed()
    }

    @Test
    fun showsInsufficientHistoryMessageAndEmptyDailyState() {
        val today = LocalDate.parse("2026-10-31")
        val insights = HistoryInsights(
            today = today.toString(),
            week = (6 downTo 0).map { offset -> HistoryDay(today.minusDays(offset.toLong()).toString(), 0, null, false) },
            days = emptyList(),
            averageCompletedDayMl = 0,
            completedDays = 6,
            goalMetDays = 0,
            goalAvailableDays = 0,
            patterns = HistoryPatternInsights()
        )
        composeRule.setContent {
            AquaTheme(dynamicColor = false) {
                HistoryScreen(HistoryUiState(isLoading = false, insights = insights))
            }
        }

        composeRule.onNodeWithText("Insights appear after you have logs on 7 days in the last 30 days.").assertIsDisplayed()
        composeRule.onNodeWithText("Your history starts here").assertIsDisplayed()
        composeRule.onNodeWithText("Goal line unavailable for these dates").assertIsDisplayed()
    }
}
