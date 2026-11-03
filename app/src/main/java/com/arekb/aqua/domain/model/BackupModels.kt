package com.arekb.aqua.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AquaBackupData(
    val version: Int = 1,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val settings: BackupSettings? = null,
    val dailyGoals: List<BackupDailyGoal> = emptyList(),
    val intakeEntries: List<BackupWaterIntake> = emptyList()
)

@Serializable
data class BackupSettings(
    val dailyGoalMl: Int = 2000,
    val presetsMl: List<Int> = listOf(200, 350, 500),
    val liquidUnit: String = "ML",
    val appTheme: String = "SYSTEM",
    val isDynamicColorEnabled: Boolean = true
)

@Serializable
data class BackupDailyGoal(
    val effectiveLocalDate: String,
    val goalMl: Int
)

@Serializable
data class BackupWaterIntake(
    val id: Long = 0,
    val amountMl: Int,
    val timestampMillis: Long,
    val localDate: String
)

fun HydrationSettings.toBackup() = BackupSettings(
    dailyGoalMl = dailyGoalMl,
    presetsMl = presetsMl,
    liquidUnit = liquidUnit.name,
    appTheme = appTheme.name,
    isDynamicColorEnabled = isDynamicColorEnabled
)

fun BackupSettings.toDomain() = HydrationSettings(
    dailyGoalMl = dailyGoalMl,
    presetsMl = if (presetsMl.size == 3) presetsMl else listOf(200, 350, 500),
    liquidUnit = try { LiquidUnit.valueOf(liquidUnit) } catch (_: Exception) { LiquidUnit.ML },
    appTheme = try { AppTheme.valueOf(appTheme) } catch (_: Exception) { AppTheme.SYSTEM },
    isDynamicColorEnabled = isDynamicColorEnabled
)

fun DailyGoal.toBackup() = BackupDailyGoal(
    effectiveLocalDate = effectiveLocalDate,
    goalMl = goalMl
)

fun BackupDailyGoal.toDomain() = DailyGoal(
    effectiveLocalDate = effectiveLocalDate,
    goalMl = goalMl
)

fun WaterIntake.toBackup() = BackupWaterIntake(
    id = id,
    amountMl = amountMl,
    timestampMillis = timestampMillis,
    localDate = localDate
)

fun BackupWaterIntake.toDomain() = WaterIntake(
    id = id,
    amountMl = amountMl,
    timestampMillis = timestampMillis,
    localDate = localDate
)
