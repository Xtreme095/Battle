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
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Trading System
 * Manages resource trading between players
 */
@HiltViewModel
class TradingViewModel @Inject constructor(
    // TODO: Inject TradingRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow<TradingUiState>(TradingUiState.Loading)
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private val _activeOffers = MutableStateFlow<List<TradeOffer>>(emptyList())
    val activeOffers: StateFlow<List<TradeOffer>> = _activeOffers.asStateFlow()

    private val _incomingOffers = MutableStateFlow<List<TradeOffer>>(emptyList())
    val incomingOffers: StateFlow<List<TradeOffer>> = _incomingOffers.asStateFlow()

    private val _tradeHistory = MutableStateFlow<List<TradeHistory>>(emptyList())
    val tradeHistory: StateFlow<List<TradeHistory>> = _tradeHistory.asStateFlow()

    private val _marketRates = MutableStateFlow(MarketRates())
    val marketRates: StateFlow<MarketRates> = _marketRates.asStateFlow()

    private val tradeLimits = TradeLimits()

    init {
        loadTrades()
    }

    /**
     * Load all trades for current player
     */
    fun loadTrades(playerId: String = "current_player") {
        viewModelScope.launch {
            try {
                _uiState.value = TradingUiState.Loading

                // TODO: Load from API
                // val offers = tradingRepository.getActiveOffers(playerId)
                // val incoming = tradingRepository.getIncomingOffers(playerId)
                // val history = tradingRepository.getTradeHistory(playerId)

                // Mock data for testing
                _activeOffers.value = generateMockActiveOffers()
                _incomingOffers.value = generateMockIncomingOffers()
                _tradeHistory.value = emptyList()

                _uiState.value = TradingUiState.Success

                Timber.d("Trades loaded: ${_activeOffers.value.size} active, ${_incomingOffers.value.size} incoming")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load trades")
                _uiState.value = TradingUiState.Error("Failed to load trades")
            }
        }
    }

    /**
     * Create a new trade offer
     */
    fun createTradeOffer(
        receiverUsername: String,
        offeredResources: ResourceBundle,
        requestedResources: ResourceBundle
    ) {
        viewModelScope.launch {
            try {
                // Validation
                when {
                    offeredResources.isEmpty() -> {
                        _uiState.value = TradingUiState.Error("You must offer something")
                        return@launch
                    }
                    requestedResources.isEmpty() -> {
                        _uiState.value = TradingUiState.Error("You must request something")
                        return@launch
                    }
                    receiverUsername.isBlank() -> {
                        _uiState.value = TradingUiState.Error("Enter a player username")
                        return@launch
                    }
                    _activeOffers.value.size >= tradeLimits.maxActiveOffers -> {
                        _uiState.value = TradingUiState.Error("Maximum ${tradeLimits.maxActiveOffers} active offers allowed")
                        return@launch
                    }
                }

                // Check if trade is fair
                val isFair = _marketRates.value.isFairTrade(offeredResources, requestedResources)
                if (!isFair) {
                    Timber.w("Trade may not be fair, but allowing it")
                }

                // TODO: Call API to create trade
                // val offer = tradingRepository.createOffer(receiverUsername, offeredResources, requestedResources)

                // Mock creation
                val offer = TradeOffer(
                    id = UUID.randomUUID().toString(),
                    senderId = "current_player",
                    senderUsername = "You",
                    receiverId = "receiver_id",
                    receiverUsername = receiverUsername,
                    offeredResources = offeredResources,
                    requestedResources = requestedResources,
                    status = TradeStatus.PENDING,
                    createdAt = Instant.now(),
                    expiresAt = Instant.now().plusSeconds(tradeLimits.offerDurationHours * 3600L)
                )

                _activeOffers.value = _activeOffers.value + offer
                _uiState.value = TradingUiState.TradeCreated(offer)

                Timber.d("Trade offer created: ${offer.id}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to create trade offer")
                _uiState.value = TradingUiState.Error("Failed to create trade offer")
            }
        }
    }

    /**
     * Accept a trade offer
     */
    fun acceptTradeOffer(offerId: String) {
        viewModelScope.launch {
            try {
                val offer = _incomingOffers.value.find { it.id == offerId }
                if (offer == null) {
                    _uiState.value = TradingUiState.Error("Trade offer not found")
                    return@launch
                }

                if (offer.status != TradeStatus.PENDING) {
                    _uiState.value = TradingUiState.Error("Trade is no longer available")
                    return@launch
                }

                // TODO: Call API to accept trade
                // tradingRepository.acceptOffer(offerId)

                // Update offer status
                val updatedOffer = offer.copy(
                    status = TradeStatus.ACCEPTED,
                    completedAt = Instant.now()
                )

                // Remove from incoming, add to history
                _incomingOffers.value = _incomingOffers.value.filter { it.id != offerId }

                _uiState.value = TradingUiState.TradeAccepted(updatedOffer)

                Timber.d("Trade accepted: $offerId")

            } catch (e: Exception) {
                Timber.e(e, "Failed to accept trade")
                _uiState.value = TradingUiState.Error("Failed to accept trade")
            }
        }
    }

    /**
     * Reject a trade offer
     */
    fun rejectTradeOffer(offerId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call API to reject trade
                // tradingRepository.rejectOffer(offerId)

                _incomingOffers.value = _incomingOffers.value.filter { it.id != offerId }

                Timber.d("Trade rejected: $offerId")

            } catch (e: Exception) {
                Timber.e(e, "Failed to reject trade")
                _uiState.value = TradingUiState.Error("Failed to reject trade")
            }
        }
    }

    /**
     * Cancel own trade offer
     */
    fun cancelTradeOffer(offerId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call API to cancel trade
                // tradingRepository.cancelOffer(offerId)

                _activeOffers.value = _activeOffers.value.filter { it.id != offerId }

                Timber.d("Trade cancelled: $offerId")

            } catch (e: Exception) {
                Timber.e(e, "Failed to cancel trade")
                _uiState.value = TradingUiState.Error("Failed to cancel trade")
            }
        }
    }

    /**
     * Calculate suggested trade based on market rates
     */
    fun getSuggestedTrade(offering: ResourceBundle): ResourceBundle {
        val offeredValue = _marketRates.value.calculateValue(offering)

        // Suggest roughly equal value in different resources
        return ResourceBundle(
            metal = (offeredValue * 0.4).toLong(),
            oil = (offeredValue * 0.3).toLong(),
            energy = (offeredValue * 0.2).toLong(),
            food = (offeredValue * 0.1).toLong()
        )
    }

    /**
     * Check if player can afford the offered resources
     */
    fun canAffordOffer(offered: ResourceBundle, playerResources: ResourcePool): Boolean {
        return playerResources.metal.amount >= offered.metal &&
               playerResources.oil.amount >= offered.oil &&
               playerResources.energy.amount >= offered.energy &&
               playerResources.food.amount >= offered.food
    }

    /**
     * Get trade fairness indicator
     */
    fun getTradeFairness(offered: ResourceBundle, requested: ResourceBundle): TradeFairness {
        val offeredValue = _marketRates.value.calculateValue(offered)
        val requestedValue = _marketRates.value.calculateValue(requested)

        if (offeredValue == 0.0 || requestedValue == 0.0) {
            return TradeFairness.UNKNOWN
        }

        val ratio = offeredValue / requestedValue

        return when {
            ratio > 1.3 -> TradeFairness.VERY_UNFAIR_TO_YOU
            ratio > 1.1 -> TradeFairness.UNFAIR_TO_YOU
            ratio in 0.9..1.1 -> TradeFairness.FAIR
            ratio < 0.7 -> TradeFairness.VERY_UNFAIR_TO_THEM
            else -> TradeFairness.UNFAIR_TO_THEM
        }
    }

    /**
     * Clear UI state
     */
    fun clearState() {
        if (_uiState.value is TradingUiState.Error ||
            _uiState.value is TradingUiState.TradeCreated ||
            _uiState.value is TradingUiState.TradeAccepted) {
            _uiState.value = TradingUiState.Success
        }
    }

    // Mock data generators
    private fun generateMockActiveOffers(): List<TradeOffer> {
        return listOf(
            TradeOffer(
                id = "1",
                senderId = "current_player",
                senderUsername = "You",
                receiverId = "player2",
                receiverUsername = "WarLord99",
                offeredResources = ResourceBundle(metal = 5000, oil = 3000),
                requestedResources = ResourceBundle(energy = 4000, food = 4000),
                status = TradeStatus.PENDING,
                createdAt = Instant.now().minusSeconds(3600),
                expiresAt = Instant.now().plusSeconds(20 * 3600)
            )
        )
    }

    private fun generateMockIncomingOffers(): List<TradeOffer> {
        return listOf(
            TradeOffer(
                id = "2",
                senderId = "player3",
                senderUsername = "TankMaster",
                receiverId = "current_player",
                receiverUsername = "You",
                offeredResources = ResourceBundle(energy = 6000, food = 2000),
                requestedResources = ResourceBundle(metal = 4000, oil = 4000),
                status = TradeStatus.PENDING,
                createdAt = Instant.now().minusSeconds(1800),
                expiresAt = Instant.now().plusSeconds(22 * 3600)
            )
        )
    }
}

/**
 * UI State for Trading Screen
 */
sealed class TradingUiState {
    object Loading : TradingUiState()
    object Success : TradingUiState()
    data class TradeCreated(val offer: TradeOffer) : TradingUiState()
    data class TradeAccepted(val offer: TradeOffer) : TradingUiState()
    data class Error(val message: String) : TradingUiState()
}

/**
 * Trade fairness indicator
 */
enum class TradeFairness {
    VERY_UNFAIR_TO_YOU,
    UNFAIR_TO_YOU,
    FAIR,
    UNFAIR_TO_THEM,
    VERY_UNFAIR_TO_THEM,
    UNKNOWN
}
