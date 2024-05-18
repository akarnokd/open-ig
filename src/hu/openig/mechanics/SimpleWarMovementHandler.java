/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Func2;
import hu.openig.core.Location;
import hu.openig.core.Pathfinding;
import hu.openig.model.WarUnit;
import hu.openig.utils.Exceptions;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Abstract base class for handling pathfinding and movement of war units.
 */
public abstract class SimpleWarMovementHandler extends WarMovementHandler {

    /** Map for reserved locations. */
    public final Map<Location, WarUnit> reservedCells = new HashMap<>();
    /** Helper map for locations occupied by units. */
    public final Map<Location, Set<WarUnit>> unitsForPathfinding = new HashMap<>();
    /** A matrix with weighs on individual locations. */
    public PathWeightMap pathWeightMap;
    /** The path planner object responsible for handling the executions of new path searches. */
    PathPlanner pathPlanner;
    /** The width of the grid walked by units. */
    int gridSizeX;
    /** The height of the grid walked by units. */
    int gridSizeY;
    /** The war units. */
    final List<? extends WarUnit> units;
    abstract boolean isBlocked(Location loc, WarUnit unit);
    public SimpleWarMovementHandler(ScheduledExecutorService commonExecutorPool, int cellSize, int simulationDelay, List<? extends WarUnit> units, int gridSizeX, int gridSizeY) {
        super(cellSize, simulationDelay);
        this.pathPlanner = new PathPlanner(commonExecutorPool, pathWeightMap);
        this.gridSizeX = gridSizeX;
        this.gridSizeY = gridSizeY;
        this.units = units;
        for (WarUnit wunit : units) {
            Location pfl = wunit.location();
            Set<WarUnit> set = unitsForPathfinding.get(pfl);
            if (set == null) {
                set = new HashSet<>();
                unitsForPathfinding.put(pfl, set);
            }
            set.add(wunit);
            wunit.setPathingMethod(getPathfinding(wunit));
        }
    }
    @Override
    public void setMovementGoal(WarUnit unit, Location loc) {
        Location lastMovementGoal = unit.getPath().peekLast();
        if (lastMovementGoal == null || !unit.getPath().peekLast().equals(loc)) {
            pathPlanner.addPathPlanning(unit, loc);
        }
    }

    @Override
    public void doPathPlannings() {
        pathPlanner.doPathPlannings();
    }

    @Override
    public void clearUnitGoal(WarUnit unit) {
        synchronized (pathWeightMap) {
            for (Location loc : unit.getPath()) {
                pathWeightMap.weightMap[loc.x + pathWeightMap.offsetX][loc.y + pathWeightMap.offsetY]--;
            }
        }
        unit.getPath().clear();
        if (unit.getNextMove() != null && unit.inMotion()) {
            addToUnitPath(unit, unit.getNextMove());
        } else {
            unit.clearNextMove();
            unit.setHasPlannedMove(false);
        }
    }
    @Override
    public boolean moveUnit(WarUnit unit) {
        if (unit.isDestroyed()) {
            return false;
        }
        if (unit.getNextMove() == null) {
            // I have no better idea when to do an occasional but this seems to work well enough
            if (Math.random() > 0.90f) {
                repath(unit);
                return false;
            }
            unit.setNextMove(unit.getPath().peekFirst());
            unit.setNextRotate(unit.getNextMove());

            // is the next move location still passable?
            if (!ignoreObstacles(unit.getNextMove(), unit) && (!isPassable(unit.getNextMove(), unit) || isCellReserved(unit.getNextMove(), unit))) {
                // trigger replanning
                repath(unit);
                return false;
            }
        }

        if (!ignoreObstacles(unit.getNextMove(), unit) && unitsForPathfinding.get(unit.getNextMove()) != null) {
            for (WarUnit wunit : unitsForPathfinding.get(unit.getNextMove())) {
                if (wunit != unit && !wunit.inMotion() && !needsRotation(wunit, wunit.getNextRotate())) {
                    repath(unit);
                    return false;
                }
            }
        }

        if (unit.getNextRotate() != null && rotateStep(unit, unit.getNextRotate().x, unit.getNextRotate().y)) {
            unit.setNextRotate(null);
        }
        if (unit.getNextRotate() == null) {
            return moveUnitStep(unit, simulationDelay);
        }
        return false;
    }

