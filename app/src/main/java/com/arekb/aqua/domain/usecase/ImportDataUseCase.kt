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
        val dailyGoals = backupData.dailyGoals.map { it.toDomain() }
        val entries = backupData.intakeEntries.map { it.toDomain() }

        repository.restoreData(settings, dailyGoals, entries)
    }
}
