package com.arekb.aqua.ui.screens.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.ClearAllHistoryUseCase
import com.arekb.aqua.domain.usecase.ExportDataUseCase
import com.arekb.aqua.domain.usecase.ImportDataUseCase
import com.arekb.aqua.domain.usecase.ResetDailyIntakeUseCase
import com.arekb.aqua.domain.usecase.SaveHydrationSettingsUseCase
import com.arekb.aqua.ui.model.UserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import java.io.OutputStream
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
    private val resetDailyIntake: ResetDailyIntakeUseCase,
    private val clearAllHistory: ClearAllHistoryUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
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

    fun wipeAllData() {
        performWrite("wipe all history", "All history wiped") { clearAllHistory() }
    }

    fun exportData(outputStream: OutputStream) {
        performWrite("export data", "Data exported successfully") { exportDataUseCase(outputStream) }
    }

    fun exportData(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    exportDataUseCase(outputStream)
                } ?: throw IllegalStateException("Could not open output stream")
                showMessage("Data exported successfully")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Couldn’t export data. Please try again.")
            }
        }
    }

    fun importData(inputStream: InputStream) {
        performWrite("import data", "Data restored successfully") { importDataUseCase(inputStream) }
    }

    fun importData(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    importDataUseCase(inputStream)
                } ?: throw IllegalStateException("Could not open input stream")
                showMessage("Data restored successfully")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Couldn’t import data. Please check the backup file.")
            }
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

    private fun showMessage(text: String) {
        messageState.value = UserMessage(++nextMessageId, text)
    }
}
