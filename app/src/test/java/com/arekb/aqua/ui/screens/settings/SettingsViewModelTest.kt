package com.arekb.aqua.ui.screens.settings

import androidx.lifecycle.ViewModelStore
import com.arekb.aqua.MainDispatcherRule
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.ClearAllHistoryUseCase
import com.arekb.aqua.domain.usecase.ExportDataUseCase
import com.arekb.aqua.domain.usecase.ImportDataUseCase
import com.arekb.aqua.domain.usecase.ResetDailyIntakeUseCase
import com.arekb.aqua.domain.usecase.SaveHydrationSettingsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun exportImportAndWipeActionsUpdateUiStateAndMessage() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeHydrationRepository(
            initialEntries = listOf(WaterIntake(1L, 300, 1000L, "2026-10-31"))
        )
        val viewModel = SettingsViewModel(
            repository = repository,
            saveHydrationSettings = SaveHydrationSettingsUseCase(repository),
            resetDailyIntake = ResetDailyIntakeUseCase(repository, Clock.systemDefaultZone()),
            clearAllHistory = ClearAllHistoryUseCase(repository),
            exportDataUseCase = ExportDataUseCase(repository),
            importDataUseCase = ImportDataUseCase(repository)
        )

        val viewModelStore = ViewModelStore().apply { put("settings", viewModel) }
        try {
            backgroundScope.launch { viewModel.uiState.collect { } }
            runCurrent()

            val outputStream = ByteArrayOutputStream()
            viewModel.exportData(outputStream)
            runCurrent()

            assertEquals("Data exported successfully", viewModel.uiState.value.message?.text)
            val jsonString = outputStream.toString(Charsets.UTF_8.name())
            assertNotNull(jsonString)

            viewModel.wipeAllData()
            runCurrent()

            assertEquals(0, repository.getAllEntries().size)
            assertEquals("All history wiped", viewModel.uiState.value.message?.text)

            viewModel.importData(ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8)))
            runCurrent()

            assertEquals(1, repository.getAllEntries().size)
            assertEquals("Data restored successfully", viewModel.uiState.value.message?.text)
        } finally {
            viewModelStore.clear()
        }
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
