package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.Colony
import com.battledawn.domain.repository.ColonyRepository
import com.battledawn.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Map Screen
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val colonyRepository: ColonyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadPlayerColonies()
    }

    private fun loadPlayerColonies() {
        viewModelScope.launch {
            try {
                val player = playerRepository.getCurrentPlayer().first()
                if (player != null) {
                    colonyRepository.getPlayerColonies(player.id).collect { colonies ->
                        _uiState.value = MapUiState.Success(colonies)
                    }
                } else {
                    _uiState.value = MapUiState.Error("No player logged in")
                }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error(e.message ?: "Failed to load colonies")
            }
        }
    }

    fun refreshMap() {
        loadPlayerColonies()
    }
}

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val colonies: List<Colony>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}
