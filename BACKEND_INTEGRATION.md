# Backend Integration Guide

This document explains how to connect the Android client to the backend server.

## Quick Start

### 1. Start the Backend Server

```bash
cd backend

# Using Docker (Recommended)
docker-compose up -d

# Or manually
npm install
npx prisma migrate dev
npm run dev
```

Server will start on `http://localhost:3000`

### 2. Configure Android Client

Update `app/src/main/java/com/battledawn/data/remote/ApiConfig.kt`:

```kotlin
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:3000/api/" // Android emulator
    // OR
    const val BASE_URL = "http://YOUR_IP:3000/api/" // Physical device

    const val WEBSOCKET_URL = "http://10.0.2.2:3000"
}
```

### 3. Add Retrofit API Service

Create `app/src/main/java/com/battledawn/data/remote/ApiService.kt`:

```kotlin
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body userData: RegisterRequest): Response<LoginResponse>

    @GET("colonies")
    suspend fun getColonies(): Response<List<ColonyDto>>

    @GET("colonies/{id}")
    suspend fun getColony(@Path("id") id: String): Response<ColonyDto>

    @POST("buildings")
    suspend fun startConstruction(@Body request: BuildingRequest): Response<BuildingDto>

    @POST("battles/attack")
    suspend fun initiateAttack(@Body request: AttackRequest): Response<BattleDto>
}
```

### 4. Add WebSocket Support

Add to `build.gradle.kts`:

```kotlin
implementation("io.socket:socket.io-client:2.1.0")
```

Create `WebSocketManager.kt`:

```kotlin
class WebSocketManager(private val token: String) {
    private var socket: Socket? = null

    fun connect() {
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
        }

        socket = IO.socket("http://10.0.2.2:3000", options)

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("WebSocket", "Connected")
        }

        socket?.on("resources:updated") { args ->
            // Handle resource update
        }

        socket?.on("construction:complete") { args ->
            // Handle construction complete
        }

        socket?.connect()
    }

    fun subscribeToColony(colonyId: String) {
        socket?.emit("subscribe:colony", colonyId)
    }

    fun disconnect() {
        socket?.disconnect()
    }
}
```

## API Endpoints

### Authentication

**Register**
```
POST /api/auth/register
Body: { username, email, password }
Response: { user, token }
```

**Login**
```
POST /api/auth/login
Body: { username, password }
Response: { user, token }
```

**Get Current User**
```
GET /api/auth/me
Headers: Authorization: Bearer <token>
Response: { id, username, email, level, ... }
```

### Colonies

**Get All Colonies**
```
GET /api/colonies
Headers: Authorization: Bearer <token>
Response: [{ id, name, resources, buildings, ... }]
```

**Get Specific Colony**
```
GET /api/colonies/:id
Headers: Authorization: Bearer <token>
Response: { id, name, resources, buildings, armies, ... }
```

**Update Resources**
```
POST /api/colonies/:id/update-resources
Headers: Authorization: Bearer <token>
Response: { metalAmount, oilAmount, ... }
```

### Buildings

**Start Construction**
```
POST /api/buildings
Headers: Authorization: Bearer <token>
Body: { colonyId, buildingType, positionX, positionY }
Response: { id, type, level, constructionEndTime, ... }
```

**Upgrade Building**
```
POST /api/buildings/:id/upgrade
Headers: Authorization: Bearer <token>
Response: { id, level, constructionEndTime, ... }
```

### Battles

**Initiate Attack**
```
POST /api/battles/attack
Headers: Authorization: Bearer <token>
Body: { attackerArmyId, defenderColonyId }
Response: { id, status, startTime, ... }
```

**Get Battle**
```
GET /api/battles/:id
Headers: Authorization: Bearer <token>
Response: { id, victor, battleData, ... }
```

## WebSocket Events

### Client → Server

