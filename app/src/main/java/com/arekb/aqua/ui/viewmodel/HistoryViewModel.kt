package com.arekb.aqua.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(val days: List<DailyTotal> = emptyList())

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: HydrationRepository
) : ViewModel() {
    val uiState = repository.observeDailyTotals()
        .map(::HistoryUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())
}
