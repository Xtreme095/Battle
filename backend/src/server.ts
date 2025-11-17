import express from 'express';
import http from 'http';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import { Server } from 'socket.io';
import dotenv from 'dotenv';
import rateLimit from 'express-rate-limit';

import { PrismaClient } from '@prisma/client';
import { RedisClient } from './config/redis';
import { logger } from './config/logger';

// Routes
import authRoutes from './routes/auth.routes';
import colonyRoutes from './routes/colony.routes';
import buildingRoutes from './routes/building.routes';
import unitRoutes from './routes/unit.routes';
import battleRoutes from './routes/battle.routes';
import allianceRoutes from './routes/alliance.routes';
import leaderboardRoutes from './routes/leaderboard.routes';

// WebSocket
import { setupWebSocket } from './websocket';

// Middleware
import { errorHandler } from './middleware/errorHandler';
import { authenticate } from './middleware/auth';

// Services
import { GameTickService } from './services/gameTick.service';

// Load environment variables
dotenv.config();

// Initialize clients
export const prisma = new PrismaClient();
export const redis = new RedisClient();

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST']
  }
});

// Middleware
app.use(helmet());
app.use(compression());
app.use(cors({
  origin: process.env.CORS_ORIGIN || '*',
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'),
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'),
  message: 'Too many requests from this IP, please try again later'
});
app.use('/api/', limiter);

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/colonies', authenticate, colonyRoutes);
app.use('/api/buildings', authenticate, buildingRoutes);
app.use('/api/units', authenticate, unitRoutes);
app.use('/api/battles', authenticate, battleRoutes);
app.use('/api/alliances', authenticate, allianceRoutes);
app.use('/api/leaderboard', leaderboardRoutes);

// Error handling
app.use(errorHandler);

// Setup WebSocket
setupWebSocket(io);

// Initialize game tick service
const gameTickService = new GameTickService(io);

// Start server
const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '0.0.0.0';

async function startServer() {
  try {
    // Connect to database
    await prisma.$connect();
    logger.info('Database connected successfully');

    // Connect to Redis
    await redis.connect();
    logger.info('Redis connected successfully');

    // Start game tick
    gameTickService.start();
    logger.info('Game tick service started');

    // Start HTTP server
    server.listen(PORT, HOST, () => {
      logger.info(`Battle Dawn Server running on ${HOST}:${PORT}`);
      logger.info(`Environment: ${process.env.NODE_ENV}`);
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

// Graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM signal received: closing HTTP server');
  server.close(async () => {
    await prisma.$disconnect();
    await redis.disconnect();
    gameTickService.stop();
    logger.info('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', async () => {
  logger.info('SIGINT signal received: closing HTTP server');
  server.close(async () => {
    await prisma.$disconnect();
    await redis.disconnect();
    gameTickService.stop();
    logger.info('Server closed');
    process.exit(0);
  });
});

startServer();

export { io };
