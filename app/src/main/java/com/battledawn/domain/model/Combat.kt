package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.max
import kotlin.random.Random

/**
 * Combat encounter between two armies
 */
@Parcelize
data class Battle(
    val id: String,
    val attackerArmyId: String,
    val defenderArmyId: String,
    val attackerColonyId: String,
    val defenderColonyId: String,
    val location: GridPosition,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val status: BattleStatus = BattleStatus.IN_PROGRESS,
    val rounds: List<BattleRound> = emptyList(),
    val result: BattleResult? = null
) : Parcelable

/**
 * Battle status
 */
enum class BattleStatus {
    PENDING,      // Battle scheduled but not started
    IN_PROGRESS,  // Currently fighting
    COMPLETED     // Battle finished
}

/**
 * Single round of combat
 */
@Parcelize
data class BattleRound(
    val roundNumber: Int,
    val attackerDamage: Int,
    val defenderDamage: Int,
    val attackerCasualties: Map<UnitType, Int> = emptyMap(),
    val defenderCasualties: Map<UnitType, Int> = emptyMap()
) : Parcelable

/**
 * Final battle result
 */
@Parcelize
data class BattleResult(
    val victor: BattleVictor,
    val attackerSurvivors: Map<UnitType, Int>,
    val defenderSurvivors: Map<UnitType, Int>,
    val attackerExperienceGained: Int,
    val defenderExperienceGained: Int,
    val resourcesPlundered: ResourcePool? = null
) : Parcelable

/**
 * Who won the battle
 */
enum class BattleVictor {
    ATTACKER,
    DEFENDER,
    DRAW
}

/**
 * Spy operation types
 */
enum class SpyOperation {
    RECONNAISSANCE,      // Gather intelligence
    SABOTAGE_RESOURCES,  // Destroy resources
    SABOTAGE_BUILDINGS,  // Disable buildings temporarily
    FREEZE_TROOPS,       // Prevent troop movement
    STEAL_INTEL,         // Steal research/tech info
    ASSASSINATION        // Kill enemy units/workers
}

/**
 * Spy mission
 */
@Parcelize
data class SpyMission(
    val id: String,
    val spyId: String,
    val operativeColonyId: String,
    val targetColonyId: String,
    val operation: SpyOperation,
    val startTime: Long,
    val completionTime: Long,
    val status: MissionStatus = MissionStatus.IN_PROGRESS,
    val result: SpyMissionResult? = null
) : Parcelable

/**
 * Mission status
 */
enum class MissionStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    DETECTED
}

/**
 * Result of spy mission
 */
@Parcelize
data class SpyMissionResult(
    val success: Boolean,
    val detected: Boolean,
    val intelligence: ColonyIntelligence? = null,
    val damageDealt: ResourceCost? = null,
    val spyCaptured: Boolean = false
) : Parcelable

/**
 * Intelligence gathered about enemy colony
 */
