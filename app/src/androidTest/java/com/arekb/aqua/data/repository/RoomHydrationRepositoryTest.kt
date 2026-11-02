package com.arekb.aqua.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arekb.aqua.data.local.AquaDatabase
import com.arekb.aqua.domain.model.HydrationSettings
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomHydrationRepositoryTest {
    private lateinit var database: AquaDatabase
    private lateinit var repository: RoomHydrationRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AquaDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomHydrationRepository(database.aquaDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun observeSettingsAutomaticallyCreatesDailyGoalForToday() = runTest {
        // Initially, the DB is empty. Observing settings triggers ensureDailyGoalForToday().
        val settings = repository.observeSettings().first()

        assertNotNull(settings)

        val today = LocalDate.now().toString()
        val goals = repository.observeDailyGoals().first()

        assertEquals(1, goals.size)
        assertEquals(today, goals.first().effectiveLocalDate)
        assertEquals(settings?.dailyGoalMl, goals.first().goalMl)
    }

    @Test
    fun saveSettingsAlsoUpdatesDailyGoalForToday() = runTest {
        // Trigger initial setup
        repository.observeSettings().first()

        // Save new settings with a different goal
        val newSettings = HydrationSettings(dailyGoalMl = 3000)
        repository.saveSettings(newSettings)

        val updatedSettings = repository.observeSettings().first()
        assertEquals(3000, updatedSettings?.dailyGoalMl)

        val goals = repository.observeDailyGoals().first()
        val today = LocalDate.now().toString()

        // The goal for today should now be updated to 3000
        val todayGoal = goals.find { it.effectiveLocalDate == today }
        assertNotNull(todayGoal)
        assertEquals(3000, todayGoal?.goalMl)
    }
}
