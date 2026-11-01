package com.arekb.aqua.data.repository

import com.arekb.aqua.data.local.AquaDao
import com.arekb.aqua.data.local.DailyGoalEntity
import com.arekb.aqua.data.local.IntakeEntity
import com.arekb.aqua.data.local.SettingsEntity
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate

class RoomHydrationRepository @Inject constructor(private val dao: AquaDao) : HydrationRepository {
    override fun observeSettings(): Flow<HydrationSettings?> = dao.observeSettings()
        .onStart { ensureDailyGoalForToday() }
        .map { it?.toDomain() }

    override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> =
        dao.observeEntriesForDate(localDate).map { entries -> entries.map { it.toDomain() } }

    override fun observeDailyTotals(): Flow<List<DailyTotal>> =
        dao.observeDailyTotals().map { totals -> totals.map { DailyTotal(it.localDate, it.totalMl) } }

    override fun observeDailyGoals(): Flow<List<DailyGoal>> = dao.observeDailyGoals()
        .onStart { ensureDailyGoalForToday() }
        .map { goals -> goals.map { DailyGoal(it.effectiveLocalDate, it.goalMl) } }

    override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
        dao.observeEntriesBetween(startLocalDate, endLocalDate)
            .map { entries -> entries.map { it.toDomain() } }

    override suspend fun saveSettings(settings: HydrationSettings) {
        dao.saveSettingsAndGoal(
            settings = settings.toEntity(),
            dailyGoal = DailyGoalEntity(LocalDate.now().toString(), settings.dailyGoalMl)
        )
    }

    override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake {
        val id = dao.insertEntry(IntakeEntity(amountMl = amountMl, timestamp = timestampMillis, localDate = localDate))
        return WaterIntake(id, amountMl, timestampMillis, localDate)
    }

    override suspend fun undoIntake(intake: WaterIntake) = dao.deleteEntry(intake.id)

    override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) =
        dao.updateEntryAmount(intakeId, amountMl)

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

    private suspend fun ensureDailyGoalForToday() {
        dao.insertDefaults(SettingsEntity())
        val settings = dao.getSettings() ?: SettingsEntity()
        dao.insertDailyGoalIfMissing(DailyGoalEntity(LocalDate.now().toString(), settings.dailyGoalMl))
    }
}
