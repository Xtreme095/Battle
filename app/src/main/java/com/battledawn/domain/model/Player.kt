package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Player account information
 */
@Parcelize
data class Player(
    val id: String,
    val username: String,
    val email: String,
    val level: Int = 1,
    val experience: Long = 0,
    val allianceId: String? = null,
    val colonies: List<String> = emptyList(),  // Colony IDs
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val wins: Int = 0,
    val losses: Int = 0,
    val achievements: List<String> = emptyList()
) : Parcelable {

    /**
     * Calculate win rate
     */
    fun getWinRate(): Double {
        val totalBattles = wins + losses
        return if (totalBattles > 0) {
            wins.toDouble() / totalBattles.toDouble()
        } else {
            0.0
        }
    }

    /**
     * Get level progress (0.0 to 1.0)
     */
    fun getLevelProgress(): Float {
        val expForNextLevel = getRequiredExpForLevel(level + 1)
        val expForCurrentLevel = getRequiredExpForLevel(level)
        val expInCurrentLevel = experience - expForCurrentLevel

        return (expInCurrentLevel.toFloat() / (expForNextLevel - expForCurrentLevel).toFloat())
            .coerceIn(0f, 1f)
    }

    private fun getRequiredExpForLevel(targetLevel: Int): Long {
        // Exponential XP curve: 1000 * (level ^ 2)
        return 1000L * targetLevel * targetLevel
    }

    /**
     * Add experience and level up if needed
     */
    fun addExperience(exp: Long): Player {
        val newExp = experience + exp
        val newLevel = calculateLevel(newExp)
        return copy(experience = newExp, level = newLevel)
    }

    private fun calculateLevel(exp: Long): Int {
        var level = 1
        while (getRequiredExpForLevel(level + 1) <= exp && level < 100) {
            level++
        }
        return level
    }
}

/**
 * Player's colony/base
 */
@Parcelize
data class Colony(
    val id: String,
    val playerId: String,
    val name: String,
    val position: GridPosition,
    val resources: ResourcePool = ResourcePool(),
    val buildings: List<Building> = emptyList(),
    val armies: List<Army> = emptyList(),
    val isMainColony: Boolean = false,
    val foundedAt: Long = System.currentTimeMillis(),
    val lastResourceUpdate: Long = System.currentTimeMillis(),
    val defenseRating: Int = 0
) : Parcelable {

    /**
     * Get building by ID
     */
    fun getBuilding(buildingId: String): Building? {
        return buildings.find { it.id == buildingId }
    }

    /**
     * Get buildings by type
     */
    fun getBuildingsByType(type: BuildingType): List<Building> {
        return buildings.filter { it.type == type && !it.isUnderConstruction }
    }

    /**
     * Check if building exists
     */
    fun hasBuilding(type: BuildingType, minLevel: Int = 1): Boolean {
        return buildings.any { it.type == type && it.level >= minLevel && !it.isUnderConstruction }
    }

    /**
     * Get total production rate for a resource type
     */
    fun getProductionRate(resourceType: ResourceType): Double {
        val buildingType = when (resourceType) {
            ResourceType.METAL -> BuildingType.METAL_MINE
            ResourceType.OIL -> BuildingType.OIL_REFINERY
            ResourceType.ENERGY -> BuildingType.ENERGY_PLANT
            ResourceType.FOOD -> BuildingType.FARM
            ResourceType.WORKERS -> return 0.0  // Workers come from food
        }

        val template = BuildingTemplates.getTemplate(buildingType)
        return buildings
            .filter { it.type == buildingType && !it.isUnderConstruction }
            .sumOf { template.getProductionForLevel(it.level) }
    }

    /**
     * Update resources based on time elapsed
     */
    fun updateResources(currentTime: Long = System.currentTimeMillis()): Colony {
        val hoursElapsed = (currentTime - lastResourceUpdate) / 3_600_000.0  // ms to hours

        val updatedPool = ResourcePool(
            metal = resources.metal.copy(
                amount = resources.metal.calculateAmount(hoursElapsed),
                productionRate = getProductionRate(ResourceType.METAL)
            ),
            oil = resources.oil.copy(
                amount = resources.oil.calculateAmount(hoursElapsed),
                productionRate = getProductionRate(ResourceType.OIL)
            ),
            energy = resources.energy.copy(
                amount = resources.energy.calculateAmount(hoursElapsed),
                productionRate = getProductionRate(ResourceType.ENERGY)
            ),
            food = resources.food.copy(
                amount = resources.food.calculateAmount(hoursElapsed),
                productionRate = getProductionRate(ResourceType.FOOD)
            ),
            workers = resources.workers  // Workers updated separately
        )

        return copy(
            resources = updatedPool,
            lastResourceUpdate = currentTime
        )
    }

    /**
     * Add a new building
     */
    fun addBuilding(building: Building): Colony {
        return copy(buildings = buildings + building)
    }

    /**
     * Update a building
     */
    fun updateBuilding(buildingId: String, updater: (Building) -> Building): Colony {
        return copy(
            buildings = buildings.map { building ->
                if (building.id == buildingId) updater(building) else building
            }
        )
    }

    /**
     * Remove a building
     */
    fun removeBuilding(buildingId: String): Colony {
        return copy(buildings = buildings.filter { it.id != buildingId })
    }

    /**
     * Get total army strength
     */
    fun getTotalMilitaryPower(): Int {
        return armies.sumOf { army ->
            army.getTotalStrength(UnitTemplates.ALL_TEMPLATES)
        }
    }

    /**
     * Calculate defense rating
     */
    fun calculateDefenseRating(): Int {
        val defenseTowers = getBuildingsByType(BuildingType.DEFENSE_TOWER)
        val towerDefense = defenseTowers.sumOf { it.level * 100 }
        val militaryPower = getTotalMilitaryPower()

        return towerDefense + (militaryPower / 2)
    }

    /**
     * Check if can afford a cost
     */
    fun canAfford(cost: ResourceCost): Boolean {
        return resources.hasEnoughResources(cost)
    }

    /**
     * Spend resources
     */
    fun spendResources(cost: ResourceCost): Colony {
        return copy(resources = resources.consumeResources(cost))
    }
}

