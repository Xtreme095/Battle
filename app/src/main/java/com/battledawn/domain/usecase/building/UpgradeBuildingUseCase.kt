package com.battledawn.domain.usecase.building

import com.battledawn.domain.model.Building
import com.battledawn.domain.model.BuildingTemplates
import com.battledawn.domain.repository.BuildingRepository
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to upgrade an existing building
 */
class UpgradeBuildingUseCase @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val colonyRepository: ColonyRepository
) {
    suspend operator fun invoke(buildingId: String): Result<Building> {
        return try {
            // Get building
            val building = buildingRepository.getBuilding(buildingId).first()
                ?: return Result.failure(Exception("Building not found"))

            // Get template
            val template = BuildingTemplates.getTemplate(building.type)

            // Check if can upgrade
            if (!building.canUpgrade(template)) {
                return Result.failure(Exception("Cannot upgrade building"))
            }

            // Get colony
            val colony = colonyRepository.getColony(building.id).first()
                ?: return Result.failure(Exception("Colony not found"))

            // Check resources
            val cost = template.getCostForLevel(building.level + 1)
            if (!colony.canAfford(cost)) {
                return Result.failure(Exception("Insufficient resources"))
            }

            // Deduct resources
            val updatedColony = colony.spendResources(cost)
            colonyRepository.updateColony(updatedColony)

            // Start upgrade
            buildingRepository.upgradeBuilding(buildingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
