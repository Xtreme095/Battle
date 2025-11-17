import { Router } from 'express';
import { ColonyController } from '../controllers/colony.controller';

const router = Router();

router.get('/', ColonyController.getMyColonies);
router.get('/:id', ColonyController.getColony);
router.post('/:id/update-resources', ColonyController.updateResources);

export default router;
