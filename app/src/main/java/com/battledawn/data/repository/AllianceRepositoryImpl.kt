package com.battledawn.data.repository

import com.battledawn.domain.model.Alliance
import com.battledawn.domain.repository.AllianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of AllianceRepository
 */
class AllianceRepositoryImpl @Inject constructor() : AllianceRepository {

    private val alliances = mutableMapOf<String, Alliance>()

    override fun getAlliance(allianceId: String): Flow<Alliance?> = flow {
        emit(alliances[allianceId])
    }

    override fun getAllAlliances(): Flow<List<Alliance>> = flow {
        emit(alliances.values.toList())
    }

    override suspend fun createAlliance(
        name: String,
        tag: String,
        leaderId: String,
        description: String
    ): Result<Alliance> {
        return try {
            val alliance = Alliance(
                id = "alliance_${UUID.randomUUID()}",
                name = name,
                tag = tag,
                leaderId = leaderId,
                memberIds = listOf(leaderId),
                description = description,
                totalMembers = 1
            )

            alliances[alliance.id] = alliance
            Result.success(alliance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinAlliance(allianceId: String, playerId: String): Result<Alliance> {
        return try {
            val alliance = alliances[allianceId]
                ?: return Result.failure(Exception("Alliance not found"))

            val updated = alliance.addMember(playerId)
            alliances[allianceId] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveAlliance(allianceId: String, playerId: String): Result<Unit> {
        return try {
            val alliance = alliances[allianceId]
                ?: return Result.failure(Exception("Alliance not found"))

            val updated = alliance.removeMember(playerId)
            alliances[allianceId] = updated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun kickMember(allianceId: String, playerId: String): Result<Alliance> {
        return try {
            val alliance = alliances[allianceId]
                ?: return Result.failure(Exception("Alliance not found"))

            val updated = alliance.removeMember(playerId)
            alliances[allianceId] = updated
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAlliance(alliance: Alliance): Result<Unit> {
        return try {
            alliances[alliance.id] = alliance
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllianceMembers(allianceId: String): Flow<List<String>> = flow {
        emit(alliances[allianceId]?.memberIds ?: emptyList())
    }

    override suspend fun sendInvitation(allianceId: String, playerId: String): Result<Unit> {
        return try {
            // TODO: Implement invitation system
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
