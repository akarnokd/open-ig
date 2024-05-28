/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.WarUnit;

/**
 * Class for a for handling free form movement of space war objects.
 */
public class FreeFormSpaceWarMovementHandler extends WarMovementHandler {

    public FreeFormSpaceWarMovementHandler(int cellSize, int simulationDelay) {
        super(cellSize, simulationDelay);
    }

    @Override
    public void initUnits() { }
    @Override
    public void removeUnit(WarUnit unit) { }
    @Override
    public void setMovementGoal(WarUnit unit, Location loc) {
        unit.setNextMove(loc);
        unit.setHasPlannedMove(true);
    }

    @Override
    public boolean moveUnit(WarUnit unit) {
        SpacewarStructure ship = (SpacewarStructure) unit;
        double nextX = ship.getNextMove().x;
        double nextY = ship.getNextMove().y;
        if (rotateStep(ship, nextX, nextY)) {
        // travel until the distance
            double dist = Math.hypot(ship.gridX - nextX, ship.gridY - nextY);
            double angle = Math.atan2(nextY - ship.gridY, nextX - ship.gridX);
            double ds = 1.0 * simulationDelay / ship.movementSpeed / cellSize;
            if (dist > ds) {
                ship.gridX += ds * Math.cos(angle);
                ship.gridY += ds * Math.sin(angle);
            } else {
                ship.gridX = nextX;
                ship.gridY = nextY;
                clearUnitGoal(unit);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearUnitGoal(WarUnit unit) {
        unit.getPath().clear();
        unit.clearNextMove();
        unit.setHasPlannedMove(false);
    }

    @Override
    public void doPathPlannings() { }
}
