import { Server } from 'socket.io';
import { prisma } from '../server';
import { logger } from '../config/logger';
import { emitResourceUpdate, emitConstructionComplete } from '../websocket';

export class GameTickService {
  private io: Server;
  private tickInterval: NodeJS.Timeout | null = null;
  private resourceUpdateInterval: NodeJS.Timeout | null = null;

  constructor(io: Server) {
    this.io = io;
  }

  start() {
    // Main game tick (every second)
    this.tickInterval = setInterval(() => {
      this.gameTick();
    }, parseInt(process.env.GAME_TICK_RATE || '1000'));

    // Resource update (every minute)
    this.resourceUpdateInterval = setInterval(() => {
      this.updateAllColonyResources();
    }, parseInt(process.env.RESOURCE_UPDATE_INTERVAL || '60000'));

    logger.info('Game tick service started');
  }

  stop() {
    if (this.tickInterval) {
      clearInterval(this.tickInterval);
      this.tickInterval = null;
    }

    if (this.resourceUpdateInterval) {
      clearInterval(this.resourceUpdateInterval);
      this.resourceUpdateInterval = null;
    }

    logger.info('Game tick service stopped');
  }

  /**
   * Main game tick - check for completed actions
   */
  private async gameTick() {
    try {
      await this.checkCompletedConstructions();
      await this.checkCompletedUnitTraining();
      await this.checkCompletedResearch();
      await this.checkArmyMovements();
    } catch (error) {
      logger.error('Game tick error:', error);
    }
  }

  /**
   * Check for completed building constructions
   */
  private async checkCompletedConstructions() {
    const now = new Date();

    const completedBuildings = await prisma.building.findMany({
      where: {
        isUnderConstruction: true,
        constructionEndTime: {
          lte: now
        }
      },
      include: {
        colony: true
      }
    });

    for (const building of completedBuildings) {
      // Complete construction
      await prisma.building.update({
        where: { id: building.id },
        data: {
          level: building.level + 1,
          isUnderConstruction: false,
          constructionStartTime: null,
          constructionEndTime: null
        }
      });

      // Update colony production
      await this.updateColonyProduction(building.colonyId);

      // Emit notification
      emitConstructionComplete(this.io, building.colony.userId, building);

      logger.debug(`Building construction completed: ${building.id}`);
    }
  }

  /**
   * Check for completed unit training
   */
  private async checkCompletedUnitTraining() {
    const now = new Date();

    const completedUnits = await prisma.unit.findMany({
      where: {
        isTraining: true,
        trainingEndTime: {
          lte: now
        }
      },
      include: {
        army: {
          include: {
            colony: true
          }
        }
      }
    });

    for (const unit of completedUnits) {
      await prisma.unit.update({
        where: { id: unit.id },
        data: {
          isTraining: false,
          trainingStartTime: null,
          trainingEndTime: null
        }
      });

      // Emit notification
      this.io.to(`user:${unit.army.colony.userId}`).emit('training:complete', unit);

      logger.debug(`Unit training completed: ${unit.id}`);
    }
  }

  /**
   * Check for completed research
   */
  private async checkCompletedResearch() {
    const now = new Date();

    const completedResearch = await prisma.research.findMany({
      where: {
        isCompleted: false,
        completionTime: {
          lte: now
        }
      },
      include: {
        colony: true
      }
    });

    for (const research of completedResearch) {
      await prisma.research.update({
        where: { id: research.id },
        data: {
          isCompleted: true
        }
      });

      // Emit notification
      this.io.to(`user:${research.colony.userId}`).emit('research:complete', research);

      logger.debug(`Research completed: ${research.id}`);
    }
  }

  /**
   * Check for armies that have completed movement
   */
  private async checkArmyMovements() {
    const now = new Date();

    const arrivedArmies = await prisma.army.findMany({
      where: {
        isMoving: true,
        movementEndTime: {
          lte: now
        }
      },
      include: {
        colony: true
      }
    });

    for (const army of arrivedArmies) {
      await prisma.army.update({
        where: { id: army.id },
        data: {
          positionX: army.destinationX!,
          positionY: army.destinationY!,
          isMoving: false,
          destinationX: null,
          destinationY: null,
          movementStartTime: null,
          movementEndTime: null
        }
      });

      // Emit notification
      this.io.to(`user:${army.colony.userId}`).emit('army:arrived', army);

      logger.debug(`Army arrived: ${army.id}`);
    }
  }

  /**
   * Update all colony resources
   */
  private async updateAllColonyResources() {
    try {
      const colonies = await prisma.colony.findMany({
        select: {
          id: true,
          userId: true,
          metalAmount: true,
          metalProduction: true,
          oilAmount: true,
          oilProduction: true,
          energyAmount: true,
          energyProduction: true,
          foodAmount: true,
          foodProduction: true,
          lastResourceUpdate: true
        }
      });

      for (const colony of colonies) {
        const now = new Date();
        const hoursElapsed = (now.getTime() - colony.lastResourceUpdate.getTime()) / 3600000;

        if (hoursElapsed > 0) {
          const updatedColony = await prisma.colony.update({
            where: { id: colony.id },
            data: {
              metalAmount: colony.metalAmount + BigInt(Math.floor(colony.metalProduction * hoursElapsed)),
              oilAmount: colony.oilAmount + BigInt(Math.floor(colony.oilProduction * hoursElapsed)),
              energyAmount: colony.energyAmount + BigInt(Math.floor(colony.energyProduction * hoursElapsed)),
              foodAmount: colony.foodAmount + BigInt(Math.floor(colony.foodProduction * hoursElapsed)),
              lastResourceUpdate: now
            }
          });

          // Emit resource update
          emitResourceUpdate(this.io, colony.id, {
            metalAmount: updatedColony.metalAmount.toString(),
            oilAmount: updatedColony.oilAmount.toString(),
            energyAmount: updatedColony.energyAmount.toString(),
            foodAmount: updatedColony.foodAmount.toString()
          });
        }
      }

      logger.debug(`Updated resources for ${colonies.length} colonies`);
    } catch (error) {
      logger.error('Resource update error:', error);
    }
  }

  /**
   * Update colony production rates
   */
  private async updateColonyProduction(colonyId: string) {
    const buildings = await prisma.building.findMany({
      where: {
        colonyId,
        isUnderConstruction: false
      }
    });

    let metalProduction = 0;
    let oilProduction = 0;
    let energyProduction = 0;
    let foodProduction = 0;

    for (const building of buildings) {
      const baseProduction = 50;
      const production = baseProduction * building.level * 1.2;

      switch (building.type) {
        case 'METAL_MINE':
          metalProduction += production;
          break;
        case 'OIL_REFINERY':
          oilProduction += production;
          break;
        case 'ENERGY_PLANT':
          energyProduction += production;
          break;
        case 'FARM':
          foodProduction += production;
          break;
      }
    }

    await prisma.colony.update({
      where: { id: colonyId },
      data: {
        metalProduction,
        oilProduction,
        energyProduction,
        foodProduction
      }
    });
  }
}
