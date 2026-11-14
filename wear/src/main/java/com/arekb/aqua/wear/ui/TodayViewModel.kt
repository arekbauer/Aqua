package com.arekb.aqua.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.wear.domain.LogPresetUseCase
import com.arekb.aqua.wear.domain.TodayState
import com.arekb.aqua.wear.domain.WatchHydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    repository: WatchHydrationRepository,
    private val logPresetUseCase: LogPresetUseCase
) : ViewModel() {

    val state: StateFlow<TodayState?> = repository.todayState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun logDrink(amountMl: Int) {
        viewModelScope.launch {
            logPresetUseCase(amountMl)
        }
    }
}
