package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Research/Technology types
 */
enum class ResearchType {
    // Military Tech
    ADVANCED_ARMOR,
    ADVANCED_WEAPONS,
    TACTICAL_TRAINING,
    VEHICLE_ENGINEERING,
    TANK_WARFARE,

    // Economic Tech
    MINING_EFFICIENCY,
    OIL_EXTRACTION,
    ENERGY_MANAGEMENT,
    AGRICULTURAL_SCIENCE,

    // Special Tech
    SPY_TRAINING,
    NUCLEAR_PHYSICS,
    ION_TECHNOLOGY,
    SCANNER_TECH,
    DEFENSIVE_SYSTEMS
}

/**
 * Research category
 */
enum class ResearchCategory {
    MILITARY,
    ECONOMIC,
    SPECIAL
}

/**
 * Research template
 */
data class ResearchTemplate(
    val type: ResearchType,
    val name: String,
    val description: String,
    val category: ResearchCategory,
    val cost: ResourceCost,
    val researchTime: Double,  // in hours
    val prerequisites: List<ResearchType> = emptyList(),
    val buildingRequirements: Map<BuildingType, Int> = emptyMap(),
    val bonuses: ResearchBonuses
)

/**
 * Bonuses granted by research
 */
@Parcelize
data class ResearchBonuses(
    val unitAttackBonus: Double = 0.0,      // % increase
    val unitDefenseBonus: Double = 0.0,     // % increase
    val unitHealthBonus: Double = 0.0,      // % increase
    val productionBonus: Double = 0.0,      // % increase
    val buildTimeReduction: Double = 0.0,   // % reduction
    val trainingTimeReduction: Double = 0.0 // % reduction
) : Parcelable

/**
 * Active research in progress
 */
