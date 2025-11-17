package com.battledawn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.GridPosition
import com.battledawn.domain.model.ResourcePool

/**
 * Room entity for Colony
 */
@Entity(tableName = "colonies")
data class ColonyEntity(
    @PrimaryKey val id: String,
    val playerId: String,
    val name: String,
    val positionX: Int,
    val positionY: Int,
    val metalAmount: Long,
    val metalProductionRate: Double,
    val oilAmount: Long,
    val oilProductionRate: Double,
    val energyAmount: Long,
    val energyProductionRate: Double,
    val foodAmount: Long,
    val foodProductionRate: Double,
    val workersAmount: Long,
    val isMainColony: Boolean,
    val foundedAt: Long,
    val lastResourceUpdate: Long,
    val defenseRating: Int
)

/**
 * Convert entity to domain model
 */
fun ColonyEntity.toDomain(
    buildings: List<com.battledawn.domain.model.Building>,
    armies: List<com.battledawn.domain.model.Army>
): Colony {
    return Colony(
        id = id,
        playerId = playerId,
        name = name,
        position = GridPosition(positionX, positionY),
        resources = ResourcePool(
            metal = com.battledawn.domain.model.Resource(
                type = com.battledawn.domain.model.ResourceType.METAL,
                amount = metalAmount,
                productionRate = metalProductionRate
            ),
            oil = com.battledawn.domain.model.Resource(
                type = com.battledawn.domain.model.ResourceType.OIL,
                amount = oilAmount,
                productionRate = oilProductionRate
            ),
            energy = com.battledawn.domain.model.Resource(
                type = com.battledawn.domain.model.ResourceType.ENERGY,
                amount = energyAmount,
                productionRate = energyProductionRate
            ),
            food = com.battledawn.domain.model.Resource(
                type = com.battledawn.domain.model.ResourceType.FOOD,
                amount = foodAmount,
                productionRate = foodProductionRate
            ),
            workers = com.battledawn.domain.model.Resource(
                type = com.battledawn.domain.model.ResourceType.WORKERS,
                amount = workersAmount
            )
        ),
        buildings = buildings,
        armies = armies,
        isMainColony = isMainColony,
        foundedAt = foundedAt,
        lastResourceUpdate = lastResourceUpdate,
        defenseRating = defenseRating
    )
}

/**
 * Convert domain model to entity
 */
fun Colony.toEntity(): ColonyEntity {
    return ColonyEntity(
        id = id,
        playerId = playerId,
        name = name,
        positionX = position.x,
        positionY = position.y,
        metalAmount = resources.metal.amount,
        metalProductionRate = resources.metal.productionRate,
        oilAmount = resources.oil.amount,
        oilProductionRate = resources.oil.productionRate,
        energyAmount = resources.energy.amount,
        energyProductionRate = resources.energy.productionRate,
        foodAmount = resources.food.amount,
        foodProductionRate = resources.food.productionRate,
        workersAmount = resources.workers.amount,
        isMainColony = isMainColony,
        foundedAt = foundedAt,
        lastResourceUpdate = lastResourceUpdate,
        defenseRating = defenseRating
    )
}
