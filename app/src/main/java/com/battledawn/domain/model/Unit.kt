package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Types of military units
 */
enum class UnitType {
    // Infantry
    LIGHT_INFANTRY,
    HEAVY_INFANTRY,
    ELITE_INFANTRY,

    // Vehicles
    LIGHT_VEHICLE,
    ARMORED_VEHICLE,
    ARTILLERY,

    // Tanks
    LIGHT_TANK,
    MEDIUM_TANK,
    HEAVY_TANK,

    // Special Units
    SPY,
    NUCLEAR_MISSILE,
    ION_STRIKE
}

/**
 * Unit category
 */
enum class UnitCategory {
    INFANTRY,      // Fast, cheap, light armor
    VEHICLE,       // Medium speed, balanced
    TANK,          // Slow, expensive, heavy armor
    SPECIAL        // Unique units
}

/**
 * Unit template/blueprint
 */
data class UnitTemplate(
    val type: UnitType,
    val name: String,
    val description: String,
    val category: UnitCategory,
    val cost: ResourceCost,
    val trainingTime: Double,  // in hours
    val attack: Int,
    val defense: Int,
    val health: Int,
    val speed: Int,  // Movement speed
    val upkeep: ResourceCost,  // Maintenance cost per hour
    val unlockRequirements: Map<BuildingType, Int> = emptyMap(),
    val researchRequirements: List<String> = emptyList(),
    val isSpecialWeapon: Boolean = false
) {
    /**
     * Combat effectiveness against another unit type
     */
    fun getEffectivenessMultiplier(target: UnitCategory): Double {
        return when (category) {
            UnitCategory.INFANTRY -> when (target) {
                UnitCategory.INFANTRY -> 1.0
                UnitCategory.VEHICLE -> 0.7
                UnitCategory.TANK -> 0.5
                UnitCategory.SPECIAL -> 1.0
            }
            UnitCategory.VEHICLE -> when (target) {
                UnitCategory.INFANTRY -> 1.3
                UnitCategory.VEHICLE -> 1.0
                UnitCategory.TANK -> 0.8
                UnitCategory.SPECIAL -> 1.0
            }
            UnitCategory.TANK -> when (target) {
                UnitCategory.INFANTRY -> 1.5
                UnitCategory.VEHICLE -> 1.2
                UnitCategory.TANK -> 1.0
                UnitCategory.SPECIAL -> 1.0
            }
            UnitCategory.SPECIAL -> 1.0
        }
    }
}

/**
 * Actual unit instance
 */
