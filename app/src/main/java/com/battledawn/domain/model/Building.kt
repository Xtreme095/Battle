package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Types of buildings in the game
 */
enum class BuildingType {
    // Production Buildings
    METAL_MINE,
    OIL_REFINERY,
    ENERGY_PLANT,
    FARM,

    // Strategic Buildings
    UNIT_PRODUCTION_FACILITY,
    RESEARCH_CENTER,
    DEFENSE_TOWER,
    SPY_AGENCY,
    NUCLEAR_SILO,
    ION_CANNON,
    SCANNER_ARRAY,

    // Special Buildings
    COMMAND_CENTER,
    WAREHOUSE,
    HEADQUARTERS
}

/**
 * Category of building
 */
enum class BuildingCategory {
    PRODUCTION,    // Generates resources
    MILITARY,      // Military units and operations
    DEFENSE,       // Defensive structures
    SPECIAL        // Unique buildings
}

/**
 * Building template/blueprint
 */
data class BuildingTemplate(
    val type: BuildingType,
    val name: String,
    val description: String,
    val category: BuildingCategory,
    val maxLevel: Int = 10,
    val baseCost: ResourceCost,
    val baseProductionBonus: Double = 0.0,  // For production buildings
    val unlockRequirements: Map<BuildingType, Int> = emptyMap(),
    val researchRequirements: List<String> = emptyList()
) {
    /**
     * Calculate cost for a specific level
     */
    fun getCostForLevel(level: Int): ResourceCost {
        require(level in 1..maxLevel) { "Level must be between 1 and $maxLevel" }
        // Cost increases exponentially: baseCost * (1.5 ^ (level - 1))
        val multiplier = Math.pow(1.5, (level - 1).toDouble())
        return baseCost * multiplier
    }

    /**
     * Calculate production rate for a specific level
     */
    fun getProductionForLevel(level: Int): Double {
        require(level in 1..maxLevel) { "Level must be between 1 and $maxLevel" }
        // Production increases: baseProduction * level * 1.2
        return baseProductionBonus * level * 1.2
    }

    /**
     * Get build time in hours for a specific level
     */
    fun getBuildTimeForLevel(level: Int): Double {
        require(level in 1..maxLevel) { "Level must be between 1 and $maxLevel" }
        // Build time: 1 hour * level * 1.5
        return 1.0 * level * 1.5
    }
}

/**
 * Actual building instance in a colony
 */
