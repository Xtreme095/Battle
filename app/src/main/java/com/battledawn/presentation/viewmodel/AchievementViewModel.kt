package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.Achievement
import com.battledawn.domain.model.AchievementCategory
import com.battledawn.domain.model.AchievementTier
import com.battledawn.domain.model.Achievements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Achievement System
 * Tracks player achievements and rewards
 */
@HiltViewModel
class AchievementViewModel @Inject constructor(
    // TODO: Inject AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AchievementUiState>(AchievementUiState.Loading)
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<AchievementCategory?>(null)
    val selectedCategory: StateFlow<AchievementCategory?> = _selectedCategory.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val unlockedAchievements: StateFlow<List<Achievement>> = _unlockedAchievements.asStateFlow()

    private val _recentlyUnlocked = MutableStateFlow<Achievement?>(null)
    val recentlyUnlocked: StateFlow<Achievement?> = _recentlyUnlocked.asStateFlow()

    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    init {
        loadAchievements()
    }

    /**
     * Load all achievements
     */
    fun loadAchievements() {
        viewModelScope.launch {
            try {
                _uiState.value = AchievementUiState.Loading

                // TODO: Load player's achievement progress from API
                // For now, use all achievements with mock progress
                val allAchievements = Achievements.allAchievements

                _achievements.value = allAchievements

                // Filter unlocked achievements
                val unlocked = allAchievements.filter { it.isUnlocked }
                _unlockedAchievements.value = unlocked

                // Calculate total points
                _totalPoints.value = unlocked.sumOf { it.tier.points }

                _uiState.value = AchievementUiState.Success

                Timber.d("Loaded ${allAchievements.size} achievements, ${unlocked.size} unlocked")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load achievements")
                _uiState.value = AchievementUiState.Error("Failed to load achievements")
            }
        }
    }

    /**
     * Filter achievements by category
     */
    fun selectCategory(category: AchievementCategory?) {
        _selectedCategory.value = category
    }

    /**
     * Get filtered achievements
     */
    fun getFilteredAchievements(): List<Achievement> {
        val category = _selectedCategory.value
        return if (category != null) {
            _achievements.value.filter { it.category == category }
        } else {
            _achievements.value
        }
    }

    /**
     * Track achievement progress
     */
    fun trackProgress(achievementId: String, progress: Int) {
        viewModelScope.launch {
            try {
                val achievement = _achievements.value.find { it.id == achievementId }
                if (achievement == null) {
                    Timber.w("Achievement not found: $achievementId")
                    return@launch
                }

                // Check if achievement is now unlocked
                if (!achievement.isUnlocked && progress >= achievement.requirement.target) {
                    unlockAchievement(achievementId)
                }

                // TODO: Update progress in repository
                // achievementRepository.updateProgress(achievementId, progress)

            } catch (e: Exception) {
                Timber.e(e, "Failed to track achievement progress")
            }
        }
    }

    /**
     * Unlock an achievement
     */
    private fun unlockAchievement(achievementId: String) {
        viewModelScope.launch {
            try {
                val achievement = _achievements.value.find { it.id == achievementId }
                if (achievement == null) {
                    return@launch
                }

                // Mark as unlocked
                val unlockedAchievement = achievement.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )

                // Update achievements list
                val updatedList = _achievements.value.map {
                    if (it.id == achievementId) unlockedAchievement else it
                }
                _achievements.value = updatedList

                // Add to unlocked list
                _unlockedAchievements.value = _unlockedAchievements.value + unlockedAchievement

                // Update total points
                _totalPoints.value += achievement.tier.points

                // Show notification
                _recentlyUnlocked.value = unlockedAchievement

                // TODO: Save to repository
                // achievementRepository.unlockAchievement(achievementId)

                // TODO: Give rewards to player
                // rewardRepository.giveReward(achievement.reward)

                Timber.d("Achievement unlocked: ${achievement.name}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to unlock achievement")
            }
        }
    }

    /**
     * Clear recently unlocked notification
     */
    fun clearRecentlyUnlocked() {
        _recentlyUnlocked.value = null
    }

    /**
     * Get achievement statistics
     */
    fun getStatistics(): AchievementStatistics {
        val total = _achievements.value.size
        val unlocked = _unlockedAchievements.value.size
        val percentage = if (total > 0) (unlocked.toFloat() / total * 100).toInt() else 0

        return AchievementStatistics(
            totalAchievements = total,
            unlockedCount = unlocked,
            completionPercentage = percentage,
            totalPoints = _totalPoints.value,
            bronzeUnlocked = _unlockedAchievements.value.count { it.tier == AchievementTier.BRONZE },
            silverUnlocked = _unlockedAchievements.value.count { it.tier == AchievementTier.SILVER },
            goldUnlocked = _unlockedAchievements.value.count { it.tier == AchievementTier.GOLD },
            platinumUnlocked = _unlockedAchievements.value.count { it.tier == AchievementTier.PLATINUM },
            diamondUnlocked = _unlockedAchievements.value.count { it.tier == AchievementTier.DIAMOND }
        )
    }

    /**
     * Get achievements sorted by completion status
     */
    fun getSortedAchievements(): List<Achievement> {
        return _achievements.value.sortedWith(
            compareByDescending<Achievement> { it.isUnlocked }
                .thenBy { it.tier }
                .thenBy { it.name }
        )
    }
}

/**
 * UI State for Achievement Screen
 */
sealed class AchievementUiState {
    object Loading : AchievementUiState()
    object Success : AchievementUiState()
    data class Error(val message: String) : AchievementUiState()
}

/**
 * Achievement statistics
 */
data class AchievementStatistics(
    val totalAchievements: Int,
    val unlockedCount: Int,
    val completionPercentage: Int,
    val totalPoints: Int,
    val bronzeUnlocked: Int,
    val silverUnlocked: Int,
    val goldUnlocked: Int,
    val platinumUnlocked: Int,
    val diamondUnlocked: Int
)
