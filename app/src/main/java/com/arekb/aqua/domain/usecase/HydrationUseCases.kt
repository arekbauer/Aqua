package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.HydrationSettings
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.domain.repository.HydrationRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class AddWaterUseCase(
    private val repository: HydrationRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(amountMl: Int): WaterIntake? {
        if (amountMl <= 0) return null
        val instant = clock.instant()
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()
        return repository.addIntake(amountMl, instant.toEpochMilli(), localDate)
    }
}

class SaveHydrationSettingsUseCase(private val repository: HydrationRepository) {
    suspend operator fun invoke(settings: HydrationSettings) {
        require(settings.dailyGoalMl > 0)
        require(settings.presetsMl.size == 4 && settings.presetsMl.all { it > 0 })
        repository.saveSettings(settings)
    }
}

class ResetDailyIntakeUseCase(
    private val repository: HydrationRepository,
    private val clock: Clock
) {
    suspend operator fun invoke() = repository.clearDate(
        clock.instant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
    )
}


