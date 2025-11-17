package com.battledawn.domain.repository

import com.battledawn.domain.model.Alliance
import kotlinx.coroutines.flow.Flow

/**
 * Repository for alliance operations
 */
interface AllianceRepository {

    /**
     * Get alliance by ID
     */
    fun getAlliance(allianceId: String): Flow<Alliance?>

    /**
     * Get all alliances
     */
    fun getAllAlliances(): Flow<List<Alliance>>

    /**
     * Create new alliance
     */
    suspend fun createAlliance(
        name: String,
        tag: String,
        leaderId: String,
        description: String
    ): Result<Alliance>

    /**
     * Join alliance
     */
    suspend fun joinAlliance(allianceId: String, playerId: String): Result<Alliance>

    /**
     * Leave alliance
     */
    suspend fun leaveAlliance(allianceId: String, playerId: String): Result<Unit>

    /**
     * Kick member from alliance
     */
    suspend fun kickMember(allianceId: String, playerId: String): Result<Alliance>

    /**
     * Update alliance details
     */
    suspend fun updateAlliance(alliance: Alliance): Result<Unit>

    /**
     * Get alliance members
     */
    fun getAllianceMembers(allianceId: String): Flow<List<String>>

    /**
     * Send alliance invitation
     */
    suspend fun sendInvitation(allianceId: String, playerId: String): Result<Unit>
}
