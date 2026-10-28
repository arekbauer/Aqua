package com.arekb.aqua.data.repository

import com.arekb.aqua.data.local.AquaDao
import com.arekb.aqua.data.local.IntakeEntity
import com.arekb.aqua.data.local.SettingsEntity
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomHydrationRepository @Inject constructor(private val dao: AquaDao) : HydrationRepository {
    override fun observeSettings(): Flow<HydrationSettings?> = dao.observeSettings()
        .onStart { dao.insertDefaults(SettingsEntity()) }
        .map { it?.toDomain() }

    override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> =
        dao.observeEntriesForDate(localDate).map { entries -> entries.map { it.toDomain() } }

    override fun observeDailyTotals(): Flow<List<DailyTotal>> =
        dao.observeDailyTotals().map { totals -> totals.map { DailyTotal(it.localDate, it.totalMl) } }

    override suspend fun saveSettings(settings: HydrationSettings) {
        dao.saveSettings(settings.toEntity())
    }

    override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake {
        val id = dao.insertEntry(IntakeEntity(amountMl = amountMl, timestamp = timestampMillis, localDate = localDate))
        return WaterIntake(id, amountMl, timestampMillis, localDate)
    }

    override suspend fun undoIntake(intake: WaterIntake) = dao.deleteEntry(intake.id)

    override suspend fun clearDate(localDate: String) = dao.clearEntriesForDate(localDate)

    private fun SettingsEntity.toDomain() = HydrationSettings(
        dailyGoalMl = dailyGoalMl,
        presetsMl = listOf(presetOneMl, presetTwoMl, presetThreeMl)
    )

    private fun HydrationSettings.toEntity() = SettingsEntity(
        dailyGoalMl = dailyGoalMl,
        presetOneMl = presetsMl[0],
        presetTwoMl = presetsMl[1],
        presetThreeMl = presetsMl[2],
        // Keep the legacy Room column populated so the database schema stays compatible.
        presetFourMl = 750
    )

    private fun IntakeEntity.toDomain() = WaterIntake(id, amountMl, timestamp, localDate)
}
