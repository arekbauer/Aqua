package com.arekb.aqua.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.AddWaterUseCase
import com.arekb.aqua.ui.model.UserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayUiState(
    val settings: HydrationSettings = HydrationSettings(),
    val today: String = LocalDate.now().toString(),
    val todayTotalMl: Int = 0,
    val entries: List<WaterIntake> = emptyList(),
    val message: UserMessage? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HydrationRepository,
    private val addWater: AddWaterUseCase
) : ViewModel() {
    private val todayKey = MutableStateFlow(localDateNow())
    private val messageState = MutableStateFlow<UserMessage?>(null)
    private var nextMessageId = 0L

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(
        repository.observeSettings(),
        todayKey.flatMapLatest(repository::observeEntriesForDate),
        todayKey,
        messageState
    ) { settings, entries, today, message ->
        TodayUiState(
            settings = settings ?: HydrationSettings(),
            today = today,
            todayTotalMl = entries.sumOf(WaterIntake::amountMl),
            entries = entries,
            message = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    init {
        viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1L))
                refreshDate()
            }
        }
    }

    fun refreshDate() {
        todayKey.value = localDateNow()
    }

    fun logWater(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            try {
                val intake = addWater(amountMl) ?: return@launch
                refreshDate()
                showMessage("${intake.amountMl} ml added", intake)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Couldn’t save that drink. Please try again.")
            }
        }
    }

    fun undo(intake: WaterIntake) {
        performWrite("undo that entry", "Entry undone") { repository.undoIntake(intake) }
    }

    fun delete(intake: WaterIntake) {
        performWrite("delete that entry", "Entry deleted") { repository.undoIntake(intake) }
    }

    fun updateAmount(intake: WaterIntake, amountMl: Int) {
        if (amountMl <= 0) return
        performWrite("update that entry", "Entry updated") {
            repository.updateIntakeAmount(intake.id, amountMl)
        }
    }

    fun messageShown(id: Long) {
        if (messageState.value?.id == id) messageState.value = null
    }

    private fun performWrite(action: String, successMessage: String, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                showMessage(successMessage)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Couldn’t $action. Please try again.")
            }
        }
    }

    private fun showMessage(text: String, undoIntake: WaterIntake? = null) {
        messageState.value = UserMessage(++nextMessageId, text, undoIntake)
    }

    private fun localDateNow(): String = LocalDate.now(ZoneId.systemDefault()).toString()
}
