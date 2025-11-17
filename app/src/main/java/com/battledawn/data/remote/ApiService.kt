package com.battledawn.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service Interface
 * Defines all REST API endpoints for the Battle Dawn backend
 */
interface ApiService {

    // ==================== Authentication ====================

    @POST(ApiConfig.Endpoints.LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST(ApiConfig.Endpoints.REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @GET(ApiConfig.Endpoints.GET_ME)
    suspend fun getCurrentUser(): Response<UserDto>

    // ==================== Colonies ====================

    @GET(ApiConfig.Endpoints.COLONIES)
    suspend fun getColonies(): Response<List<ColonyDto>>

    @GET(ApiConfig.Endpoints.COLONY_BY_ID)
    suspend fun getColony(
        @Path("id") colonyId: String
    ): Response<ColonyDto>

    @POST(ApiConfig.Endpoints.UPDATE_RESOURCES)
    suspend fun updateColonyResources(
        @Path("id") colonyId: String
    ): Response<ResourcesDto>

    // ==================== Buildings ====================

    @POST(ApiConfig.Endpoints.BUILDINGS)
    suspend fun startConstruction(
        @Body request: BuildingRequest
    ): Response<BuildingDto>

    @POST(ApiConfig.Endpoints.UPGRADE_BUILDING)
    suspend fun upgradeBuilding(
        @Path("id") buildingId: String
    ): Response<BuildingDto>

    @GET(ApiConfig.Endpoints.BUILDING_BY_ID)
    suspend fun getBuilding(
        @Path("id") buildingId: String
    ): Response<BuildingDto>

    @DELETE(ApiConfig.Endpoints.BUILDING_BY_ID)
    suspend fun demolishBuilding(
        @Path("id") buildingId: String
    ): Response<Unit>

    // ==================== Armies & Units ====================

    @GET("armies")
    suspend fun getArmies(
        @Query("colonyId") colonyId: String? = null
    ): Response<List<ArmyDto>>

    @GET("armies/{id}")
    suspend fun getArmy(
        @Path("id") armyId: String
    ): Response<ArmyDto>

    @POST("armies")
    suspend fun createArmy(
        @Body request: Map<String, String>
    ): Response<ArmyDto>

    @POST("units/train")
    suspend fun trainUnit(
        @Body request: TrainUnitRequest
    ): Response<UnitDto>

    @POST("armies/{id}/move")
    suspend fun moveArmy(
        @Path("id") armyId: String,
        @Body destination: Map<String, Int>
    ): Response<ArmyDto>

    // ==================== Battles ====================

    @POST(ApiConfig.Endpoints.INITIATE_ATTACK)
    suspend fun initiateAttack(
        @Body request: AttackRequest
    ): Response<BattleDto>

    @GET(ApiConfig.Endpoints.BATTLE_BY_ID)
    suspend fun getBattle(
        @Path("id") battleId: String
    ): Response<BattleDto>

    @GET("battles")
    suspend fun getBattleHistory(
        @Query("userId") userId: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<List<BattleDto>>

    // ==================== Alliances ====================

    @GET(ApiConfig.Endpoints.ALLIANCES)
    suspend fun getAlliances(
        @Query("limit") limit: Int = 50
    ): Response<List<AllianceDto>>

    @GET(ApiConfig.Endpoints.ALLIANCE_BY_ID)
    suspend fun getAlliance(
        @Path("id") allianceId: String
    ): Response<AllianceDto>

    @POST(ApiConfig.Endpoints.ALLIANCES)
    suspend fun createAlliance(
        @Body request: CreateAllianceRequest
    ): Response<AllianceDto>

    @POST(ApiConfig.Endpoints.JOIN_ALLIANCE)
    suspend fun joinAlliance(
        @Path("id") allianceId: String
    ): Response<AllianceDto>

    @POST(ApiConfig.Endpoints.LEAVE_ALLIANCE)
    suspend fun leaveAlliance(
        @Path("id") allianceId: String
    ): Response<Unit>

    @GET("alliances/{id}/members")
    suspend fun getAllianceMembers(
        @Path("id") allianceId: String
    ): Response<List<UserDto>>

    @GET("alliances/{id}/chat")
    suspend fun getAllianceChat(
        @Path("id") allianceId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessageDto>>

    // ==================== Research ====================

    @GET("research")
    suspend fun getResearch(
        @Query("colonyId") colonyId: String
    ): Response<List<ResearchDto>>

    @POST("research")
    suspend fun startResearch(
        @Body request: StartResearchRequest
    ): Response<ResearchDto>

    // ==================== Spy Missions ====================

    @POST("spy-missions")
    suspend fun launchSpyMission(
        @Body request: LaunchSpyMissionRequest
    ): Response<SpyMissionDto>

    @GET("spy-missions")
    suspend fun getSpyMissions(
        @Query("colonyId") colonyId: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<List<SpyMissionDto>>

    // ==================== Leaderboard ====================

    @GET("leaderboard/players")
    suspend fun getPlayerLeaderboard(
        @Query("limit") limit: Int = 100
    ): Response<List<UserDto>>

    @GET("leaderboard/alliances")
    suspend fun getAllianceLeaderboard(
        @Query("limit") limit: Int = 100
    ): Response<List<AllianceDto>>

    @GET("leaderboard/colonies")
    suspend fun getColonyLeaderboard(
        @Query("limit") limit: Int = 100
    ): Response<List<ColonyDto>>
}
