package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 * Manages app settings, notifications, audio, and account management
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
    // TODO: Inject SettingsRepository or PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Success)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Audio Settings
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.6f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    // Notification Settings
    private val _attackNotifications = MutableStateFlow(true)
    val attackNotifications: StateFlow<Boolean> = _attackNotifications.asStateFlow()

    private val _constructionNotifications = MutableStateFlow(true)
    val constructionNotifications: StateFlow<Boolean> = _constructionNotifications.asStateFlow()

    private val _allianceNotifications = MutableStateFlow(true)
    val allianceNotifications: StateFlow<Boolean> = _allianceNotifications.asStateFlow()

    // Display Settings
    private val _landscapeMode = MutableStateFlow(false)
    val landscapeMode: StateFlow<Boolean> = _landscapeMode.asStateFlow()

    private val _showGridLines = MutableStateFlow(true)
    val showGridLines: StateFlow<Boolean> = _showGridLines.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    // Account Info
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    init {
        loadSettings()
        loadUserInfo()
    }

    /**
     * Load all settings from storage
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                // TODO: Load from DataStore/SharedPreferences
                // val prefs = settingsRepository.getSettings()

                // For now, use defaults
                Timber.d("Settings loaded")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
            }
        }
    }

    /**
     * Load user account information
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getUserId()
                if (userId != null) {
                    val result = authRepository.getCurrentUser()
                    result.fold(
                        onSuccess = { player ->
                            _username.value = player.username
                            _email.value = player.email
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to load user info")
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user info")
            }
        }
    }

    // Audio Settings

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            _soundEnabled.value = enabled
            saveSettings()
            Timber.d("Sound ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            _musicEnabled.value = enabled
            saveSettings()
            Timber.d("Music ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun setSoundVolume(volume: Float) {
        viewModelScope.launch {
            _soundVolume.value = volume.coerceIn(0f, 1f)
            saveSettings()
        }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch {
            _musicVolume.value = volume.coerceIn(0f, 1f)
            saveSettings()
        }
    }

    // Notification Settings

    fun toggleAttackNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _attackNotifications.value = enabled
            saveSettings()
            Timber.d("Attack notifications ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun toggleConstructionNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _constructionNotifications.value = enabled
            saveSettings()
            Timber.d("Construction notifications ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun toggleAllianceNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _allianceNotifications.value = enabled
            saveSettings()
            Timber.d("Alliance notifications ${if (enabled) "enabled" else "disabled"}")
        }
    }

    // Display Settings

    fun toggleLandscapeMode(enabled: Boolean) {
        viewModelScope.launch {
            _landscapeMode.value = enabled
            saveSettings()
            Timber.d("Landscape mode ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun toggleGridLines(enabled: Boolean) {
        viewModelScope.launch {
            _showGridLines.value = enabled
            saveSettings()
        }
    }

    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch {
            _animationsEnabled.value = enabled
            saveSettings()
        }
    }

    // Account Management

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                // Validation
                when {
                    currentPassword.isBlank() -> {
                        _uiState.value = SettingsUiState.Error("Current password cannot be empty")
                        return@launch
                    }
                    newPassword.isBlank() -> {
                        _uiState.value = SettingsUiState.Error("New password cannot be empty")
                        return@launch
                    }
                    newPassword.length < 6 -> {
                        _uiState.value = SettingsUiState.Error("Password must be at least 6 characters")
                        return@launch
                    }
                    newPassword != confirmPassword -> {
                        _uiState.value = SettingsUiState.Error("Passwords do not match")
                        return@launch
                    }
                }

                _uiState.value = SettingsUiState.Loading

                // TODO: Call API to change password
                // authRepository.changePassword(currentPassword, newPassword)

                _uiState.value = SettingsUiState.PasswordChanged
                Timber.d("Password changed successfully")

            } catch (e: Exception) {
                Timber.e(e, "Failed to change password")
                _uiState.value = SettingsUiState.Error("Failed to change password")
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                if (!newEmail.contains("@")) {
                    _uiState.value = SettingsUiState.Error("Invalid email format")
                    return@launch
                }

                _uiState.value = SettingsUiState.Loading

                // TODO: Call API to update email
                // authRepository.updateEmail(newEmail)

                _email.value = newEmail
                _uiState.value = SettingsUiState.EmailUpdated
                Timber.d("Email updated successfully")

            } catch (e: Exception) {
                Timber.e(e, "Failed to update email")
                _uiState.value = SettingsUiState.Error("Failed to update email")
            }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            try {
                if (password.isBlank()) {
                    _uiState.value = SettingsUiState.Error("Password required")
                    return@launch
                }

                _uiState.value = SettingsUiState.Loading

                // TODO: Call API to delete account
                // authRepository.deleteAccount(password)

                _uiState.value = SettingsUiState.AccountDeleted
                Timber.d("Account deleted")

            } catch (e: Exception) {
                Timber.e(e, "Failed to delete account")
                _uiState.value = SettingsUiState.Error("Failed to delete account")
            }
        }
    }

    /**
     * Save settings to storage
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // TODO: Save to DataStore/SharedPreferences
                // settingsRepository.saveSettings(...)

                Timber.d("Settings saved")

            } catch (e: Exception) {
                Timber.e(e, "Failed to save settings")
            }
        }
    }

    /**
     * Reset to default settings
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _soundEnabled.value = true
            _musicEnabled.value = true
            _soundVolume.value = 0.8f
            _musicVolume.value = 0.6f
            _attackNotifications.value = true
            _constructionNotifications.value = true
            _allianceNotifications.value = true
            _landscapeMode.value = false
            _showGridLines.value = true
            _animationsEnabled.value = true

            saveSettings()
            Timber.d("Settings reset to defaults")
        }
    }

    /**
     * Clear cache/data
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                // TODO: Clear local database cache
                // databaseRepository.clearCache()

                _uiState.value = SettingsUiState.CacheCleared
                Timber.d("Cache cleared")

            } catch (e: Exception) {
                Timber.e(e, "Failed to clear cache")
                _uiState.value = SettingsUiState.Error("Failed to clear cache")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is SettingsUiState.Error) {
            _uiState.value = SettingsUiState.Success
        }
    }
}

/**
 * UI State for Settings Screen
 */
sealed class SettingsUiState {
    object Success : SettingsUiState()
    object Loading : SettingsUiState()
    object PasswordChanged : SettingsUiState()
    object EmailUpdated : SettingsUiState()
    object AccountDeleted : SettingsUiState()
    object CacheCleared : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
