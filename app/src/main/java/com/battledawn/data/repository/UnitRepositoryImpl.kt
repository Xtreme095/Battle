package com.battledawn.data.repository

import com.battledawn.domain.model.Army
import com.battledawn.domain.model.GridPosition
import com.battledawn.domain.model.Unit
import com.battledawn.domain.model.UnitTemplates
import com.battledawn.domain.model.UnitType
import com.battledawn.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of UnitRepository
 */
class UnitRepositoryImpl @Inject constructor() : UnitRepository {

    private val armies = mutableMapOf<String, Army>()
    private val units = mutableMapOf<String, Unit>()

    override fun getColonyArmies(colonyId: String): Flow<List<Army>> = flow {
        emit(armies.values.filter { it.id.startsWith(colonyId) }.toList())
    }

    override fun getArmy(armyId: String): Flow<Army?> = flow {
        emit(armies[armyId])
    }

    override suspend fun createArmy(army: Army): Result<Army> {
        return try {
            armies[army.id] = army
            Result.success(army)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun trainUnit(colonyId: String, unitType: UnitType, quantity: Int): Result<List<Unit>> {
        return try {
            val template = UnitTemplates.getTemplate(unitType)
            val trainingTime = (template.trainingTime * 3_600_000).toLong()
            val currentTime = System.currentTimeMillis()

            val newUnits = (1..quantity).map { i ->
                Unit(
                    id = "${colonyId}_unit_${UUID.randomUUID()}",
                    type = unitType,
                    currentHealth = template.health,
                    isTraining = true,
                    trainingStartTime = currentTime + (i - 1) * trainingTime,
                    trainingEndTime = currentTime + i * trainingTime
                )
            }

            newUnits.forEach { units[it.id] = it }
            Result.success(newUnits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeTraining(unitId: String): Result<Unit> {
        return try {
            val unit = units[unitId]
                ?: return Result.failure(Exception("Unit not found"))

            val completed = unit.completeTraining()
            units[unitId] = completed
            Result.success(completed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTrainingQueue(colonyId: String): Flow<List<Unit>> = flow {
        emit(units.values.filter {
            it.id.startsWith(colonyId) && it.isTraining
        }.sortedBy { it.trainingEndTime }.toList())
    }

    override suspend fun addUnitsToArmy(armyId: String, newUnits: List<Unit>): Result<Army> {
        return try {
            val army = armies[armyId]
                ?: return Result.failure(Exception("Army not found"))

            val updated = army.addUnits(newUnits)
            armies[armyId] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveArmy(armyId: String, destination: GridPosition): Result<Army> {
        return try {
            val army = armies[armyId]
                ?: return Result.failure(Exception("Army not found"))

            val distance = army.position.distanceTo(destination)
            val travelTime = (distance * 600_000).toLong() // 10 min per unit distance

            val moving = army.copy(
                destination = destination,
                isMoving = true,
                movementStartTime = System.currentTimeMillis(),
                movementEndTime = System.currentTimeMillis() + travelTime
            )

            armies[armyId] = moving
            Result.success(moving)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun splitArmy(armyId: String, unitIds: List<String>): Result<Pair<Army, Army>> {
        return try {
            val army = armies[armyId]
                ?: return Result.failure(Exception("Army not found"))

            val splitUnits = army.units.filter { it.id in unitIds }
            val remainingUnits = army.units.filter { it.id !in unitIds }

            val newArmy = army.copy(
                id = "${armyId}_split_${UUID.randomUUID()}",
                name = "${army.name} (Split)",
                units = splitUnits
            )

            val updatedArmy = army.copy(units = remainingUnits)

            armies[armyId] = updatedArmy
            armies[newArmy.id] = newArmy

            Result.success(Pair(updatedArmy, newArmy))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mergeArmies(armyId1: String, armyId2: String): Result<Army> {
        return try {
            val army1 = armies[armyId1]
                ?: return Result.failure(Exception("Army 1 not found"))
            val army2 = armies[armyId2]
                ?: return Result.failure(Exception("Army 2 not found"))

            val merged = army1.addUnits(army2.units)
            armies[armyId1] = merged
            armies.remove(armyId2)

            Result.success(merged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
