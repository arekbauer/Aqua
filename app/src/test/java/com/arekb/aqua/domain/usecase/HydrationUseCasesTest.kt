package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class HydrationUseCasesTest {

    private val fixedInstant = Instant.parse("2026-10-31T12:00:00Z")
    private val fixedZone = ZoneId.of("UTC")
    private val fixedClock = Clock.fixed(fixedInstant, fixedZone)

    @Test
    fun addWaterUseCase_ignoresNonPositiveAmount() = runTest {
        val repository = FakeHydrationRepository()
        val addWater = AddWaterUseCase(repository, fixedClock)

        val resultZero = addWater(0)
        val resultNegative = addWater(-100)

        assertNull(resultZero)
        assertNull(resultNegative)
        assertEquals(0, repository.getAllEntries().size)
    }

    @Test
    fun addWaterUseCase_addsPositiveAmountWithClockTimestamp() = runTest {
        val repository = FakeHydrationRepository()
        val addWater = AddWaterUseCase(repository, fixedClock)

        val intake = addWater(250)

        assertEquals(250, intake?.amountMl)
        assertEquals(fixedInstant.toEpochMilli(), intake?.timestampMillis)
        assertEquals("2026-10-31", intake?.localDate)
        assertEquals(1, repository.getAllEntries().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun saveHydrationSettingsUseCase_throwsOnInvalidDailyGoal() = runTest {
        val repository = FakeHydrationRepository()
        val saveSettings = SaveHydrationSettingsUseCase(repository)

        saveSettings(HydrationSettings(dailyGoalMl = 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun saveHydrationSettingsUseCase_throwsOnInvalidPresets() = runTest {
        val repository = FakeHydrationRepository()
        val saveSettings = SaveHydrationSettingsUseCase(repository)

        saveSettings(HydrationSettings(presetsMl = listOf(200, 0, 500)))
    }

    @Test
    fun saveHydrationSettingsUseCase_savesValidSettings() = runTest {
        val repository = FakeHydrationRepository()
        val saveSettings = SaveHydrationSettingsUseCase(repository)

        val newSettings = HydrationSettings(dailyGoalMl = 3000, presetsMl = listOf(250, 400, 750))
        saveSettings(newSettings)

        assertEquals(newSettings, repository.savedSettings)
    }

    @Test
    fun resetDailyIntakeUseCase_clearsEntriesForClockDate() = runTest {
        val repository = FakeHydrationRepository()
        repository.addIntake(250, fixedInstant.toEpochMilli(), "2026-10-31")
        repository.addIntake(300, fixedInstant.toEpochMilli(), "2026-10-30")

        val resetDaily = ResetDailyIntakeUseCase(repository, fixedClock)
        resetDaily()

        val remaining = repository.getAllEntries()
        assertEquals(1, remaining.size)
        assertEquals("2026-10-30", remaining[0].localDate)
    }

    @Test
    fun clearAllHistoryUseCase_clearsRepositoryData() = runTest {
        val repository = FakeHydrationRepository()
        repository.addIntake(250, fixedInstant.toEpochMilli(), "2026-10-31")

        val clearAll = ClearAllHistoryUseCase(repository)
        clearAll()

        assertEquals(0, repository.getAllEntries().size)
    }

    @Test
    fun observeSettings_emitsWhenSettingsAreSaved() = runTest {
        val repository = FakeHydrationRepository()
        val saveSettings = SaveHydrationSettingsUseCase(repository)

        val settings = HydrationSettings(dailyGoalMl = 2500)
        saveSettings(settings)

        assertEquals(settings, repository.observeSettings().first())
    }

    @Test
    fun observeEntriesForDate_emitsWhenWaterIsAdded() = runTest {
        val repository = FakeHydrationRepository()
        val addWater = AddWaterUseCase(repository, fixedClock)

        addWater(300)

        val entries = repository.observeEntriesForDate("2026-10-31").first()
        assertEquals(1, entries.size)
        assertEquals(300, entries[0].amountMl)
    }

    private class FakeHydrationRepository : HydrationRepository {
        var savedSettings: HydrationSettings? = null
            private set
        private val settingsFlow = MutableStateFlow<HydrationSettings?>(null)
        private val entriesList = mutableListOf<WaterIntake>()
        private val entriesFlow = MutableStateFlow<List<WaterIntake>>(emptyList())
        private val goalsList = mutableListOf<DailyGoal>()
        private val goalsFlow = MutableStateFlow<List<DailyGoal>>(emptyList())

        override fun observeSettings(): Flow<HydrationSettings?> = settingsFlow
        override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> =
            entriesFlow.map { list -> list.filter { it.localDate == localDate } }
        override fun observeDailyTotals(): Flow<List<DailyTotal>> = entriesFlow.map { list ->
            list.groupBy { it.localDate }
                .map { (date, intakes) -> DailyTotal(date, intakes.sumOf { it.amountMl }) }
        }
        override fun observeDailyGoals(): Flow<List<DailyGoal>> = goalsFlow
        override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
            entriesFlow.map { list -> list.filter { it.localDate in startLocalDate..endLocalDate } }

        override suspend fun saveSettings(settings: HydrationSettings) {
            savedSettings = settings
            settingsFlow.value = settings
        }

        override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake {
            val intake = WaterIntake(entriesList.size + 1L, amountMl, timestampMillis, localDate)
            entriesList.add(intake)
            notifyEntriesChanged()
            return intake
        }

        override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) {
            val idx = entriesList.indexOfFirst { it.id == intakeId }
            if (idx != -1) {
                entriesList[idx] = entriesList[idx].copy(amountMl = amountMl)
                notifyEntriesChanged()
            }
        }

        override suspend fun undoIntake(intake: WaterIntake) {
            if (entriesList.removeAll { it.id == intake.id }) {
                notifyEntriesChanged()
            }
        }

        override suspend fun clearDate(localDate: String) {
            if (entriesList.removeAll { it.localDate == localDate }) {
                notifyEntriesChanged()
            }
        }

        override suspend fun clearAllHistory() {
            entriesList.clear()
            goalsList.clear()
            notifyEntriesChanged()
            goalsFlow.value = emptyList()
        }

        override suspend fun saveDailyGoal(localDate: String, goalMl: Int) {
            goalsList.removeAll { it.effectiveLocalDate == localDate }
            goalsList.add(DailyGoal(localDate, goalMl))
            goalsFlow.value = goalsList.toList()
        }

        override suspend fun getAllEntries(): List<WaterIntake> = entriesList.toList()
        override suspend fun getAllDailyGoals(): List<DailyGoal> = goalsList.toList()
        override suspend fun restoreData(
            settings: HydrationSettings?,
            dailyGoals: List<DailyGoal>,
            entries: List<WaterIntake>
        ) {
            if (settings != null) {
                savedSettings = settings
                settingsFlow.value = settings
            }
            goalsList.clear()
            goalsList.addAll(dailyGoals)
            goalsFlow.value = goalsList.toList()

            entriesList.clear()
            entriesList.addAll(entries)
            notifyEntriesChanged()
        }

        private fun notifyEntriesChanged() {
            entriesFlow.value = entriesList.toList()
        }
    }
}
