package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Tutorial System
 * Manages tutorial flow and completion tracking
 */
@HiltViewModel
class TutorialViewModel @Inject constructor(
    // TODO: Inject TutorialRepository/PreferencesManager
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _tutorialState = MutableStateFlow<TutorialState>(TutorialState.Active)
    val tutorialState: StateFlow<TutorialState> = _tutorialState.asStateFlow()

    private val _tutorialProgress = MutableStateFlow(0f)
    val tutorialProgress: StateFlow<Float> = _tutorialProgress.asStateFlow()

    val tutorialSteps = listOf(
        TutorialStep(
            id = 0,
            title = "Welcome to Battle Dawn!",
            description = "In this strategy game, you'll build colonies, train armies, and conquer territories.",
            action = "Get Started",
            highlightArea = null
        ),
        TutorialStep(
            id = 1,
            title = "Your Colony",
            description = "This is your colony. Here you'll build structures to produce resources and train units.",
            action = "View Colony",
            highlightArea = "colony_view"
        ),
        TutorialStep(
            id = 2,
            title = "Resources",
            description = "You have 5 resources: Metal, Oil, Energy, Food, and Workers. Build structures to produce more.",
            action = "Check Resources",
            highlightArea = "resource_bar"
        ),
        TutorialStep(
            id = 3,
            title = "Build Structures",
            description = "Tap on an empty plot to build. Start with a Metal Mine to produce metal.",
            action = "Build Metal Mine",
            highlightArea = "building_grid"
        ),
        TutorialStep(
            id = 4,
            title = "Train Units",
            description = "Build a Unit Production Facility to train units. Units are used for attacking and defending.",
            action = "Train Units",
            highlightArea = "units_tab"
        ),
        TutorialStep(
            id = 5,
            title = "Research Technology",
            description = "Research technologies to boost your economy and military strength.",
            action = "View Research",
            highlightArea = "research_button"
        ),
        TutorialStep(
            id = 6,
            title = "Join an Alliance",
            description = "Alliances provide protection and teamwork. Join one to chat and coordinate with other players.",
            action = "View Alliances",
            highlightArea = "alliance_button"
        ),
        TutorialStep(
            id = 7,
            title = "Explore the Map",
            description = "The world map shows all territories. Scout for resources and plan your expansion.",
            action = "Open Map",
            highlightArea = "map_button"
        ),
        TutorialStep(
            id = 8,
            title = "You're Ready!",
            description = "You've learned the basics! Continue building your empire and dominating the world!",
            action = "Start Playing",
            highlightArea = null
        )
    )

    init {
        updateProgress()
    }

    /**
     * Advance to next tutorial step
     */
    fun nextStep() {
        viewModelScope.launch {
            if (_currentStep.value < tutorialSteps.size - 1) {
                _currentStep.value += 1
                updateProgress()
                saveTutorialProgress()
                Timber.d("Tutorial step ${_currentStep.value}")
            } else {
                completeTutorial()
            }
        }
    }

    /**
     * Go back to previous step
     */
    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
            updateProgress()
        }
    }

    /**
     * Skip to specific step
     */
    fun skipToStep(step: Int) {
        if (step in tutorialSteps.indices) {
            _currentStep.value = step
            updateProgress()
        }
    }

    /**
     * Skip entire tutorial
     */
    fun skipTutorial() {
        viewModelScope.launch {
            _tutorialState.value = TutorialState.Skipped
            markTutorialComplete()
            Timber.d("Tutorial skipped")
        }
    }

    /**
     * Complete tutorial
     */
    private fun completeTutorial() {
        viewModelScope.launch {
            _tutorialState.value = TutorialState.Completed
            markTutorialComplete()
            Timber.d("Tutorial completed")
        }
    }

    /**
     * Restart tutorial
     */
    fun restartTutorial() {
        _currentStep.value = 0
        _tutorialState.value = TutorialState.Active
        updateProgress()
    }

    /**
     * Update tutorial progress
     */
    private fun updateProgress() {
        _tutorialProgress.value = _currentStep.value.toFloat() / tutorialSteps.size
    }

    /**
     * Save tutorial progress to storage
     */
    private fun saveTutorialProgress() {
        viewModelScope.launch {
            try {
                // TODO: Save to DataStore/SharedPreferences
                // preferencesManager.setTutorialStep(_currentStep.value)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save tutorial progress")
            }
        }
    }

    /**
     * Mark tutorial as complete
     */
    private fun markTutorialComplete() {
        viewModelScope.launch {
            try {
                // TODO: Save to DataStore/SharedPreferences
                // preferencesManager.setTutorialCompleted(true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark tutorial complete")
            }
        }
    }

    /**
     * Check if tutorial is completed
     */
    suspend fun isTutorialCompleted(): Boolean {
        // TODO: Load from DataStore/SharedPreferences
        // return preferencesManager.isTutorialCompleted()
        return false
    }

    /**
     * Get current step
     */
    fun getCurrentTutorialStep(): TutorialStep {
        return tutorialSteps[_currentStep.value]
    }

    /**
     * Check if on last step
     */
    fun isLastStep(): Boolean {
        return _currentStep.value == tutorialSteps.size - 1
    }

    /**
     * Check if on first step
     */
    fun isFirstStep(): Boolean {
        return _currentStep.value == 0
    }
}

/**
 * Tutorial state
 */
sealed class TutorialState {
    object Active : TutorialState()
    object Completed : TutorialState()
    object Skipped : TutorialState()
}

/**
 * Tutorial step data
 */
data class TutorialStep(
    val id: Int,
    val title: String,
    val description: String,
    val action: String,
    val highlightArea: String? // ID of UI element to highlight
)
