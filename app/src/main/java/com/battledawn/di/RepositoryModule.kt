package com.battledawn.di

import com.battledawn.data.repository.*
import com.battledawn.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindColonyRepository(
        impl: ColonyRepositoryImpl
    ): ColonyRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(
        impl: PlayerRepositoryImpl
    ): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindBuildingRepository(
        impl: BuildingRepositoryImpl
    ): BuildingRepository

    @Binds
    @Singleton
    abstract fun bindUnitRepository(
        impl: UnitRepositoryImpl
    ): UnitRepository

    @Binds
    @Singleton
    abstract fun bindCombatRepository(
        impl: CombatRepositoryImpl
    ): CombatRepository

    @Binds
    @Singleton
    abstract fun bindAllianceRepository(
        impl: AllianceRepositoryImpl
    ): AllianceRepository
}
