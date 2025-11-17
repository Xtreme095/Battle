import { Response } from 'express';
import { AuthRequest } from '../middleware/auth';
import { prisma } from '../server';
import { logger } from '../config/logger';
import { BattleResolver } from '../services/battleResolver.service';

export class BattleController {
  /**
   * Initiate an attack
   */
  static async initiateAttack(req: AuthRequest, res: Response) {
    try {
      const { attackerArmyId, defenderColonyId } = req.body;

      // Get attacker army
      const attackerArmy = await prisma.army.findFirst({
        where: { id: attackerArmyId },
        include: {
          units: true,
          colony: true
        }
      });

      if (!attackerArmy) {
        return res.status(404).json({ error: 'Attacker army not found' });
      }

      if (attackerArmy.colony.userId !== req.userId) {
        return res.status(403).json({ error: 'Unauthorized' });
      }

      if (attackerArmy.units.length === 0) {
        return res.status(400).json({ error: 'Army has no units' });
      }

      // Get defender colony
      const defenderColony = await prisma.colony.findUnique({
        where: { id: defenderColonyId },
        include: {
          user: true,
          armies: {
            include: {
              units: true
            }
          }
        }
      });

      if (!defenderColony) {
        return res.status(404).json({ error: 'Defender colony not found' });
      }

      // Create battle
      const battle = await prisma.battle.create({
        data: {
          attackerUserId: req.userId!,
          defenderUserId: defenderColony.userId,
          attackerArmyId,
          defenderColonyId,
          locationX: defenderColony.positionX,
          locationY: defenderColony.positionY,
          status: 'PENDING'
        }
      });

      // Schedule battle resolution
      setTimeout(async () => {
        await BattleResolver.resolveBattle(battle.id);
      }, parseInt(process.env.BATTLE_RESOLUTION_DELAY || '5000'));

      logger.info(`Battle initiated: ${battle.id}`);

      res.status(201).json(battle);
    } catch (error) {
      logger.error('Initiate attack error:', error);
      res.status(500).json({ error: 'Failed to initiate attack' });
    }
  }

  /**
   * Get battle details
   */
  static async getBattle(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;

      const battle = await prisma.battle.findFirst({
        where: {
          id,
          OR: [
            { attackerUserId: req.userId },
            { defenderUserId: req.userId }
          ]
        },
        include: {
          attackerUser: {
            select: {
              id: true,
              username: true
            }
          },
          defenderUser: {
            select: {
              id: true,
              username: true
            }
          }
        }
      });

      if (!battle) {
        return res.status(404).json({ error: 'Battle not found' });
      }

      res.json(battle);
    } catch (error) {
      logger.error('Get battle error:', error);
      res.status(500).json({ error: 'Failed to get battle' });
    }
  }

  /**
   * Get user's battles
   */
  static async getMyBattles(req: AuthRequest, res: Response) {
    try {
      const battles = await prisma.battle.findMany({
        where: {
          OR: [
            { attackerUserId: req.userId },
            { defenderUserId: req.userId }
          ]
        },
        include: {
          attackerUser: {
            select: {
              id: true,
              username: true
            }
          },
          defenderUser: {
            select: {
              id: true,
              username: true
            }
          }
        },
        orderBy: {
          startTime: 'desc'
        },
        take: 50
      });

      res.json(battles);
    } catch (error) {
      logger.error('Get my battles error:', error);
      res.status(500).json({ error: 'Failed to get battles' });
    }
  }
}
