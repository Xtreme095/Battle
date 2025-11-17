package com.battledawn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the different types of resources in the game
 */
enum class ResourceType {
    METAL,      // Primary construction resource
    OIL,        // Required for vehicles and advanced units
    ENERGY,     // Powers special operations
    FOOD,       // Generates workers
    WORKERS     // Collect resources and build units
}

/**
 * Represents a resource amount
 */
@Parcelize
data class Resource(
    val type: ResourceType,
    val amount: Long = 0L,
    val productionRate: Double = 0.0,  // per hour
    val capacity: Long = 10000L
) : Parcelable {

    /**
     * Calculate resource amount after a given duration
     */
    fun calculateAmount(hoursElapsed: Double): Long {
        val projected = amount + (productionRate * hoursElapsed).toLong()
        return projected.coerceAtMost(capacity)
    }

    /**
     * Check if there are enough resources
     */
    fun hasEnough(required: Long): Boolean = amount >= required

    /**
     * Consume resources
     */
    fun consume(consumeAmount: Long): Resource {
        require(consumeAmount >= 0) { "Consume amount must be positive" }
        return copy(amount = (amount - consumeAmount).coerceAtLeast(0))
    }

    /**
     * Add resources
     */
    fun add(addAmount: Long): Resource {
        require(addAmount >= 0) { "Add amount must be positive" }
        return copy(amount = (amount + addAmount).coerceAtMost(capacity))
    }
}

/**
 * Collection of all resources for a player/colony
 */
@Parcelize
data class ResourcePool(
    val metal: Resource = Resource(ResourceType.METAL),
    val oil: Resource = Resource(ResourceType.OIL),
    val energy: Resource = Resource(ResourceType.ENERGY),
    val food: Resource = Resource(ResourceType.FOOD),
    val workers: Resource = Resource(ResourceType.WORKERS)
) : Parcelable {

    fun getResource(type: ResourceType): Resource = when (type) {
        ResourceType.METAL -> metal
        ResourceType.OIL -> oil
        ResourceType.ENERGY -> energy
        ResourceType.FOOD -> food
        ResourceType.WORKERS -> workers
    }

    fun updateResource(type: ResourceType, resource: Resource): ResourcePool = when (type) {
        ResourceType.METAL -> copy(metal = resource)
        ResourceType.OIL -> copy(oil = resource)
        ResourceType.ENERGY -> copy(energy = resource)
        ResourceType.FOOD -> copy(food = resource)
        ResourceType.WORKERS -> copy(workers = resource)
    }

    /**
     * Check if all required resources are available
     */
    fun hasEnoughResources(cost: ResourceCost): Boolean {
        return metal.hasEnough(cost.metal) &&
                oil.hasEnough(cost.oil) &&
                energy.hasEnough(cost.energy) &&
                food.hasEnough(cost.food) &&
                workers.hasEnough(cost.workers)
    }

    /**
     * Consume resources based on cost
     */
    fun consumeResources(cost: ResourceCost): ResourcePool {
        return copy(
            metal = metal.consume(cost.metal),
            oil = oil.consume(cost.oil),
            energy = energy.consume(cost.energy),
            food = food.consume(cost.food),
            workers = workers.consume(cost.workers)
        )
    }

    /**
     * Update all resources based on time elapsed
     */
    fun updateProduction(hoursElapsed: Double): ResourcePool {
        return copy(
            metal = metal.copy(amount = metal.calculateAmount(hoursElapsed)),
            oil = oil.copy(amount = oil.calculateAmount(hoursElapsed)),
            energy = energy.copy(amount = energy.calculateAmount(hoursElapsed)),
            food = food.copy(amount = food.calculateAmount(hoursElapsed)),
            workers = workers.copy(amount = workers.calculateAmount(hoursElapsed))
        )
    }
}

/**
 * Cost of an action/building/unit in resources
 */
@Parcelize
data class ResourceCost(
    val metal: Long = 0,
    val oil: Long = 0,
    val energy: Long = 0,
    val food: Long = 0,
    val workers: Long = 0
) : Parcelable {

    companion object {
        val ZERO = ResourceCost()
    }

    operator fun times(multiplier: Double): ResourceCost {
        return ResourceCost(
            metal = (metal * multiplier).toLong(),
            oil = (oil * multiplier).toLong(),
            energy = (energy * multiplier).toLong(),
            food = (food * multiplier).toLong(),
            workers = (workers * multiplier).toLong()
        )
    }
}
