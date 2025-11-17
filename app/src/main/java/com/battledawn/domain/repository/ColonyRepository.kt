package com.battledawn.domain.repository

import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.ResourcePool
import kotlinx.coroutines.flow.Flow

/**
 * Repository for colony management operations
 */
interface ColonyRepository {

    /**
     * Get all colonies for a player
     */
    fun getPlayerColonies(playerId: String): Flow<List<Colony>>

    /**
     * Get a specific colony by ID
     */
    fun getColony(colonyId: String): Flow<Colony?>

    /**
     * Create a new colony
     */
    suspend fun createColony(colony: Colony): Result<Colony>

    /**
     * Update colony data
     */
    suspend fun updateColony(colony: Colony): Result<Unit>

    /**
     * Delete a colony
     */
    suspend fun deleteColony(colonyId: String): Result<Unit>

    /**
     * Update colony resources
     */
    suspend fun updateResources(colonyId: String, resources: ResourcePool): Result<Unit>

    /**
     * Sync colony with remote server
     */
    suspend fun syncColony(colonyId: String): Result<Colony>
}
