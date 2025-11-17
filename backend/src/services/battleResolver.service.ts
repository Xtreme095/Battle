import { prisma } from '../server';
import { logger } from '../config/logger';

// Unit stats (should match Android app)
const UNIT_STATS: Record<string, any> = {
  LIGHT_INFANTRY: { attack: 10, defense: 5, health: 50 },
  HEAVY_INFANTRY: { attack: 20, defense: 15, health: 100 },
  ELITE_INFANTRY: { attack: 35, defense: 25, health: 150 },
  LIGHT_VEHICLE: { attack: 25, defense: 20, health: 120 },
  ARMORED_VEHICLE: { attack: 40, defense: 30, health: 200 },
  ARTILLERY: { attack: 60, defense: 15, health: 150 },
  LIGHT_TANK: { attack: 50, defense: 40, health: 300 },
  MEDIUM_TANK: { attack: 70, defense: 60, health: 500 },
  HEAVY_TANK: { attack: 100, defense: 90, health: 800 }
};

export class BattleResolver {
  /**
   * Resolve a battle
   */
  static async resolveBattle(battleId: string) {
    try {
      const battle = await prisma.battle.findUnique({
        where: { id: battleId },
        include: {
          attackerArmy: {
            include: {
              units: true
            }
          },
          defenderColony: {
            include: {
              armies: {
                include: {
                  units: true
                }
              }
            }
          }
        }
      });

      if (!battle) {
        logger.error(`Battle ${battleId} not found`);
        return;
      }

      if (battle.status !== 'PENDING') {
        logger.warn(`Battle ${battleId} already resolved`);
        return;
      }

      // Get defender army (largest army in colony)
      const defenderArmy = battle.defenderColony.armies.sort(
        (a, b) => b.units.length - a.units.length
      )[0];

      if (!defenderArmy) {
        // Auto-win if no defender
        await this.finalizeBattle(battleId, 'ATTACKER', [], battle.attackerArmy.units.map(u => u.id));
        return;
      }

      // Simulate battle
      const attackerUnits = [...battle.attackerArmy.units];
      const defenderUnits = [...defenderArmy.units];

      const rounds: any[] = [];
      let roundNumber = 1;

      while (attackerUnits.length > 0 && defenderUnits.length > 0 && roundNumber <= 20) {
        const round = this.simulateRound(attackerUnits, defenderUnits, roundNumber);
        rounds.push(round);

        // Remove dead units
        const deadAttackers = attackerUnits.filter(u => u.currentHealth <= 0);
        const deadDefenders = defenderUnits.filter(u => u.currentHealth <= 0);

        for (const unit of deadAttackers) {
          const index = attackerUnits.indexOf(unit);
          if (index > -1) attackerUnits.splice(index, 1);
        }

        for (const unit of deadDefenders) {
          const index = defenderUnits.indexOf(unit);
          if (index > -1) defenderUnits.splice(index, 1);
        }

        roundNumber++;
      }

      // Determine victor
      let victor: string;
      if (attackerUnits.length > 0 && defenderUnits.length === 0) {
        victor = 'ATTACKER';
      } else if (defenderUnits.length > 0 && attackerUnits.length === 0) {
        victor = 'DEFENDER';
      } else {
        victor = 'DRAW';
      }

      const survivorIds = attackerUnits.map(u => u.id);

      await this.finalizeBattle(battleId, victor, rounds, survivorIds);

      logger.info(`Battle ${battleId} resolved: ${victor} wins`);
    } catch (error) {
      logger.error(`Battle resolution error for ${battleId}:`, error);
    }
  }

  /**
   * Simulate a single round of combat
   */
  private static simulateRound(attackers: any[], defenders: any[], roundNumber: number) {
    let attackerDamage = 0;
    let defenderDamage = 0;

    // Attackers strike
    for (const attacker of attackers) {
      if (defenders.length === 0) break;

      const target = defenders[Math.floor(Math.random() * defenders.length)];
      const stats = UNIT_STATS[attacker.type];

      if (stats) {
        const damage = Math.max(stats.attack - UNIT_STATS[target.type]?.defense || 0, stats.attack * 0.2);
        target.currentHealth -= Math.floor(damage);
        attackerDamage += Math.floor(damage);
      }
    }

    // Defenders counter-attack
    for (const defender of defenders) {
      if (attackers.length === 0) break;

      const target = attackers[Math.floor(Math.random() * attackers.length)];
      const stats = UNIT_STATS[defender.type];

      if (stats) {
        const damage = Math.max(stats.attack - UNIT_STATS[target.type]?.defense || 0, stats.attack * 0.2);
        target.currentHealth -= Math.floor(damage);
        defenderDamage += Math.floor(damage);
      }
    }

    return {
      roundNumber,
      attackerDamage,
      defenderDamage
    };
  }

  /**
   * Finalize battle results
   */
  private static async finalizeBattle(battleId: string, victor: string, rounds: any[], survivorIds: string[]) {
    // Update battle
    await prisma.battle.update({
      where: { id: battleId },
      data: {
        status: 'COMPLETED',
        endTime: new Date(),
        victor,
        battleData: { rounds }
      }
    });

    // Update user stats
    const battle = await prisma.battle.findUnique({
      where: { id: battleId }
    });

    if (battle) {
      if (victor === 'ATTACKER') {
        await prisma.user.update({
          where: { id: battle.attackerUserId },
          data: { wins: { increment: 1 } }
        });
        await prisma.user.update({
          where: { id: battle.defenderUserId },
          data: { losses: { increment: 1 } }
        });
      } else if (victor === 'DEFENDER') {
        await prisma.user.update({
          where: { id: battle.defenderUserId },
          data: { wins: { increment: 1 } }
        });
        await prisma.user.update({
          where: { id: battle.attackerUserId },
          data: { losses: { increment: 1 } }
        });
      }

      // Delete dead units
      await prisma.unit.deleteMany({
        where: {
          id: {
            notIn: survivorIds
          }
        }
      });
    }
  }
}
