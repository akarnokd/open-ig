/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.model.PlanetSurface;
import hu.openig.model.WarUnit;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class for a for handling pathfinding and movement of ground war units.
 */
public class GroundWarMovementHandler extends SimpleWarMovementHandler {

    /** Planetary surface placement helper used for finding obstacles. */
    PlanetSurface.PlacementHelper placement;

    public GroundWarMovementHandler(ScheduledExecutorService commonExecutorPool, int cellSize, int simulationDelay, List<? extends WarUnit> units, int gridSizeX, int gridSizeY, PlanetSurface.PlacementHelper placement) {
        super(commonExecutorPool, cellSize, simulationDelay, units, gridSizeX, gridSizeY);
        this.pathWeightMap = new PathWeightMap(gridSizeX + gridSizeY + 3, gridSizeX + gridSizeY + 3, gridSizeY + 1, gridSizeX + gridSizeY + 1);
        this.pathPlanner = new PathPlanner(commonExecutorPool, pathWeightMap);
        this.placement = placement;
    }
    @Override
    boolean isBlocked(Location loc, WarUnit unit) {
        return !placement.canPlaceBuilding(loc.x, loc.y);
    }

}
