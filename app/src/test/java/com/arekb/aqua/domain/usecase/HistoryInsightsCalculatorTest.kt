package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.WaterIntake
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryInsightsCalculatorTest {
    private val today = LocalDate.parse("2026-10-31")
    private val utc = ZoneId.of("UTC")

    @Test
    fun weeklySummaryIncludesNoLogDaysAndUsesEffectiveGoalDates() {
        val totals = listOf(
            DailyTotal("2026-10-25", 2_000),
            DailyTotal("2026-10-27", 1_000),
            DailyTotal("2026-10-28", 1_600),
            DailyTotal("2026-10-30", 2_100),
            DailyTotal("2026-10-24", 300)
        )
        val goals = listOf(DailyGoal("2026-10-25", 2_000), DailyGoal("2026-10-28", 1_500))

        val result = HistoryInsightsCalculator.calculate(today, totals, emptyList(), goals, utc)

        assertEquals(7, result.week.size)
        assertEquals(listOf(2_000, 0, 1_000, 1_600, 0, 2_100, 0), result.week.map { it.totalMl })
        assertEquals(1_116, result.averageCompletedDayMl)
        assertEquals(3, result.goalMetDays)
        assertEquals(6, result.goalAvailableDays)
        assertNull(result.days.first { it.localDate == "2026-10-24" }.goalMl)
    }

    @Test
    fun patternsUseMedianFirstDrinkDominantAmountPeriodAndLongestWithinDayGap() {
        val entries = (1L..7L).flatMap { daysAgo ->
            val date = today.minusDays(daysAgo).toString()
            listOf(
                intake(date, LocalTime.of(9, 0), 200, daysAgo * 10),
                intake(date, LocalTime.of(14, 0), 500, daysAgo * 10 + 1)
            )
        }

        val result = HistoryInsightsCalculator.calculate(today, emptyList(), entries, emptyList(), utc)

        assertEquals(7, result.patterns.loggedDaysInWindow)
        assertEquals(9 * 60, result.patterns.usualFirstDrinkMinute)
        assertEquals(DayPeriod.AFTERNOON, result.patterns.mostActivePeriod)
        assertEquals(5 * 60 * 60 * 1_000L, result.patterns.longestGap)
        assertEquals(today.minusDays(1).toString(), result.patterns.longestGapLocalDate)
    }

    @Test
    fun patternsStayHiddenUntilSevenLoggedDaysAreAvailable() {
        val entries = (1L..6L).map { daysAgo ->
            intake(today.minusDays(daysAgo).toString(), LocalTime.of(9, 0), 250, daysAgo)
        }

        val result = HistoryInsightsCalculator.calculate(today, emptyList(), entries, emptyList(), utc)

        assertEquals(6, result.patterns.loggedDaysInWindow)
        assertNull(result.patterns.usualFirstDrinkMinute)
        assertNull(result.patterns.mostActivePeriod)
        assertNull(result.patterns.longestGap)
    }

    @Test
    fun goalStatusDistinguishesTodayNoLogsUnavailableMetAndBelowGoal() {
        fun day(date: String, total: Int, goal: Int?, logs: Boolean) = HistoryDay(date, total, goal, logs)

        assertEquals(GoalStatus.IN_PROGRESS, HistoryInsightsCalculator.goalStatus(day("2026-10-31", 100, 2_000, true), today.toString()))
        assertEquals(GoalStatus.NO_LOGS, HistoryInsightsCalculator.goalStatus(day("2026-10-30", 0, 2_000, false), today.toString()))
        assertEquals(GoalStatus.GOAL_UNAVAILABLE, HistoryInsightsCalculator.goalStatus(day("2026-10-29", 900, null, true), today.toString()))
        assertEquals(GoalStatus.GOAL_MET, HistoryInsightsCalculator.goalStatus(day("2026-10-28", 2_000, 2_000, true), today.toString()))
        assertEquals(GoalStatus.BELOW_GOAL, HistoryInsightsCalculator.goalStatus(day("2026-10-27", 1_999, 2_000, true), today.toString()))
    }

    private fun intake(date: String, time: LocalTime, amount: Int, id: Long): WaterIntake {
        val timestamp = time.atDate(LocalDate.parse(date)).atZone(utc).toInstant().toEpochMilli()
        return WaterIntake(id, amount, timestamp, date)
    }
}