@Parcelize
data class Building(
    val id: String,
    val type: BuildingType,
    val level: Int = 1,
    val position: GridPosition,
    val constructionStartTime: Long? = null,  // Timestamp in millis
    val constructionEndTime: Long? = null,    // When construction/upgrade completes
    val isUnderConstruction: Boolean = false
) : Parcelable {

    /**
     * Check if building construction is complete
     */
    fun isConstructionComplete(currentTime: Long): Boolean {
        return if (isUnderConstruction && constructionEndTime != null) {
            currentTime >= constructionEndTime
        } else {
            !isUnderConstruction
        }
    }

    /**
     * Get construction progress (0.0 to 1.0)
     */
    fun getConstructionProgress(currentTime: Long): Float {
        if (!isUnderConstruction || constructionStartTime == null || constructionEndTime == null) {
            return 1.0f
        }

        val totalTime = constructionEndTime - constructionStartTime
        val elapsedTime = currentTime - constructionStartTime

        return (elapsedTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Complete construction
     */
    fun completeConstruction(): Building {
        return copy(
            isUnderConstruction = false,
            constructionStartTime = null,
            constructionEndTime = null
        )
    }

    /**
     * Can this building be upgraded?
     */
    fun canUpgrade(template: BuildingTemplate): Boolean {
        return level < template.maxLevel && !isUnderConstruction
    }
}

/**
 * Position on the game grid
 */
@Parcelize
data class GridPosition(
    val x: Int,
    val y: Int
) : Parcelable {

    /**
     * Calculate distance to another position
     */
    fun distanceTo(other: GridPosition): Double {
        val dx = (x - other.x).toDouble()
        val dy = (y - other.y).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * Check if position is adjacent (including diagonals)
     */
    fun isAdjacentTo(other: GridPosition): Boolean {
        val dx = kotlin.math.abs(x - other.x)
        val dy = kotlin.math.abs(y - other.y)
        return dx <= 1 && dy <= 1 && (dx + dy) > 0
    }
}

/**
 * Companion object with building templates
 */
object BuildingTemplates {

    val METAL_MINE = BuildingTemplate(
        type = BuildingType.METAL_MINE,
        name = "Metal Mine",
        description = "Extracts metal ore for construction",
        category = BuildingCategory.PRODUCTION,
        baseCost = ResourceCost(metal = 100, workers = 5),
        baseProductionBonus = 50.0
    )

    val OIL_REFINERY = BuildingTemplate(
        type = BuildingType.OIL_REFINERY,
        name = "Oil Refinery",
        description = "Refines oil for advanced units and vehicles",
        category = BuildingCategory.PRODUCTION,
        baseCost = ResourceCost(metal = 150, workers = 5),
        baseProductionBonus = 40.0
    )

    val ENERGY_PLANT = BuildingTemplate(
        type = BuildingType.ENERGY_PLANT,
        name = "Energy Plant",
        description = "Generates energy for special operations",
        category = BuildingCategory.PRODUCTION,
        baseCost = ResourceCost(metal = 200, workers = 8),
        baseProductionBonus = 30.0
    )

    val FARM = BuildingTemplate(
        type = BuildingType.FARM,
        name = "Farm",
        description = "Produces food to support workers",
        category = BuildingCategory.PRODUCTION,
        baseCost = ResourceCost(metal = 80, workers = 3),
        baseProductionBonus = 60.0
    )

    val UNIT_PRODUCTION_FACILITY = BuildingTemplate(
        type = BuildingType.UNIT_PRODUCTION_FACILITY,
        name = "Unit Production Facility",
        description = "Trains military units. Level 5 unlocks nuclear missiles.",
        category = BuildingCategory.MILITARY,
        maxLevel = 5,
        baseCost = ResourceCost(metal = 500, oil = 200, workers = 15),
        unlockRequirements = mapOf(BuildingType.METAL_MINE to 3)
    )

    val RESEARCH_CENTER = BuildingTemplate(
        type = BuildingType.RESEARCH_CENTER,
        name = "Research Center",
        description = "Unlocks new technologies and upgrades",
        category = BuildingCategory.SPECIAL,
        baseCost = ResourceCost(metal = 400, oil = 100, workers = 10),
        unlockRequirements = mapOf(BuildingType.METAL_MINE to 2)
    )

    val DEFENSE_TOWER = BuildingTemplate(
        type = BuildingType.DEFENSE_TOWER,
        name = "Defense Tower",
        description = "Provides defensive firepower",
        category = BuildingCategory.DEFENSE,
        baseCost = ResourceCost(metal = 300, oil = 100, workers = 8)
    )

    val SPY_AGENCY = BuildingTemplate(
        type = BuildingType.SPY_AGENCY,
        name = "Spy Agency",
        description = "Trains spies for espionage operations",
        category = BuildingCategory.MILITARY,
        baseCost = ResourceCost(metal = 600, oil = 300, energy = 200, workers = 12),
        unlockRequirements = mapOf(BuildingType.RESEARCH_CENTER to 3)
    )

    val NUCLEAR_SILO = BuildingTemplate(
        type = BuildingType.NUCLEAR_SILO,
        name = "Nuclear Silo",
        description = "Launches devastating nuclear missiles",
        category = BuildingCategory.MILITARY,
        maxLevel = 3,
        baseCost = ResourceCost(metal = 2000, oil = 1000, energy = 500, workers = 30),
        unlockRequirements = mapOf(BuildingType.UNIT_PRODUCTION_FACILITY to 5)
    )

    val ION_CANNON = BuildingTemplate(
        type = BuildingType.ION_CANNON,
        name = "Ion Cannon",
        description = "Powerful energy-based superweapon",
        category = BuildingCategory.MILITARY,
        maxLevel = 3,
        baseCost = ResourceCost(metal = 2500, oil = 800, energy = 1000, workers = 35),
        unlockRequirements = mapOf(
            BuildingType.ENERGY_PLANT to 5,
            BuildingType.RESEARCH_CENTER to 5
        )
    )

    val SCANNER_ARRAY = BuildingTemplate(
        type = BuildingType.SCANNER_ARRAY,
        name = "Scanner Array",
        description = "Provides reconnaissance and early warning",
        category = BuildingCategory.SPECIAL,
        baseCost = ResourceCost(metal = 400, energy = 200, workers = 10),
        unlockRequirements = mapOf(BuildingType.ENERGY_PLANT to 3)
    )

    val COMMAND_CENTER = BuildingTemplate(
        type = BuildingType.COMMAND_CENTER,
        name = "Command Center",
        description = "Central hub of your colony",
        category = BuildingCategory.SPECIAL,
        baseCost = ResourceCost(metal = 0),  // Pre-built
        maxLevel = 10
    )

    val WAREHOUSE = BuildingTemplate(
        type = BuildingType.WAREHOUSE,
        name = "Warehouse",
        description = "Increases resource storage capacity",
        category = BuildingCategory.SPECIAL,
        baseCost = ResourceCost(metal = 250, workers = 8)
    )

    // Map of all templates
    val ALL_TEMPLATES = mapOf(
        BuildingType.METAL_MINE to METAL_MINE,
        BuildingType.OIL_REFINERY to OIL_REFINERY,
        BuildingType.ENERGY_PLANT to ENERGY_PLANT,
        BuildingType.FARM to FARM,
        BuildingType.UNIT_PRODUCTION_FACILITY to UNIT_PRODUCTION_FACILITY,
        BuildingType.RESEARCH_CENTER to RESEARCH_CENTER,
        BuildingType.DEFENSE_TOWER to DEFENSE_TOWER,
        BuildingType.SPY_AGENCY to SPY_AGENCY,
        BuildingType.NUCLEAR_SILO to NUCLEAR_SILO,
        BuildingType.ION_CANNON to ION_CANNON,
        BuildingType.SCANNER_ARRAY to SCANNER_ARRAY,
        BuildingType.COMMAND_CENTER to COMMAND_CENTER,
        BuildingType.WAREHOUSE to WAREHOUSE
    )

    fun getTemplate(type: BuildingType): BuildingTemplate {
        return ALL_TEMPLATES[type] ?: error("Unknown building type: $type")
    }
}
