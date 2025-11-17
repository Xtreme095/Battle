package com.battledawn.domain.repository

import com.battledawn.domain.model.Battle
import com.battledawn.domain.model.BattleResult
import com.battledawn.domain.model.SpyMission
import com.battledawn.domain.model.SpyOperation
import kotlinx.coroutines.flow.Flow

/**
 * Repository for combat operations
 */
interface CombatRepository {

    /**
     * Initiate an attack
     */
    suspend fun initiateAttack(
        attackerArmyId: String,
        defenderColonyId: String
    ): Result<Battle>

    /**
     * Get ongoing battle
     */
    fun getBattle(battleId: String): Flow<Battle?>

    /**
     * Get all battles for a player
     */
    fun getPlayerBattles(playerId: String): Flow<List<Battle>>

    /**
     * Simulate battle (local calculation)
     */
    suspend fun simulateBattle(battleId: String): Result<BattleResult>

    /**
     * Launch nuclear missile
     */
    suspend fun launchNuke(
        fromColonyId: String,
        targetColonyId: String
    ): Result<Unit>

    /**
     * Fire ion cannon
     */
    suspend fun fireIonCannon(
        fromColonyId: String,
        targetColonyId: String
    ): Result<Unit>

    /**
     * Launch spy mission
     */
    suspend fun launchSpyMission(
        spyId: String,
        operativeColonyId: String,
        targetColonyId: String,
        operation: SpyOperation
    ): Result<SpyMission>

    /**
     * Get active spy missions
     */
    fun getActiveMissions(colonyId: String): Flow<List<SpyMission>>

    /**
     * Complete spy mission
     */
    suspend fun completeSpyMission(missionId: String): Result<SpyMission>
}
