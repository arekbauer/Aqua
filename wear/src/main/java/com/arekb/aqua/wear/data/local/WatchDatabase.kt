package com.arekb.aqua.wear.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PendingEventEntity::class], version = 1, exportSchema = false)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao
}
