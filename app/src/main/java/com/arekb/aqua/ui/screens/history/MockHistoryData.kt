package com.arekb.aqua.ui.screens.history

import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.repository.HydrationRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

/**
 * Contains mock data and utilities for testing the History Screen.
 */
object MockHistoryData {

    /**
     * A mock HistoryInsights object ready to be used in Compose Previews and UI Tests.
     */
    val insights: HistoryInsights
        get() {
            val today = LocalDate.now()
            val week = (6 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                HistoryDay(
                    localDate = date.toString(),
                    totalMl = if (offset == 3) 0 else 1_450 + offset * 100,
                    goalMl = 2_000,
                    hasLogs = offset != 3
                )
            }
            return HistoryInsights(
                today = today.toString(),
                week = week,
                days = week.filter(HistoryDay::hasLogs).reversed(),
                averageCompletedDayMl = 1_720,
                completedDays = 6,
                goalMetDays = 2,
                goalAvailableDays = 6,
                patterns = HistoryPatternInsights(
                    loggedDaysInWindow = 10,
                    usualFirstDrinkMinute = 9 * 60 + 15, // 9:15 AM
                    mostActivePeriod = DayPeriod.AFTERNOON,
                    longestGap = 12_600_000L, // 3.5 hours
                    longestGapLocalDate = today.minusDays(2).toString()
                )
            )
        }

    /**
     * Helper to populate the Room database with random data for manual testing on a device/emulator.
     * You can call this from your ViewModel's init block:
     * ```
     * viewModelScope.launch { MockHistoryData.populateDatabase(repository) }
     * ```
     */
    suspend fun populateDatabase(repository: HydrationRepository) {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // Generate data for the last 30 days
        for (i in 0..30) {
            val date = today.minusDays(i.toLong())

            // Randomly skip some days to test the "no logs" UI
            val hasLogs = Random.nextBoolean() || i < 3 // ensure the last 3 days always have logs
            if (!hasLogs) continue

            // 2 to 7 drinks per day
            val numDrinks = Random.nextInt(2, 8)
            var currentMillis = ZonedDateTime.of(date.atTime(8, 0), zone).toInstant().toEpochMilli()

            repeat(numDrinks) {
                val amount = listOf(150, 200, 250, 300, 400, 500).random()
                repository.addIntake(
                    amountMl = amount,
                    timestampMillis = currentMillis,
                    localDate = date.toString()
                )
                // Advance time by 1 to 4 hours for the next drink
                currentMillis += Random.nextLong(1 * 60 * 60 * 1000L, 4 * 60 * 60 * 1000L)
            }
        }
    }
}
