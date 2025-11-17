package com.battledawn.data.repository

import com.battledawn.data.remote.ApiService
import com.battledawn.data.remote.LoginRequest
import com.battledawn.data.remote.RegisterRequest
import com.battledawn.data.remote.toDomain
import com.battledawn.di.TokenManager
import com.battledawn.domain.model.Player
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Authentication
 * Handles login, register, and token management
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Login with username and password
     * Returns the authenticated player and stores the auth token
     */
    suspend fun login(username: String, password: String): Result<Player> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Store auth token
                tokenManager.saveToken(authResponse.token)
                tokenManager.saveUserId(authResponse.user.id)

                val player = authResponse.user.toDomain()
                Timber.d("Login successful for user: ${player.username}")
                Result.success(player)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid username or password"
                    404 -> "User not found"
                    else -> "Login failed: ${response.message()}"
                }
                Timber.e("Login failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     * Returns the newly created player and stores the auth token
     */
    suspend fun register(username: String, email: String, password: String): Result<Player> {
        return try {
            val request = RegisterRequest(username, email, password)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Store auth token
                tokenManager.saveToken(authResponse.token)
                tokenManager.saveUserId(authResponse.user.id)

                val player = authResponse.user.toDomain()
                Timber.d("Registration successful for user: ${player.username}")
                Result.success(player)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Invalid registration data"
                    409 -> "Username or email already exists"
                    else -> "Registration failed: ${response.message()}"
                }
                Timber.e("Registration failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Result.failure(e)
        }
    }

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Result<Player> {
        return try {
            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body() != null) {
                val player = response.body()!!.toDomain()
                Timber.d("Fetched current user: ${player.username}")
                Result.success(player)
            } else {
                val errorMsg = "Failed to get current user: ${response.message()}"
                Timber.e(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current user")
            Result.failure(e)
        }
    }

    /**
     * Logout - clears auth token
     */
    suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearToken()
            Timber.d("User logged out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            Result.failure(e)
        }
    }

    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    /**
     * Get stored user ID
     */
    suspend fun getUserId(): String? {
        return tokenManager.getUserId()
    }

    /**
     * Get stored auth token
     */
    suspend fun getToken(): String? {
        return tokenManager.getToken()
    }
}
