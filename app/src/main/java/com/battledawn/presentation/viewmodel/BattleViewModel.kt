package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.data.remote.WebSocketManager
import com.battledawn.domain.model.Army
import com.battledawn.domain.model.BattleResult
import com.battledawn.domain.model.CombatEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Battle Screen
 * Manages battle initiation, viewing results, and battle history
 */
@HiltViewModel
class BattleViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager
    // TODO: Inject BattleRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleUiState>(BattleUiState.Initial)
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private val _currentBattle = MutableStateFlow<BattleResult?>(null)
    val currentBattle: StateFlow<BattleResult?> = _currentBattle.asStateFlow()

    private val _battleHistory = MutableStateFlow<List<BattleHistoryItem>>(emptyList())
    val battleHistory: StateFlow<List<BattleHistoryItem>> = _battleHistory.asStateFlow()

    private val _ongoingBattles = MutableStateFlow<List<OngoingBattle>>(emptyList())
    val ongoingBattles: StateFlow<List<OngoingBattle>> = _ongoingBattles.asStateFlow()

    init {
        setupBattleListener()
        loadBattleHistory()
    }

    /**
     * Initiate an attack
     */
    fun initiateAttack(attackerArmyId: String, defenderColonyId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BattleUiState.Loading

                // TODO: Call API to initiate attack
                // val battle = battleRepository.initiateAttack(attackerArmyId, defenderColonyId)

                Timber.d("Attack initiated: $attackerArmyId -> $defenderColonyId")
                _uiState.value = BattleUiState.AttackInitiated

                // Reload ongoing battles
                loadOngoingBattles()

            } catch (e: Exception) {
                Timber.e(e, "Failed to initiate attack")
                _uiState.value = BattleUiState.Error("Failed to initiate attack")
            }
        }
    }

    /**
     * Simulate battle locally (for preview/testing)
     */
    fun simulateBattle(attackerArmy: Army, defenderArmy: Army, defenseBonus: Int = 0) {
        viewModelScope.launch {
            try {
                _uiState.value = BattleUiState.Loading

                // Run combat simulation
                val result = CombatEngine.simulateBattle(attackerArmy, defenderArmy, defenseBonus)

                _currentBattle.value = result
                _uiState.value = BattleUiState.BattleComplete(result)

                Timber.d("Battle simulated: ${result.victor}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to simulate battle")
                _uiState.value = BattleUiState.Error("Battle simulation failed")
            }
        }
    }

    /**
     * Load specific battle details
     */
    fun loadBattle(battleId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = BattleUiState.Loading

                // TODO: Call API to get battle details
                // val battle = battleRepository.getBattle(battleId)

                // For now, use mock data
                // _currentBattle.value = battle

                _uiState.value = BattleUiState.Error("Battle not found")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load battle")
                _uiState.value = BattleUiState.Error("Failed to load battle")
            }
        }
    }

    /**
     * Load battle history for current player
     */
    fun loadBattleHistory(playerId: String = "default", limit: Int = 20) {
        viewModelScope.launch {
            try {
                // TODO: Call API to get battle history
                // val history = battleRepository.getBattleHistory(playerId, limit)

                val history = emptyList<BattleHistoryItem>() // Temporary
                _battleHistory.value = history

                Timber.d("Loaded ${history.size} battle history entries")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load battle history")
            }
        }
    }

    /**
     * Load ongoing battles (armies in transit, battles in progress)
     */
    fun loadOngoingBattles(playerId: String = "default") {
        viewModelScope.launch {
            try {
                // TODO: Call API to get ongoing battles
                // val battles = battleRepository.getOngoingBattles(playerId)

                val battles = emptyList<OngoingBattle>() // Temporary
                _ongoingBattles.value = battles

                Timber.d("Loaded ${battles.size} ongoing battles")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load ongoing battles")
            }
        }
    }

    /**
     * Setup listener for battle updates via WebSocket
     */
    private fun setupBattleListener() {
        viewModelScope.launch {
            // Listen for battle updates
            webSocketManager.onBattleUpdated().collect { battleDto ->
                Timber.d("Battle updated: ${battleDto.id}")
                // Reload battle details
                loadBattle(battleDto.id)
            }
        }

        viewModelScope.launch {
            // Listen for incoming attacks
            webSocketManager.onAttackIncoming().collect { attack ->
                Timber.w("Incoming attack! Battle: ${attack.battleId}")
                // Show notification or update UI
                loadOngoingBattles()
            }
        }
    }

    /**
     * Subscribe to specific battle updates
     */
    fun subscribeToBattle(battleId: String) {
        webSocketManager.subscribeToBattle(battleId)
    }

    /**
     * Get battle statistics for player
     */
    fun getBattleStats(playerId: String = "default"): BattleStats {
        // TODO: Calculate from battle history
        return BattleStats(
            totalBattles = _battleHistory.value.size,
            victories = _battleHistory.value.count { it.isVictory },
            defeats = _battleHistory.value.count { !it.isVictory },
            ongoingBattles = _ongoingBattles.value.size
        )
    }

    /**
     * Clear current battle view
     */
    fun clearCurrentBattle() {
        _currentBattle.value = null
        _uiState.value = BattleUiState.Initial
    }
}

/**
 * UI State for Battle Screen
 */
sealed class BattleUiState {
    object Initial : BattleUiState()
    object Loading : BattleUiState()
    object AttackInitiated : BattleUiState()
    data class BattleComplete(val result: BattleResult) : BattleUiState()
    data class Error(val message: String) : BattleUiState()
}

/**
 * Battle history item
 */
data class BattleHistoryItem(
    val battleId: String,
    val opponentName: String,
    val isVictory: Boolean,
    val timestamp: Long,
    val plunderedResources: Long = 0,
    val unitsLost: Int = 0
)

/**
 * Ongoing battle
 */
data class OngoingBattle(
    val battleId: String,
    val opponentName: String,
    val isAttacker: Boolean,
    val arrivalTime: Long?,
    val status: String // "IN_TRANSIT", "IN_PROGRESS", "COMPLETED"
)

/**
 * Battle statistics
 */
data class BattleStats(
    val totalBattles: Int,
    val victories: Int,
    val defeats: Int,
    val ongoingBattles: Int
) {
    val winRate: Float
        get() = if (totalBattles > 0) victories.toFloat() / totalBattles else 0f
}
