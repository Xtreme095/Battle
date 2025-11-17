package com.battledawn.data.remote

import com.battledawn.domain.model.*
import java.time.Instant

/**
 * Mappers to convert between DTOs and Domain Models
 */

// ==================== Colony Mappers ====================

fun ColonyDto.toDomain(): Colony {
    return Colony(
        id = id,
        playerId = userId,
        name = name,
        position = GridPosition(positionX, positionY),
        resources = ResourcePool(
            metal = Resource(
                type = ResourceType.METAL,
                amount = metalAmount,
                productionRate = metalProduction,
                capacity = metalCapacity
            ),
            oil = Resource(
                type = ResourceType.OIL,
                amount = oilAmount,
                productionRate = oilProduction,
                capacity = oilCapacity
            ),
            energy = Resource(
                type = ResourceType.ENERGY,
                amount = energyAmount,
                productionRate = energyProduction,
                capacity = energyCapacity
            ),
            food = Resource(
                type = ResourceType.FOOD,
                amount = foodAmount,
                productionRate = foodProduction,
                capacity = foodCapacity
            ),
            workers = Resource(
                type = ResourceType.WORKERS,
                amount = workersAmount,
                productionRate = 0.0,
                capacity = workersCapacity
            )
        ),
        buildings = buildings?.map { it.toDomain() } ?: emptyList(),
        armies = armies?.map { it.toDomain() } ?: emptyList()
    )
}

fun Colony.toDto(): ColonyDto {
    return ColonyDto(
        id = id,
        userId = playerId,
        name = name,
        positionX = position.x,
        positionY = position.y,
        metalAmount = resources.metal.amount,
        metalProduction = resources.metal.productionRate,
        metalCapacity = resources.metal.capacity,
        oilAmount = resources.oil.amount,
        oilProduction = resources.oil.productionRate,
        oilCapacity = resources.oil.capacity,
        energyAmount = resources.energy.amount,
        energyProduction = resources.energy.productionRate,
        energyCapacity = resources.energy.capacity,
        foodAmount = resources.food.amount,
        foodProduction = resources.food.productionRate,
        foodCapacity = resources.food.capacity,
        workersAmount = resources.workers.amount,
        workersCapacity = resources.workers.capacity,
        lastResourceUpdate = Instant.now().toString(),
        buildings = buildings.map { it.toDto() },
        armies = armies.map { it.toDto() }
    )
}

// ==================== Building Mappers ====================

fun BuildingDto.toDomain(): Building {
    return Building(
        id = id,
        colonyId = colonyId,
        type = BuildingType.valueOf(type),
        level = level,
        position = GridPosition(positionX, positionY),
        isConstructing = isConstructing,
        constructionEndTime = constructionEndTime?.let { Instant.parse(it) }
    )
}

fun Building.toDto(): BuildingDto {
    return BuildingDto(
        id = id,
        colonyId = colonyId,
        type = type.name,
        level = level,
        positionX = position.x,
        positionY = position.y,
        isConstructing = isConstructing,
        constructionEndTime = constructionEndTime?.toString()
    )
}

// ==================== Army Mappers ====================

fun ArmyDto.toDomain(): Army {
    return Army(
        id = id,
        colonyId = colonyId,
        name = name,
        units = units?.map { it.toDomain() } ?: emptyList(),
        stance = when (stance) {
            "DEFENSIVE" -> ArmyStance.DEFENSIVE
            "NEUTRAL" -> ArmyStance.NEUTRAL
            "AGGRESSIVE" -> ArmyStance.AGGRESSIVE
            else -> ArmyStance.NEUTRAL
        },
        position = if (positionX != null && positionY != null)
            GridPosition(positionX, positionY) else null,
        destination = if (destinationX != null && destinationY != null)
            GridPosition(destinationX, destinationY) else null,
        arrivalTime = arrivalTime?.let { Instant.parse(it) }
    )
}

fun Army.toDto(): ArmyDto {
    return ArmyDto(
        id = id,
        colonyId = colonyId,
        name = name,
        stance = stance.name,
        positionX = position?.x,
        positionY = position?.y,
        destinationX = destination?.x,
        destinationY = destination?.y,
        arrivalTime = arrivalTime?.toString(),
        units = units.map { it.toDto() }
    )
}

// ==================== Unit Mappers ====================

fun UnitDto.toDomain(): UnitStack {
    return UnitStack(
        id = id,
        type = UnitType.valueOf(type),
        quantity = quantity,
        experience = experience,
        currentHealth = health
    )
}

fun UnitStack.toDto(): UnitDto {
    return UnitDto(
        id = id,
        armyId = "", // Will be set by army
        type = type.name,
        quantity = quantity,
        experience = experience,
        health = currentHealth
    )
}

// ==================== Battle Mappers ====================

fun BattleDto.toDomain(): BattleResult {
    // Parse battleData JSON if available
    // For now, return basic battle result
    return BattleResult(
        victor = when (victor) {
            "ATTACKER" -> BattleVictor.ATTACKER
            "DEFENDER" -> BattleVictor.DEFENDER
            "DRAW" -> BattleVictor.DRAW
            else -> BattleVictor.DRAW
        },
        rounds = emptyList(), // Parse from battleData JSON
        attackerSurvivors = emptyList(),
        defenderSurvivors = emptyList(),
        plunderedResources = ResourcePool.empty(),
        experienceGained = 0,
        casualtiesAttacker = 0,
        casualtiesDefender = 0
    )
}

// ==================== User Mappers ====================

fun UserDto.toDomain(): Player {
    return Player(
        id = id,
        username = username,
        email = email,
        level = level,
        experience = experience,
        allianceId = allianceId,
        colonies = emptyList() // Colonies loaded separately
    )
}

fun Player.toDto(): UserDto {
    return UserDto(
        id = id,
        username = username,
        email = email,
        level = level,
        experience = experience,
        allianceId = allianceId,
        createdAt = Instant.now().toString(),
        lastActiveAt = Instant.now().toString()
    )
}

// ==================== Alliance Mappers ====================

fun AllianceDto.toDomain(): Alliance {
    return Alliance(
        id = id,
        name = name,
        tag = tag,
        description = description,
        leaderId = leaderId,
        members = emptyList(), // Members loaded separately
        totalScore = totalScore,
        wars = emptyList()
    )
}

fun Alliance.toDto(): AllianceDto {
    return AllianceDto(
        id = id,
        name = name,
        tag = tag,
        description = description,
        leaderId = leaderId,
        memberCount = members.size,
        totalScore = totalScore,
        createdAt = Instant.now().toString()
    )
}

// ==================== Research Mappers ====================

fun ResearchDto.toDomain(): Research {
    return Research(
        id = id,
        colonyId = colonyId,
        techType = TechnologyType.valueOf(techType),
        level = level,
        isResearching = isResearching,
        researchEndTime = researchEndTime?.let { Instant.parse(it) }
    )
}

fun Research.toDto(): ResearchDto {
    return ResearchDto(
        id = id,
        colonyId = colonyId,
        techType = techType.name,
        level = level,
        isResearching = isResearching,
        researchEndTime = researchEndTime?.toString()
    )
}

// ==================== Spy Mission Mappers ====================

fun SpyMissionDto.toDomain(): SpyMissionReport {
    return SpyMissionReport(
        missionType = SpyMissionType.valueOf(missionType),
        success = success,
        spiesLost = 0, // Parse from result
        enemySpiesKilled = 0,
        intelGathered = result ?: "No intel available",
        damageDealt = 0
    )
}
