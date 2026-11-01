package com.arekb.aqua.domain.model

data class WaterIntake(
    val id: Long,
    val amountMl: Int,
    val timestampMillis: Long,
    val localDate: String
)

data class HydrationSettings(
    val dailyGoalMl: Int = 2_000,
    val presetsMl: List<Int> = listOf(200, 350, 500)
)

data class DailyTotal(
    val localDate: String,
    val totalMl: Int
)

data class DailyGoal(
    val effectiveLocalDate: String,
    val goalMl: Int
)

data class HistoryDay(
    val localDate: String,
    val totalMl: Int,
    val goalMl: Int?,
    val hasLogs: Boolean
)

enum class GoalStatus { IN_PROGRESS, NO_LOGS, GOAL_UNAVAILABLE, GOAL_MET, BELOW_GOAL }

data class HistoryPatternInsights(
    val loggedDaysInWindow: Int = 0,
    val usualFirstDrinkMinute: Int? = null,
    val mostActivePeriod: DayPeriod? = null,
    val longestGap: Long? = null,
    val longestGapLocalDate: String? = null
)

enum class DayPeriod { MORNING, AFTERNOON, EVENING, NIGHT }

data class HistoryInsights(
    val today: String,
    val week: List<HistoryDay>,
    val days: List<HistoryDay>,
    val averageCompletedDayMl: Int?,
    val completedDays: Int,
    val goalMetDays: Int,
    val goalAvailableDays: Int,
    val patterns: HistoryPatternInsights
)
