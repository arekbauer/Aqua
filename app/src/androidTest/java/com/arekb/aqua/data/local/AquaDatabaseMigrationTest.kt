package com.arekb.aqua.data.local

import androidx.room.testing.MigrationTestHelper
import android.database.sqlite.SQLiteConstraintException
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AquaDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AquaDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrationPreservesExistingDataAndStartsGoalHistoryToday() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL("INSERT INTO app_settings (id, dailyGoalMl, presetOneMl, presetTwoMl, presetThreeMl, presetFourMl) VALUES (1, 2300, 200, 350, 500, 750)")
            execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate) VALUES (450, 1793400000000, '2026-10-31')")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            DatabaseMigrations.MIGRATION_1_2
        )
        migrated.query("SELECT effectiveLocalDate, goalMl FROM daily_goal_history").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(2_300, cursor.getInt(1))
        }
        migrated.query("SELECT amountMl FROM intake_entries").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(450, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migration3To4AddsIsDynamicColorEnabledColumn() {
        helper.createDatabase(TEST_DATABASE, 3).apply {
            execSQL("INSERT INTO app_settings (id, dailyGoalMl, presetOneMl, presetTwoMl, presetThreeMl, presetFourMl, liquidUnit, appTheme) VALUES (1, 2000, 200, 350, 500, 750, 'ML', 'SYSTEM')")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            4,
            true,
            DatabaseMigrations.MIGRATION_3_4
        )
        migrated.query("SELECT isDynamicColorEnabled FROM app_settings WHERE id = 1").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migration4To5AddsNullableExternalIdAndKeepsExistingEntries() {
        helper.createDatabase(TEST_DATABASE, 4).apply {
            execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate) VALUES (450, 1793400000000, '2026-10-31')")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            5,
            true,
            DatabaseMigrations.MIGRATION_4_5
        )
        migrated.query("SELECT amountMl, externalId FROM intake_entries").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(450, cursor.getInt(0))
            assertTrue(cursor.isNull(1))
        }
        migrated.close()
    }

    @Test
    fun migration4To5EnforcesUniqueExternalIdsButAllowsManyNulls() {
        helper.createDatabase(TEST_DATABASE, 4).close()
        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            5,
            true,
            DatabaseMigrations.MIGRATION_4_5
        )

        migrated.execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate, externalId) VALUES (200, 1, '2026-10-31', NULL)")
        migrated.execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate, externalId) VALUES (300, 2, '2026-10-31', NULL)")
        migrated.execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate, externalId) VALUES (350, 3, '2026-10-31', 'event-1')")
        assertThrows(SQLiteConstraintException::class.java) {
            migrated.execSQL("INSERT INTO intake_entries (amountMl, timestamp, localDate, externalId) VALUES (350, 4, '2026-10-31', 'event-1')")
        }
        migrated.close()
    }

    private companion object {
        const val TEST_DATABASE = "aqua-migration-test"
    }
}
