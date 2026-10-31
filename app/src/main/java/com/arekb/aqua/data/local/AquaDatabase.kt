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
    val presetFourMl: Int = 750
)

data class DailyTotalRow(val localDate: String, val totalMl: Int)

@Dao
interface AquaDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(settings: SettingsEntity)

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

    @Query("SELECT localDate, SUM(amountMl) AS totalMl FROM intake_entries GROUP BY localDate ORDER BY localDate DESC")
    fun observeDailyTotals(): Flow<List<DailyTotalRow>>
}

@Database(entities = [IntakeEntity::class, SettingsEntity::class], version = 1, exportSchema = true)
abstract class AquaDatabase : RoomDatabase() {
    abstract fun aquaDao(): AquaDao
}
