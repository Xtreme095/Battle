package com.battledawn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battledawn.domain.model.DailyReward
import com.battledawn.domain.model.DailyRewardStatus
import com.battledawn.domain.model.DailyRewards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Daily Rewards System
 * Manages daily login rewards and streak tracking
 */
@HiltViewModel
class DailyRewardViewModel @Inject constructor(
    // TODO: Inject DailyRewardRepository
) : ViewModel() {

    private val _rewardStatus = MutableStateFlow<DailyRewardStatus?>(null)
    val rewardStatus: StateFlow<DailyRewardStatus?> = _rewardStatus.asStateFlow()

    private val _claimState = MutableStateFlow<ClaimState>(ClaimState.Idle)
    val claimState: StateFlow<ClaimState> = _claimState.asStateFlow()

    init {
        loadRewardStatus()
    }

    /**
     * Load daily reward status
     */
    fun loadRewardStatus() {
        viewModelScope.launch {
            try {
                // TODO: Load from repository/preferences
                // val status = dailyRewardRepository.getStatus()

                // For now, create mock status
                val currentStreak = 3 // Mock data
                val currentDayReward = DailyRewards.getRewardForDay(currentStreak + 1)

                val status = DailyRewardStatus(
                    currentStreak = currentStreak,
                    longestStreak = 7,
                    totalDaysLoggedIn = 15,
                    lastClaimedDate = null, // Mock: can claim today
                    canClaimToday = true,
                    currentDayReward = currentDayReward
                )

                _rewardStatus.value = status
                Timber.d("Daily reward status loaded: streak = ${status.currentStreak}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to load daily reward status")
            }
        }
    }

    /**
     * Claim today's reward
     */
    fun claimReward() {
        viewModelScope.launch {
            try {
                val status = _rewardStatus.value
                if (status == null) {
                    _claimState.value = ClaimState.Error("Reward status not loaded")
                    return@launch
                }

                if (!status.canClaimToday) {
                    _claimState.value = ClaimState.Error("Already claimed today")
                    return@launch
                }

                _claimState.value = ClaimState.Claiming

                // TODO: Call API to claim reward
                // val result = dailyRewardRepository.claimReward()

                // Simulate claiming
                val reward = status.currentDayReward

                // Update status
                val newStreak = status.currentStreak + 1
                val newStatus = status.copy(
                    currentStreak = newStreak,
                    longestStreak = maxOf(status.longestStreak, newStreak),
                    totalDaysLoggedIn = status.totalDaysLoggedIn + 1,
                    lastClaimedDate = System.currentTimeMillis(),
                    canClaimToday = false,
                    currentDayReward = DailyRewards.getRewardForDay(newStreak + 1)
                )

                _rewardStatus.value = newStatus
                _claimState.value = ClaimState.Success(reward)

                // TODO: Give rewards to player
                // rewardRepository.giveResources(reward)

                Timber.d("Daily reward claimed: Day ${reward.day}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to claim daily reward")
                _claimState.value = ClaimState.Error("Failed to claim reward")
            }
        }
    }

    /**
     * Check if streak is broken
     */
    fun checkStreak() {
        viewModelScope.launch {
            try {
                val status = _rewardStatus.value ?: return@launch

                if (status.lastClaimedDate == null) {
                    return@launch
                }

                val lastClaimed = Calendar.getInstance().apply {
                    timeInMillis = status.lastClaimedDate
                }

                val today = Calendar.getInstance()

                // Check if more than 1 day has passed
                val daysDiff = ((today.timeInMillis - lastClaimed.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                if (daysDiff > 1) {
                    // Streak broken
                    val newStatus = status.copy(
                        currentStreak = 0,
                        canClaimToday = true
                    )
                    _rewardStatus.value = newStatus

                    Timber.w("Daily reward streak broken: $daysDiff days passed")
                } else if (daysDiff == 1) {
                    // Can claim today
                    val newStatus = status.copy(canClaimToday = true)
                    _rewardStatus.value = newStatus
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to check streak")
            }
        }
    }

    /**
     * Reset claim state
     */
    fun resetClaimState() {
        _claimState.value = ClaimState.Idle
    }

    /**
     * Get all weekly rewards for preview
     */
    fun getWeeklyRewards(): List<DailyReward> {
        return DailyRewards.weeklyRewards
    }

    /**
     * Get current streak bonus description
     */
    fun getStreakBonus(): String {
        val streak = _rewardStatus.value?.currentStreak ?: 0
        return when {
            streak >= 7 -> "Amazing! Keep it up! 🔥"
            streak >= 3 -> "Great streak! 🌟"
            streak >= 1 -> "Good start! 👍"
            else -> "Start your streak today!"
        }
    }
}

/**
 * Claim state
 */
sealed class ClaimState {
    object Idle : ClaimState()
    object Claiming : ClaimState()
    data class Success(val reward: DailyReward) : ClaimState()
    data class Error(val message: String) : ClaimState()
}
