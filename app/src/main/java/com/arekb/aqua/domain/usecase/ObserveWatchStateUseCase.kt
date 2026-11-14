package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.repository.WatchIntakeRepository
import com.arekb.aqua.sync.WatchState
import com.arekb.aqua.sync.WearSyncContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Emits the snapshot the watch should display: today's total, goal, presets, unit and the
 * watch events the phone has already stored. Re-emits when any of these change or the day rolls over.
 */
class ObserveWatchStateUseCase(
    private val repository: HydrationRepository,
    private val watchRepository: WatchIntakeRepository,
    private val clock: Clock,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<WatchState> = todayDates().flatMapLatest { today ->
        combine(
            repository.observeSettings(),
            repository.observeEntriesForDate(today.toString()),
            watchRepository.observeWatchEventIdsSince(today.minusDays(WearSyncContract.ACK_WINDOW_DAYS).toString())
        ) { settings, entries, acknowledgedIds ->
            val resolved = settings ?: HydrationSettings()
            WatchState(
                localDate = today.toString(),
                totalMl = entries.sumOf(WaterIntake::amountMl),
                goalMl = resolved.dailyGoalMl,
                presetsMl = resolved.presetsMl,
                liquidUnit = resolved.liquidUnit.name,
                acknowledgedEventIds = acknowledgedIds.sorted()
            )
        }
    }.distinctUntilChanged()

    private fun todayDates(): Flow<LocalDate> = flow {
        while (true) {
            val now = ZonedDateTime.ofInstant(clock.instant(), zoneId)
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zoneId)
            delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1L))
        }
    }
}
