package com.arekb.aqua.ui.screens.today

import com.arekb.aqua.MainDispatcherRule
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.AddWaterUseCase
import java.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import androidx.lifecycle.ViewModelStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loggingUpdatingDeletingAndUndoingEntriesUpdatesTodayState() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeHydrationRepository()
        val viewModel = TodayViewModel(
            repository = repository,
            addWater = AddWaterUseCase(repository, Clock.systemDefaultZone())
        )
        val viewModelStore = ViewModelStore().apply { put("today", viewModel) }
        try {
            backgroundScope.launch { viewModel.uiState.collect { } }
            runCurrent()

            viewModel.logWater(250)
            runCurrent()
            val created = repository.entries.value.single()
            assertEquals(250, viewModel.uiState.value.todayTotalMl)
            assertEquals("250 ml added", viewModel.uiState.value.message?.text)
            assertEquals(created, viewModel.uiState.value.message?.undoIntake)

            viewModel.updateAmount(created, 400)
            runCurrent()
            assertEquals(400, viewModel.uiState.value.todayTotalMl)
            assertEquals("Entry updated", viewModel.uiState.value.message?.text)

            viewModel.delete(created)
            runCurrent()
            assertEquals(0, viewModel.uiState.value.todayTotalMl)
            assertEquals("Entry deleted", viewModel.uiState.value.message?.text)

            viewModel.logWater(300)
            runCurrent()
            val undoable = repository.entries.value.single()
            viewModel.undo(undoable)
            runCurrent()
            assertEquals(0, viewModel.uiState.value.todayTotalMl)
            assertEquals("Entry undone", viewModel.uiState.value.message?.text)

            val staleMessageId = viewModel.uiState.value.message!!.id - 1
            viewModel.messageShown(staleMessageId)
            assertEquals("Entry undone", viewModel.uiState.value.message?.text)
            viewModel.messageShown(viewModel.uiState.value.message!!.id)
            runCurrent()
            assertNull(viewModel.uiState.value.message)
        } finally {
            viewModelStore.clear()
        }
    }

    @Test
    fun nonPositiveAmountsAreIgnored() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeHydrationRepository()
        val viewModel = TodayViewModel(
            repository,
            AddWaterUseCase(repository, Clock.systemUTC())
        )
        val viewModelStore = ViewModelStore().apply { put("today", viewModel) }
        try {
            viewModel.logWater(0)
            runCurrent()
            assertEquals(emptyList<WaterIntake>(), repository.entries.value)
        } finally {
            viewModelStore.clear()
        }
    }
}

private class FakeHydrationRepository : HydrationRepository {
    private val entriesByDate = mutableMapOf<String, MutableStateFlow<List<WaterIntake>>>()
    val entries = entriesFor(java.time.LocalDate.now().toString())
    private val settings = MutableStateFlow<HydrationSettings?>(HydrationSettings())
    private var nextId = 1L

    override fun observeSettings(): Flow<HydrationSettings?> = settings
    override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> = entriesFor(localDate)
    override fun observeDailyTotals(): Flow<List<DailyTotal>> = entriesByDate.map { (date, flow) ->
        DailyTotal(date, flow.value.sumOf(WaterIntake::amountMl))
    }.let { flowOfValue(it) }
    override fun observeDailyGoals(): Flow<List<DailyGoal>> = flowOfValue(emptyList())
    override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
        flowOfValue(entriesByDate.filterKeys { it in startLocalDate..endLocalDate }.values.flatMap { it.value })

    override suspend fun saveSettings(settings: HydrationSettings) { this.settings.value = settings }
    override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake {
        val intake = WaterIntake(nextId++, amountMl, timestampMillis, localDate)
        entriesFor(localDate).value = entriesFor(localDate).value + intake
        return intake
    }
    override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) {
        entriesByDate.values.forEach { flow ->
            flow.value = flow.value.map { if (it.id == intakeId) it.copy(amountMl = amountMl) else it }
        }
    }
    override suspend fun undoIntake(intake: WaterIntake) {
        entriesFor(intake.localDate).value = entriesFor(intake.localDate).value.filterNot { it.id == intake.id }
    }
    override suspend fun clearDate(localDate: String) { entriesFor(localDate).value = emptyList() }

    private fun entriesFor(date: String) = entriesByDate.getOrPut(date) { MutableStateFlow(emptyList()) }
    private fun <T> flowOfValue(value: List<T>): Flow<List<T>> = MutableStateFlow(value)
}
