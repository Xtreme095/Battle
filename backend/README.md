# Battle Dawn Server

Backend server for the Battle Dawn MMORTS game. Provides REST API, WebSocket support, and real-time game logic.

## Features

- **REST API**: Complete API for all game operations
- **WebSocket**: Real-time updates for resources, battles, and chat
- **Battle System**: Server-side battle resolution
- **Real-time Updates**: Automatic resource production, construction completion
- **Alliance Chat**: Live messaging for alliance members
- **Leaderboards**: Player and alliance rankings
- **Authentication**: JWT-based authentication
- **Database**: PostgreSQL with Prisma ORM
- **Caching**: Redis for performance
- **Scalable**: Dockerized for easy deployment

## Tech Stack

- **Runtime**: Node.js 18+
- **Language**: TypeScript
- **Framework**: Express.js
- **WebSocket**: Socket.io
- **Database**: PostgreSQL 15
- **ORM**: Prisma
- **Cache**: Redis
- **Authentication**: JWT + bcrypt
- **Logging**: Winston
- **Containerization**: Docker

## Prerequisites

- Node.js 18 or higher
- PostgreSQL 15
- Redis 7
- npm or yarn

## Installation

### Using Docker (Recommended)

```bash
# Clone the repository
cd backend

# Create .env file
cp .env.example .env

# Start all services
docker-compose up -d

# Run database migrations
docker-compose exec server npx prisma migrate deploy

# Check logs
docker-compose logs -f server
```

### Manual Setup

```bash
# Install dependencies
npm install

# Set up environment variables
cp .env.example .env
# Edit .env with your configuration

# Start PostgreSQL and Redis
# (Use your preferred method or Docker)

# Run database migrations
npx prisma migrate dev

# Generate Prisma Client
npx prisma generate

# Start development server
npm run dev
```

## Environment Variables

See `.env.example` for all available configuration options.

Key variables:
- `DATABASE_URL`: PostgreSQL connection string
- `REDIS_URL`: Redis connection string
- `JWT_SECRET`: Secret key for JWT tokens
- `PORT`: Server port (default: 3000)

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/me` - Get current user

### Colonies
- `GET /api/colonies` - Get user's colonies
- `GET /api/colonies/:id` - Get specific colony
- `POST /api/colonies/:id/update-resources` - Update resources

### Buildings
- `POST /api/buildings` - Start building construction
- `POST /api/buildings/:id/upgrade` - Upgrade building
- `POST /api/buildings/:id/complete` - Complete construction

### Battles
- `POST /api/battles/attack` - Initiate attack
- `GET /api/battles/:id` - Get battle details
- `GET /api/battles` - Get user's battles

### WebSocket Events

#### Client → Server
- `subscribe:colony` - Subscribe to colony updates
- `subscribe:alliance` - Subscribe to alliance chat
- `alliance:message` - Send alliance message
- `subscribe:battle` - Subscribe to battle updates

#### Server → Client
- `resources:updated` - Resource update
- `construction:complete` - Building completed
- `training:complete` - Unit training completed
- `research:complete` - Research completed
- `alliance:message` - New alliance message
- `battle:updated` - Battle state changed
- `attack:incoming` - Colony under attack
- `army:arrived` - Army reached destination

## Game Systems

### Resource Production
- Resources update every minute
- Production based on buildings
- Automatic persistence to database

### Construction System
- Time-based building construction
- Cost scales with level (1.5x multiplier)
- Automatic completion detection

### Battle Resolution
- Server-side battle simulation
- 20 round maximum
- Unit type effectiveness
- Experience and plunder rewards

### Real-time Features
- WebSocket for live updates
- Redis pub/sub for scaling
- JWT authentication for WebSocket

## Database Schema

See `prisma/schema.prisma` for complete schema.

Main entities:
- Users
- Colonies
- Buildings
- Armies
- Units
- Battles
- Alliances
- ChatMessages
- Research
- SpyMissions

## Development

```bash
# Start dev server with hot reload
npm run dev

# Run tests
npm test

# Build for production
npm run build

# Database commands
npx prisma migrate dev      # Create migration
npx prisma migrate deploy   # Apply migrations
npx prisma studio          # GUI for database
npx prisma generate        # Generate client

# Seed database
npm run seed
```

## Production Deployment

### Using Docker

```bash
# Build image
docker build -t battledawn-server .

# Run with docker-compose
docker-compose up -d

# Scale if needed
docker-compose up --scale server=3
```

### Manual Deployment

```bash
# Install production dependencies
npm ci --only=production

# Run migrations
npx prisma migrate deploy

# Build
npm run build

# Start
npm start
```

## Monitoring

Logs are output to:
- Console (development)
- `logs/error.log` (errors only)
- `logs/combined.log` (all logs)

## Performance

- Uses Redis for caching
- Connection pooling with Prisma
- Compression middleware
- Rate limiting enabled
- WebSocket for efficient real-time updates

## Security

- Helmet for security headers
- CORS configured
- JWT token expiration
- Password hashing with bcrypt
- Rate limiting on API endpoints
- Input validation
- SQL injection protection (Prisma)

## Scaling

The server is designed to scale horizontally:
- Stateless API (except WebSocket)
- Redis for shared state
- PostgreSQL for persistence
- Load balancer ready

## API Documentation

For detailed API documentation, see the `/docs` endpoint or use tools like Postman with the provided collection.

## Support

For issues or questions:
- Check logs in `logs/` directory
- Review environment variables
- Ensure PostgreSQL and Redis are running
- Check Docker container status

## License

MIT
