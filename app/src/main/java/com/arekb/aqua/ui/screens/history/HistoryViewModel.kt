package com.arekb.aqua.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.HistoryInsightsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class HistoryUiState(
    val isLoading: Boolean = true,
    val insights: HistoryInsights? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HydrationRepository,
    private val clock: Clock
) : ViewModel() {
    private val today = MutableStateFlow(todayLocalDate())

    init {
        viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now(clock.zone)
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1L).milliseconds)
                refreshDate()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entries = today
        .map { date -> date.minusDays(29).toString() to date.toString() }
        .flatMapLatest { (start, end) -> repository.observeEntriesBetween(start, end) }

    val uiState = combine(
        repository.observeDailyTotals(),
        entries,
        repository.observeDailyGoals(),
        today
    ) { totals, intakeEntries, goals, currentDate ->
        HistoryUiState(
            isLoading = false,
            insights = HistoryInsightsCalculator.calculate(
                today = currentDate,
                totals = totals,
                entries = intakeEntries,
                goals = goals,
                zoneId = ZoneId.systemDefault()
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun refreshDate() {
        today.value = todayLocalDate()
    }

    private fun todayLocalDate(): LocalDate = clock.instant().atZone(ZoneId.systemDefault()).toLocalDate()
}
