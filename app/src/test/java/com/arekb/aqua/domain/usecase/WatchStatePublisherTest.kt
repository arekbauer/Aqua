package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class WatchStatePublisherTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC)
    private val hydration = FakeObservedHydrationRepository()
    private val watch = FakeWatchIntakeRepository()

    private fun publisher(sink: RecordingWatchStateSink) =
        WatchStatePublisher(ObserveWatchStateUseCase(hydration, watch, clock, ZoneOffset.UTC), sink)

    @Test
    fun publishesInitialSnapshotAndEveryChange() = runTest {
        val sink = RecordingWatchStateSink()
        publisher(sink).start(backgroundScope + UnconfinedTestDispatcher(testScheduler))

        hydration.entries.value = listOf(WaterIntake(1, 350, 1_000, "2026-10-31"))
        hydration.settings.value = HydrationSettings(dailyGoalMl = 3_000)

        assertEquals(listOf(0, 350, 350), sink.published.map { it.totalMl })
        assertEquals(listOf(2_000, 2_000, 3_000), sink.published.map { it.goalMl })
    }

    @Test
    fun aFailedPublishDoesNotStopLaterUpdates() = runTest {
        val sink = RecordingWatchStateSink(failFirstPublish = true)
        publisher(sink).start(backgroundScope + UnconfinedTestDispatcher(testScheduler))

        assertTrue("first publish failed and was swallowed", sink.published.isEmpty())
        hydration.entries.value = listOf(WaterIntake(1, 200, 1_000, "2026-10-31"))

        assertEquals(listOf(200), sink.published.map { it.totalMl })
    }

    @Test
    fun acknowledgementOfAWatchEventIsPublished() = runTest {
        val sink = RecordingWatchStateSink()
        publisher(sink).start(backgroundScope + UnconfinedTestDispatcher(testScheduler))

        watch.addIntakeFromWatch("event-1", 250, 1_000, "2026-10-31")

        assertEquals(listOf("event-1"), sink.published.last().acknowledgedEventIds)
    }
}
