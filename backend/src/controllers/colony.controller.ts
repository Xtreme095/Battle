import { Response } from 'express';
import { AuthRequest } from '../middleware/auth';
import { prisma } from '../server';
import { logger } from '../config/logger';

export class ColonyController {
  /**
   * Get all colonies for the current user
   */
  static async getMyColonies(req: AuthRequest, res: Response) {
    try {
      const colonies = await prisma.colony.findMany({
        where: { userId: req.userId },
        include: {
          buildings: true,
          armies: {
            include: {
              units: true
            }
          },
          research: true
        }
      });

      // Update resources before returning
      const updatedColonies = colonies.map(colony => {
        const now = new Date();
        const hoursElapsed = (now.getTime() - colony.lastResourceUpdate.getTime()) / 3600000;

        return {
          ...colony,
          metalAmount: colony.metalAmount + BigInt(Math.floor(colony.metalProduction * hoursElapsed)),
          oilAmount: colony.oilAmount + BigInt(Math.floor(colony.oilProduction * hoursElapsed)),
          energyAmount: colony.energyAmount + BigInt(Math.floor(colony.energyProduction * hoursElapsed)),
          foodAmount: colony.foodAmount + BigInt(Math.floor(colony.foodProduction * hoursElapsed)),
          lastResourceUpdate: now
        };
      });

      res.json(updatedColonies);
    } catch (error) {
      logger.error('Get colonies error:', error);
      res.status(500).json({ error: 'Failed to get colonies' });
    }
  }

  /**
   * Get a specific colony by ID
   */
  static async getColony(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;

      const colony = await prisma.colony.findFirst({
        where: {
          id,
          userId: req.userId
        },
        include: {
          buildings: true,
          armies: {
            include: {
              units: true
            }
          },
          research: true
        }
      });

      if (!colony) {
        return res.status(404).json({ error: 'Colony not found' });
      }

      // Update resources
      const now = new Date();
      const hoursElapsed = (now.getTime() - colony.lastResourceUpdate.getTime()) / 3600000;

      const updatedColony = {
        ...colony,
        metalAmount: colony.metalAmount + BigInt(Math.floor(colony.metalProduction * hoursElapsed)),
        oilAmount: colony.oilAmount + BigInt(Math.floor(colony.oilProduction * hoursElapsed)),
        energyAmount: colony.energyAmount + BigInt(Math.floor(colony.energyProduction * hoursElapsed)),
        foodAmount: colony.foodAmount + BigInt(Math.floor(colony.foodProduction * hoursElapsed))
      };

      // Persist updated resources
      await prisma.colony.update({
        where: { id },
        data: {
          metalAmount: updatedColony.metalAmount,
          oilAmount: updatedColony.oilAmount,
          energyAmount: updatedColony.energyAmount,
          foodAmount: updatedColony.foodAmount,
          lastResourceUpdate: now
        }
      });

      res.json(updatedColony);
    } catch (error) {
      logger.error('Get colony error:', error);
      res.status(500).json({ error: 'Failed to get colony' });
    }
  }

  /**
   * Update colony resources manually
   */
  static async updateResources(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;

      const colony = await prisma.colony.findFirst({
        where: {
          id,
          userId: req.userId
        }
      });

      if (!colony) {
        return res.status(404).json({ error: 'Colony not found' });
      }

      const now = new Date();
      const hoursElapsed = (now.getTime() - colony.lastResourceUpdate.getTime()) / 3600000;

      const updatedColony = await prisma.colony.update({
        where: { id },
        data: {
          metalAmount: colony.metalAmount + BigInt(Math.floor(colony.metalProduction * hoursElapsed)),
          oilAmount: colony.oilAmount + BigInt(Math.floor(colony.oilProduction * hoursElapsed)),
          energyAmount: colony.energyAmount + BigInt(Math.floor(colony.energyProduction * hoursElapsed)),
          foodAmount: colony.foodAmount + BigInt(Math.floor(colony.foodProduction * hoursElapsed)),
          lastResourceUpdate: now
        }
      });

      res.json(updatedColony);
    } catch (error) {
      logger.error('Update resources error:', error);
      res.status(500).json({ error: 'Failed to update resources' });
    }
  }
}
