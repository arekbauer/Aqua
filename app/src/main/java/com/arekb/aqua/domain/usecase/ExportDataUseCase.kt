package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.AquaBackupData
import com.arekb.aqua.domain.model.toBackup
import com.arekb.aqua.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import java.io.OutputStream

class ExportDataUseCase(
    private val repository: HydrationRepository,
    private val json: Json = Json { prettyPrint = true; ignoreUnknownKeys = true }
) {
    suspend operator fun invoke(outputStream: OutputStream) {
        val settings = repository.observeSettings().firstOrNull()?.toBackup()
        val dailyGoals = repository.getAllDailyGoals().map { it.toBackup() }
        val entries = repository.getAllEntries().map { it.toBackup() }

        val backupData = AquaBackupData(
            version = 1,
            exportedAtMillis = System.currentTimeMillis(),
            settings = settings,
            dailyGoals = dailyGoals,
            intakeEntries = entries
        )

        val jsonString = json.encodeToString(AquaBackupData.serializer(), backupData)
        outputStream.bufferedWriter().use { writer ->
            writer.write(jsonString)
            writer.flush()
        }
    }
}
