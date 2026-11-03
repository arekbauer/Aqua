package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.AquaBackupData
import com.arekb.aqua.domain.model.toDomain
import com.arekb.aqua.domain.repository.HydrationRepository
import kotlinx.serialization.json.Json
import java.io.InputStream

class ImportDataUseCase(
    private val repository: HydrationRepository,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {
    suspend operator fun invoke(inputStream: InputStream) {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        require(jsonString.isNotBlank()) { "Backup file is empty" }

        val backupData = json.decodeFromString(AquaBackupData.serializer(), jsonString)
        val settings = backupData.settings?.toDomain()
        if (settings != null) {
            require(settings.dailyGoalMl > 0) { "Invalid daily goal in backup settings" }
            require(settings.presetsMl.all { it > 0 }) { "Invalid preset amounts in backup settings" }
        }

        val dailyGoals = backupData.dailyGoals.map { it.toDomain() }
        require(dailyGoals.all { it.effectiveLocalDate.isNotBlank() && it.goalMl > 0 }) {
            "Invalid daily goal history in backup"
        }

        val entries = backupData.intakeEntries.map { it.toDomain() }
        require(entries.all { it.localDate.isNotBlank() && it.amountMl > 0 }) {
            "Invalid water intake entry in backup"
        }

        repository.restoreData(settings, dailyGoals, entries)
    }
}
