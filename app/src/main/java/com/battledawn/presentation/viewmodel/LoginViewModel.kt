package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.data.remote.WebSocketManager
import com.battledawn.data.repository.AuthRepository
import com.battledawn.data.repository.ColonyRepositoryImpl
import com.battledawn.domain.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Login and Registration screens
 * Handles authentication and initial data loading
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val colonyRepository: ColonyRepositoryImpl,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * Check if user is already logged in
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
            if (_isLoggedIn.value) {
                _uiState.value = LoginUiState.AlreadyLoggedIn
                connectWebSocket()
            }
        }
    }

    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            // Validation
            if (username.isBlank() || password.isBlank()) {
                _uiState.value = LoginUiState.Error("Username and password cannot be empty")
                return@launch
            }

            _uiState.value = LoginUiState.Loading

            val result = authRepository.login(username, password)

            result.fold(
                onSuccess = { player ->
                    Timber.d("Login successful: ${player.username}")
                    _isLoggedIn.value = true

                    // Sync player data
                    syncPlayerData()

                    // Connect WebSocket
                    connectWebSocket()

                    _uiState.value = LoginUiState.Success(player)
                },
                onFailure = { error ->
                    Timber.e(error, "Login failed")
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Login failed. Please try again."
                    )
                }
            )
        }
    }

    /**
     * Register new account
     */
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Validation
            when {
                username.isBlank() -> {
                    _uiState.value = LoginUiState.Error("Username cannot be empty")
                    return@launch
                }
                username.length < 3 -> {
                    _uiState.value = LoginUiState.Error("Username must be at least 3 characters")
                    return@launch
                }
                email.isBlank() -> {
                    _uiState.value = LoginUiState.Error("Email cannot be empty")
                    return@launch
                }
                !email.contains("@") -> {
                    _uiState.value = LoginUiState.Error("Invalid email format")
                    return@launch
                }
                password.isBlank() -> {
                    _uiState.value = LoginUiState.Error("Password cannot be empty")
                    return@launch
                }
                password.length < 6 -> {
                    _uiState.value = LoginUiState.Error("Password must be at least 6 characters")
                    return@launch
                }
                password != confirmPassword -> {
                    _uiState.value = LoginUiState.Error("Passwords do not match")
                    return@launch
                }
            }

            _uiState.value = LoginUiState.Loading

            val result = authRepository.register(username, email, password)

            result.fold(
                onSuccess = { player ->
                    Timber.d("Registration successful: ${player.username}")
                    _isLoggedIn.value = true

                    // Sync player data
                    syncPlayerData()

                    // Connect WebSocket
                    connectWebSocket()

                    _uiState.value = LoginUiState.Success(player)
                },
                onFailure = { error ->
                    Timber.e(error, "Registration failed")
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Registration failed. Please try again."
                    )
                }
            )
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            webSocketManager.disconnect()
            _isLoggedIn.value = false
            _uiState.value = LoginUiState.Initial
            Timber.d("User logged out")
        }
    }

    /**
     * Sync player data after login
     */
    private fun syncPlayerData() {
        viewModelScope.launch {
            try {
                // Sync all colonies
                colonyRepository.syncAllColonies()
                Timber.d("Player data synced successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync player data")
            }
        }
    }

    /**
     * Connect WebSocket for real-time updates
     */
    private fun connectWebSocket() {
        try {
            if (!webSocketManager.isConnected()) {
                webSocketManager.connect()
                Timber.d("WebSocket connected")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect WebSocket")
        }
    }

    /**
     * Reset error state
     */
    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Initial
        }
    }
}

/**
 * UI State for Login/Register screens
 */
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    object AlreadyLoggedIn : LoginUiState()
    data class Success(val player: Player) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
