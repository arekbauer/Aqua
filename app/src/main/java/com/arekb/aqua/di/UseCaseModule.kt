package com.arekb.aqua.di

import com.arekb.aqua.domain.repository.HydrationRepository
import com.arekb.aqua.domain.usecase.AddWaterUseCase
import com.arekb.aqua.domain.usecase.ResetDailyIntakeUseCase
import com.arekb.aqua.domain.usecase.SaveHydrationSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    fun provideAddWaterUseCase(repository: HydrationRepository, clock: Clock) =
        AddWaterUseCase(repository, clock)

    @Provides
    fun provideSaveHydrationSettingsUseCase(repository: HydrationRepository) =
        SaveHydrationSettingsUseCase(repository)

    @Provides
    fun provideResetDailyIntakeUseCase(repository: HydrationRepository, clock: Clock) =
        ResetDailyIntakeUseCase(repository, clock)

    @Provides
    fun provideClearAllHistoryUseCase(repository: HydrationRepository) =
        com.arekb.aqua.domain.usecase.ClearAllHistoryUseCase(repository)

    @Provides
    fun provideExportDataUseCase(repository: HydrationRepository) =
        com.arekb.aqua.domain.usecase.ExportDataUseCase(repository)

    @Provides
    fun provideImportDataUseCase(repository: HydrationRepository) =
        com.arekb.aqua.domain.usecase.ImportDataUseCase(repository)
}
