package com.arekb.aqua.di

import com.arekb.aqua.data.wear.DataClientWatchStateSink
import com.arekb.aqua.domain.repository.WatchStateSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearSyncModule {
    @Binds
    @Singleton
    abstract fun bindWatchStateSink(implementation: DataClientWatchStateSink): WatchStateSink
}
