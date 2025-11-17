import { Server, Socket } from 'socket.io';
import jwt from 'jsonwebtoken';
import { logger } from '../config/logger';
import { redis } from '../server';

interface AuthenticatedSocket extends Socket {
  userId?: string;
}

export function setupWebSocket(io: Server) {
  // Authentication middleware
  io.use((socket: AuthenticatedSocket, next) => {
    const token = socket.handshake.auth.token;

    if (!token) {
      return next(new Error('Authentication error'));
    }

    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET!) as { userId: string };
      socket.userId = decoded.userId;
      next();
    } catch (err) {
      next(new Error('Authentication error'));
    }
  });

  io.on('connection', (socket: AuthenticatedSocket) => {
    logger.info(`User connected: ${socket.userId}`);

    // Join user's personal room
    socket.join(`user:${socket.userId}`);

    // Resource updates
    socket.on('subscribe:colony', (colonyId: string) => {
      socket.join(`colony:${colonyId}`);
      logger.debug(`User ${socket.userId} subscribed to colony ${colonyId}`);
    });

    socket.on('unsubscribe:colony', (colonyId: string) => {
      socket.leave(`colony:${colonyId}`);
      logger.debug(`User ${socket.userId} unsubscribed from colony ${colonyId}`);
    });

    // Alliance chat
    socket.on('subscribe:alliance', (allianceId: string) => {
      socket.join(`alliance:${allianceId}`);
      logger.debug(`User ${socket.userId} subscribed to alliance ${allianceId}`);
    });

    socket.on('unsubscribe:alliance', (allianceId: string) => {
      socket.leave(`alliance:${allianceId}`);
      logger.debug(`User ${socket.userId} unsubscribed from alliance ${allianceId}`);
    });

    socket.on('alliance:message', async (data: { allianceId: string; message: string }) => {
      try {
        const message = {
          allianceId: data.allianceId,
          senderId: socket.userId,
          message: data.message,
          timestamp: new Date()
        };

        // Broadcast to alliance room
        io.to(`alliance:${data.allianceId}`).emit('alliance:message', message);

        logger.debug(`Alliance message sent in ${data.allianceId}`);
      } catch (error) {
        logger.error('Alliance message error:', error);
      }
    });

    // Battle updates
    socket.on('subscribe:battle', (battleId: string) => {
      socket.join(`battle:${battleId}`);
      logger.debug(`User ${socket.userId} subscribed to battle ${battleId}`);
    });

    socket.on('unsubscribe:battle', (battleId: string) => {
      socket.leave(`battle:${battleId}`);
      logger.debug(`User ${socket.userId} unsubscribed from battle ${battleId}`);
    });

    // Disconnect
    socket.on('disconnect', () => {
      logger.info(`User disconnected: ${socket.userId}`);
    });
  });

  return io;
}

// Helper functions to emit events
export function emitResourceUpdate(io: Server, colonyId: string, resources: any) {
  io.to(`colony:${colonyId}`).emit('resources:updated', resources);
}

export function emitBattleUpdate(io: Server, battleId: string, battleData: any) {
  io.to(`battle:${battleId}`).emit('battle:updated', battleData);
}

export function emitConstructionComplete(io: Server, userId: string, building: any) {
  io.to(`user:${userId}`).emit('construction:complete', building);
}

export function emitAttackNotification(io: Server, userId: string, attackData: any) {
  io.to(`user:${userId}`).emit('attack:incoming', attackData);
}
