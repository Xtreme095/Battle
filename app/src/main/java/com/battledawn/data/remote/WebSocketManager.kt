package com.battledawn.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import timber.log.Timber
import java.net.URISyntaxException

/**
 * WebSocket Manager using Socket.io
 * Handles real-time communication with the backend server
 */
class WebSocketManager(private val token: String) {

    private var socket: Socket? = null
    private var isConnected = false

    /**
     * Connect to the WebSocket server
     */
    fun connect() {
        try {
            val options = IO.Options().apply {
                // Authentication
                auth = mapOf("token" to token)

                // Connection settings
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = 5

                // Timeouts
                timeout = 10000
            }

            socket = IO.socket(ApiConfig.WEBSOCKET_URL, options)

            socket?.apply {
                // Connection events
                on(Socket.EVENT_CONNECT) {
                    isConnected = true
                    Timber.d("WebSocket connected successfully")
                }

                on(Socket.EVENT_DISCONNECT) {
                    isConnected = false
                    Timber.d("WebSocket disconnected")
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Timber.e("WebSocket connection error: ${args.firstOrNull()}")
                }

                on(Socket.EVENT_RECONNECT) { args ->
                    Timber.d("WebSocket reconnected after ${args.firstOrNull()} attempts")
                }

                // Connect
                connect()
            }
        } catch (e: URISyntaxException) {
            Timber.e(e, "Invalid WebSocket URL")
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect WebSocket")
        }
    }

    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        Timber.d("WebSocket disconnected")
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean = isConnected

    // ==================== Subscriptions ====================

    /**
     * Subscribe to colony updates
     */
    fun subscribeToColony(colonyId: String) {
        if (!isConnected) {
            Timber.w("Cannot subscribe to colony: not connected")
            return
        }
        socket?.emit(ApiConfig.Events.SUBSCRIBE_COLONY, colonyId)
        Timber.d("Subscribed to colony: $colonyId")
    }

    /**
     * Subscribe to alliance updates
     */
    fun subscribeToAlliance(allianceId: String) {
        if (!isConnected) {
            Timber.w("Cannot subscribe to alliance: not connected")
            return
        }
        socket?.emit(ApiConfig.Events.SUBSCRIBE_ALLIANCE, allianceId)
        Timber.d("Subscribed to alliance: $allianceId")
    }

    /**
     * Subscribe to battle updates
     */
    fun subscribeToBattle(battleId: String) {
        if (!isConnected) {
            Timber.w("Cannot subscribe to battle: not connected")
            return
        }
        socket?.emit(ApiConfig.Events.SUBSCRIBE_BATTLE, battleId)
        Timber.d("Subscribed to battle: $battleId")
    }

    // ==================== Outgoing Events ====================

    /**
     * Send alliance chat message
     */
    fun sendAllianceMessage(allianceId: String, message: String) {
        if (!isConnected) {
            Timber.w("Cannot send message: not connected")
            return
        }
        val data = JSONObject().apply {
            put("allianceId", allianceId)
            put("message", message)
        }
        socket?.emit(ApiConfig.Events.ALLIANCE_MESSAGE, data)
    }

    // ==================== Incoming Events (as Flows) ====================

