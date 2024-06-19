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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class for handling pathfinding and movement of space war ships.
 * Simplified with all ships having 1x1 size.
 */
public class SimpleSpaceWarMovementHandler extends SimpleWarMovementHandler {

    public SimpleSpaceWarMovementHandler(ScheduledExecutorService commonExecutorPool, int cellSize, int simulationDelay, List<? extends WarUnit> units, int gridSizeX, int gridSizeY) {
        super(commonExecutorPool, cellSize, simulationDelay, units, gridSizeX, gridSizeY);
        this.pathWeightMap = new PathWeightMap(gridSizeX + 20, gridSizeY, 10, 0);
        this.pathPlanner = new PathPlanner(commonExecutorPool, pathWeightMap);
    }
    /**
     * Check if the given cell location fall into the bounds of the battle space.
     * @param loc location of the cell
     * @param unit the unit
     * @return true if the cell is within the map bounds
     */
    public boolean cellInMap(Location loc, WarUnit unit) {
        if (((SpacewarStructure) unit).flee) {
            return (loc.y >= 0 && loc.x >= -10 && loc.y <= gridSizeY - 1 && loc.x <= gridSizeX + 9);
        }
        return (loc.y >= 0 && loc.x >= 0 && loc.y <= gridSizeY - 1 && loc.x <= gridSizeX - 1);
    }

    @Override
    boolean isBlocked(Location loc, WarUnit unit) {
        return !cellInMap(loc, unit);
    }

    @Override
    boolean ignoreObstacles(Location loc, WarUnit unit) {
        boolean ignore = super.ignoreObstacles(loc, unit);
        if (ignore) {
            return true;
        }
        SpacewarStructure sws = (SpacewarStructure) unit;
        if (sws.kamikaze > 0) {
            Set<WarUnit> wunits = unitsForPathfinding.get(loc);
            return wunits != null && wunits.contains(sws.attackUnit);
        }
        return false;
    }
}