    /**
     * Move the war unit one step.
     * @param unit the unit
     * @param time the available time
     * @return the unit finished it's planned path
     */
    boolean moveUnitStep(WarUnit unit, double time) {
        double dv = 1.0 * time / unit.getMovementSpeed() / cellSize;
        // detect collision
        if (!ignoreObstacles(unit.getNextMove(), unit) && !reserveCellFor(unit)) {
            return false;
        }
        double distanceToTarget = Math.hypot(unit.getNextMove().x - unit.exactLocation().x, unit.getNextMove().y - unit.exactLocation().y);

        if (distanceToTarget < dv) {
            releaseCellFrom(unit);

            updateUnitLocation(unit, unit.getNextMove().x, unit.getNextMove().y, false);

            unit.clearNextMove();
            removeFirstFromUnitPath(unit);
            double remaining = dv - distanceToTarget;
            if (unit.hasPlannedMove()) {
                Location nextCell = unit.getPath().peekFirst();
                if (!needsRotation(unit, nextCell)) {
                    double time2 = remaining * unit.getMovementSpeed() * cellSize;
                    unit.setNextMove(nextCell);
                    moveUnitStep(unit, time2);
                }
            } else {
                return true;
            }
        } else {
            double angle = Math.atan2(unit.getNextMove().y - unit.exactLocation().y, unit.getNextMove().x - unit.exactLocation().x);
            updateUnitLocation(unit, dv * Math.cos(angle), dv * Math.sin(angle), true);
        }
        return false;
    }
    /**
     * Reserve the next movement cell for the war unit.
     * @param unit the unit
     * @return true if the next cell is successfully reserved
     */
    boolean reserveCellFor(WarUnit unit) {
        if (unitsForPathfinding.get(unit.getNextMove()) != null) {
            for (WarUnit wunit : unitsForPathfinding.get(unit.getNextMove())) {
                if (wunit != unit) {
                    return false;
                }
            }
        } else if (isCellReserved(unit.getNextMove(), unit)) {
            return false;
        }
        reservedCells.put(unit.getNextMove(), unit);
        return true;
    }

    /**
     * Release the reserved movement cell from the war unit.
     * @param unit the unit
     */
    void releaseCellFrom(WarUnit unit) {
        // remove cell reservation
        for (Iterator<Map.Entry<Location, WarUnit>> it = reservedCells.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Location, WarUnit> entry = it.next();
            if (entry.getValue() == unit) {
                it.remove();
            }
        }
    }
    /**
     * Check if a cell is available for a war unit.
     * @param loc the location to check
     * @param unit the unit
     * @return true if the cell is already reserved
     */
    boolean isCellReserved(Location loc, WarUnit unit) {
        return reservedCells.get(loc) != null &&  reservedCells.get(loc) != unit;
    }

    /**
     * Returns a preset pathfinding object.
     * @param unit the unit doing the pathfinding
     * @return the pathfinding object
     */
    Pathfinding getPathfinding(final WarUnit unit) {
        Pathfinding pathfinding = new Pathfinding();
        pathfinding.isPassable = new Func1<Location, Boolean>() {
            @Override
            public Boolean invoke(Location value) {
                return isPassable(value, unit);
            }
        };
        pathfinding.isBlocked = new Func1<Location, Boolean>() {
            @Override
            public Boolean invoke(Location value) {
                return isBlocked(value, unit);
            }
        };
        pathfinding.distance = pathingDistance;
        return pathfinding;
    }

    /** Routine that tells the distance between two neighboring locations. */
    final Func2<Location, Location, Integer> pathingDistance = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            double multiplier = 1.0;
            multiplier += pathWeightMap.weightMap[t.x + pathWeightMap.offsetX][t.y + pathWeightMap.offsetY] * 0.05;

