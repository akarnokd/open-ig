/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.model.WarUnit;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * The task to plan a route to the given destination asynchronously.

 * @author akarnokd, 2011.12.25.
 */
public class PathPlanning implements Callable<PathPlanning> {
    /** The initial location. */
    final Location current;
    /** The goal location. */
    final Location goal;
    /** The unit. */
    public final WarUnit unit;
    /** The computed path. */
    public boolean pathFound = true;
    /** A matrix with weighs on individual locations. */
    PathWeightMap pathWeightMap;
    /** Number of reattempts after a failed pathing attempt. */
    public int pathingAttempts = 20;
    /** The path to merge with. */
    List<Location> path;
    /**
     * Constructor. Initializes the fields.
     * @param goal the goal location
     * @param unit the unit
     * @param pathWeightMap the path weight map
     */
    public PathPlanning(WarUnit unit, Location goal, PathWeightMap pathWeightMap) {
        if (unit.inMotion() && (unit.getNextMove() != null) && (unit.location() != unit.getNextMove())) {
            this.current = unit.getNextMove();
        } else {
            this.current = unit.location();
        }
        this.pathWeightMap = pathWeightMap;
        this.goal = goal;
        this.unit = unit;
    }

    @Override
    public PathPlanning call() {
        Pair<Boolean, List<Location>> result = unit.getPathingMethod().searchApproximate(current, goal);
        pathingAttempts--;
        if (result.first) {
            pathFound = true;
            path = result.second;
        } else {
            pathFound = false;
        }
        return this;
    }
    /**
     * Apply the computation result.
     * Increase the weighs on each location that are part of the newly found path.
     */
    public void apply() {
        synchronized (pathWeightMap) {
            for (Location loc : unit.getPath()) {
                pathWeightMap.weightMap[loc.x + pathWeightMap.offsetX][loc.y + pathWeightMap.offsetY]--;
            }
        }
        unit.setPath(path);
        synchronized (pathWeightMap) {
            for (Location loc : path) {
                pathWeightMap.weightMap[loc.x + pathWeightMap.offsetX][loc.y + pathWeightMap.offsetY]++;
            }
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            return unit.equals(((PathPlanning)obj).unit);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return unit.hashCode();
    }
}
