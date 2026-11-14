package com.arekb.aqua.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Add a versioned Migration here whenever AquaDatabase's schema changes. */
internal object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `daily_goal_history` " +
                    "(`effectiveLocalDate` TEXT NOT NULL, `goalMl` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`effectiveLocalDate`))"
            )
            // Existing history has no saved target. Record today's target as the first known value.
            db.execSQL(
                "INSERT OR IGNORE INTO `daily_goal_history` (`effectiveLocalDate`, `goalMl`) " +
                    "SELECT date('now', 'localtime'), `dailyGoalMl` FROM `app_settings` WHERE `id` = 1"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `liquidUnit` TEXT NOT NULL DEFAULT 'ML'")
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `appTheme` TEXT NOT NULL DEFAULT 'SYSTEM'")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `isDynamicColorEnabled` INTEGER NOT NULL DEFAULT 1")
        }
    }

    /** Adds the nullable, uniquely indexed `externalId` used to de-duplicate entries synced from the watch. */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `intake_entries` ADD COLUMN `externalId` TEXT")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_intake_entries_externalId` " +
                    "ON `intake_entries` (`externalId`)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
