package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.Research
import com.battledawn.domain.model.TechnologyTemplates
import com.battledawn.domain.model.TechnologyType
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
 * ViewModel for Research Screen
 * Manages technology research and progression
 */
@HiltViewModel
class ResearchViewModel @Inject constructor(
    // TODO: Inject ResearchRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResearchUiState>(ResearchUiState.Loading)
    val uiState: StateFlow<ResearchUiState> = _uiState.asStateFlow()

    private val _currentResearch = MutableStateFlow<Research?>(null)
    val currentResearch: StateFlow<Research?> = _currentResearch.asStateFlow()

    // Temporary in-memory storage until repository is connected
    private val researchProgress = mutableMapOf<String, Research>()

    init {
        loadResearch()
    }

    /**
     * Load all research for current colony
     */
    fun loadResearch(colonyId: String = "default") {
        viewModelScope.launch {
            try {
                _uiState.value = ResearchUiState.Loading

                // Initialize default research levels
                val technologies = TechnologyType.values().map { techType ->
                    researchProgress.getOrPut("${colonyId}_${techType.name}") {
                        Research(
                            id = "${colonyId}_${techType.name}",
                            colonyId = colonyId,
                            techType = techType,
                            level = 0,
                            isResearching = false,
                            researchEndTime = null
                        )
                    }
                }

                _uiState.value = ResearchUiState.Success(
                    technologies = technologies,
                    availableTechnologies = getAvailableTechnologies(technologies)
                )

                // Update current research if any
                _currentResearch.value = technologies.firstOrNull { it.isResearching }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load research")
                _uiState.value = ResearchUiState.Error("Failed to load research data")
            }
        }
    }

    /**
     * Start researching a technology
     */
    fun startResearch(colonyId: String, techType: TechnologyType) {
        viewModelScope.launch {
            try {
                val key = "${colonyId}_${techType.name}"
                val existingResearch = researchProgress[key]

                if (existingResearch == null) {
                    _uiState.value = ResearchUiState.Error("Technology not found")
                    return@launch
                }

                // Check if already at max level
                val template = TechnologyTemplates.getTemplate(techType)
                if (existingResearch.level >= template.maxLevel) {
                    _uiState.value = ResearchUiState.Error("Technology already at max level")
                    return@launch
                }

                // Check if prerequisites are met
                val prerequisitesMet = checkPrerequisites(colonyId, techType)
                if (!prerequisitesMet) {
                    _uiState.value = ResearchUiState.Error("Prerequisites not met")
                    return@launch
                }

                // Check if already researching something
                if (_currentResearch.value != null) {
                    _uiState.value = ResearchUiState.Error("Already researching another technology")
                    return@launch
                }

                // Calculate research time
                val researchTime = template.getResearchTimeForLevel(existingResearch.level + 1)
                val endTime = Instant.now().plusSeconds(researchTime * 3600L)

                // Start research
                val updatedResearch = existingResearch.copy(
                    isResearching = true,
                    researchEndTime = endTime
                )

                researchProgress[key] = updatedResearch
                _currentResearch.value = updatedResearch

                // TODO: Call API to start research
                // researchRepository.startResearch(colonyId, techType)

                Timber.d("Started researching ${techType.name} (will complete at $endTime)")
                loadResearch(colonyId)

            } catch (e: Exception) {
                Timber.e(e, "Failed to start research")
                _uiState.value = ResearchUiState.Error("Failed to start research")
            }
        }
    }

    /**
     * Complete research (called when timer ends or by game tick)
     */
    fun completeResearch(colonyId: String, techType: TechnologyType) {
        viewModelScope.launch {
            try {
                val key = "${colonyId}_${techType.name}"
                val existingResearch = researchProgress[key]

                if (existingResearch == null || !existingResearch.isResearching) {
                    return@launch
                }

                // Complete research
                val completedResearch = existingResearch.copy(
                    level = existingResearch.level + 1,
                    isResearching = false,
                    researchEndTime = null
                )

                researchProgress[key] = completedResearch
                _currentResearch.value = null

                // TODO: Call API to complete research
                // researchRepository.completeResearch(colonyId, techType)

                Timber.d("Completed researching ${techType.name} to level ${completedResearch.level}")
                loadResearch(colonyId)

            } catch (e: Exception) {
                Timber.e(e, "Failed to complete research")
            }
        }
    }

    /**
     * Cancel ongoing research
     */
    fun cancelResearch(colonyId: String, techType: TechnologyType) {
        viewModelScope.launch {
            try {
                val key = "${colonyId}_${techType.name}"
                val existingResearch = researchProgress[key]

                if (existingResearch == null || !existingResearch.isResearching) {
                    return@launch
                }

                // Cancel research (optionally refund some resources)
                val cancelledResearch = existingResearch.copy(
                    isResearching = false,
                    researchEndTime = null
                )

                researchProgress[key] = cancelledResearch
                _currentResearch.value = null

                // TODO: Call API to cancel research
                // researchRepository.cancelResearch(colonyId, techType)

                Timber.d("Cancelled researching ${techType.name}")
                loadResearch(colonyId)

            } catch (e: Exception) {
                Timber.e(e, "Failed to cancel research")
            }
        }
    }

    /**
     * Get technologies available for research (prerequisites met)
     */
    private fun getAvailableTechnologies(technologies: List<Research>): List<TechnologyType> {
        return TechnologyType.values().filter { techType ->
            val research = technologies.find { it.techType == techType }
            val template = TechnologyTemplates.getTemplate(techType)

            // Not at max level and prerequisites met
            (research?.level ?: 0) < template.maxLevel &&
                    checkPrerequisites(research?.colonyId ?: "default", techType)
        }
    }

    /**
     * Check if prerequisites for a technology are met
     */
    private fun checkPrerequisites(colonyId: String, techType: TechnologyType): Boolean {
        val template = TechnologyTemplates.getTemplate(techType)
        val prerequisites = template.prerequisites

        return prerequisites.all { prereqType ->
            val prereqResearch = researchProgress["${colonyId}_${prereqType.name}"]
            (prereqResearch?.level ?: 0) > 0
        }
    }

    /**
     * Get research progress for a specific technology
     */
    fun getResearchLevel(colonyId: String, techType: TechnologyType): Int {
        val key = "${colonyId}_${techType.name}"
        return researchProgress[key]?.level ?: 0
    }

    /**
     * Check if can start researching (no other research in progress)
     */
    fun canStartResearch(): Boolean {
        return _currentResearch.value == null
    }
}

/**
 * UI State for Research Screen
 */
sealed class ResearchUiState {
    object Loading : ResearchUiState()
    data class Success(
        val technologies: List<Research>,
        val availableTechnologies: List<TechnologyType>
    ) : ResearchUiState()
    data class Error(val message: String) : ResearchUiState()
}
