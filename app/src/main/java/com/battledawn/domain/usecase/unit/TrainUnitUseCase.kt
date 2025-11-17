package com.battledawn.domain.usecase.unit

import com.battledawn.domain.model.Unit
import com.battledawn.domain.model.UnitTemplates
import com.battledawn.domain.model.UnitType
import com.battledawn.domain.repository.ColonyRepository
import com.battledawn.domain.repository.UnitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to train units
 */
class TrainUnitUseCase @Inject constructor(
    private val unitRepository: UnitRepository,
    private val colonyRepository: ColonyRepository
) {
    suspend operator fun invoke(
        colonyId: String,
        unitType: UnitType,
        quantity: Int
    ): Result<List<Unit>> {
        return try {
            require(quantity > 0) { "Quantity must be positive" }

            // Get colony
            val colony = colonyRepository.getColony(colonyId).first()
                ?: return Result.failure(Exception("Colony not found"))

            // Get unit template
            val template = UnitTemplates.getTemplate(unitType)

            // Check unlock requirements
            for ((requiredBuilding, requiredLevel) in template.unlockRequirements) {
                if (!colony.hasBuilding(requiredBuilding, requiredLevel)) {
                    return Result.failure(
                        Exception("Requires ${requiredBuilding.name} level $requiredLevel")
                    )
                }
            }

            // Calculate total cost
            val totalCost = template.cost * quantity.toDouble()

            // Check resources
            if (!colony.canAfford(totalCost)) {
                return Result.failure(Exception("Insufficient resources"))
            }

            // Deduct resources
            val updatedColony = colony.spendResources(totalCost)
            colonyRepository.updateColony(updatedColony)

            // Start training
            unitRepository.trainUnit(colonyId, unitType, quantity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
