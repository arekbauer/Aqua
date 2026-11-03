package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.AppTheme
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BackupDataTest {

    @Test
    fun exportAndImportDataRestoresFullBackup() = runTest {
        val repository = FakeHydrationRepository(
            initialSettings = HydrationSettings(dailyGoalMl = 2500, liquidUnit = LiquidUnit.OZ, appTheme = AppTheme.DARK),
            initialGoals = listOf(DailyGoal("2026-10-30", 2500)),
            initialEntries = listOf(WaterIntake(1L, 500, 100000L, "2026-10-30"))
        )

        val exportDataUseCase = ExportDataUseCase(repository)
        val outputStream = ByteArrayOutputStream()
        exportDataUseCase(outputStream)

        val exportedJson = outputStream.toString(Charsets.UTF_8.name())
        assertTrue(exportedJson.contains("2500"))
        assertTrue(exportedJson.contains("2026-10-30"))

        val targetRepository = FakeHydrationRepository()
        val importDataUseCase = ImportDataUseCase(targetRepository)
        importDataUseCase(ByteArrayInputStream(exportedJson.toByteArray(Charsets.UTF_8)))

        val restoredEntries = targetRepository.getAllEntries()
        val restoredGoals = targetRepository.getAllDailyGoals()

        assertEquals(1, restoredEntries.size)
        assertEquals(500, restoredEntries[0].amountMl)
        assertEquals("2026-10-30", restoredEntries[0].localDate)

        assertEquals(1, restoredGoals.size)
        assertEquals(2500, restoredGoals[0].goalMl)
    }

    private class FakeHydrationRepository(
        initialSettings: HydrationSettings = HydrationSettings(),
        initialGoals: List<DailyGoal> = emptyList(),
        initialEntries: List<WaterIntake> = emptyList()
    ) : HydrationRepository {
        private val settingsState = MutableStateFlow<HydrationSettings?>(initialSettings)
        private val goalsList = initialGoals.toMutableList()
        private val entriesList = initialEntries.toMutableList()

        override fun observeSettings(): Flow<HydrationSettings?> = settingsState
        override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> =
            MutableStateFlow(entriesList.filter { it.localDate == localDate })
        override fun observeDailyTotals(): Flow<List<DailyTotal>> = MutableStateFlow(emptyList())
        override fun observeDailyGoals(): Flow<List<DailyGoal>> = MutableStateFlow(goalsList)
        override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
            MutableStateFlow(entriesList.filter { it.localDate in startLocalDate..endLocalDate })

        override suspend fun saveSettings(settings: HydrationSettings) { settingsState.value = settings }
        override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake {
            val intake = WaterIntake(entriesList.size + 1L, amountMl, timestampMillis, localDate)
            entriesList.add(intake)
            return intake
        }
        override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) {
            val idx = entriesList.indexOfFirst { it.id == intakeId }
            if (idx != -1) entriesList[idx] = entriesList[idx].copy(amountMl = amountMl)
        }
        override suspend fun undoIntake(intake: WaterIntake) { entriesList.removeAll { it.id == intake.id } }
        override suspend fun clearDate(localDate: String) { entriesList.removeAll { it.localDate == localDate } }
        override suspend fun clearAllHistory() {
            entriesList.clear()
            goalsList.clear()
        }
        override suspend fun saveDailyGoal(localDate: String, goalMl: Int) {
            goalsList.removeAll { it.effectiveLocalDate == localDate }
            goalsList.add(DailyGoal(localDate, goalMl))
        }
        override suspend fun getAllEntries(): List<WaterIntake> = entriesList.toList()
        override suspend fun getAllDailyGoals(): List<DailyGoal> = goalsList.toList()
        override suspend fun restoreData(
            settings: HydrationSettings?,
            dailyGoals: List<DailyGoal>,
            entries: List<WaterIntake>
        ) {
            if (settings != null) settingsState.value = settings
            goalsList.clear()
            goalsList.addAll(dailyGoals)
            entriesList.clear()
            entriesList.addAll(entries)
        }
    }
}