@Parcelize
data class ColonyIntelligence(
    val colonyId: String,
    val resources: ResourcePool? = null,
    val buildings: List<BuildingType>? = null,
    val militaryStrength: Int? = null,
    val defenseRating: Int? = null,
    val gatheredAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Combat calculator
 */
object CombatEngine {

    /**
     * Simulate a battle between two armies
     */
    fun simulateBattle(
        attackerArmy: Army,
        defenderArmy: Army,
        defenseBonus: Int = 0  // From defensive structures
    ): BattleResult {
        val attackerUnits = attackerArmy.units.toMutableList()
        val defenderUnits = defenderArmy.units.toMutableList()

        val rounds = mutableListOf<BattleRound>()
        var roundNumber = 1

        // Battle continues while both sides have living units (max 20 rounds)
        while (attackerUnits.any { it.isAlive() } &&
               defenderUnits.any { it.isAlive() } &&
               roundNumber <= 20) {

            val round = simulateRound(
                attackerUnits,
                defenderUnits,
                defenseBonus,
                roundNumber
            )
            rounds.add(round)

            // Remove dead units
            attackerUnits.removeAll { !it.isAlive() }
            defenderUnits.removeAll { !it.isAlive() }

            roundNumber++
        }

        // Determine victor
        val victor = when {
            attackerUnits.any { it.isAlive() } && defenderUnits.none { it.isAlive() } ->
                BattleVictor.ATTACKER
            defenderUnits.any { it.isAlive() } && attackerUnits.none { it.isAlive() } ->
                BattleVictor.DEFENDER
            else -> BattleVictor.DRAW
        }

        // Calculate survivors
        val attackerSurvivors = attackerUnits
            .filter { it.isAlive() }
            .groupBy { it.type }
            .mapValues { it.value.size }

        val defenderSurvivors = defenderUnits
            .filter { it.isAlive() }
            .groupBy { it.type }
            .mapValues { it.value.size }

        // Calculate experience
        val attackerExp = calculateExperience(defenderArmy.units.size, victor == BattleVictor.ATTACKER)
        val defenderExp = calculateExperience(attackerArmy.units.size, victor == BattleVictor.DEFENDER)

        return BattleResult(
            victor = victor,
            attackerSurvivors = attackerSurvivors,
            defenderSurvivors = defenderSurvivors,
            attackerExperienceGained = attackerExp,
            defenderExperienceGained = defenderExp
        )
    }

    /**
     * Simulate a single round of combat
     */
    private fun simulateRound(
        attackers: MutableList<Unit>,
        defenders: MutableList<Unit>,
        defenseBonus: Int,
        roundNumber: Int
    ): BattleRound {
        val attackerCasualties = mutableMapOf<UnitType, Int>()
        val defenderCasualties = mutableMapOf<UnitType, Int>()

        var totalAttackerDamage = 0
        var totalDefenderDamage = 0

        // Attackers strike first
        for (attacker in attackers.filter { it.isAlive() }) {
            val template = UnitTemplates.getTemplate(attacker.type)
            val target = defenders.randomOrNull() ?: break

            val damage = calculateDamage(
                attacker,
                target,
                template,
                UnitTemplates.getTemplate(target.type),
                isDefending = false
            )

            val targetIndex = defenders.indexOf(target)
            if (targetIndex >= 0) {
                defenders[targetIndex] = target.takeDamage(damage)
                totalAttackerDamage += damage

                if (!defenders[targetIndex].isAlive()) {
                    defenderCasualties[target.type] =
                        defenderCasualties.getOrDefault(target.type, 0) + 1
                }
            }
        }

        // Defenders counter-attack
        for (defender in defenders.filter { it.isAlive() }) {
            val template = UnitTemplates.getTemplate(defender.type)
            val target = attackers.randomOrNull() ?: break

            val damage = calculateDamage(
                defender,
                target,
                template,
                UnitTemplates.getTemplate(target.type),
                isDefending = true,
                defenseBonus
            )

            val targetIndex = attackers.indexOf(target)
            if (targetIndex >= 0) {
                attackers[targetIndex] = target.takeDamage(damage)
                totalDefenderDamage += damage

                if (!attackers[targetIndex].isAlive()) {
                    attackerCasualties[target.type] =
                        attackerCasualties.getOrDefault(target.type, 0) + 1
                }
            }
        }

        return BattleRound(
            roundNumber = roundNumber,
            attackerDamage = totalAttackerDamage,
            defenderDamage = totalDefenderDamage,
            attackerCasualties = attackerCasualties,
            defenderCasualties = defenderCasualties
        )
    }

    /**
     * Calculate damage dealt by one unit to another
     */
    private fun calculateDamage(
        attacker: Unit,
        defender: Unit,
        attackerTemplate: UnitTemplate,
        defenderTemplate: UnitTemplate,
        isDefending: Boolean,
        defenseBonus: Int = 0
    ): Int {
        // Base damage from attack stat
        var damage = attackerTemplate.attack.toDouble()

        // Unit level bonus (5% per level)
        damage *= (1.0 + (attacker.level - 1) * 0.05)

        // Unit type effectiveness
        val effectiveness = attackerTemplate.getEffectivenessMultiplier(defenderTemplate.category)
        damage *= effectiveness

        // Defender's defense reduces damage
        val totalDefense = defenderTemplate.defense + (if (isDefending) defenseBonus else 0)
        damage = max(damage - totalDefense, damage * 0.2)  // Minimum 20% damage gets through

        // Add some randomness (+/- 15%)
        val randomFactor = Random.nextDouble(0.85, 1.15)
        damage *= randomFactor

        return damage.toInt().coerceAtLeast(1)
    }

    /**
     * Calculate experience gained from battle
     */
    private fun calculateExperience(enemyUnitCount: Int, won: Boolean): Int {
        val baseExp = enemyUnitCount * 10
        val victoryBonus = if (won) 1.5 else 1.0
        return (baseExp * victoryBonus).toInt()
    }

    /**
     * Calculate spy mission success chance
     */
    fun calculateSpySuccessChance(
        operation: SpyOperation,
        targetDefenseRating: Int,
        spyLevel: Int
    ): Double {
        val baseChance = when (operation) {
            SpyOperation.RECONNAISSANCE -> 0.80
            SpyOperation.SABOTAGE_RESOURCES -> 0.60
            SpyOperation.SABOTAGE_BUILDINGS -> 0.50
            SpyOperation.FREEZE_TROOPS -> 0.55
            SpyOperation.STEAL_INTEL -> 0.65
            SpyOperation.ASSASSINATION -> 0.40
        }

        // Spy level increases success
        val levelBonus = spyLevel * 0.05

        // Defense reduces success
        val defensePenalty = targetDefenseRating / 10000.0

        return (baseChance + levelBonus - defensePenalty).coerceIn(0.1, 0.95)
    }

    /**
     * Calculate nuclear missile damage
     */
    fun calculateNuclearDamage(targetColony: Colony): ResourceCost {
        // Nuclear missile destroys a percentage of resources
        val destructionRate = 0.3  // 30% of resources destroyed

        return ResourceCost(
            metal = (targetColony.resources.metal.amount * destructionRate).toLong(),
            oil = (targetColony.resources.oil.amount * destructionRate).toLong(),
            energy = (targetColony.resources.energy.amount * destructionRate).toLong(),
            food = (targetColony.resources.food.amount * destructionRate).toLong(),
            workers = (targetColony.resources.workers.amount * destructionRate).toLong()
        )
    }

    /**
     * Calculate resources plundered after victory
     */
    fun calculatePlunder(defeatedColony: Colony, victorArmySize: Int): ResourcePool {
        // Can plunder up to 20% of resources, limited by army carrying capacity
        val plunderRate = 0.20
        val carryingCapacity = victorArmySize * 100L  // 100 resources per unit

        fun plunderResource(resource: Resource): Resource {
            val availablePlunder = (resource.amount * plunderRate).toLong()
            val actualPlunder = availablePlunder.coerceAtMost(carryingCapacity / 4)  // Split among 4 resources
            return resource.copy(amount = actualPlunder)
        }

        return ResourcePool(
            metal = plunderResource(defeatedColony.resources.metal),
            oil = plunderResource(defeatedColony.resources.oil),
            energy = plunderResource(defeatedColony.resources.energy),
            food = plunderResource(defeatedColony.resources.food),
            workers = Resource(ResourceType.WORKERS, 0)  // Can't plunder workers
        )
    }
}
