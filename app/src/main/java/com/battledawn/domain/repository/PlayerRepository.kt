package com.battledawn.domain.repository

import com.battledawn.domain.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Repository for player operations
 */
interface PlayerRepository {

    /**
     * Get current player
     */
    fun getCurrentPlayer(): Flow<Player?>

    /**
     * Get player by ID
     */
    fun getPlayer(playerId: String): Flow<Player?>

    /**
     * Create new player
     */
    suspend fun createPlayer(player: Player): Result<Player>

    /**
     * Update player data
     */
    suspend fun updatePlayer(player: Player): Result<Unit>

    /**
     * Add experience to player
     */
    suspend fun addExperience(playerId: String, experience: Long): Result<Player>

    /**
     * Update battle statistics
     */
    suspend fun updateBattleStats(playerId: String, won: Boolean): Result<Player>

    /**
     * Login player
     */
    suspend fun login(username: String, password: String): Result<Player>

    /**
     * Register new player
     */
    suspend fun register(username: String, email: String, password: String): Result<Player>

    /**
     * Logout current player
     */
    suspend fun logout(): Result<Unit>
}