    /**
     * Listen for resource updates
     */
    fun onResourcesUpdated(): Flow<ResourcesDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val resources = ResourcesDto(
                    metalAmount = data.getLong("metalAmount"),
                    oilAmount = data.getLong("oilAmount"),
                    energyAmount = data.getLong("energyAmount"),
                    foodAmount = data.getLong("foodAmount"),
                    workersAmount = data.getLong("workersAmount")
                )
                trySend(resources)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing resources update")
            }
        }

        socket?.on(ApiConfig.Events.RESOURCES_UPDATED, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.RESOURCES_UPDATED, listener)
        }
    }

    /**
     * Listen for construction completion
     */
    fun onConstructionComplete(): Flow<BuildingDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val building = BuildingDto(
                    id = data.getString("id"),
                    colonyId = data.getString("colonyId"),
                    type = data.getString("type"),
                    level = data.getInt("level"),
                    positionX = data.getInt("positionX"),
                    positionY = data.getInt("positionY"),
                    isConstructing = data.getBoolean("isConstructing"),
                    constructionEndTime = if (data.has("constructionEndTime"))
                        data.getString("constructionEndTime") else null
                )
                trySend(building)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing construction complete")
            }
        }

        socket?.on(ApiConfig.Events.CONSTRUCTION_COMPLETE, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.CONSTRUCTION_COMPLETE, listener)
        }
    }

    /**
     * Listen for training completion
     */
    fun onTrainingComplete(): Flow<UnitDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val unit = UnitDto(
                    id = data.getString("id"),
                    armyId = data.getString("armyId"),
                    type = data.getString("type"),
                    quantity = data.getInt("quantity"),
                    experience = data.getInt("experience"),
                    health = data.getInt("health")
                )
                trySend(unit)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing training complete")
            }
        }

        socket?.on(ApiConfig.Events.TRAINING_COMPLETE, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.TRAINING_COMPLETE, listener)
        }
    }

    /**
     * Listen for alliance chat messages
     */
    fun onAllianceMessage(): Flow<ChatMessageDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val message = ChatMessageDto(
                    id = data.getString("id"),
                    allianceId = data.getString("allianceId"),
                    userId = data.getString("userId"),
                    username = data.getString("username"),
                    message = data.getString("message"),
                    createdAt = data.getString("createdAt")
                )
                trySend(message)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing alliance message")
            }
        }

        socket?.on(ApiConfig.Events.ALLIANCE_MESSAGE_RECEIVED, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.ALLIANCE_MESSAGE_RECEIVED, listener)
        }
    }

    /**
     * Listen for incoming attacks
     */
    fun onAttackIncoming(): Flow<AttackNotification> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val notification = AttackNotification(
                    battleId = data.getString("battleId"),
                    attackerUsername = data.getString("attackerUsername"),
                    targetColonyId = data.getString("targetColonyId"),
                    arrivalTime = data.getString("arrivalTime")
                )
                trySend(notification)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing attack notification")
            }
        }

        socket?.on(ApiConfig.Events.ATTACK_INCOMING, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.ATTACK_INCOMING, listener)
        }
    }

    /**
     * Listen for battle updates
     */
    fun onBattleUpdated(): Flow<BattleDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val battle = BattleDto(
                    id = data.getString("id"),
                    attackerArmyId = data.getString("attackerArmyId"),
                    defenderColonyId = data.getString("defenderColonyId"),
                    status = data.getString("status"),
                    startTime = data.getString("startTime"),
                    endTime = if (data.has("endTime")) data.getString("endTime") else null,
                    victor = if (data.has("victor")) data.getString("victor") else null,
                    battleData = if (data.has("battleData")) data.getString("battleData") else null
                )
                trySend(battle)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing battle update")
            }
        }

        socket?.on(ApiConfig.Events.BATTLE_UPDATED, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.BATTLE_UPDATED, listener)
        }
    }

    /**
     * Listen for army arrival
     */
    fun onArmyArrived(): Flow<ArmyDto> = callbackFlow {
        val listener = { args: Array<Any> ->
            try {
                val data = args[0] as JSONObject
                val army = ArmyDto(
                    id = data.getString("id"),
                    colonyId = data.getString("colonyId"),
                    name = data.getString("name"),
                    stance = data.getString("stance"),
                    positionX = if (data.has("positionX")) data.getInt("positionX") else null,
                    positionY = if (data.has("positionY")) data.getInt("positionY") else null,
                    destinationX = null,
                    destinationY = null,
                    arrivalTime = null,
                    units = null
                )
                trySend(army)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing army arrival")
            }
        }

        socket?.on(ApiConfig.Events.ARMY_ARRIVED, listener)

        awaitClose {
            socket?.off(ApiConfig.Events.ARMY_ARRIVED, listener)
        }
    }
}

// ==================== Helper Data Classes ====================

data class AttackNotification(
    val battleId: String,
    val attackerUsername: String,
    val targetColonyId: String,
    val arrivalTime: String
)
