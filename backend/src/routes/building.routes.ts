import { Router } from 'express';
import { BuildingController } from '../controllers/building.controller';

const router = Router();

router.post('/', BuildingController.startConstruction);
router.post('/:id/upgrade', BuildingController.upgradeBuilding);
router.post('/:id/complete', BuildingController.completeConstruction);

export default router;
