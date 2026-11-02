package com.arekb.aqua.domain.repository

import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import kotlinx.coroutines.flow.Flow

interface HydrationRepository {
    fun observeSettings(): Flow<HydrationSettings?>
    fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>>
    fun observeDailyTotals(): Flow<List<DailyTotal>>
    fun observeDailyGoals(): Flow<List<DailyGoal>>
    fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>>
    suspend fun saveSettings(settings: HydrationSettings)
    suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake
    suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int)
    suspend fun undoIntake(intake: WaterIntake)
    suspend fun clearDate(localDate: String)
    suspend fun clearAllHistory()
    suspend fun saveDailyGoal(localDate: String, goalMl: Int)
}
