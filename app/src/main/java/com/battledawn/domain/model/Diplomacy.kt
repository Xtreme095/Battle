package com.battledawn.domain.model

import java.time.Instant

/**
 * Diplomacy system for alliance-to-alliance relationships
 */

/**
 * Diplomatic relationship between two alliances
 */
data class DiplomaticRelationship(
    val id: String,
    val alliance1Id: String,
    val alliance1Name: String,
    val alliance2Id: String,
    val alliance2Name: String,
    val status: DiplomaticStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val expiresAt: Instant? = null
)

/**
 * Diplomatic status types
 */
enum class DiplomaticStatus {
    NEUTRAL,           // No relationship
    NON_AGGRESSION,    // NAP - Cannot attack each other
    ALLIED,            // Allied - Can share resources, coordinate attacks
    AT_WAR,            // At war - Can attack each other
    PEACE_PENDING,     // Peace treaty proposed but not accepted
    NAP_PENDING        // NAP proposed but not accepted
}

/**
 * Diplomatic proposal (NAP, Peace Treaty, Alliance)
 */
data class DiplomaticProposal(
    val id: String,
    val proposerId: String,
    val proposerName: String,
    val targetId: String,
    val targetName: String,
    val proposalType: ProposalType,
    val terms: DiplomaticTerms,
    val status: ProposalStatus,
    val createdAt: Instant,
    val expiresAt: Instant,
    val respondedAt: Instant? = null
)

/**
 * Type of diplomatic proposal
 */
enum class ProposalType {
    NON_AGGRESSION_PACT,
    PEACE_TREATY,
    ALLIANCE,
    WAR_DECLARATION
}

/**
 * Status of proposal
 */
enum class ProposalStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    CANCELLED
}

/**
 * Terms of diplomatic agreement
 */
data class DiplomaticTerms(
    val duration: DiplomaticDuration,
    val noAttackZones: List<String> = emptyList(),
    val resourceTribute: ResourceBundle? = null,
    val sharedIntelligence: Boolean = false,
    val mutualDefense: Boolean = false
)

/**
 * Duration of diplomatic agreement
 */
enum class DiplomaticDuration(val days: Int) {
    TEMPORARY(7),      // 7 days
    SHORT_TERM(14),    // 2 weeks
    MEDIUM_TERM(30),   // 1 month
    LONG_TERM(90),     // 3 months
    PERMANENT(-1)      // Until cancelled
}

/**
 * Diplomatic history entry
 */
data class DiplomaticHistory(
    val id: String,
    val alliance1Id: String,
    val alliance1Name: String,
    val alliance2Id: String,
    val alliance2Name: String,
    val action: DiplomaticAction,
    val timestamp: Instant,
    val initiatorId: String,
    val details: String
)

/**
 * Diplomatic actions for history
 */
enum class DiplomaticAction {
    WAR_DECLARED,
    PEACE_PROPOSED,
    PEACE_ACCEPTED,
    PEACE_REJECTED,
    NAP_PROPOSED,
    NAP_ACCEPTED,
    NAP_REJECTED,
    NAP_BROKEN,
    ALLIANCE_FORMED,
    ALLIANCE_BROKEN
}

/**
 * Violation of diplomatic agreement
 */
data class DiplomaticViolation(
    val id: String,
    val relationshipId: String,
    val violatorId: String,
    val violatorName: String,
    val victimId: String,
    val victimName: String,
    val violationType: ViolationType,
    val timestamp: Instant,
    val penalty: DiplomaticPenalty
)

/**
 * Type of violation
 */
enum class ViolationType {
    ATTACKED_DURING_NAP,
    ATTACKED_ALLY,
    BROKE_TREATY_TERMS,
    RESOURCE_THEFT
}

/**
 * Penalty for violation
 */
data class DiplomaticPenalty(
    val reputationLoss: Int,
    val resourceFine: ResourceBundle? = null,
    val banDuration: Int? = null // Days banned from diplomacy
)

/**
 * Alliance reputation score
 */
data class AllianceReputation(
    val allianceId: String,
    val score: Int,
    val rank: ReputationRank,
    val treatiesHonored: Int,
    val treatiesBroken: Int,
    val violations: Int
) {
    val trustworthiness: Float
        get() = if (treatiesHonored + treatiesBroken == 0) 0.5f
                else treatiesHonored.toFloat() / (treatiesHonored + treatiesBroken)
}

/**
 * Reputation ranking
 */
enum class ReputationRank {
    HONORABLE,      // 90-100
    TRUSTWORTHY,    // 70-89
    NEUTRAL,        // 40-69
    UNRELIABLE,     // 20-39
    DISHONORABLE    // 0-19
}
