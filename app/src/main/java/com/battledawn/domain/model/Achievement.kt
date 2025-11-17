package com.battledawn.domain.model

/**
 * Achievement system for player progression and engagement
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val icon: String,
    val requirement: AchievementRequirement,
    val reward: AchievementReward,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val unlockedAt: Long? = null
)

/**
 * Achievement categories
 */
enum class AchievementCategory {
    COMBAT,      // Battle-related achievements
    ECONOMY,     // Resource and building achievements
    MILITARY,    // Unit training achievements
    RESEARCH,    // Technology achievements
    SOCIAL,      // Alliance and player interaction
    EXPLORATION, // Map and territory achievements
    SPECIAL      // Unique/seasonal achievements
}

/**
 * Achievement tiers (difficulty/rarity)
 */
enum class AchievementTier(val points: Int) {
    BRONZE(10),
    SILVER(25),
    GOLD(50),
    PLATINUM(100),
    DIAMOND(250)
}

/**
 * Achievement requirement
 */
data class AchievementRequirement(
    val type: RequirementType,
    val target: Int
)

enum class RequirementType {
    WIN_BATTLES,
    ATTACK_COLONIES,
    DEFEND_SUCCESSFULLY,
    TOTAL_KILLS,
    BUILD_STRUCTURES,
    UPGRADE_STRUCTURES,
    TRAIN_UNITS,
    COMPLETE_RESEARCH,
    COLLECT_RESOURCES,
    JOIN_ALLIANCE,
    ALLIANCE_MEMBERS,
    CAPTURE_TERRITORIES,
    REACH_LEVEL,
    LOGIN_DAYS_CONSECUTIVE,
    SEND_MESSAGES,
    PLUNDER_RESOURCES
}

/**
 * Achievement reward
 */
data class AchievementReward(
    val experience: Long = 0,
    val metal: Long = 0,
    val oil: Long = 0,
    val energy: Long = 0,
    val food: Long = 0,
    val premiumCurrency: Int = 0 // Gems/crystals (if implemented)
)

/**
 * Pre-defined achievements
 */
object Achievements {

