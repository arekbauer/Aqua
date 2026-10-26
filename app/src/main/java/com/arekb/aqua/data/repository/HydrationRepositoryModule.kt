package com.arekb.aqua.data.repository

import com.arekb.aqua.domain.repository.HydrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HydrationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHydrationRepository(implementation: RoomHydrationRepository): HydrationRepository
}
