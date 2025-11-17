package com.battledawn.domain.model

/**
 * Daily Rewards System
 * Encourages daily login and player retention
 */
data class DailyReward(
    val day: Int,
    val metal: Long,
    val oil: Long,
    val energy: Long,
    val food: Long,
    val experience: Long,
    val premiumCurrency: Int = 0,
    val isClaimed: Boolean = false
)

/**
 * Daily rewards calendar (7-day cycle)
 */
object DailyRewards {

    val weeklyRewards = listOf(
        DailyReward(
            day = 1,
            metal = 1000,
            oil = 500,
            energy = 500,
            food = 1000,
            experience = 100
        ),
        DailyReward(
            day = 2,
            metal = 1500,
            oil = 1000,
            energy = 1000,
            food = 1500,
            experience = 150
        ),
        DailyReward(
            day = 3,
            metal = 2000,
            oil = 1500,
            energy = 1500,
            food = 2000,
            experience = 200
        ),
        DailyReward(
            day = 4,
            metal = 2500,
            oil = 2000,
            energy = 2000,
            food = 2500,
            experience = 250
        ),
        DailyReward(
            day = 5,
            metal = 3000,
            oil = 2500,
            energy = 2500,
            food = 3000,
            experience = 300
        ),
        DailyReward(
            day = 6,
            metal = 4000,
            oil = 3000,
            energy = 3000,
            food = 4000,
            experience = 400
        ),
        DailyReward(
            day = 7,
            metal = 5000,
            oil = 5000,
            energy = 5000,
            food = 5000,
            experience = 500,
            premiumCurrency = 10 // Bonus for full week
        )
    )

    /**
     * Get reward for specific day
     */
    fun getRewardForDay(day: Int): DailyReward {
        val adjustedDay = ((day - 1) % 7) + 1 // Loop rewards every 7 days
        return weeklyRewards[adjustedDay - 1]
    }
}

/**
 * Daily reward status
 */
data class DailyRewardStatus(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalDaysLoggedIn: Int,
    val lastClaimedDate: Long?,
    val canClaimToday: Boolean,
    val currentDayReward: DailyReward
)
