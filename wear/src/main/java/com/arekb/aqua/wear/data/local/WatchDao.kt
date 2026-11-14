package com.arekb.aqua.wear.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PendingEventEntity)

    @Query("SELECT * FROM pending_events")
    fun getPendingEvents(): Flow<List<PendingEventEntity>>

    @Query("DELETE FROM pending_events WHERE eventId IN (:eventIds)")
    suspend fun deleteEvents(eventIds: List<String>)
}
