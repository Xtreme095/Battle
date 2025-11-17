package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.data.remote.ChatMessageDto
import com.battledawn.data.remote.WebSocketManager
import com.battledawn.domain.model.Alliance
import com.battledawn.domain.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Alliance Screen
 * Manages alliance data, members, chat, and diplomacy
 */
@HiltViewModel
class AllianceViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager
    // TODO: Inject AllianceRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllianceUiState>(AllianceUiState.Loading)
    val uiState: StateFlow<AllianceUiState> = _uiState.asStateFlow()

    private val _currentAlliance = MutableStateFlow<Alliance?>(null)
    val currentAlliance: StateFlow<Alliance?> = _currentAlliance.asStateFlow()

    private val _allianceMembers = MutableStateFlow<List<Player>>(emptyList())
    val allianceMembers: StateFlow<List<Player>> = _allianceMembers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageDto>> = _chatMessages.asStateFlow()

    private val _availableAlliances = MutableStateFlow<List<Alliance>>(emptyList())
    val availableAlliances: StateFlow<List<Alliance>> = _availableAlliances.asStateFlow()

    init {
        loadPlayerAlliance()
        setupChatListener()
    }

    /**
     * Load player's current alliance
     */
    fun loadPlayerAlliance(playerId: String = "default") {
        viewModelScope.launch {
            try {
                _uiState.value = AllianceUiState.Loading

                // TODO: Call API to get player's alliance
                // val alliance = allianceRepository.getPlayerAlliance(playerId)

                // Temporary mock data
                val alliance: Alliance? = null // Will be loaded from API

                if (alliance != null) {
                    _currentAlliance.value = alliance
                    loadAllianceMembers(alliance.id)
                    loadAllianceChat(alliance.id)

                    // Subscribe to alliance updates via WebSocket
                    webSocketManager.subscribeToAlliance(alliance.id)

                    _uiState.value = AllianceUiState.InAlliance(alliance)
                } else {
                    // Player not in an alliance
                    loadAvailableAlliances()
                    _uiState.value = AllianceUiState.NoAlliance
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to load alliance")
                _uiState.value = AllianceUiState.Error("Failed to load alliance data")
            }
        }
    }

    /**
     * Load alliance members
     */
    private fun loadAllianceMembers(allianceId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call API to get alliance members
                // val members = allianceRepository.getAllianceMembers(allianceId)

                val members = emptyList<Player>() // Temporary
                _allianceMembers.value = members

                Timber.d("Loaded ${members.size} alliance members")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load alliance members")
            }
        }
    }

    /**
     * Load alliance chat history
     */
    private fun loadAllianceChat(allianceId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call API to get chat history
                // val messages = allianceRepository.getAllianceChat(allianceId, limit = 50)

                val messages = emptyList<ChatMessageDto>() // Temporary
                _chatMessages.value = messages

                Timber.d("Loaded ${messages.size} chat messages")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load chat messages")
            }
        }
    }

    /**
     * Load available alliances to join
     */
    fun loadAvailableAlliances() {
        viewModelScope.launch {
            try {
                // TODO: Call API to get all alliances
                // val alliances = allianceRepository.getAllAlliances(limit = 50)

                val alliances = emptyList<Alliance>() // Temporary
                _availableAlliances.value = alliances

                Timber.d("Loaded ${alliances.size} available alliances")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load available alliances")
            }
        }
    }

    /**
     * Create a new alliance
     */
    fun createAlliance(name: String, tag: String, description: String?) {
        viewModelScope.launch {
            try {
                // Validation
                when {
                    name.isBlank() -> {
                        _uiState.value = AllianceUiState.Error("Alliance name cannot be empty")
                        return@launch
                    }
                    name.length < 3 -> {
                        _uiState.value = AllianceUiState.Error("Alliance name must be at least 3 characters")
                        return@launch
                    }
                    tag.isBlank() -> {
                        _uiState.value = AllianceUiState.Error("Alliance tag cannot be empty")
                        return@launch
                    }
                    tag.length > 6 -> {
                        _uiState.value = AllianceUiState.Error("Alliance tag must be 6 characters or less")
                        return@launch
                    }
                }

                // TODO: Call API to create alliance
                // val alliance = allianceRepository.createAlliance(name, tag, description)

                Timber.d("Alliance created: $name [$tag]")
                loadPlayerAlliance()

            } catch (e: Exception) {
                Timber.e(e, "Failed to create alliance")
                _uiState.value = AllianceUiState.Error("Failed to create alliance")
            }
        }
    }

    /**
     * Join an alliance
     */
    fun joinAlliance(allianceId: String) {
        viewModelScope.launch {
            try {
                // TODO: Call API to join alliance
                // allianceRepository.joinAlliance(allianceId)

                Timber.d("Joined alliance: $allianceId")
                loadPlayerAlliance()

            } catch (e: Exception) {
                Timber.e(e, "Failed to join alliance")
                _uiState.value = AllianceUiState.Error("Failed to join alliance")
            }
        }
    }

    /**
     * Leave current alliance
     */
    fun leaveAlliance() {
        viewModelScope.launch {
            try {
                val alliance = _currentAlliance.value
                if (alliance == null) {
                    _uiState.value = AllianceUiState.Error("Not in an alliance")
                    return@launch
                }

                // TODO: Call API to leave alliance
                // allianceRepository.leaveAlliance(alliance.id)

                _currentAlliance.value = null
                _allianceMembers.value = emptyList()
                _chatMessages.value = emptyList()

                Timber.d("Left alliance: ${alliance.name}")
                _uiState.value = AllianceUiState.NoAlliance
                loadAvailableAlliances()

            } catch (e: Exception) {
                Timber.e(e, "Failed to leave alliance")
                _uiState.value = AllianceUiState.Error("Failed to leave alliance")
            }
        }
    }

    /**
     * Send chat message to alliance
     */
    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            try {
                val alliance = _currentAlliance.value
                if (alliance == null) {
                    Timber.w("Cannot send message: not in alliance")
                    return@launch
                }

                if (message.isBlank()) {
                    return@launch
                }

                // Send via WebSocket
                webSocketManager.sendAllianceMessage(alliance.id, message)

                Timber.d("Sent chat message to alliance ${alliance.id}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to send chat message")
            }
        }
    }

    /**
     * Setup listener for incoming chat messages
     */
    private fun setupChatListener() {
        viewModelScope.launch {
            webSocketManager.onAllianceMessage().collect { message ->
                // Add new message to list
                val currentMessages = _chatMessages.value.toMutableList()
                currentMessages.add(message)
                _chatMessages.value = currentMessages

                Timber.d("Received chat message from ${message.username}")
            }
        }
    }

    /**
     * Declare war on another alliance
     */
    fun declareWar(targetAllianceId: String) {
        viewModelScope.launch {
            try {
                val alliance = _currentAlliance.value
                if (alliance == null) {
                    _uiState.value = AllianceUiState.Error("Not in an alliance")
                    return@launch
                }

                // TODO: Call API to declare war
                // allianceRepository.declareWar(alliance.id, targetAllianceId)

                Timber.d("Declared war on alliance: $targetAllianceId")
                loadPlayerAlliance()

            } catch (e: Exception) {
                Timber.e(e, "Failed to declare war")
                _uiState.value = AllianceUiState.Error("Failed to declare war")
            }
        }
    }

    /**
     * Offer peace to another alliance
     */
    fun offerPeace(targetAllianceId: String) {
        viewModelScope.launch {
            try {
                val alliance = _currentAlliance.value
                if (alliance == null) {
                    _uiState.value = AllianceUiState.Error("Not in an alliance")
                    return@launch
                }

                // TODO: Call API to offer peace
                // allianceRepository.offerPeace(alliance.id, targetAllianceId)

                Timber.d("Offered peace to alliance: $targetAllianceId")
                loadPlayerAlliance()

            } catch (e: Exception) {
                Timber.e(e, "Failed to offer peace")
                _uiState.value = AllianceUiState.Error("Failed to offer peace")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup WebSocket subscriptions if needed
    }
}

/**
 * UI State for Alliance Screen
 */
sealed class AllianceUiState {
    object Loading : AllianceUiState()
    object NoAlliance : AllianceUiState()
    data class InAlliance(val alliance: Alliance) : AllianceUiState()
    data class Error(val message: String) : AllianceUiState()
}