    val allAchievements = listOf(
        // COMBAT Achievements
        Achievement(
            id = "first_blood",
            name = "First Blood",
            description = "Win your first battle",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.BRONZE,
            icon = "⚔️",
            requirement = AchievementRequirement(RequirementType.WIN_BATTLES, 1),
            reward = AchievementReward(experience = 100, metal = 500)
        ),
        Achievement(
            id = "warrior",
            name = "Warrior",
            description = "Win 10 battles",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.SILVER,
            icon = "🗡️",
            requirement = AchievementRequirement(RequirementType.WIN_BATTLES, 10),
            reward = AchievementReward(experience = 500, metal = 2000, oil = 1000)
        ),
        Achievement(
            id = "warlord",
            name = "Warlord",
            description = "Win 50 battles",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.GOLD,
            icon = "⚔️",
            requirement = AchievementRequirement(RequirementType.WIN_BATTLES, 50),
            reward = AchievementReward(experience = 2000, metal = 5000, oil = 5000)
        ),
        Achievement(
            id = "destroyer",
            name = "Destroyer",
            description = "Destroy 1,000 enemy units",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.PLATINUM,
            icon = "💀",
            requirement = AchievementRequirement(RequirementType.TOTAL_KILLS, 1000),
            reward = AchievementReward(experience = 5000, energy = 10000)
        ),

        // ECONOMY Achievements
        Achievement(
            id = "builder",
            name = "Builder",
            description = "Construct 10 buildings",
            category = AchievementCategory.ECONOMY,
            tier = AchievementTier.BRONZE,
            icon = "🏗️",
            requirement = AchievementRequirement(RequirementType.BUILD_STRUCTURES, 10),
            reward = AchievementReward(experience = 200, food = 1000)
        ),
        Achievement(
            id = "architect",
            name = "Architect",
            description = "Upgrade buildings to max level (10)",
            category = AchievementCategory.ECONOMY,
            tier = AchievementTier.GOLD,
            icon = "🏛️",
            requirement = AchievementRequirement(RequirementType.UPGRADE_STRUCTURES, 10),
            reward = AchievementReward(experience = 3000, metal = 10000)
        ),
        Achievement(
            id = "resource_tycoon",
            name = "Resource Tycoon",
            description = "Collect 1,000,000 total resources",
            category = AchievementCategory.ECONOMY,
            tier = AchievementTier.PLATINUM,
            icon = "💰",
            requirement = AchievementRequirement(RequirementType.COLLECT_RESOURCES, 1000000),
            reward = AchievementReward(experience = 5000, metal = 5000, oil = 5000, energy = 5000, food = 5000)
        ),

        // MILITARY Achievements
        Achievement(
            id = "recruit",
            name = "Recruit",
            description = "Train 100 units",
            category = AchievementCategory.MILITARY,
            tier = AchievementTier.BRONZE,
            icon = "👤",
            requirement = AchievementRequirement(RequirementType.TRAIN_UNITS, 100),
            reward = AchievementReward(experience = 150, metal = 800)
        ),
        Achievement(
            id = "general",
            name = "General",
            description = "Train 1,000 units",
            category = AchievementCategory.MILITARY,
            tier = AchievementTier.SILVER,
            icon = "⭐",
            requirement = AchievementRequirement(RequirementType.TRAIN_UNITS, 1000),
            reward = AchievementReward(experience = 1000, oil = 5000)
        ),
        Achievement(
            id = "supreme_commander",
            name = "Supreme Commander",
            description = "Train 10,000 units",
            category = AchievementCategory.MILITARY,
            tier = AchievementTier.DIAMOND,
            icon = "⭐⭐⭐",
            requirement = AchievementRequirement(RequirementType.TRAIN_UNITS, 10000),
            reward = AchievementReward(experience = 10000, metal = 20000, oil = 20000)
        ),

        // RESEARCH Achievements
        Achievement(
            id = "scientist",
            name = "Scientist",
            description = "Complete 5 researches",
            category = AchievementCategory.RESEARCH,
            tier = AchievementTier.BRONZE,
            icon = "🔬",
            requirement = AchievementRequirement(RequirementType.COMPLETE_RESEARCH, 5),
            reward = AchievementReward(experience = 300, energy = 2000)
        ),
        Achievement(
            id = "tech_master",
            name = "Tech Master",
            description = "Complete all 14 technologies to max level",
            category = AchievementCategory.RESEARCH,
            tier = AchievementTier.DIAMOND,
            icon = "🧪",
            requirement = AchievementRequirement(RequirementType.COMPLETE_RESEARCH, 140), // 14 techs * 10 levels
            reward = AchievementReward(experience = 20000, metal = 30000, oil = 30000, energy = 30000)
        ),

        // SOCIAL Achievements
        Achievement(
            id = "team_player",
            name = "Team Player",
            description = "Join an alliance",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.BRONZE,
            icon = "🤝",
            requirement = AchievementRequirement(RequirementType.JOIN_ALLIANCE, 1),
            reward = AchievementReward(experience = 100)
        ),
        Achievement(
            id = "alliance_leader",
            name = "Alliance Leader",
            description = "Lead an alliance with 50 members",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.GOLD,
            icon = "👑",
            requirement = AchievementRequirement(RequirementType.ALLIANCE_MEMBERS, 50),
            reward = AchievementReward(experience = 5000)
        ),
        Achievement(
            id = "social_butterfly",
            name = "Social Butterfly",
            description = "Send 1,000 alliance chat messages",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.SILVER,
            icon = "💬",
            requirement = AchievementRequirement(RequirementType.SEND_MESSAGES, 1000),
            reward = AchievementReward(experience = 500)
        ),

        // EXPLORATION Achievements
        Achievement(
            id = "explorer",
            name = "Explorer",
            description = "Capture 5 territories",
            category = AchievementCategory.EXPLORATION,
            tier = AchievementTier.BRONZE,
            icon = "🗺️",
            requirement = AchievementRequirement(RequirementType.CAPTURE_TERRITORIES, 5),
            reward = AchievementReward(experience = 250, metal = 1000)
        ),
        Achievement(
            id = "conqueror",
            name = "Conqueror",
            description = "Capture 50 territories",
            category = AchievementCategory.EXPLORATION,
            tier = AchievementTier.GOLD,
            icon = "🏴",
            requirement = AchievementRequirement(RequirementType.CAPTURE_TERRITORIES, 50),
            reward = AchievementReward(experience = 5000, metal = 10000, oil = 10000)
        ),

        // SPECIAL Achievements
        Achievement(
            id = "veteran",
            name = "Veteran",
            description = "Log in for 7 consecutive days",
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.SILVER,
            icon = "📅",
            requirement = AchievementRequirement(RequirementType.LOGIN_DAYS_CONSECUTIVE, 7),
            reward = AchievementReward(experience = 1000, metal = 3000, oil = 3000, energy = 3000, food = 3000)
        ),
        Achievement(
            id = "legend",
            name = "Legend",
            description = "Reach level 50",
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.DIAMOND,
            icon = "🏆",
            requirement = AchievementRequirement(RequirementType.REACH_LEVEL, 50),
            reward = AchievementReward(experience = 10000, metal = 50000, oil = 50000, energy = 50000, food = 50000)
        ),
        Achievement(
            id = "plunderer",
            name = "Plunderer",
            description = "Plunder 100,000 resources from enemies",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.PLATINUM,
            icon = "💎",
            requirement = AchievementRequirement(RequirementType.PLUNDER_RESOURCES, 100000),
            reward = AchievementReward(experience = 7500)
        )
    )

    /**
     * Get achievements by category
     */
    fun getByCategory(category: AchievementCategory): List<Achievement> {
        return allAchievements.filter { it.category == category }
    }

    /**
     * Get achievements by tier
     */
    fun getByTier(tier: AchievementTier): List<Achievement> {
        return allAchievements.filter { it.tier == tier }
    }

    /**
     * Get achievement by ID
     */
    fun getById(id: String): Achievement? {
        return allAchievements.find { it.id == id }
    }
}
