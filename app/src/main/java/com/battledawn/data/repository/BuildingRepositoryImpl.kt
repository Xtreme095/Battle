package com.battledawn.data.repository

import com.battledawn.data.remote.ApiService
import com.battledawn.data.remote.BuildingRequest
import com.battledawn.data.remote.toDomain
import com.battledawn.domain.model.Building
import com.battledawn.domain.model.BuildingTemplates
import com.battledawn.domain.model.BuildingType
import com.battledawn.domain.model.GridPosition
import com.battledawn.domain.repository.BuildingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of BuildingRepository
 * Uses both in-memory cache and API for building operations
 */
class BuildingRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BuildingRepository {

    private val buildings = mutableMapOf<String, Building>()

    override fun getColonyBuildings(colonyId: String): Flow<List<Building>> = flow {
        emit(buildings.values.filter { it.id.startsWith(colonyId) }.toList())
    }

    override fun getBuilding(buildingId: String): Flow<Building?> = flow {
        emit(buildings[buildingId])
    }

    override suspend fun startConstruction(
        colonyId: String,
        buildingType: BuildingType,
        position: GridPosition
    ): Result<Building> {
        return try {
            // Call API to start construction
            val request = BuildingRequest(
                colonyId = colonyId,
                buildingType = buildingType.name,
                positionX = position.x,
                positionY = position.y
            )

            val response = apiService.startConstruction(request)

            if (response.isSuccessful && response.body() != null) {
                val building = response.body()!!.toDomain()

                // Cache locally
                buildings[building.id] = building

                Timber.d("Construction started: ${building.type} at ${building.position}")
                Result.success(building)
            } else {
                // Fallback to local-only if API fails
                Timber.w("API call failed, using local-only construction")
                val template = BuildingTemplates.getTemplate(buildingType)
                val buildTime = (template.getBuildTimeForLevel(1) * 3_600_000).toLong()
                val currentTime = System.currentTimeMillis()

                val building = Building(
                    id = "${colonyId}_${UUID.randomUUID()}",
                    type = buildingType,
                    level = 1,
                    position = position,
                    constructionStartTime = currentTime,
                    constructionEndTime = currentTime + buildTime,
                    isUnderConstruction = true
                )

                buildings[building.id] = building
                Result.success(building)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error starting construction")
            Result.failure(e)
        }
    }

    override suspend fun upgradeBuilding(buildingId: String): Result<Building> {
        return try {
            // Call API to upgrade building
            val response = apiService.upgradeBuilding(buildingId)

            if (response.isSuccessful && response.body() != null) {
                val building = response.body()!!.toDomain()

                // Update cache
                buildings[building.id] = building

                Timber.d("Building upgraded: ${building.type} to level ${building.level}")
                Result.success(building)
            } else {
                // Fallback to local upgrade
                val building = buildings[buildingId]
                    ?: return Result.failure(Exception("Building not found"))

                val template = BuildingTemplates.getTemplate(building.type)
                val buildTime = (template.getBuildTimeForLevel(building.level + 1) * 3_600_000).toLong()
                val currentTime = System.currentTimeMillis()

                val upgraded = building.copy(
                    level = building.level + 1,
                    constructionStartTime = currentTime,
                    constructionEndTime = currentTime + buildTime,
                    isUnderConstruction = true
                )

                buildings[buildingId] = upgraded
                Result.success(upgraded)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error upgrading building")
            Result.failure(e)
        }
    }

    override suspend fun completeConstruction(buildingId: String): Result<Building> {
        return try {
            val building = buildings[buildingId]
                ?: return Result.failure(Exception("Building not found"))

            val completed = building.completeConstruction()
            buildings[buildingId] = completed
            Result.success(completed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelConstruction(buildingId: String): Result<Unit> {
        return try {
            buildings.remove(buildingId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getConstructionQueue(colonyId: String): Flow<List<Building>> = flow {
        emit(buildings.values.filter {
            it.id.startsWith(colonyId) && it.isUnderConstruction
        }.sortedBy { it.constructionEndTime }.toList())
    }
}
