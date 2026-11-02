package com.arekb.aqua.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AquaDaoTest {
    private lateinit var database: AquaDatabase
    private lateinit var dao: AquaDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AquaDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.aquaDao()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun entriesAreOrderedByTimeAndTotalsAreGroupedByDate() = runBlocking {
        dao.insertEntry(IntakeEntity(amountMl = 200, timestamp = 1_000, localDate = "2026-10-31"))
        dao.insertEntry(IntakeEntity(amountMl = 350, timestamp = 3_000, localDate = "2026-10-31"))
        dao.insertEntry(IntakeEntity(amountMl = 500, timestamp = 2_000, localDate = "2026-10-30"))

        val today = dao.observeEntriesForDate("2026-10-31").first()
        assertEquals(listOf(350, 200), today.map(IntakeEntity::amountMl))
        assertEquals(listOf(DailyTotalRow("2026-10-31", 550), DailyTotalRow("2026-10-30", 500)), dao.observeDailyTotals().first())
    }

    @Test
    fun updatesAndDeletesChangeTheDailyTotal() = runBlocking {
        val id = dao.insertEntry(IntakeEntity(amountMl = 250, timestamp = 1_000, localDate = "2026-10-31"))
        dao.updateEntryAmount(id, 400)
        assertEquals(400, dao.observeDailyTotals().first().single().totalMl)

        dao.deleteEntry(id)
        assertEquals(emptyList<DailyTotalRow>(), dao.observeDailyTotals().first())
    }

    @Test
    fun dailyGoalHistoryStoresEffectiveDatesAndReplacesSameDayGoal() = runBlocking {
        dao.saveDailyGoal(DailyGoalEntity("2026-10-29", 2_000))
        dao.saveDailyGoal(DailyGoalEntity("2026-10-31", 2_500))
        dao.saveDailyGoal(DailyGoalEntity("2026-10-31", 2_300))

        assertEquals(
            listOf(DailyGoalEntity("2026-10-29", 2_000), DailyGoalEntity("2026-10-31", 2_300)),
            dao.observeDailyGoals().first()
        )
    }

    @Test
    fun savingSettingsAlsoRecordsThatDaysGoal() = runBlocking {
        dao.saveSettingsAndGoal(
            SettingsEntity(dailyGoalMl = 2_400),
            DailyGoalEntity("2026-10-31", 2_400)
        )

        assertEquals(2_400, dao.getSettings()?.dailyGoalMl)
        assertEquals(DailyGoalEntity("2026-10-31", 2_400), dao.observeDailyGoals().first().single())
    }

    @Test
    fun entriesBetweenDatesAreInclusiveAndChronological() = runBlocking {
        dao.insertEntry(IntakeEntity(amountMl = 100, timestamp = 3_000, localDate = "2026-10-31"))
        dao.insertEntry(IntakeEntity(amountMl = 200, timestamp = 1_000, localDate = "2026-10-30"))
        dao.insertEntry(IntakeEntity(amountMl = 300, timestamp = 2_000, localDate = "2026-11-01"))

        val entries = dao.observeEntriesBetween("2026-10-30", "2026-10-31").first()

        assertEquals(listOf("2026-10-30", "2026-10-31"), entries.map(IntakeEntity::localDate))
        assertEquals(listOf(200, 100), entries.map(IntakeEntity::amountMl))
    }
}
