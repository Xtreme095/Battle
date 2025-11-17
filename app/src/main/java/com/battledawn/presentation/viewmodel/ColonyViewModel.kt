package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.Building
import com.battledawn.domain.model.BuildingType
import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.GridPosition
import com.battledawn.domain.usecase.building.StartBuildingConstructionUseCase
import com.battledawn.domain.usecase.building.UpgradeBuildingUseCase
import com.battledawn.domain.usecase.colony.GetColonyUseCase
import com.battledawn.domain.usecase.colony.UpdateResourcesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Colony Screen
 */
@HiltViewModel
class ColonyViewModel @Inject constructor(
    private val getColonyUseCase: GetColonyUseCase,
    private val updateResourcesUseCase: UpdateResourcesUseCase,
    private val startBuildingConstructionUseCase: StartBuildingConstructionUseCase,
    private val upgradeBuildingUseCase: UpgradeBuildingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ColonyUiState>(ColonyUiState.Loading)
    val uiState: StateFlow<ColonyUiState> = _uiState.asStateFlow()

    fun loadColony(colonyId: String) {
        viewModelScope.launch {
            getColonyUseCase(colonyId).collect { colony ->
                if (colony != null) {
                    _uiState.value = ColonyUiState.Success(colony)
                    // Start resource updates
                    updateResources(colonyId)
                } else {
                    _uiState.value = ColonyUiState.Error("Colony not found")
                }
            }
        }
    }

    private fun updateResources(colonyId: String) {
        viewModelScope.launch {
            updateResourcesUseCase(colonyId)
        }
    }

    fun startConstruction(colonyId: String, buildingType: BuildingType, position: GridPosition) {
        viewModelScope.launch {
            startBuildingConstructionUseCase(colonyId, buildingType, position).fold(
                onSuccess = {
                    // Reload colony
                    loadColony(colonyId)
                },
                onFailure = { error ->
                    _uiState.value = ColonyUiState.Error(error.message ?: "Failed to start construction")
                }
            )
        }
    }

    fun upgradeBuilding(buildingId: String, colonyId: String) {
        viewModelScope.launch {
            upgradeBuildingUseCase(buildingId).fold(
                onSuccess = {
                    loadColony(colonyId)
                },
                onFailure = { error ->
                    _uiState.value = ColonyUiState.Error(error.message ?: "Failed to upgrade building")
                }
            )
        }
    }
}

sealed class ColonyUiState {
    object Loading : ColonyUiState()
    data class Success(val colony: Colony) : ColonyUiState()
    data class Error(val message: String) : ColonyUiState()
}
