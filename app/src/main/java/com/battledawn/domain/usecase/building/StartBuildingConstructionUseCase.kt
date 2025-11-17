package com.battledawn.domain.usecase.building

import com.battledawn.domain.model.Building
import com.battledawn.domain.model.BuildingTemplates
import com.battledawn.domain.model.BuildingType
import com.battledawn.domain.model.GridPosition
import com.battledawn.domain.repository.BuildingRepository
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to start building construction
 */
class StartBuildingConstructionUseCase @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val colonyRepository: ColonyRepository
) {
    suspend operator fun invoke(
        colonyId: String,
        buildingType: BuildingType,
        position: GridPosition
    ): Result<Building> {
        return try {
            // Get colony
            val colony = colonyRepository.getColony(colonyId).first()
                ?: return Result.failure(Exception("Colony not found"))

            // Get building template
            val template = BuildingTemplates.getTemplate(buildingType)

            // Check requirements
            for ((requiredType, requiredLevel) in template.unlockRequirements) {
                if (!colony.hasBuilding(requiredType, requiredLevel)) {
                    return Result.failure(
                        Exception("Requires ${requiredType.name} level $requiredLevel")
                    )
                }
            }

            // Check resources
            val cost = template.getCostForLevel(1)
            if (!colony.canAfford(cost)) {
                return Result.failure(Exception("Insufficient resources"))
            }

            // Deduct resources
            val updatedColony = colony.spendResources(cost)
            colonyRepository.updateColony(updatedColony)

            // Start construction
            buildingRepository.startConstruction(colonyId, buildingType, position)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
