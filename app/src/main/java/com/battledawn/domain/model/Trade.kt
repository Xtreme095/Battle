package com.battledawn.domain.model

import java.time.Instant

/**
 * Trading system for resource exchange between players
 */
data class TradeOffer(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val receiverId: String,
    val receiverUsername: String,
    val offeredResources: ResourceBundle,
    val requestedResources: ResourceBundle,
    val status: TradeStatus,
    val createdAt: Instant,
    val expiresAt: Instant,
    val completedAt: Instant? = null
)

/**
 * Resource bundle for trading
 */
data class ResourceBundle(
    val metal: Long = 0,
    val oil: Long = 0,
    val energy: Long = 0,
    val food: Long = 0
) {
    fun isEmpty(): Boolean {
        return metal == 0L && oil == 0L && energy == 0L && food == 0L
    }

    fun total(): Long {
        return metal + oil + energy + food
    }

    operator fun plus(other: ResourceBundle): ResourceBundle {
        return ResourceBundle(
            metal = this.metal + other.metal,
            oil = this.oil + other.oil,
            energy = this.energy + other.energy,
            food = this.food + other.food
        )
    }

    operator fun minus(other: ResourceBundle): ResourceBundle {
        return ResourceBundle(
            metal = this.metal - other.metal,
            oil = this.oil - other.oil,
            energy = this.energy - other.energy,
            food = this.food - other.food
        )
    }

    fun hasEnough(required: ResourceBundle): Boolean {
        return metal >= required.metal &&
               oil >= required.oil &&
               energy >= required.energy &&
               food >= required.food
    }
}

/**
 * Trade status
 */
enum class TradeStatus {
    PENDING,    // Waiting for receiver to accept/reject
    ACCEPTED,   // Receiver accepted, trade completed
    REJECTED,   // Receiver rejected the offer
    CANCELLED,  // Sender cancelled the offer
    EXPIRED     // Offer expired
}

/**
 * Trade history entry
 */
data class TradeHistory(
    val id: String,
    val tradeId: String,
    val participantId: String,
    val otherPartyUsername: String,
    val wasOfferer: Boolean,
    val resourcesGiven: ResourceBundle,
    val resourcesReceived: ResourceBundle,
    val completedAt: Instant,
    val success: Boolean
)

/**
 * Trade market rates (optional - for suggested pricing)
 */
data class MarketRates(
    val metalToOil: Double = 1.0,
    val metalToEnergy: Double = 1.0,
    val metalToFood: Double = 1.0,
    val oilToEnergy: Double = 1.0,
    val oilToFood: Double = 1.0,
    val energyToFood: Double = 1.0,
    val lastUpdated: Instant = Instant.now()
) {
    /**
     * Calculate fair value of a resource bundle in metal equivalent
     */
    fun calculateValue(bundle: ResourceBundle): Double {
        return bundle.metal.toDouble() +
               (bundle.oil * metalToOil) +
               (bundle.energy * metalToEnergy) +
               (bundle.food * metalToFood)
    }

    /**
     * Check if trade is fair (within 20% difference)
     */
    fun isFairTrade(offered: ResourceBundle, requested: ResourceBundle): Boolean {
        val offeredValue = calculateValue(offered)
        val requestedValue = calculateValue(requested)

        if (offeredValue == 0.0 || requestedValue == 0.0) return false

        val ratio = offeredValue / requestedValue
        return ratio in 0.8..1.2 // Within 20% is considered fair
    }
}

/**
 * Trade limits to prevent abuse
 */
data class TradeLimits(
    val maxOffersPerDay: Int = 10,
    val maxActiveOffers: Int = 5,
    val minPlayerLevel: Int = 5,
    val offerDurationHours: Int = 24,
    val maxResourceAmount: Long = 1000000
)