@Parcelize
data class Unit(
    val id: String,
    val type: UnitType,
    val currentHealth: Int,
    val experience: Int = 0,
    val level: Int = 1,
    val isTraining: Boolean = false,
    val trainingStartTime: Long? = null,
    val trainingEndTime: Long? = null
) : Parcelable {

    /**
     * Check if unit training is complete
     */
    fun isTrainingComplete(currentTime: Long): Boolean {
        return if (isTraining && trainingEndTime != null) {
            currentTime >= trainingEndTime
        } else {
            !isTraining
        }
    }

    /**
     * Get training progress (0.0 to 1.0)
     */
    fun getTrainingProgress(currentTime: Long): Float {
        if (!isTraining || trainingStartTime == null || trainingEndTime == null) {
            return 1.0f
        }

        val totalTime = trainingEndTime - trainingStartTime
        val elapsedTime = currentTime - trainingStartTime

        return (elapsedTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Complete training
     */
    fun completeTraining(): Unit {
        return copy(
            isTraining = false,
            trainingStartTime = null,
            trainingEndTime = null
        )
    }

    /**
     * Check if unit is alive
     */
    fun isAlive(): Boolean = currentHealth > 0

    /**
     * Take damage
     */
    fun takeDamage(damage: Int): Unit {
        return copy(currentHealth = (currentHealth - damage).coerceAtLeast(0))
    }

    /**
     * Heal unit
     */
    fun heal(amount: Int, maxHealth: Int): Unit {
        return copy(currentHealth = (currentHealth + amount).coerceAtMost(maxHealth))
    }

    /**
     * Gain experience
     */
    fun gainExperience(exp: Int): Unit {
        val newExp = experience + exp
        val newLevel = calculateLevel(newExp)
        return copy(experience = newExp, level = newLevel)
    }

    private fun calculateLevel(exp: Int): Int {
        // Simple leveling: level = sqrt(exp / 100) + 1
        return (kotlin.math.sqrt(exp / 100.0) + 1).toInt().coerceAtMost(10)
    }
}

/**
 * Army - collection of units
 */
@Parcelize
data class Army(
    val id: String,
    val name: String,
    val units: List<Unit> = emptyList(),
    val position: GridPosition,
    val destination: GridPosition? = null,
    val isMoving: Boolean = false,
    val movementStartTime: Long? = null,
    val movementEndTime: Long? = null
) : Parcelable {

    /**
     * Get total unit count
     */
    fun getTotalUnits(): Int = units.size

    /**
     * Get unit count by type
     */
    fun getUnitCount(type: UnitType): Int = units.count { it.type == type }

    /**
     * Get total army strength
     */
    fun getTotalStrength(templates: Map<UnitType, UnitTemplate>): Int {
        return units.sumOf { unit ->
            templates[unit.type]?.attack ?: 0
        }
    }

    /**
     * Check if movement is complete
     */
    fun isMovementComplete(currentTime: Long): Boolean {
        return if (isMoving && movementEndTime != null) {
            currentTime >= movementEndTime
        } else {
            !isMoving
        }
    }

    /**
     * Add units to army
     */
    fun addUnits(newUnits: List<Unit>): Army {
        return copy(units = units + newUnits)
    }

    /**
     * Remove destroyed units
     */
    fun removeDeadUnits(): Army {
        return copy(units = units.filter { it.isAlive() })
    }
}

/**
 * Unit templates database
 */
object UnitTemplates {

    // Infantry Units
    val LIGHT_INFANTRY = UnitTemplate(
        type = UnitType.LIGHT_INFANTRY,
        name = "Light Infantry",
        description = "Basic foot soldiers, cheap and fast",
        category = UnitCategory.INFANTRY,
        cost = ResourceCost(metal = 50, food = 10, workers = 1),
        trainingTime = 0.5,
        attack = 10,
        defense = 5,
        health = 50,
        speed = 10,
        upkeep = ResourceCost(food = 1)
    )

    val HEAVY_INFANTRY = UnitTemplate(
        type = UnitType.HEAVY_INFANTRY,
        name = "Heavy Infantry",
        description = "Well-armored soldiers with better firepower",
        category = UnitCategory.INFANTRY,
        cost = ResourceCost(metal = 100, food = 20, workers = 2),
        trainingTime = 1.0,
        attack = 20,
        defense = 15,
        health = 100,
        speed = 8,
        upkeep = ResourceCost(food = 2),
        unlockRequirements = mapOf(BuildingType.UNIT_PRODUCTION_FACILITY to 2)
    )

    val ELITE_INFANTRY = UnitTemplate(
        type = UnitType.ELITE_INFANTRY,
        name = "Elite Infantry",
        description = "Special forces with advanced training",
        category = UnitCategory.INFANTRY,
        cost = ResourceCost(metal = 200, oil = 50, food = 30, workers = 3),
        trainingTime = 2.0,
        attack = 35,
        defense = 25,
        health = 150,
        speed = 9,
        upkeep = ResourceCost(food = 3, oil = 1),
        unlockRequirements = mapOf(BuildingType.UNIT_PRODUCTION_FACILITY to 4)
    )

    // Vehicle Units
    val LIGHT_VEHICLE = UnitTemplate(
        type = UnitType.LIGHT_VEHICLE,
        name = "Light Vehicle",
        description = "Fast reconnaissance and transport vehicle",
        category = UnitCategory.VEHICLE,
        cost = ResourceCost(metal = 150, oil = 100, workers = 2),
        trainingTime = 1.5,
        attack = 25,
        defense = 20,
        health = 120,
        speed = 15,
        upkeep = ResourceCost(oil = 2),
        unlockRequirements = mapOf(BuildingType.OIL_REFINERY to 2)
    )

    val ARMORED_VEHICLE = UnitTemplate(
        type = UnitType.ARMORED_VEHICLE,
        name = "Armored Vehicle",
        description = "Heavy firepower with decent mobility",
        category = UnitCategory.VEHICLE,
        cost = ResourceCost(metal = 300, oil = 200, workers = 4),
        trainingTime = 2.5,
        attack = 40,
        defense = 30,
        health = 200,
        speed = 12,
        upkeep = ResourceCost(oil = 3),
        unlockRequirements = mapOf(
            BuildingType.UNIT_PRODUCTION_FACILITY to 3,
            BuildingType.OIL_REFINERY to 3
        )
    )

    val ARTILLERY = UnitTemplate(
        type = UnitType.ARTILLERY,
        name = "Artillery",
        description = "Long-range bombardment vehicle",
        category = UnitCategory.VEHICLE,
        cost = ResourceCost(metal = 400, oil = 250, workers = 5),
        trainingTime = 3.0,
        attack = 60,
        defense = 15,
        health = 150,
        speed = 8,
        upkeep = ResourceCost(oil = 4),
        unlockRequirements = mapOf(BuildingType.UNIT_PRODUCTION_FACILITY to 4)
    )

    // Tank Units
    val LIGHT_TANK = UnitTemplate(
        type = UnitType.LIGHT_TANK,
        name = "Light Tank",
        description = "Basic armored unit with good firepower",
        category = UnitCategory.TANK,
        cost = ResourceCost(metal = 500, oil = 300, workers = 6),
        trainingTime = 3.5,
        attack = 50,
        defense = 40,
        health = 300,
        speed = 6,
        upkeep = ResourceCost(oil = 5),
        unlockRequirements = mapOf(
            BuildingType.UNIT_PRODUCTION_FACILITY to 3,
            BuildingType.OIL_REFINERY to 4
        )
    )

    val MEDIUM_TANK = UnitTemplate(
        type = UnitType.MEDIUM_TANK,
        name = "Medium Tank",
        description = "Well-balanced heavy armor unit",
        category = UnitCategory.TANK,
        cost = ResourceCost(metal = 800, oil = 500, workers = 8),
        trainingTime = 5.0,
        attack = 70,
        defense = 60,
        health = 500,
        speed = 5,
        upkeep = ResourceCost(oil = 7),
        unlockRequirements = mapOf(
            BuildingType.UNIT_PRODUCTION_FACILITY to 4,
            BuildingType.OIL_REFINERY to 5
        )
    )

    val HEAVY_TANK = UnitTemplate(
        type = UnitType.HEAVY_TANK,
        name = "Heavy Tank",
        description = "Ultimate ground warfare machine",
        category = UnitCategory.TANK,
        cost = ResourceCost(metal = 1500, oil = 1000, workers = 12),
        trainingTime = 8.0,
        attack = 100,
        defense = 90,
        health = 800,
        speed = 4,
        upkeep = ResourceCost(oil = 10),
        unlockRequirements = mapOf(
            BuildingType.UNIT_PRODUCTION_FACILITY to 5,
            BuildingType.OIL_REFINERY to 5
        )
    )

    // Special Units
    val SPY = UnitTemplate(
        type = UnitType.SPY,
        name = "Spy",
        description = "Covert operative for espionage missions",
        category = UnitCategory.SPECIAL,
        cost = ResourceCost(metal = 300, energy = 200, workers = 3),
        trainingTime = 4.0,
        attack = 0,
        defense = 0,
        health = 10,
        speed = 20,
        upkeep = ResourceCost(energy = 5),
        unlockRequirements = mapOf(BuildingType.SPY_AGENCY to 1),
        isSpecialWeapon = true
    )

    val NUCLEAR_MISSILE = UnitTemplate(
        type = UnitType.NUCLEAR_MISSILE,
        name = "Nuclear Missile",
        description = "Weapon of mass destruction",
        category = UnitCategory.SPECIAL,
        cost = ResourceCost(metal = 5000, oil = 3000, energy = 2000, workers = 20),
        trainingTime = 12.0,
        attack = 1000,
        defense = 0,
        health = 1,
        speed = 50,
        upkeep = ResourceCost(),
        unlockRequirements = mapOf(
            BuildingType.NUCLEAR_SILO to 1,
            BuildingType.UNIT_PRODUCTION_FACILITY to 5
        ),
        isSpecialWeapon = true
    )

    val ION_STRIKE = UnitTemplate(
        type = UnitType.ION_STRIKE,
        name = "Ion Strike",
        description = "Precision energy weapon from orbit",
        category = UnitCategory.SPECIAL,
        cost = ResourceCost(metal = 4000, energy = 5000, workers = 15),
        trainingTime = 10.0,
        attack = 800,
        defense = 0,
        health = 1,
        speed = 100,
        upkeep = ResourceCost(),
        unlockRequirements = mapOf(BuildingType.ION_CANNON to 1),
        isSpecialWeapon = true
    )

    // Map of all templates
    val ALL_TEMPLATES = mapOf(
        UnitType.LIGHT_INFANTRY to LIGHT_INFANTRY,
        UnitType.HEAVY_INFANTRY to HEAVY_INFANTRY,
        UnitType.ELITE_INFANTRY to ELITE_INFANTRY,
        UnitType.LIGHT_VEHICLE to LIGHT_VEHICLE,
        UnitType.ARMORED_VEHICLE to ARMORED_VEHICLE,
        UnitType.ARTILLERY to ARTILLERY,
        UnitType.LIGHT_TANK to LIGHT_TANK,
        UnitType.MEDIUM_TANK to MEDIUM_TANK,
        UnitType.HEAVY_TANK to HEAVY_TANK,
        UnitType.SPY to SPY,
        UnitType.NUCLEAR_MISSILE to NUCLEAR_MISSILE,
        UnitType.ION_STRIKE to ION_STRIKE
    )

    fun getTemplate(type: UnitType): UnitTemplate {
        return ALL_TEMPLATES[type] ?: error("Unknown unit type: $type")
    }
}
