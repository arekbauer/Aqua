package com.arekb.aqua.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAquaDatabase(@ApplicationContext context: Context): AquaDatabase =
        Room.databaseBuilder(context, AquaDatabase::class.java, "aqua.db")
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

    @Provides
    fun provideAquaDao(database: AquaDatabase): AquaDao = database.aquaDao()
}
