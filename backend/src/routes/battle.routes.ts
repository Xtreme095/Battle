import { Router } from 'express';
import { BattleController } from '../controllers/battle.controller';

const router = Router();

router.post('/attack', BattleController.initiateAttack);
router.get('/:id', BattleController.getBattle);
router.get('/', BattleController.getMyBattles);

export default router;
