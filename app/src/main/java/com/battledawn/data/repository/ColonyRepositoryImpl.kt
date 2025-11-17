package com.battledawn.data.repository

import com.battledawn.data.local.dao.ColonyDao
import com.battledawn.data.local.entity.toEntity
import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.ResourcePool
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ColonyRepository
 */
class ColonyRepositoryImpl @Inject constructor(
    private val colonyDao: ColonyDao
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
        // TODO: Implement Firebase sync
        return Result.failure(NotImplementedError("Firebase sync not yet implemented"))
    }
}