@Parcelize
data class Research(
    val id: String,
    val colonyId: String,
    val type: ResearchType,
    val startTime: Long,
    val completionTime: Long,
    val isCompleted: Boolean = false
) : Parcelable {

    /**
     * Check if research is complete
     */
    fun isResearchComplete(currentTime: Long): Boolean {
        return currentTime >= completionTime
    }

    /**
     * Get research progress (0.0 to 1.0)
     */
    fun getProgress(currentTime: Long): Float {
        if (isCompleted) return 1.0f

        val totalTime = completionTime - startTime
        val elapsedTime = currentTime - startTime

        return (elapsedTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    }
}

/**
 * Player's completed research
 */
@Parcelize
data class TechnologyTree(
    val playerId: String,
    val completedResearch: Set<ResearchType> = emptySet()
) : Parcelable {

    /**
     * Check if technology is unlocked
     */
    fun hasResearch(type: ResearchType): Boolean {
        return type in completedResearch
    }

    /**
     * Check if all prerequisites are met
     */
    fun canResearch(template: ResearchTemplate): Boolean {
        return template.prerequisites.all { it in completedResearch }
    }

    /**
     * Add completed research
     */
    fun completeResearch(type: ResearchType): TechnologyTree {
        return copy(completedResearch = completedResearch + type)
    }

    /**
     * Get total bonuses from all research
     */
    fun getTotalBonuses(): ResearchBonuses {
        val templates = ResearchTemplates.ALL_TEMPLATES
        val bonusList = completedResearch.mapNotNull { templates[it]?.bonuses }

        return ResearchBonuses(
            unitAttackBonus = bonusList.sumOf { it.unitAttackBonus },
            unitDefenseBonus = bonusList.sumOf { it.unitDefenseBonus },
            unitHealthBonus = bonusList.sumOf { it.unitHealthBonus },
            productionBonus = bonusList.sumOf { it.productionBonus },
            buildTimeReduction = bonusList.sumOf { it.buildTimeReduction },
            trainingTimeReduction = bonusList.sumOf { it.trainingTimeReduction }
        )
    }
}

/**
 * Research templates database
 */
object ResearchTemplates {

    // Military Research
    val ADVANCED_ARMOR = ResearchTemplate(
        type = ResearchType.ADVANCED_ARMOR,
        name = "Advanced Armor",
        description = "Improves unit defense by 15%",
        category = ResearchCategory.MILITARY,
        cost = ResourceCost(metal = 5000, oil = 2000),
        researchTime = 6.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 2),
        bonuses = ResearchBonuses(unitDefenseBonus = 0.15)
    )

    val ADVANCED_WEAPONS = ResearchTemplate(
        type = ResearchType.ADVANCED_WEAPONS,
        name = "Advanced Weapons",
        description = "Improves unit attack by 15%",
        category = ResearchCategory.MILITARY,
        cost = ResourceCost(metal = 5000, oil = 2000),
        researchTime = 6.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 2),
        bonuses = ResearchBonuses(unitAttackBonus = 0.15)
    )

    val TACTICAL_TRAINING = ResearchTemplate(
        type = ResearchType.TACTICAL_TRAINING,
        name = "Tactical Training",
        description = "Reduces unit training time by 20%",
        category = ResearchCategory.MILITARY,
        cost = ResourceCost(metal = 3000, food = 2000),
        researchTime = 4.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 1),
        bonuses = ResearchBonuses(trainingTimeReduction = 0.20)
    )

    val VEHICLE_ENGINEERING = ResearchTemplate(
        type = ResearchType.VEHICLE_ENGINEERING,
        name = "Vehicle Engineering",
        description = "Improves vehicle stats by 10%",
        category = ResearchCategory.MILITARY,
        cost = ResourceCost(metal = 6000, oil = 4000),
        researchTime = 8.0,
        prerequisites = listOf(ResearchType.ADVANCED_WEAPONS),
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 3),
        bonuses = ResearchBonuses(unitAttackBonus = 0.10, unitDefenseBonus = 0.10)
    )

    val TANK_WARFARE = ResearchTemplate(
        type = ResearchType.TANK_WARFARE,
        name = "Tank Warfare",
        description = "Improves tank stats by 20%",
        category = ResearchCategory.MILITARY,
        cost = ResourceCost(metal = 10000, oil = 8000),
        researchTime = 12.0,
        prerequisites = listOf(ResearchType.VEHICLE_ENGINEERING),
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 4),
        bonuses = ResearchBonuses(unitAttackBonus = 0.20, unitHealthBonus = 0.20)
    )

    // Economic Research
    val MINING_EFFICIENCY = ResearchTemplate(
        type = ResearchType.MINING_EFFICIENCY,
        name = "Mining Efficiency",
        description = "Increases metal production by 25%",
        category = ResearchCategory.ECONOMIC,
        cost = ResourceCost(metal = 4000, workers = 50),
        researchTime = 5.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 1),
        bonuses = ResearchBonuses(productionBonus = 0.25)
    )

    val OIL_EXTRACTION = ResearchTemplate(
        type = ResearchType.OIL_EXTRACTION,
        name = "Oil Extraction",
        description = "Increases oil production by 25%",
        category = ResearchCategory.ECONOMIC,
        cost = ResourceCost(metal = 4000, oil = 2000, workers = 50),
        researchTime = 5.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 1),
        bonuses = ResearchBonuses(productionBonus = 0.25)
    )

    val ENERGY_MANAGEMENT = ResearchTemplate(
        type = ResearchType.ENERGY_MANAGEMENT,
        name = "Energy Management",
        description = "Increases energy production by 30%",
        category = ResearchCategory.ECONOMIC,
        cost = ResourceCost(metal = 5000, energy = 3000, workers = 60),
        researchTime = 6.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 2),
        bonuses = ResearchBonuses(productionBonus = 0.30)
    )

    val AGRICULTURAL_SCIENCE = ResearchTemplate(
        type = ResearchType.AGRICULTURAL_SCIENCE,
        name = "Agricultural Science",
        description = "Increases food production by 30%",
        category = ResearchCategory.ECONOMIC,
        cost = ResourceCost(metal = 3000, food = 2000, workers = 40),
        researchTime = 4.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 1),
        bonuses = ResearchBonuses(productionBonus = 0.30)
    )

    // Special Research
    val SPY_TRAINING = ResearchTemplate(
        type = ResearchType.SPY_TRAINING,
        name = "Spy Training",
        description = "Unlocks spy units and espionage",
        category = ResearchCategory.SPECIAL,
        cost = ResourceCost(metal = 8000, energy = 5000),
        researchTime = 10.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 3),
        bonuses = ResearchBonuses()
    )

    val NUCLEAR_PHYSICS = ResearchTemplate(
        type = ResearchType.NUCLEAR_PHYSICS,
        name = "Nuclear Physics",
        description = "Unlocks nuclear missiles",
        category = ResearchCategory.SPECIAL,
        cost = ResourceCost(metal = 15000, oil = 10000, energy = 8000),
        researchTime = 20.0,
        prerequisites = listOf(ResearchType.ADVANCED_WEAPONS),
        buildingRequirements = mapOf(
            BuildingType.RESEARCH_CENTER to 5,
            BuildingType.UNIT_PRODUCTION_FACILITY to 5
        ),
        bonuses = ResearchBonuses()
    )

    val ION_TECHNOLOGY = ResearchTemplate(
        type = ResearchType.ION_TECHNOLOGY,
        name = "Ion Technology",
        description = "Unlocks ion cannon",
        category = ResearchCategory.SPECIAL,
        cost = ResourceCost(metal = 18000, energy = 15000),
        researchTime = 24.0,
        prerequisites = listOf(ResearchType.ENERGY_MANAGEMENT),
        buildingRequirements = mapOf(
            BuildingType.RESEARCH_CENTER to 5,
            BuildingType.ENERGY_PLANT to 5
        ),
        bonuses = ResearchBonuses()
    )

    val SCANNER_TECH = ResearchTemplate(
        type = ResearchType.SCANNER_TECH,
        name = "Scanner Technology",
        description = "Unlocks scanner arrays for reconnaissance",
        category = ResearchCategory.SPECIAL,
        cost = ResourceCost(metal = 6000, energy = 4000),
        researchTime = 8.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 2),
        bonuses = ResearchBonuses()
    )

    val DEFENSIVE_SYSTEMS = ResearchTemplate(
        type = ResearchType.DEFENSIVE_SYSTEMS,
        name = "Defensive Systems",
        description = "Reduces building construction time by 25%",
        category = ResearchCategory.SPECIAL,
        cost = ResourceCost(metal = 7000, workers = 80),
        researchTime = 9.0,
        buildingRequirements = mapOf(BuildingType.RESEARCH_CENTER to 3),
        bonuses = ResearchBonuses(buildTimeReduction = 0.25)
    )

    // Map of all templates
    val ALL_TEMPLATES = mapOf(
        ResearchType.ADVANCED_ARMOR to ADVANCED_ARMOR,
        ResearchType.ADVANCED_WEAPONS to ADVANCED_WEAPONS,
        ResearchType.TACTICAL_TRAINING to TACTICAL_TRAINING,
        ResearchType.VEHICLE_ENGINEERING to VEHICLE_ENGINEERING,
        ResearchType.TANK_WARFARE to TANK_WARFARE,
        ResearchType.MINING_EFFICIENCY to MINING_EFFICIENCY,
        ResearchType.OIL_EXTRACTION to OIL_EXTRACTION,
        ResearchType.ENERGY_MANAGEMENT to ENERGY_MANAGEMENT,
        ResearchType.AGRICULTURAL_SCIENCE to AGRICULTURAL_SCIENCE,
        ResearchType.SPY_TRAINING to SPY_TRAINING,
        ResearchType.NUCLEAR_PHYSICS to NUCLEAR_PHYSICS,
        ResearchType.ION_TECHNOLOGY to ION_TECHNOLOGY,
        ResearchType.SCANNER_TECH to SCANNER_TECH,
        ResearchType.DEFENSIVE_SYSTEMS to DEFENSIVE_SYSTEMS
    )

    fun getTemplate(type: ResearchType): ResearchTemplate {
        return ALL_TEMPLATES[type] ?: error("Unknown research type: $type")
    }
}