```kotlin
// Subscribe to colony updates
socket.emit("subscribe:colony", colonyId)

// Subscribe to alliance chat
socket.emit("subscribe:alliance", allianceId)

// Send alliance message
socket.emit("alliance:message", mapOf(
    "allianceId" to allianceId,
    "message" to "Hello!"
))

// Subscribe to battle updates
socket.emit("subscribe:battle", battleId)
```

### Server → Client

```kotlin
// Resource updates
socket.on("resources:updated") { args ->
    val data = args[0] as JSONObject
    val metalAmount = data.getString("metalAmount")
    // Update UI
}

// Construction complete
socket.on("construction:complete") { args ->
    val building = args[0] as JSONObject
    // Show notification
}

// Training complete
socket.on("training:complete") { args ->
    // Unit training finished
}

// Alliance message
socket.on("alliance:message") { args ->
    val message = args[0] as JSONObject
    // Display in chat
}

// Attack notification
socket.on("attack:incoming") { args ->
    // Colony under attack!
}

// Battle updated
socket.on("battle:updated") { args ->
    // Battle state changed
}
```

## Data Models (DTOs)

```kotlin
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val user: UserDto,
    val token: String
)

@Serializable
data class ColonyDto(
    val id: String,
    val name: String,
    val metalAmount: Long,
    val oilAmount: Long,
    val energyAmount: Long,
    val foodAmount: Long,
    val buildings: List<BuildingDto>,
    val armies: List<ArmyDto>
)

@Serializable
data class BuildingRequest(
    val colonyId: String,
    val buildingType: String,
    val positionX: Int,
    val positionY: Int
)

@Serializable
data class AttackRequest(
    val attackerArmyId: String,
    val defenderColonyId: String
)
```

## Error Handling

All API responses follow this format:

**Success**
```json
{
  "data": { ... }
}
```

**Error**
```json
{
  "error": "Error message",
  "stack": "..." // Only in development
}
```

Handle errors in repository:

```kotlin
try {
    val response = apiService.getColonies()
    if (response.isSuccessful) {
        Result.success(response.body()!!)
    } else {
        Result.failure(Exception(response.errorBody()?.string()))
    }
} catch (e: Exception) {
    Result.failure(e)
}
```

## Testing

### Test Backend Connection

```kotlin
// In your repository or ViewModel
suspend fun testConnection() {
    try {
        val response = apiService.getColonies()
        if (response.isSuccessful) {
            Log.d("API", "Connection successful!")
        }
    } catch (e: Exception) {
        Log.e("API", "Connection failed", e)
    }
}
```

### Test WebSocket

```kotlin
socket.on(Socket.EVENT_CONNECT) {
    Log.d("WebSocket", "Connected successfully!")
}

socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
    Log.e("WebSocket", "Connection error: ${args[0]}")
}
```

## Production Deployment

### Backend

1. Set environment variables in production
2. Use proper JWT secret
3. Enable HTTPS
4. Configure CORS for your domain
5. Set up database backups

### Android

1. Update BASE_URL to production server
2. Use HTTPS for API calls
3. Use WSS for WebSocket
4. Add certificate pinning for security

```kotlin
object ApiConfig {
    const val BASE_URL = "https://api.yourdomain.com/api/"
    const val WEBSOCKET_URL = "wss://api.yourdomain.com"
}
```

## Troubleshooting

**Can't connect from Android emulator**
- Use `10.0.2.2` instead of `localhost`
- Ensure backend is running on `0.0.0.0:3000`

**Can't connect from physical device**
- Use your computer's IP address
- Ensure device is on same network
- Check firewall settings

**WebSocket not connecting**
- Check authentication token
- Verify WebSocket URL
- Check browser console for errors

**401 Unauthorized**
- Check if token is included in headers
- Verify token hasn't expired
- Re-login to get new token

## Next Steps

1. Implement remaining repository methods
2. Connect ViewModels to API
3. Add offline mode with Room sync
4. Implement push notifications
5. Add error handling and retries
6. Test multiplayer features

See [backend/README.md](backend/README.md) for server documentation.