            if (t.x == u.x || u.y == t.y) {
                return (int) (1000 * multiplier);
            }
            return (int)(1414 * multiplier);
        }
    };
    @Override
    public void removeUnit(WarUnit unit) {
        // remove from pathfinding helper
        Location pfl = unit.location();
        Set<WarUnit> set = unitsForPathfinding.get(pfl);
        if (set != null) {
            if (!set.remove(unit)) {
                Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", pfl.x, pfl.y)));
            }
        }
        clearUnitGoal(unit);
        releaseCellFrom(unit);
    }

    /**
     * Update the location of the specified unit by the given amount.
     * @param unit the unit to move
     * @param dx the delta X to move
     * @param dy the delta Y to move
     * @param relative is this a relative move
     */
    void updateUnitLocation(WarUnit unit, double dx, double dy, boolean relative) {
        Location currentLocation = unit.location();
        if (relative) {
            unit.setLocation(unit.exactLocation().x + dx, unit.exactLocation().y + dy);
        } else {
            unit.setLocation(dx, dy);
        }
        Location nextLocation = unit.location();

        if (!currentLocation.equals(nextLocation)) {
            // remove from pathfinding helper
            Set<WarUnit> set = unitsForPathfinding.get(currentLocation);
            if (set != null) {
                if (!set.remove(unit)) {
                    Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", currentLocation.x, currentLocation.y)));
                }
                if (set.isEmpty()) {
                    unitsForPathfinding.remove(currentLocation);
                }
            }
        }

        Set<WarUnit> set = unitsForPathfinding.get(nextLocation);
        if (set == null) {
            set = new HashSet<>();
            unitsForPathfinding.put(nextLocation, set);
        }
        set.add(unit);
    }

    /**
     * Check if the given cell is passable.
     * Other units are ignore in case they are friendly and in motion
     * or enemy in case of attackmove.
     * @param loc the target cell.
     * @param unit the unit checking the cell.
     * @return true if the place is passable
     */
    boolean isPassable(Location loc, WarUnit unit) {
            if (ignoreObstacles(loc, unit)) {
                return true;
            }
            Set<WarUnit> wunits = unitsForPathfinding.get(loc);
            if (wunits == null) {
                return true;
            }
            if (wunits.isEmpty()) {
                return true;
            }
            boolean ip = false;
            for (WarUnit u : wunits) {
                ip = ((u.owner() != unit.owner()) && (unit.attackMoveLocation() != null))
                        || ((u.owner() == unit.owner()) && (u.inMotion() || needsRotation(u, u.getNextMove())))
                        || u.equals(unit)
                        || u.isDestroyed();
            }
            return ip;
    }

    /**
     * Plan a new route to the current destination.
     * @param u the unit.
     */
    void repath(final WarUnit u) {
        if (u.getPath().size() >= 1) {
            final Location goal = u.getPath().peekLast();
            clearUnitGoal(u);
            u.clearNextMove();
            u.clearNextRotate();
            pathPlanner.addPathPlanning(u, goal);
        }
    }

    /**
     * Check if pathfinding restrictions can be ignored in a location.
     * @param loc the location to ignore.
     * @param unit the unit.
     * @return true if all obstacles can be ignored on the location
     */
    boolean ignoreObstacles(Location loc, WarUnit unit) {
        // If somehow got into this situation where it's own location is impassable
        if (loc.equals(unit.location())) {
            return true;
        }
        return false;
    }
    /**
     * Add an extra location to the unit's planned path.
     * @param unit the unit.
     * @param loc the location to add to unit path.
     */
    protected void addToUnitPath(WarUnit unit, Location loc) {
        synchronized (pathWeightMap) {
            pathWeightMap.weightMap[loc.x + pathWeightMap.offsetX][loc.y + pathWeightMap.offsetY]++;
        }
        unit.getPath().add(loc);
    }
    /**
     * Remove the first location in the unit's planned path.
     * @param unit the unit.
     */
    protected void removeFirstFromUnitPath(WarUnit unit) {
        Location loc = unit.getPath().removeFirst();
        synchronized (pathWeightMap) {
            pathWeightMap.weightMap[loc.x + pathWeightMap.offsetX][loc.y + pathWeightMap.offsetY]--;
        }
        if (unit.getPath().isEmpty()) {
            unit.setHasPlannedMove(false);
        }
    }
}
