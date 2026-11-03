package com.arekb.aqua.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "intake_entries")
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long,
    val localDate: String
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyGoalMl: Int = 2_000,
    val presetOneMl: Int = 200,
    val presetTwoMl: Int = 350,
    val presetThreeMl: Int = 500,
    val presetFourMl: Int = 750,
    val liquidUnit: String = "ML",
    val appTheme: String = "SYSTEM",
    val isDynamicColorEnabled: Boolean = true
)

@Entity(tableName = "daily_goal_history")
data class DailyGoalEntity(
    @PrimaryKey val effectiveLocalDate: String,
    val goalMl: Int
)

data class DailyTotalRow(val localDate: String, val totalMl: Int)

@Dao
interface AquaDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @androidx.room.Transaction
    suspend fun saveSettingsAndGoal(settings: SettingsEntity, dailyGoal: DailyGoalEntity) {
        saveSettings(settings)
        saveDailyGoal(dailyGoal)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(settings: SettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyGoal(goal: DailyGoalEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyGoalIfMissing(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goal_history ORDER BY effectiveLocalDate ASC")
    fun observeDailyGoals(): Flow<List<DailyGoalEntity>>

    @Insert
    suspend fun insertEntry(entry: IntakeEntity): Long

    @Query("DELETE FROM intake_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("UPDATE intake_entries SET amountMl = :amountMl WHERE id = :id")
    suspend fun updateEntryAmount(id: Long, amountMl: Int)

    @Query("DELETE FROM intake_entries WHERE localDate = :date")
    suspend fun clearEntriesForDate(date: String)

    @Query("SELECT * FROM intake_entries WHERE localDate = :date ORDER BY timestamp DESC")
    fun observeEntriesForDate(date: String): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intake_entries WHERE localDate BETWEEN :startDate AND :endDate ORDER BY localDate ASC, timestamp ASC")
    fun observeEntriesBetween(startDate: String, endDate: String): Flow<List<IntakeEntity>>

    @Query("SELECT localDate, SUM(amountMl) AS totalMl FROM intake_entries GROUP BY localDate ORDER BY localDate DESC")
    fun observeDailyTotals(): Flow<List<DailyTotalRow>>

    @Query("DELETE FROM intake_entries")
    suspend fun clearAllEntries()

    @Query("DELETE FROM daily_goal_history")
    suspend fun clearAllDailyGoals()

    @Query("SELECT * FROM intake_entries ORDER BY timestamp ASC")
    suspend fun getAllEntries(): List<IntakeEntity>

    @Query("SELECT * FROM daily_goal_history ORDER BY effectiveLocalDate ASC")
    suspend fun getAllDailyGoals(): List<DailyGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<IntakeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGoals(goals: List<DailyGoalEntity>)

    @androidx.room.Transaction
    suspend fun clearAllHistory() {
        clearAllEntries()
        clearAllDailyGoals()
    }

    @androidx.room.Transaction
    suspend fun restoreAllData(settings: SettingsEntity?, goals: List<DailyGoalEntity>, entries: List<IntakeEntity>) {
        clearAllEntries()
        clearAllDailyGoals()
        if (settings != null) {
            saveSettings(settings)
        }
        if (goals.isNotEmpty()) {
            insertDailyGoals(goals)
        }
        if (entries.isNotEmpty()) {
            insertEntries(entries)
        }
    }
}

@Database(entities = [IntakeEntity::class, SettingsEntity::class, DailyGoalEntity::class], version = 4, exportSchema = true)
abstract class AquaDatabase : RoomDatabase() {
    abstract fun aquaDao(): AquaDao
}
