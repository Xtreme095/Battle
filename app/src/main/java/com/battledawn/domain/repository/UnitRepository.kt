package com.battledawn.domain.repository

import com.battledawn.domain.model.Army
import com.battledawn.domain.model.Unit
import com.battledawn.domain.model.UnitType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for unit and army operations
 */
interface UnitRepository {

    /**
     * Get all armies for a colony
     */
    fun getColonyArmies(colonyId: String): Flow<List<Army>>

    /**
     * Get a specific army
     */
    fun getArmy(armyId: String): Flow<Army?>

    /**
     * Create a new army
     */
    suspend fun createArmy(army: Army): Result<Army>

    /**
     * Start unit training
     */
    suspend fun trainUnit(colonyId: String, unitType: UnitType, quantity: Int): Result<List<Unit>>

    /**
     * Complete unit training
     */
    suspend fun completeTraining(unitId: String): Result<Unit>

    /**
     * Get training queue for a colony
     */
    fun getTrainingQueue(colonyId: String): Flow<List<Unit>>

    /**
     * Add units to an army
     */
    suspend fun addUnitsToArmy(armyId: String, units: List<Unit>): Result<Army>

    /**
     * Move army to a position
     */
    suspend fun moveArmy(
        armyId: String,
        destination: com.battledawn.domain.model.GridPosition
    ): Result<Army>

    /**
     * Split army
     */
    suspend fun splitArmy(armyId: String, unitIds: List<String>): Result<Pair<Army, Army>>

    /**
     * Merge armies
     */
    suspend fun mergeArmies(armyId1: String, armyId2: String): Result<Army>
}
