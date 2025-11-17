package com.battledawn.domain.usecase.combat

import com.battledawn.domain.model.Battle
import com.battledawn.domain.repository.CombatRepository
import com.battledawn.domain.repository.UnitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to initiate an attack on another colony
 */
class InitiateAttackUseCase @Inject constructor(
    private val combatRepository: CombatRepository,
    private val unitRepository: UnitRepository
) {
    suspend operator fun invoke(
        attackerArmyId: String,
        defenderColonyId: String
    ): Result<Battle> {
        return try {
            // Get attacker army
            val attackerArmy = unitRepository.getArmy(attackerArmyId).first()
                ?: return Result.failure(Exception("Army not found"))

            // Validate army has units
            if (attackerArmy.units.isEmpty()) {
                return Result.failure(Exception("Army has no units"))
            }

            // Validate army is not moving
            if (attackerArmy.isMoving) {
                return Result.failure(Exception("Army is currently moving"))
            }

            // Initiate attack
            combatRepository.initiateAttack(attackerArmyId, defenderColonyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
