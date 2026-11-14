package com.arekb.aqua.wear.di

import android.content.Context
import androidx.room.Room
import com.arekb.aqua.wear.data.WatchHydrationRepositoryImpl
import com.arekb.aqua.wear.data.WearSyncGateway
import com.arekb.aqua.wear.data.WearSyncGatewayImpl
import com.arekb.aqua.wear.data.local.WatchDao
import com.arekb.aqua.wear.data.local.WatchDatabase
import com.arekb.aqua.wear.domain.WatchHydrationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearModule {

    @Binds
    abstract fun bindWatchHydrationRepository(
        impl: WatchHydrationRepositoryImpl
    ): WatchHydrationRepository

    @Binds
    abstract fun bindWearSyncGateway(
        impl: WearSyncGatewayImpl
    ): WearSyncGateway

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()

        @Provides
        @Singleton
        fun provideWatchDatabase(@ApplicationContext context: Context): WatchDatabase =
            Room.databaseBuilder(
                context,
                WatchDatabase::class.java,
                "watch_aqua_db"
            ).build()

        @Provides
        fun provideWatchDao(db: WatchDatabase): WatchDao = db.watchDao()
    }
}
