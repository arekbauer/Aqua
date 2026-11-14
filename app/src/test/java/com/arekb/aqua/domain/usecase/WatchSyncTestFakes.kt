package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.repository.WatchIntakeRepository
import com.arekb.aqua.domain.repository.WatchStateSink
import com.arekb.aqua.sync.WatchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** One drink stored through [FakeWatchIntakeRepository]. */
internal data class StoredWatchEntry(
    val externalId: String,
    val amountMl: Int,
    val timestampMillis: Long,
    val localDate: String
)

internal class FakeWatchIntakeRepository : WatchIntakeRepository {
    private val entries = MutableStateFlow<List<StoredWatchEntry>>(emptyList())
    val stored: List<StoredWatchEntry> get() = entries.value

    override suspend fun addIntakeFromWatch(
        externalId: String,
        amountMl: Int,
        timestampMillis: Long,
        localDate: String
    ): Boolean {
        if (entries.value.any { it.externalId == externalId }) return false
        entries.value = entries.value + StoredWatchEntry(externalId, amountMl, timestampMillis, localDate)
        return true
    }

    override fun observeWatchEventIdsSince(startLocalDate: String): Flow<List<String>> =
        entries.map { all -> all.filter { it.localDate >= startLocalDate }.map { it.externalId } }
}

/** Serves settings and entries from memory; every other operation is outside what these tests exercise. */
internal class FakeObservedHydrationRepository(
    val settings: MutableStateFlow<HydrationSettings?> = MutableStateFlow(HydrationSettings()),
    val entries: MutableStateFlow<List<WaterIntake>> = MutableStateFlow(emptyList())
) : HydrationRepository {
    override fun observeSettings(): Flow<HydrationSettings?> = settings

    override fun observeEntriesForDate(localDate: String): Flow<List<WaterIntake>> =
        entries.map { all -> all.filter { it.localDate == localDate } }

    override fun observeDailyTotals(): Flow<List<DailyTotal>> = unsupported()
    override fun observeDailyGoals(): Flow<List<DailyGoal>> = unsupported()
    override fun observeEntriesBetween(startLocalDate: String, endLocalDate: String): Flow<List<WaterIntake>> =
        unsupported()

    override suspend fun saveSettings(settings: HydrationSettings) = unsupported()
    override suspend fun addIntake(amountMl: Int, timestampMillis: Long, localDate: String): WaterIntake = unsupported()
    override suspend fun updateIntakeAmount(intakeId: Long, amountMl: Int) = unsupported()
    override suspend fun undoIntake(intake: WaterIntake) = unsupported()
    override suspend fun clearDate(localDate: String) = unsupported()
    override suspend fun clearAllHistory() = unsupported()
    override suspend fun saveDailyGoal(localDate: String, goalMl: Int) = unsupported()
    override suspend fun getAllEntries(): List<WaterIntake> = unsupported()
    override suspend fun getAllDailyGoals(): List<DailyGoal> = unsupported()
    override suspend fun restoreData(
        settings: HydrationSettings?,
        dailyGoals: List<DailyGoal>,
        entries: List<WaterIntake>
    ) = unsupported()

    private fun unsupported(): Nothing = throw UnsupportedOperationException("Not used by these tests")
}

internal class RecordingWatchStateSink(failFirstPublish: Boolean = false) : WatchStateSink {
    val published = mutableListOf<WatchState>()
    private var failuresLeft = if (failFirstPublish) 1 else 0

    override suspend fun publish(state: WatchState) {
        if (failuresLeft > 0) {
            failuresLeft--
            throw IllegalStateException("Simulated publish failure")
        }
        published += state
    }
}
