package com.arekb.aqua.ui.screens.history

import com.arekb.aqua.MainDispatcherRule
import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialStateLoadsInsightsCorrectly() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeHydrationRepository()
        val fixedClock = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneId.of("UTC"))

        val viewModel = HistoryViewModel(repository, fixedClock)

        val viewModelStore = androidx.lifecycle.ViewModelStore().apply { put("history", viewModel) }
        try {
            backgroundScope.launch { viewModel.uiState.collect { } }
            runCurrent()
    
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.insights)
            assertEquals("2026-10-31", state.insights?.today)
        } finally {
            viewModelStore.clear()
        }
    }

    @Test
    fun midnightRolloverUpdatesTodayState() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeHydrationRepository()
        // Start just before midnight
        var currentTime = Instant.parse("2026-10-31T23:59:59Z")
        val testClock = object : Clock() {
            override fun instant(): Instant = currentTime
            override fun withZone(zone: ZoneId?): Clock = this
            override fun getZone(): ZoneId = ZoneId.of("UTC")
        }

        val viewModel = HistoryViewModel(repository, testClock)

        val viewModelStore = androidx.lifecycle.ViewModelStore().apply { put("history", viewModel) }
        try {
            backgroundScope.launch { viewModel.uiState.collect { } }
            runCurrent()
    
            assertEquals("2026-10-31", viewModel.uiState.value.insights?.today)
    
            // Advance time and scheduler past midnight
            currentTime = currentTime.plusSeconds(2)
            advanceTimeBy(2.days)
            runCurrent()
    
            assertEquals("2026-11-01", viewModel.uiState.value.insights?.today)
        } finally {
            viewModelStore.clear()
        }
    }
}

private class FakeHydrationRepository : HydrationRepository {
    private val entriesByDate = mutableMapOf<String, MutableStateFlow<List<WaterIntake>>>()
    private val settings = MutableStateFlow<HydrationSettings?>(HydrationSettings())

    override fun observeSettings(): Flow<HydrationSettings?> = settings
    override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> = entriesFor(localDate)
    override fun observeDailyTotals(): Flow<List<DailyTotal>> = MutableStateFlow(emptyList())
    override fun observeDailyGoals(): Flow<List<DailyGoal>> = MutableStateFlow(emptyList())
    override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
        MutableStateFlow(emptyList())

    override suspend fun saveSettings(settings: HydrationSettings) { this.settings.value = settings }
    override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake = WaterIntake(1, amountMl, timestampMillis, localDate)
    override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) {}
    override suspend fun undoIntake(intake: WaterIntake) {}
    override suspend fun clearDate(localDate: String) {}
    override suspend fun saveDailyGoal(localDate: String, goalMl: Int) {}
    override suspend fun clearAllHistory() { entriesByDate.clear() }

    private fun entriesFor(date: String) = entriesByDate.getOrPut(date) { MutableStateFlow(emptyList()) }
}