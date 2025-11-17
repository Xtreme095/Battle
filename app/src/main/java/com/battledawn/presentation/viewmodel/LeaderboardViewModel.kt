package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.Alliance
import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Leaderboard Screen
 * Displays rankings for players, alliances, and colonies
 */
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    // TODO: Inject LeaderboardRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(LeaderboardTab.PLAYERS)
    val selectedTab: StateFlow<LeaderboardTab> = _selectedTab.asStateFlow()

    private val _playerLeaderboard = MutableStateFlow<List<PlayerRanking>>(emptyList())
    val playerLeaderboard: StateFlow<List<PlayerRanking>> = _playerLeaderboard.asStateFlow()

    private val _allianceLeaderboard = MutableStateFlow<List<AllianceRanking>>(emptyList())
    val allianceLeaderboard: StateFlow<List<AllianceRanking>> = _allianceLeaderboard.asStateFlow()

    private val _colonyLeaderboard = MutableStateFlow<List<ColonyRanking>>(emptyList())
    val colonyLeaderboard: StateFlow<List<ColonyRanking>> = _colonyLeaderboard.asStateFlow()

    init {
        loadLeaderboards()
    }

    /**
     * Load all leaderboards
     */
    fun loadLeaderboards() {
        viewModelScope.launch {
            try {
                _uiState.value = LeaderboardUiState.Loading

                when (_selectedTab.value) {
                    LeaderboardTab.PLAYERS -> loadPlayerLeaderboard()
                    LeaderboardTab.ALLIANCES -> loadAllianceLeaderboard()
                    LeaderboardTab.COLONIES -> loadColonyLeaderboard()
                }

                _uiState.value = LeaderboardUiState.Success

            } catch (e: Exception) {
                Timber.e(e, "Failed to load leaderboards")
                _uiState.value = LeaderboardUiState.Error("Failed to load leaderboard data")
            }
        }
    }

    /**
     * Load player leaderboard
     */
    private suspend fun loadPlayerLeaderboard(limit: Int = 100) {
        try {
            // TODO: Call API to get player leaderboard
            // val players = leaderboardRepository.getPlayerLeaderboard(limit)

            // Generate mock data for now
            val players = generateMockPlayerRankings(limit)
            _playerLeaderboard.value = players

            Timber.d("Loaded player leaderboard: ${players.size} players")

        } catch (e: Exception) {
            Timber.e(e, "Failed to load player leaderboard")
        }
    }

    /**
     * Load alliance leaderboard
     */
    private suspend fun loadAllianceLeaderboard(limit: Int = 100) {
        try {
            // TODO: Call API to get alliance leaderboard
            // val alliances = leaderboardRepository.getAllianceLeaderboard(limit)

            // Generate mock data for now
            val alliances = generateMockAllianceRankings(limit)
            _allianceLeaderboard.value = alliances

            Timber.d("Loaded alliance leaderboard: ${alliances.size} alliances")

        } catch (e: Exception) {
            Timber.e(e, "Failed to load alliance leaderboard")
        }
    }

    /**
     * Load colony leaderboard
     */
    private suspend fun loadColonyLeaderboard(limit: Int = 100) {
        try {
            // TODO: Call API to get colony leaderboard
            // val colonies = leaderboardRepository.getColonyLeaderboard(limit)

            // Generate mock data for now
            val colonies = generateMockColonyRankings(limit)
            _colonyLeaderboard.value = colonies

            Timber.d("Loaded colony leaderboard: ${colonies.size} colonies")

        } catch (e: Exception) {
            Timber.e(e, "Failed to load colony leaderboard")
        }
    }

    /**
     * Switch leaderboard tab
     */
    fun selectTab(tab: LeaderboardTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
            loadLeaderboards()
        }
    }

    /**
     * Refresh leaderboards
     */
    fun refresh() {
        loadLeaderboards()
    }

    /**
     * Get player's rank
     */
    fun getPlayerRank(playerId: String): Int? {
        return _playerLeaderboard.value.find { it.playerId == playerId }?.rank
    }

    /**
     * Get alliance rank
     */
    fun getAllianceRank(allianceId: String): Int? {
        return _allianceLeaderboard.value.find { it.allianceId == allianceId }?.rank
    }

    // Temporary mock data generators

    private fun generateMockPlayerRankings(count: Int): List<PlayerRanking> {
        return (1..count).map { rank ->
            PlayerRanking(
                rank = rank,
                playerId = "player_$rank",
                username = "Player$rank",
                level = 50 - (rank / 2),
                experience = (100000 - rank * 1000).toLong(),
                victoriesCount = 100 - rank,
                allianceTag = if (rank % 3 == 0) "[TAG]" else null
            )
        }
    }

    private fun generateMockAllianceRankings(count: Int): List<AllianceRanking> {
        return (1..count).map { rank ->
            AllianceRanking(
                rank = rank,
                allianceId = "alliance_$rank",
                name = "Alliance $rank",
                tag = "[A$rank]",
                totalScore = (1000000 - rank * 10000).toLong(),
                memberCount = 50 - rank,
                territories = 100 - rank
            )
        }
    }

    private fun generateMockColonyRankings(count: Int): List<ColonyRanking> {
        return (1..count).map { rank ->
            ColonyRanking(
                rank = rank,
                colonyId = "colony_$rank",
                name = "Colony $rank",
                ownerUsername = "Player$rank",
                power = (500000 - rank * 5000).toLong(),
                buildingCount = 13 - (rank / 10),
                unitCount = 1000 - rank * 10
            )
        }
    }
}

/**
 * UI State for Leaderboard Screen
 */
sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    object Success : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}

/**
 * Leaderboard tabs
 */
enum class LeaderboardTab {
    PLAYERS,
    ALLIANCES,
    COLONIES
}

/**
 * Player ranking entry
 */
data class PlayerRanking(
    val rank: Int,
    val playerId: String,
    val username: String,
    val level: Int,
    val experience: Long,
    val victoriesCount: Int,
    val allianceTag: String? = null
)

/**
 * Alliance ranking entry
 */
data class AllianceRanking(
    val rank: Int,
    val allianceId: String,
    val name: String,
    val tag: String,
    val totalScore: Long,
    val memberCount: Int,
    val territories: Int
)

/**
 * Colony ranking entry
 */
data class ColonyRanking(
    val rank: Int,
    val colonyId: String,
    val name: String,
    val ownerUsername: String,
    val power: Long,
    val buildingCount: Int,
    val unitCount: Int
)
