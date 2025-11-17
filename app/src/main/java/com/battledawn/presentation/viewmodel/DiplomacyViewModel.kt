package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Diplomacy management
 * Handles NAPs, peace treaties, alliances, and diplomatic relationships
 */
@HiltViewModel
class DiplomacyViewModel @Inject constructor(
    // TODO: Inject repositories when backend is ready
) : ViewModel() {

    private val _relationships = MutableStateFlow<List<DiplomaticRelationship>>(emptyList())
    val relationships: StateFlow<List<DiplomaticRelationship>> = _relationships.asStateFlow()

    private val _incomingProposals = MutableStateFlow<List<DiplomaticProposal>>(emptyList())
    val incomingProposals: StateFlow<List<DiplomaticProposal>> = _incomingProposals.asStateFlow()

    private val _sentProposals = MutableStateFlow<List<DiplomaticProposal>>(emptyList())
    val sentProposals: StateFlow<List<DiplomaticProposal>> = _sentProposals.asStateFlow()

    private val _diplomaticHistory = MutableStateFlow<List<DiplomaticHistory>>(emptyList())
    val diplomaticHistory: StateFlow<List<DiplomaticHistory>> = _diplomaticHistory.asStateFlow()

    private val _violations = MutableStateFlow<List<DiplomaticViolation>>(emptyList())
    val violations: StateFlow<List<DiplomaticViolation>> = _violations.asStateFlow()

    private val _allianceReputation = MutableStateFlow<AllianceReputation?>(null)
    val allianceReputation: StateFlow<AllianceReputation?> = _allianceReputation.asStateFlow()

    private val _uiState = MutableStateFlow<DiplomacyUiState>(DiplomacyUiState.Idle)
    val uiState: StateFlow<DiplomacyUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentAllianceId: String = ""
    private var currentAllianceName: String = ""

    init {
        // Load mock data for testing
        loadMockData()
    }

    /**
     * Initialize with current alliance
     */
    fun initialize(allianceId: String, allianceName: String) {
        currentAllianceId = allianceId
        currentAllianceName = allianceName
        loadDiplomaticRelationships()
        loadProposals()
        loadReputation()
    }

    /**
     * Load diplomatic relationships
     */
    private fun loadDiplomaticRelationships() {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                // TODO: Replace with API call
                // val response = diplomacyRepository.getRelationships(currentAllianceId)
                // _relationships.value = response

                _uiState.value = DiplomacyUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to load diplomatic relationships")
                _errorMessage.value = "Failed to load relationships: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Load diplomatic proposals
     */
    private fun loadProposals() {
        viewModelScope.launch {
            try {
                // TODO: Replace with API call
                // val incoming = diplomacyRepository.getIncomingProposals(currentAllianceId)
                // val sent = diplomacyRepository.getSentProposals(currentAllianceId)
                // _incomingProposals.value = incoming
                // _sentProposals.value = sent
            } catch (e: Exception) {
                Timber.e(e, "Failed to load proposals")
            }
        }
    }

    /**
     * Load alliance reputation
     */
    private fun loadReputation() {
        viewModelScope.launch {
            try {
                // TODO: Replace with API call
                // val reputation = diplomacyRepository.getReputation(currentAllianceId)
                // _allianceReputation.value = reputation
            } catch (e: Exception) {
                Timber.e(e, "Failed to load reputation")
            }
        }
    }

    /**
     * Propose Non-Aggression Pact
     */
    fun proposeNAP(
        targetAllianceId: String,
        targetAllianceName: String,
        duration: DiplomaticDuration,
        noAttackZones: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val terms = DiplomaticTerms(
                    duration = duration,
                    noAttackZones = noAttackZones,
                    resourceTribute = null,
                    sharedIntelligence = false,
                    mutualDefense = false
                )

                val proposal = DiplomaticProposal(
                    id = UUID.randomUUID().toString(),
                    proposerId = currentAllianceId,
                    proposerName = currentAllianceName,
                    targetId = targetAllianceId,
                    targetName = targetAllianceName,
                    proposalType = ProposalType.NON_AGGRESSION_PACT,
                    terms = terms,
                    status = ProposalStatus.PENDING,
                    createdAt = Instant.now(),
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                    respondedAt = null
                )

                // TODO: Replace with API call
                // diplomacyRepository.createProposal(proposal)

                _sentProposals.value = _sentProposals.value + proposal
                _uiState.value = DiplomacyUiState.Success

                Timber.d("NAP proposed to $targetAllianceName")
            } catch (e: Exception) {
                Timber.e(e, "Failed to propose NAP")
                _errorMessage.value = "Failed to propose NAP: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Propose Peace Treaty
     */
    fun proposePeaceTreaty(
        targetAllianceId: String,
        targetAllianceName: String,
        duration: DiplomaticDuration,
        resourceTribute: ResourceBundle? = null
    ) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val terms = DiplomaticTerms(
                    duration = duration,
                    noAttackZones = emptyList(),
                    resourceTribute = resourceTribute,
                    sharedIntelligence = false,
                    mutualDefense = false
                )

                val proposal = DiplomaticProposal(
                    id = UUID.randomUUID().toString(),
                    proposerId = currentAllianceId,
                    proposerName = currentAllianceName,
                    targetId = targetAllianceId,
                    targetName = targetAllianceName,
                    proposalType = ProposalType.PEACE_TREATY,
                    terms = terms,
                    status = ProposalStatus.PENDING,
                    createdAt = Instant.now(),
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                    respondedAt = null
                )

                // TODO: Replace with API call
                // diplomacyRepository.createProposal(proposal)

                _sentProposals.value = _sentProposals.value + proposal
                _uiState.value = DiplomacyUiState.Success

                Timber.d("Peace treaty proposed to $targetAllianceName")
            } catch (e: Exception) {
                Timber.e(e, "Failed to propose peace treaty")
                _errorMessage.value = "Failed to propose peace: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Propose Alliance
     */
    fun proposeAlliance(
        targetAllianceId: String,
        targetAllianceName: String,
        sharedIntelligence: Boolean,
        mutualDefense: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val terms = DiplomaticTerms(
                    duration = DiplomaticDuration.PERMANENT,
                    noAttackZones = emptyList(),
                    resourceTribute = null,
                    sharedIntelligence = sharedIntelligence,
                    mutualDefense = mutualDefense
                )

                val proposal = DiplomaticProposal(
                    id = UUID.randomUUID().toString(),
                    proposerId = currentAllianceId,
                    proposerName = currentAllianceName,
                    targetId = targetAllianceId,
                    targetName = targetAllianceName,
                    proposalType = ProposalType.ALLIANCE,
                    terms = terms,
                    status = ProposalStatus.PENDING,
                    createdAt = Instant.now(),
                    expiresAt = Instant.now().plus(14, ChronoUnit.DAYS),
                    respondedAt = null
                )

                // TODO: Replace with API call
                // diplomacyRepository.createProposal(proposal)

                _sentProposals.value = _sentProposals.value + proposal
                _uiState.value = DiplomacyUiState.Success

                Timber.d("Alliance proposed to $targetAllianceName")
            } catch (e: Exception) {
                Timber.e(e, "Failed to propose alliance")
                _errorMessage.value = "Failed to propose alliance: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Declare War
     */
    fun declareWar(targetAllianceId: String, targetAllianceName: String) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                // Check if there's an existing NAP or alliance
                val existingRelationship = _relationships.value.find {
                    (it.alliance1Id == targetAllianceId || it.alliance2Id == targetAllianceId) &&
                    (it.status == DiplomaticStatus.NON_AGGRESSION || it.status == DiplomaticStatus.ALLIED)
                }

                if (existingRelationship != null) {
                    // Breaking treaty - record violation
                    recordViolation(targetAllianceId, targetAllianceName, ViolationType.BROKE_TREATY_TERMS)
                }

                val warRelationship = DiplomaticRelationship(
                    id = UUID.randomUUID().toString(),
                    alliance1Id = currentAllianceId,
                    alliance1Name = currentAllianceName,
                    alliance2Id = targetAllianceId,
                    alliance2Name = targetAllianceName,
                    status = DiplomaticStatus.AT_WAR,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    expiresAt = null
                )

                // TODO: Replace with API call
                // diplomacyRepository.declareWar(warRelationship)

                // Update relationships
                _relationships.value = _relationships.value.filter {
                    it.alliance1Id != targetAllianceId && it.alliance2Id != targetAllianceId
                } + warRelationship

                // Add to history
                addToHistory(
                    targetAllianceId,
                    targetAllianceName,
                    DiplomaticAction.WAR_DECLARED,
                    "War declared by $currentAllianceName"
                )

                _uiState.value = DiplomacyUiState.Success

                Timber.d("War declared on $targetAllianceName")
            } catch (e: Exception) {
                Timber.e(e, "Failed to declare war")
                _errorMessage.value = "Failed to declare war: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Accept diplomatic proposal
     */
    fun acceptProposal(proposalId: String) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val proposal = _incomingProposals.value.find { it.id == proposalId }
                if (proposal == null) {
                    _errorMessage.value = "Proposal not found"
                    _uiState.value = DiplomacyUiState.Error("Proposal not found")
                    return@launch
                }

                // TODO: Replace with API call
                // diplomacyRepository.acceptProposal(proposalId)

                // Update proposal status
                val updatedProposal = proposal.copy(
                    status = ProposalStatus.ACCEPTED,
                    respondedAt = Instant.now()
                )
                _incomingProposals.value = _incomingProposals.value.map {
                    if (it.id == proposalId) updatedProposal else it
                }

                // Create relationship
                val status = when (proposal.proposalType) {
                    ProposalType.NON_AGGRESSION_PACT -> DiplomaticStatus.NON_AGGRESSION
                    ProposalType.PEACE_TREATY -> DiplomaticStatus.NEUTRAL
                    ProposalType.ALLIANCE -> DiplomaticStatus.ALLIED
                    ProposalType.WAR_DECLARATION -> DiplomaticStatus.AT_WAR
                }

                val expiresAt = if (proposal.terms.duration == DiplomaticDuration.PERMANENT) {
                    null
                } else {
                    Instant.now().plus(proposal.terms.duration.days.toLong(), ChronoUnit.DAYS)
                }

                val relationship = DiplomaticRelationship(
                    id = UUID.randomUUID().toString(),
                    alliance1Id = proposal.proposerId,
                    alliance1Name = proposal.proposerName,
                    alliance2Id = currentAllianceId,
                    alliance2Name = currentAllianceName,
                    status = status,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    expiresAt = expiresAt
                )

                _relationships.value = _relationships.value + relationship

                // Add to history
                val action = when (proposal.proposalType) {
                    ProposalType.NON_AGGRESSION_PACT -> DiplomaticAction.NAP_ACCEPTED
                    ProposalType.PEACE_TREATY -> DiplomaticAction.PEACE_ACCEPTED
                    ProposalType.ALLIANCE -> DiplomaticAction.ALLIANCE_FORMED
                    else -> DiplomaticAction.PEACE_ACCEPTED
                }
                addToHistory(proposal.proposerId, proposal.proposerName, action, "Accepted by $currentAllianceName")

                _uiState.value = DiplomacyUiState.Success

                Timber.d("Proposal accepted: ${proposal.proposalType}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to accept proposal")
                _errorMessage.value = "Failed to accept proposal: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Reject diplomatic proposal
     */
    fun rejectProposal(proposalId: String) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val proposal = _incomingProposals.value.find { it.id == proposalId }
                if (proposal == null) {
                    _errorMessage.value = "Proposal not found"
                    _uiState.value = DiplomacyUiState.Error("Proposal not found")
                    return@launch
                }

                // TODO: Replace with API call
                // diplomacyRepository.rejectProposal(proposalId)

                // Update proposal status
                val updatedProposal = proposal.copy(
                    status = ProposalStatus.REJECTED,
                    respondedAt = Instant.now()
                )
                _incomingProposals.value = _incomingProposals.value.map {
                    if (it.id == proposalId) updatedProposal else it
                }

                // Add to history
                val action = when (proposal.proposalType) {
                    ProposalType.NON_AGGRESSION_PACT -> DiplomaticAction.NAP_REJECTED
                    ProposalType.PEACE_TREATY -> DiplomaticAction.PEACE_REJECTED
                    else -> DiplomaticAction.PEACE_REJECTED
                }
                addToHistory(proposal.proposerId, proposal.proposerName, action, "Rejected by $currentAllianceName")

                _uiState.value = DiplomacyUiState.Success

                Timber.d("Proposal rejected: ${proposal.proposalType}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to reject proposal")
                _errorMessage.value = "Failed to reject proposal: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Cancel sent proposal
     */
    fun cancelProposal(proposalId: String) {
        viewModelScope.launch {
            try {
                // TODO: Replace with API call
                // diplomacyRepository.cancelProposal(proposalId)

                _sentProposals.value = _sentProposals.value.map {
                    if (it.id == proposalId) {
                        it.copy(status = ProposalStatus.CANCELLED)
                    } else it
                }

                Timber.d("Proposal cancelled: $proposalId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to cancel proposal")
                _errorMessage.value = "Failed to cancel proposal: ${e.message}"
            }
        }
    }

    /**
     * Break existing treaty/relationship
     */
    fun breakTreaty(relationshipId: String) {
        viewModelScope.launch {
            _uiState.value = DiplomacyUiState.Loading
            try {
                val relationship = _relationships.value.find { it.id == relationshipId }
                if (relationship == null) {
                    _errorMessage.value = "Relationship not found"
                    _uiState.value = DiplomacyUiState.Error("Relationship not found")
                    return@launch
                }

                // Record violation
                val otherAllianceId = if (relationship.alliance1Id == currentAllianceId) {
                    relationship.alliance2Id
                } else {
                    relationship.alliance1Id
                }
                val otherAllianceName = if (relationship.alliance1Id == currentAllianceId) {
                    relationship.alliance2Name
                } else {
                    relationship.alliance1Name
                }

                recordViolation(otherAllianceId, otherAllianceName, ViolationType.BROKE_TREATY_TERMS)

                // TODO: Replace with API call
                // diplomacyRepository.breakTreaty(relationshipId)

                // Remove relationship
                _relationships.value = _relationships.value.filter { it.id != relationshipId }

                // Add to history
                val action = when (relationship.status) {
                    DiplomaticStatus.NON_AGGRESSION -> DiplomaticAction.NAP_BROKEN
                    DiplomaticStatus.ALLIED -> DiplomaticAction.ALLIANCE_BROKEN
                    else -> DiplomaticAction.NAP_BROKEN
                }
                addToHistory(otherAllianceId, otherAllianceName, action, "Treaty broken by $currentAllianceName")

                _uiState.value = DiplomacyUiState.Success

                Timber.d("Treaty broken with $otherAllianceName")
            } catch (e: Exception) {
                Timber.e(e, "Failed to break treaty")
                _errorMessage.value = "Failed to break treaty: ${e.message}"
                _uiState.value = DiplomacyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Record diplomatic violation
     */
    private fun recordViolation(
        otherAllianceId: String,
        otherAllianceName: String,
        violationType: ViolationType
    ) {
        val violation = DiplomaticViolation(
            id = UUID.randomUUID().toString(),
            relationshipId = "",
            violatorId = currentAllianceId,
            violatorName = currentAllianceName,
            victimId = otherAllianceId,
            victimName = otherAllianceName,
            violationType = violationType,
            timestamp = Instant.now(),
            penalty = DiplomaticPenalty(
                reputationLoss = 50,
                resourceFine = null,
                banDuration = 7
            )
        )

        _violations.value = _violations.value + violation

        // Update reputation
        _allianceReputation.value?.let { rep ->
            _allianceReputation.value = rep.copy(
                score = (rep.score - 50).coerceAtLeast(0),
                treatiesBroken = rep.treatiesBroken + 1,
                violations = rep.violations + 1
            )
        }

        Timber.d("Violation recorded: $violationType")
    }

    /**
     * Add to diplomatic history
     */
    private fun addToHistory(
        otherAllianceId: String,
        otherAllianceName: String,
        action: DiplomaticAction,
        details: String
    ) {
        val historyEntry = DiplomaticHistory(
            id = UUID.randomUUID().toString(),
            alliance1Id = currentAllianceId,
            alliance1Name = currentAllianceName,
            alliance2Id = otherAllianceId,
            alliance2Name = otherAllianceName,
            action = action,
            timestamp = Instant.now(),
            initiatorId = currentAllianceId,
            details = details
        )

        _diplomaticHistory.value = listOf(historyEntry) + _diplomaticHistory.value
    }

    /**
     * Get relationship status with specific alliance
     */
    fun getRelationshipStatus(allianceId: String): DiplomaticStatus {
        val relationship = _relationships.value.find {
            it.alliance1Id == allianceId || it.alliance2Id == allianceId
        }
        return relationship?.status ?: DiplomaticStatus.NEUTRAL
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Load mock data for testing
     */
    private fun loadMockData() {
        viewModelScope.launch {
            // Mock relationships
            _relationships.value = listOf(
                DiplomaticRelationship(
                    id = "1",
                    alliance1Id = "alliance1",
                    alliance1Name = "Steel Legion",
                    alliance2Id = "alliance2",
                    alliance2Name = "Iron Brotherhood",
                    status = DiplomaticStatus.NON_AGGRESSION,
                    createdAt = Instant.now().minus(10, ChronoUnit.DAYS),
                    updatedAt = Instant.now().minus(10, ChronoUnit.DAYS),
                    expiresAt = Instant.now().plus(20, ChronoUnit.DAYS)
                ),
                DiplomaticRelationship(
                    id = "2",
                    alliance1Id = "alliance1",
                    alliance1Name = "Steel Legion",
                    alliance2Id = "alliance3",
                    alliance2Name = "Shadow Empire",
                    status = DiplomaticStatus.AT_WAR,
                    createdAt = Instant.now().minus(5, ChronoUnit.DAYS),
                    updatedAt = Instant.now().minus(5, ChronoUnit.DAYS),
                    expiresAt = null
                )
            )

            // Mock reputation
            _allianceReputation.value = AllianceReputation(
                allianceId = currentAllianceId,
                score = 75,
                rank = ReputationRank.TRUSTWORTHY,
                treatiesHonored = 8,
                treatiesBroken = 1,
                violations = 1
            )
        }
    }
}

/**
 * UI State for diplomacy screen
 */
sealed class DiplomacyUiState {
    object Idle : DiplomacyUiState()
    object Loading : DiplomacyUiState()
    object Success : DiplomacyUiState()
    data class Error(val message: String) : DiplomacyUiState()
}