/**
 * Alliance of players
 */
@Parcelize
data class Alliance(
    val id: String,
    val name: String,
    val tag: String,  // Short alliance tag/abbreviation
    val leaderId: String,
    val memberIds: List<String> = emptyList(),
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val wins: Int = 0,
    val totalMembers: Int = 0,
    val maxMembers: Int = 50,
    val isRecruiting: Boolean = true
) : Parcelable {

    /**
     * Check if alliance is full
     */
    fun isFull(): Boolean = memberIds.size >= maxMembers

    /**
     * Check if player is a member
     */
    fun isMember(playerId: String): Boolean = playerId in memberIds

    /**
     * Check if player is the leader
     */
    fun isLeader(playerId: String): Boolean = playerId == leaderId

    /**
     * Add member
     */
    fun addMember(playerId: String): Alliance {
        require(!isFull()) { "Alliance is full" }
        require(!isMember(playerId)) { "Player is already a member" }
        return copy(
            memberIds = memberIds + playerId,
            totalMembers = totalMembers + 1
        )
    }

    /**
     * Remove member
     */
    fun removeMember(playerId: String): Alliance {
        require(playerId != leaderId) { "Cannot remove the leader" }
        return copy(
            memberIds = memberIds.filter { it != playerId },
            totalMembers = totalMembers - 1
        )
    }
}

/**
 * Game world/server
 */
@Parcelize
data class GameWorld(
    val id: String,
    val name: String,
    val mapSize: Int = 100,  // 100x100 grid
    val startedAt: Long = System.currentTimeMillis(),
    val endsAt: Long? = null,  // Null for ongoing worlds
    val maxPlayers: Int = 1000,
    val currentPlayers: Int = 0,
    val status: WorldStatus = WorldStatus.ACTIVE
) : Parcelable

enum class WorldStatus {
    UPCOMING,    // Not started yet
    ACTIVE,      // Currently running
    ENDING_SOON, // Last days
    ENDED        // Completed
}
