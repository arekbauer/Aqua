package com.arekb.aqua.domain.model

data class WaterIntake(
    val id: Long,
    val amountMl: Int,
    val timestampMillis: Long,
    val localDate: String
)

data class HydrationSettings(
    val dailyGoalMl: Int = 2_000,
    val presetsMl: List<Int> = listOf(200, 350, 500, 750)
)

data class DailyTotal(
    val localDate: String,
    val totalMl: Int
)
