package com.arekb.aqua.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.ResetDailyIntakeUseCase
import com.arekb.aqua.domain.usecase.SaveHydrationSettingsUseCase
import com.arekb.aqua.ui.model.UserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: HydrationSettings = HydrationSettings(),
    val message: UserMessage? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    repository: HydrationRepository,
    private val saveHydrationSettings: SaveHydrationSettingsUseCase,
    private val resetDailyIntake: ResetDailyIntakeUseCase
) : ViewModel() {
    private val messageState = MutableStateFlow<UserMessage?>(null)
    private var nextMessageId = 0L

    val uiState = combine(repository.observeSettings(), messageState) { settings, message ->
        SettingsUiState(settings ?: HydrationSettings(), message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun saveSettings(settings: HydrationSettings) {
        performWrite("save your settings", "Settings saved") { saveHydrationSettings(settings) }
    }

    fun resetToday() {
        performWrite("reset today’s total", "Today’s total reset") { resetDailyIntake() }
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

    private fun showMessage(text: String) {
        messageState.value = UserMessage(++nextMessageId, text)
    }
}
