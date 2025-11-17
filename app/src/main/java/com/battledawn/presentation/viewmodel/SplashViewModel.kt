package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Splash Screen
 * Handles app initialization and determines navigation destination
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    init {
        initializeApp()
    }

    /**
     * Initialize application
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Step 1: Check authentication (20%)
                _loadingProgress.value = 0.2f
                delay(300)

                val isLoggedIn = authRepository.isLoggedIn()
                Timber.d("User logged in: $isLoggedIn")

                // Step 2: Load critical data (40%)
                _loadingProgress.value = 0.4f
                delay(300)

                // TODO: Pre-load critical game data
                // - Game configuration
                // - Asset preloading
                // - Database initialization

                // Step 3: Initialize services (60%)
                _loadingProgress.value = 0.6f
                delay(300)

                // TODO: Initialize game services
                // - Analytics
                // - Crash reporting
                // - Notification service

                // Step 4: Check for updates (80%)
                _loadingProgress.value = 0.8f
                delay(300)

                // TODO: Check for app updates
                // - Version check
                // - Force update if needed

                // Step 5: Complete (100%)
                _loadingProgress.value = 1.0f
                delay(300)

                // Determine navigation destination
                if (isLoggedIn) {
                    // Check if tutorial was completed
                    val tutorialCompleted = checkTutorialCompleted()

                    if (tutorialCompleted) {
                        _navigationState.value = SplashNavigationState.NavigateToGame
                    } else {
                        _navigationState.value = SplashNavigationState.NavigateToTutorial
                    }
                } else {
                    _navigationState.value = SplashNavigationState.NavigateToLogin
                }

                Timber.d("App initialization complete")

            } catch (e: Exception) {
                Timber.e(e, "App initialization failed")
                _navigationState.value = SplashNavigationState.Error("Failed to initialize app")
            }
        }
    }

    /**
     * Check if user has completed tutorial
     */
    private suspend fun checkTutorialCompleted(): Boolean {
        // TODO: Check from preferences/database
        // For now, assume tutorial is completed
        return true
    }

    /**
     * Retry initialization if error occurred
     */
    fun retry() {
        _navigationState.value = SplashNavigationState.Loading
        _loadingProgress.value = 0f
        initializeApp()
    }
}

/**
 * Navigation state for Splash Screen
 */
sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
    object NavigateToTutorial : SplashNavigationState()
    object NavigateToGame : SplashNavigationState()
    data class Error(val message: String) : SplashNavigationState()
}
