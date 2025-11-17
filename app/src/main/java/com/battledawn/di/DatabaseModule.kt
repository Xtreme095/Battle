package com.battledawn.di

import android.content.Context
import androidx.room.Room
import com.battledawn.data.local.BattleDawnDatabase
import com.battledawn.data.local.dao.ColonyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BattleDawnDatabase {
        return Room.databaseBuilder(
            context,
            BattleDawnDatabase::class.java,
            BattleDawnDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideColonyDao(database: BattleDawnDatabase): ColonyDao {
        return database.colonyDao()
    }
}
