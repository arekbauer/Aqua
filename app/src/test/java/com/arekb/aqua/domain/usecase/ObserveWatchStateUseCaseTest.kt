package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.sync.WatchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveWatchStateUseCaseTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC)
    private val hydration = FakeObservedHydrationRepository()
    private val watch = FakeWatchIntakeRepository()

    private fun useCase(clock: Clock = this.clock) =
        ObserveWatchStateUseCase(hydration, watch, clock, ZoneOffset.UTC)

    private fun intake(id: Long, amountMl: Int, localDate: String) =
        WaterIntake(id, amountMl, timestampMillis = 1_000L + id, localDate = localDate)

    @Test
    fun snapshotContainsTodaysTotalGoalPresetsAndUnit() = runTest {
        hydration.settings.value = HydrationSettings(
            dailyGoalMl = 2_500,
            presetsMl = listOf(150, 300, 600),
            liquidUnit = LiquidUnit.OZ
        )
        hydration.entries.value = listOf(
            intake(1, 250, "2026-10-31"),
            intake(2, 400, "2026-10-31"),
            intake(3, 900, "2026-10-30") // yesterday is not part of today's total
        )

        val state = useCase()().first()

        assertEquals(
            WatchState(
                localDate = "2026-10-31",
                totalMl = 650,
                goalMl = 2_500,
                presetsMl = listOf(150, 300, 600),
                liquidUnit = "OZ"
            ),
            state
        )
    }

    @Test
    fun defaultsAreUsedUntilSettingsExist() = runTest {
        hydration.settings.value = null

        val state = useCase()().first()

        assertEquals(HydrationSettings().dailyGoalMl, state.goalMl)
        assertEquals(HydrationSettings().presetsMl, state.presetsMl)
        assertEquals("ML", state.liquidUnit)
        assertEquals(0, state.totalMl)
    }

    @Test
    fun acknowledgedIdsAreSortedAndLimitedToTheAckWindow() = runTest {
        watch.addIntakeFromWatch("b", 200, 1_000, "2026-10-31")
        watch.addIntakeFromWatch("a", 200, 1_000, "2026-10-24") // exactly 7 days back: included
        watch.addIntakeFromWatch("old", 200, 1_000, "2026-10-23") // 8 days back: dropped

        val state = useCase()().first()

        assertEquals(listOf("a", "b"), state.acknowledgedEventIds)
    }

    @Test
    fun newEntriesAndAcknowledgementsProduceANewSnapshot() = runTest {
        val emissions = mutableListOf<WatchState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase()().collect { emissions += it }
        }

        hydration.entries.value = listOf(intake(1, 300, "2026-10-31"))
        watch.addIntakeFromWatch("event-1", 300, 1_000, "2026-10-31")

        assertEquals(0, emissions.first().totalMl)
        assertEquals(300, emissions.last().totalMl)
        assertEquals(listOf("event-1"), emissions.last().acknowledgedEventIds)
    }

    @Test
    fun identicalSnapshotsAreNotEmittedTwice() = runTest {
        val emissions = mutableListOf<WatchState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase()().collect { emissions += it }
        }

        // Re-assigning an equal list re-triggers the upstream flow without changing the snapshot.
        hydration.entries.value = emptyList()
        hydration.settings.value = HydrationSettings()

        assertEquals(1, emissions.size)
    }

    @Test
    fun snapshotRollsOverToTheNextDayAtMidnight() = runTest {
        val startUtc = Instant.parse("2026-10-31T23:00:00Z")
        val virtualClock = object : Clock() {
            override fun getZone(): ZoneId = ZoneOffset.UTC
            override fun withZone(zone: ZoneId): Clock = this
            override fun instant(): Instant = startUtc.plusMillis(testScheduler.currentTime)
        }
        hydration.entries.value = listOf(intake(1, 500, "2026-10-31"))
        val emissions = mutableListOf<WatchState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(virtualClock)().collect { emissions += it }
        }
        assertEquals("2026-10-31", emissions.last().localDate)
        assertEquals(500, emissions.last().totalMl)

        advanceTimeBy(60 * 60 * 1000L + 1) // one hour plus a millisecond: past midnight

        assertEquals("2026-11-01", emissions.last().localDate)
        assertEquals(0, emissions.last().totalMl)
    }
}
