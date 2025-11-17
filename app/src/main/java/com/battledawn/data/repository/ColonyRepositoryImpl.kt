package com.battledawn.data.repository

import com.battledawn.data.local.dao.ColonyDao
import com.battledawn.data.local.entity.toEntity
import com.battledawn.data.remote.ApiService
import com.battledawn.data.remote.toDomain
import com.battledawn.data.remote.toDto
import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.ResourcePool
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of ColonyRepository
 * Uses both local database (for caching/offline) and API (for syncing)
 */
class ColonyRepositoryImpl @Inject constructor(
    private val colonyDao: ColonyDao,
    private val apiService: ApiService
) : ColonyRepository {

    override fun getPlayerColonies(playerId: String): Flow<List<Colony>> {
        return colonyDao.getPlayerColonies(playerId).map { entities ->
            entities.map { entity ->
                entity.toDomain(emptyList(), emptyList())
            }
        }
    }

    override fun getColony(colonyId: String): Flow<Colony?> {
        return colonyDao.getColony(colonyId).map { entity ->
            entity?.toDomain(emptyList(), emptyList())
        }
    }

    override suspend fun createColony(colony: Colony): Result<Colony> {
        return try {
            colonyDao.insertColony(colony.toEntity())
            Result.success(colony)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateColony(colony: Colony): Result<Unit> {
        return try {
            colonyDao.updateColony(colony.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteColony(colonyId: String): Result<Unit> {
        return try {
            colonyDao.deleteColonyById(colonyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateResources(colonyId: String, resources: ResourcePool): Result<Unit> {
        // TODO: Implement resource update logic
        return Result.success(Unit)
    }

    override suspend fun syncColony(colonyId: String): Result<Colony> {
        return try {
            // Fetch from API
            val response = apiService.getColony(colonyId)

            if (response.isSuccessful && response.body() != null) {
                val colonyDto = response.body()!!
                val colony = colonyDto.toDomain()

                // Cache in local database
                colonyDao.insertColony(colony.toEntity())

                Timber.d("Colony synced successfully: $colonyId")
                Result.success(colony)
            } else {
                val error = "Failed to sync colony: ${response.code()} ${response.message()}"
                Timber.e(error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing colony: $colonyId")
            Result.failure(e)
        }
    }

    /**
     * Sync all player colonies from API
     */
    suspend fun syncAllColonies(): Result<List<Colony>> {
        return try {
            val response = apiService.getColonies()

            if (response.isSuccessful && response.body() != null) {
                val colonies = response.body()!!.map { it.toDomain() }

                // Cache all in local database
                colonies.forEach { colony ->
                    colonyDao.insertColony(colony.toEntity())
                }

                Timber.d("Synced ${colonies.size} colonies")
                Result.success(colonies)
            } else {
                val error = "Failed to sync colonies: ${response.code()}"
                Timber.e(error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing colonies")
            Result.failure(e)
        }
    }

    /**
     * Update colony resources via API
     */
    suspend fun updateColonyResourcesFromApi(colonyId: String): Result<ResourcePool> {
        return try {
            val response = apiService.updateColonyResources(colonyId)

            if (response.isSuccessful && response.body() != null) {
                val resourcesDto = response.body()!!
                val resources = ResourcePool(
                    metal = com.battledawn.domain.model.Resource(
                        type = com.battledawn.domain.model.ResourceType.METAL,
                        amount = resourcesDto.metalAmount
                    ),
                    oil = com.battledawn.domain.model.Resource(
                        type = com.battledawn.domain.model.ResourceType.OIL,
                        amount = resourcesDto.oilAmount
                    ),
                    energy = com.battledawn.domain.model.Resource(
                        type = com.battledawn.domain.model.ResourceType.ENERGY,
                        amount = resourcesDto.energyAmount
                    ),
                    food = com.battledawn.domain.model.Resource(
                        type = com.battledawn.domain.model.ResourceType.FOOD,
                        amount = resourcesDto.foodAmount
                    ),
                    workers = com.battledawn.domain.model.Resource(
                        type = com.battledawn.domain.model.ResourceType.WORKERS,
                        amount = resourcesDto.workersAmount
                    )
                )

                Timber.d("Colony resources updated: $colonyId")
                Result.success(resources)
            } else {
                Result.failure(Exception("Failed to update resources"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating resources")
            Result.failure(e)
        }
    }
}
