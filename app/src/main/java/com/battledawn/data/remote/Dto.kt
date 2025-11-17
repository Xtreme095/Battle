package com.battledawn.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects (DTOs) for API communication
 * These match the backend server's request/response formats
 */

// ==================== Auth DTOs ====================

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String
)

// ==================== User DTOs ====================

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("level") val level: Int,
    @SerializedName("experience") val experience: Long,
    @SerializedName("allianceId") val allianceId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("lastActiveAt") val lastActiveAt: String
)

// ==================== Colony DTOs ====================

data class ColonyDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("positionX") val positionX: Int,
    @SerializedName("positionY") val positionY: Int,
    @SerializedName("metalAmount") val metalAmount: Long,
    @SerializedName("metalProduction") val metalProduction: Double,
    @SerializedName("metalCapacity") val metalCapacity: Long,
    @SerializedName("oilAmount") val oilAmount: Long,
    @SerializedName("oilProduction") val oilProduction: Double,
    @SerializedName("oilCapacity") val oilCapacity: Long,
    @SerializedName("energyAmount") val energyAmount: Long,
    @SerializedName("energyProduction") val energyProduction: Double,
    @SerializedName("energyCapacity") val energyCapacity: Long,
    @SerializedName("foodAmount") val foodAmount: Long,
    @SerializedName("foodProduction") val foodProduction: Double,
    @SerializedName("foodCapacity") val foodCapacity: Long,
    @SerializedName("workersAmount") val workersAmount: Long,
    @SerializedName("workersCapacity") val workersCapacity: Long,
    @SerializedName("lastResourceUpdate") val lastResourceUpdate: String,
    @SerializedName("buildings") val buildings: List<BuildingDto>? = null,
    @SerializedName("armies") val armies: List<ArmyDto>? = null
)

data class ResourcesDto(
    @SerializedName("metalAmount") val metalAmount: Long,
    @SerializedName("oilAmount") val oilAmount: Long,
    @SerializedName("energyAmount") val energyAmount: Long,
    @SerializedName("foodAmount") val foodAmount: Long,
    @SerializedName("workersAmount") val workersAmount: Long
)

// ==================== Building DTOs ====================

data class BuildingDto(
    @SerializedName("id") val id: String,
    @SerializedName("colonyId") val colonyId: String,
    @SerializedName("type") val type: String,
    @SerializedName("level") val level: Int,
    @SerializedName("positionX") val positionX: Int,
    @SerializedName("positionY") val positionY: Int,
    @SerializedName("isConstructing") val isConstructing: Boolean,
    @SerializedName("constructionEndTime") val constructionEndTime: String?
)

data class BuildingRequest(
    @SerializedName("colonyId") val colonyId: String,
    @SerializedName("buildingType") val buildingType: String,
    @SerializedName("positionX") val positionX: Int,
    @SerializedName("positionY") val positionY: Int
)

// ==================== Army & Unit DTOs ====================

data class ArmyDto(
    @SerializedName("id") val id: String,
    @SerializedName("colonyId") val colonyId: String,
    @SerializedName("name") val name: String,
    @SerializedName("stance") val stance: String,
    @SerializedName("positionX") val positionX: Int?,
    @SerializedName("positionY") val positionY: Int?,
    @SerializedName("destinationX") val destinationX: Int?,
    @SerializedName("destinationY") val destinationY: Int?,
    @SerializedName("arrivalTime") val arrivalTime: String?,
    @SerializedName("units") val units: List<UnitDto>? = null
)

data class UnitDto(
    @SerializedName("id") val id: String,
    @SerializedName("armyId") val armyId: String,
    @SerializedName("type") val type: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("experience") val experience: Int,
    @SerializedName("health") val health: Int
)

data class TrainUnitRequest(
    @SerializedName("armyId") val armyId: String,
    @SerializedName("unitType") val unitType: String,
    @SerializedName("quantity") val quantity: Int
)

// ==================== Battle DTOs ====================

data class BattleDto(
    @SerializedName("id") val id: String,
    @SerializedName("attackerArmyId") val attackerArmyId: String,
    @SerializedName("defenderColonyId") val defenderColonyId: String,
    @SerializedName("status") val status: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("victor") val victor: String?,
    @SerializedName("battleData") val battleData: String?
)

data class AttackRequest(
    @SerializedName("attackerArmyId") val attackerArmyId: String,
    @SerializedName("defenderColonyId") val defenderColonyId: String
)

// ==================== Alliance DTOs ====================

data class AllianceDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("description") val description: String?,
    @SerializedName("leaderId") val leaderId: String,
    @SerializedName("memberCount") val memberCount: Int,
    @SerializedName("totalScore") val totalScore: Long,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateAllianceRequest(
    @SerializedName("name") val name: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("description") val description: String?
)

data class ChatMessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("allianceId") val allianceId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("message") val message: String,
    @SerializedName("createdAt") val createdAt: String
)

// ==================== Research DTOs ====================

data class ResearchDto(
    @SerializedName("id") val id: String,
    @SerializedName("colonyId") val colonyId: String,
    @SerializedName("techType") val techType: String,
    @SerializedName("level") val level: Int,
    @SerializedName("isResearching") val isResearching: Boolean,
    @SerializedName("researchEndTime") val researchEndTime: String?
)

data class StartResearchRequest(
    @SerializedName("colonyId") val colonyId: String,
    @SerializedName("techType") val techType: String
)

// ==================== Spy Mission DTOs ====================

data class SpyMissionDto(
    @SerializedName("id") val id: String,
    @SerializedName("attackerId") val attackerId: String,
    @SerializedName("targetColonyId") val targetColonyId: String,
    @SerializedName("missionType") val missionType: String,
    @SerializedName("spiesUsed") val spiesUsed: Int,
    @SerializedName("success") val success: Boolean,
    @SerializedName("result") val result: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class LaunchSpyMissionRequest(
    @SerializedName("targetColonyId") val targetColonyId: String,
    @SerializedName("missionType") val missionType: String,
    @SerializedName("spiesCount") val spiesCount: Int
)

// ==================== API Response Wrappers ====================

data class ApiResponse<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

data class ApiError(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: Int? = null
)
