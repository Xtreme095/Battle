package com.battledawn.domain.repository

import com.battledawn.domain.model.Building
import com.battledawn.domain.model.BuildingType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for building operations
 */
interface BuildingRepository {

    /**
     * Get all buildings for a colony
     */
    fun getColonyBuildings(colonyId: String): Flow<List<Building>>

    /**
     * Get a specific building
     */
    fun getBuilding(buildingId: String): Flow<Building?>

    /**
     * Start building construction
     */
    suspend fun startConstruction(
        colonyId: String,
        buildingType: BuildingType,
        position: com.battledawn.domain.model.GridPosition
    ): Result<Building>

    /**
     * Upgrade an existing building
     */
    suspend fun upgradeBuilding(buildingId: String): Result<Building>

    /**
     * Complete construction/upgrade
     */
    suspend fun completeConstruction(buildingId: String): Result<Building>

    /**
     * Cancel construction
     */
    suspend fun cancelConstruction(buildingId: String): Result<Unit>

    /**
     * Get construction queue for a colony
     */
    fun getConstructionQueue(colonyId: String): Flow<List<Building>>
}
