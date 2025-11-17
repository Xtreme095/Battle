package com.battledawn.data.repository

import com.battledawn.domain.model.Player
import com.battledawn.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of PlayerRepository
 */
class PlayerRepositoryImpl @Inject constructor() : PlayerRepository {

    private var currentPlayer: Player? = null

    override fun getCurrentPlayer(): Flow<Player?> = flow {
        emit(currentPlayer)
    }

    override fun getPlayer(playerId: String): Flow<Player?> = flow {
        // TODO: Fetch from database or Firebase
        emit(null)
    }

    override suspend fun createPlayer(player: Player): Result<Player> {
        return try {
            currentPlayer = player
            Result.success(player)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlayer(player: Player): Result<Unit> {
        return try {
            if (player.id == currentPlayer?.id) {
                currentPlayer = player
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExperience(playerId: String, experience: Long): Result<Player> {
        return try {
            val player = currentPlayer ?: return Result.failure(Exception("No player logged in"))
            val updatedPlayer = player.addExperience(experience)
            currentPlayer = updatedPlayer
            Result.success(updatedPlayer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBattleStats(playerId: String, won: Boolean): Result<Player> {
        return try {
            val player = currentPlayer ?: return Result.failure(Exception("No player logged in"))
            val updatedPlayer = if (won) {
                player.copy(wins = player.wins + 1)
            } else {
                player.copy(losses = player.losses + 1)
            }
            currentPlayer = updatedPlayer
            Result.success(updatedPlayer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<Player> {
        return try {
            // TODO: Implement actual authentication
            val player = Player(
                id = "player_${System.currentTimeMillis()}",
                username = username,
                email = "$username@battledawn.com",
                level = 1
            )
            currentPlayer = player
            Result.success(player)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, email: String, password: String): Result<Player> {
        return try {
            val player = Player(
                id = "player_${System.currentTimeMillis()}",
                username = username,
                email = email,
                level = 1
            )
            currentPlayer = player
            Result.success(player)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            currentPlayer = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
