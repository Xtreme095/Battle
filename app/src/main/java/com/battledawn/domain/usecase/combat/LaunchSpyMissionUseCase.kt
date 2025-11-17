package com.battledawn.domain.usecase.combat

import com.battledawn.domain.model.SpyMission
import com.battledawn.domain.model.SpyOperation
import com.battledawn.domain.model.UnitType
import com.battledawn.domain.repository.CombatRepository
import com.battledawn.domain.repository.ColonyRepository
import com.battledawn.domain.repository.UnitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to launch a spy mission
 */
class LaunchSpyMissionUseCase @Inject constructor(
    private val combatRepository: CombatRepository,
    private val colonyRepository: ColonyRepository,
    private val unitRepository: UnitRepository
) {
    suspend operator fun invoke(
        operativeColonyId: String,
        targetColonyId: String,
        operation: SpyOperation
    ): Result<SpyMission> {
        return try {
            // Get operative colony
            val colony = colonyRepository.getColony(operativeColonyId).first()
                ?: return Result.failure(Exception("Colony not found"))

            // Find available spy
            val armies = unitRepository.getColonyArmies(operativeColonyId).first()
            val availableSpy = armies
                .flatMap { it.units }
                .firstOrNull { it.type == UnitType.SPY && !it.isTraining }
                ?: return Result.failure(Exception("No available spy"))

            // Launch mission
            combatRepository.launchSpyMission(
                spyId = availableSpy.id,
                operativeColonyId = operativeColonyId,
                targetColonyId = targetColonyId,
                operation = operation
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
