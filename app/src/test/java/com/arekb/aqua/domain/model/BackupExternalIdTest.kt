package com.arekb.aqua.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupExternalIdTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun externalIdSurvivesBackupMapping() {
        val intake = WaterIntake(7, 300, 1_000, "2026-10-31", externalId = "event-1")

        assertEquals(intake, intake.toBackup().toDomain())
    }

    @Test
    fun blankExternalIdIsTreatedAsMissing() {
        val backup = BackupWaterIntake(amountMl = 200, timestampMillis = 1_000, localDate = "2026-10-31", externalId = "  ")

        assertNull(backup.toDomain().externalId)
    }

    @Test
    fun backupsWrittenBeforeWatchSyncStillDecode() {
        val legacy = """{"id":3,"amountMl":250,"timestampMillis":5000,"localDate":"2026-10-31"}"""

        val intake = json.decodeFromString(BackupWaterIntake.serializer(), legacy).toDomain()

        assertNull(intake.externalId)
        assertEquals(250, intake.amountMl)
    }
}
