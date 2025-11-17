import { Response } from 'express';
import { AuthRequest } from '../middleware/auth';
import { prisma } from '../server';
import { logger } from '../config/logger';

// Building costs and times (should match Android app)
const BUILDING_COSTS: Record<string, any> = {
  METAL_MINE: { metal: 100, workers: 5 },
  OIL_REFINERY: { metal: 150, workers: 5 },
  ENERGY_PLANT: { metal: 200, workers: 8 },
  FARM: { metal: 80, workers: 3 }
};

export class BuildingController {
  /**
   * Start building construction
   */
  static async startConstruction(req: AuthRequest, res: Response) {
    try {
      const { colonyId, buildingType, positionX, positionY } = req.body;

      // Verify colony ownership
      const colony = await prisma.colony.findFirst({
        where: {
          id: colonyId,
          userId: req.userId
        }
      });

      if (!colony) {
        return res.status(404).json({ error: 'Colony not found' });
      }

      // Get building cost
      const cost = BUILDING_COSTS[buildingType];
      if (!cost) {
        return res.status(400).json({ error: 'Invalid building type' });
      }

      // Check resources
      if (colony.metalAmount < cost.metal || colony.workersAmount < cost.workers) {
        return res.status(400).json({ error: 'Insufficient resources' });
      }

      // Deduct resources
      await prisma.colony.update({
        where: { id: colonyId },
        data: {
          metalAmount: colony.metalAmount - BigInt(cost.metal),
          workersAmount: colony.workersAmount - BigInt(cost.workers)
        }
      });

      // Calculate construction time (1.5 hours for level 1)
      const constructionTime = 1.5 * 3600 * 1000; // in milliseconds
      const now = new Date();
      const endTime = new Date(now.getTime() + constructionTime);

      // Create building
      const building = await prisma.building.create({
        data: {
          colonyId,
          type: buildingType,
          level: 1,
          positionX,
          positionY,
          constructionStartTime: now,
          constructionEndTime: endTime,
          isUnderConstruction: true
        }
      });

      logger.info(`Building construction started: ${buildingType} at colony ${colonyId}`);

      res.status(201).json(building);
    } catch (error) {
      logger.error('Start construction error:', error);
      res.status(500).json({ error: 'Failed to start construction' });
    }
  }

  /**
   * Upgrade building
   */
  static async upgradeBuilding(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;

      const building = await prisma.building.findFirst({
        where: { id },
        include: { colony: true }
      });

      if (!building) {
        return res.status(404).json({ error: 'Building not found' });
      }

      if (building.colony.userId !== req.userId) {
        return res.status(403).json({ error: 'Unauthorized' });
      }

      if (building.isUnderConstruction) {
        return res.status(400).json({ error: 'Building is already under construction' });
      }

      // Calculate upgrade cost (1.5x multiplier per level)
      const baseCost = BUILDING_COSTS[building.type];
      const multiplier = Math.pow(1.5, building.level);
      const upgradeCost = {
        metal: Math.floor(baseCost.metal * multiplier),
        workers: Math.floor(baseCost.workers * multiplier)
      };

      // Check resources
      if (building.colony.metalAmount < upgradeCost.metal ||
          building.colony.workersAmount < upgradeCost.workers) {
        return res.status(400).json({ error: 'Insufficient resources' });
      }

      // Deduct resources
      await prisma.colony.update({
        where: { id: building.colonyId },
        data: {
          metalAmount: building.colony.metalAmount - BigInt(upgradeCost.metal),
          workersAmount: building.colony.workersAmount - BigInt(upgradeCost.workers)
        }
      });

      // Calculate construction time
      const constructionTime = 1.5 * (building.level + 1) * 3600 * 1000;
      const now = new Date();
      const endTime = new Date(now.getTime() + constructionTime);

      // Update building
      const updatedBuilding = await prisma.building.update({
        where: { id },
        data: {
          constructionStartTime: now,
          constructionEndTime: endTime,
          isUnderConstruction: true
        }
      });

      logger.info(`Building upgrade started: ${building.type} to level ${building.level + 1}`);

      res.json(updatedBuilding);
    } catch (error) {
      logger.error('Upgrade building error:', error);
      res.status(500).json({ error: 'Failed to upgrade building' });
    }
  }

  /**
   * Complete construction
   */
  static async completeConstruction(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;

      const building = await prisma.building.findFirst({
        where: { id },
        include: { colony: true }
      });

      if (!building) {
        return res.status(404).json({ error: 'Building not found' });
      }

      if (building.colony.userId !== req.userId) {
        return res.status(403).json({ error: 'Unauthorized' });
      }

      if (!building.isUnderConstruction) {
        return res.status(400).json({ error: 'Building is not under construction' });
      }

      if (building.constructionEndTime && building.constructionEndTime > new Date()) {
        return res.status(400).json({ error: 'Construction not yet complete' });
      }

      // Complete construction
      const updatedBuilding = await prisma.building.update({
        where: { id },
        data: {
          level: building.level + (building.level > 0 ? 0 : 1), // Increment level for new buildings
          isUnderConstruction: false,
          constructionStartTime: null,
          constructionEndTime: null
        }
      });

      // Update colony production rates
      await this.updateColonyProduction(building.colonyId);

      logger.info(`Building construction completed: ${building.type}`);

      res.json(updatedBuilding);
    } catch (error) {
      logger.error('Complete construction error:', error);
      res.status(500).json({ error: 'Failed to complete construction' });
    }
  }

  /**
   * Update colony production rates based on buildings
   */
  private static async updateColonyProduction(colonyId: string) {
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
      const baseProduction = 50; // Base production per level
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
