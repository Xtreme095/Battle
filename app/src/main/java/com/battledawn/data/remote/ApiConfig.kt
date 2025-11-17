package com.battledawn.data.remote

/**
 * API Configuration
 *
 * Configure the base URL based on your environment:
 * - Android Emulator: Use 10.0.2.2 (maps to host machine's localhost)
 * - Physical Device: Use your computer's IP address on the same network
 * - Production: Use your production server URL with HTTPS
 */
object ApiConfig {
    // Development URLs
    const val BASE_URL_EMULATOR = "http://10.0.2.2:3000/api/"
    const val WEBSOCKET_URL_EMULATOR = "http://10.0.2.2:3000"

    // Change this IP to your computer's IP when using physical device
    const val BASE_URL_DEVICE = "http://192.168.1.100:3000/api/"
    const val WEBSOCKET_URL_DEVICE = "http://192.168.1.100:3000"

    // Production URLs (update when deploying)
    const val BASE_URL_PRODUCTION = "https://api.yourdomain.com/api/"
    const val WEBSOCKET_URL_PRODUCTION = "wss://api.yourdomain.com"

    // Current configuration - change based on environment
    const val BASE_URL = BASE_URL_EMULATOR
    const val WEBSOCKET_URL = WEBSOCKET_URL_EMULATOR

    // API Endpoints
    object Endpoints {
        // Auth
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val GET_ME = "auth/me"

        // Colonies
        const val COLONIES = "colonies"
        const val COLONY_BY_ID = "colonies/{id}"
        const val UPDATE_RESOURCES = "colonies/{id}/update-resources"

        // Buildings
        const val BUILDINGS = "buildings"
        const val BUILDING_BY_ID = "buildings/{id}"
        const val UPGRADE_BUILDING = "buildings/{id}/upgrade"

        // Battles
        const val INITIATE_ATTACK = "battles/attack"
        const val BATTLE_BY_ID = "battles/{id}"

        // Alliances
        const val ALLIANCES = "alliances"
        const val ALLIANCE_BY_ID = "alliances/{id}"
        const val JOIN_ALLIANCE = "alliances/{id}/join"
        const val LEAVE_ALLIANCE = "alliances/{id}/leave"
    }

    // WebSocket Events
    object Events {
        // Client → Server
        const val SUBSCRIBE_COLONY = "subscribe:colony"
        const val SUBSCRIBE_ALLIANCE = "subscribe:alliance"
        const val SUBSCRIBE_BATTLE = "subscribe:battle"
        const val ALLIANCE_MESSAGE = "alliance:message"

        // Server → Client
        const val RESOURCES_UPDATED = "resources:updated"
        const val CONSTRUCTION_COMPLETE = "construction:complete"
        const val TRAINING_COMPLETE = "training:complete"
        const val RESEARCH_COMPLETE = "research:complete"
        const val ALLIANCE_MESSAGE_RECEIVED = "alliance:message"
        const val ATTACK_INCOMING = "attack:incoming"
        const val BATTLE_UPDATED = "battle:updated"
        const val ARMY_ARRIVED = "army:arrived"
    }
}
