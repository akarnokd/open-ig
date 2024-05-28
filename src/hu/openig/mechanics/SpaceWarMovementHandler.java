/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Func1;
import hu.openig.core.Location;
import hu.openig.core.Pathfinding;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.WarUnit;
import hu.openig.utils.Exceptions;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class for handling pathfinding and movement of space war ships.
 * All ships have the size 1x1 with big ships(battleships and cruisers) having a 3x3 exclusion
 * zone for other big ships. Fighters ignore this zone.
 */
public class SpaceWarMovementHandler extends SimpleSpaceWarMovementHandler {

    public SpaceWarMovementHandler(ScheduledExecutorService commonExecutorPool, int cellSize, int simulationDelay, List<? extends WarUnit> units, int gridSizeX, int gridSizeY) {
        super(commonExecutorPool, cellSize, simulationDelay, units, gridSizeX, gridSizeY);
    }

    @Override
    public void initUnits() {
        for (WarUnit wunit : units) {
            Location pfl = wunit.location();
            Set<WarUnit> set = unitsForPathfinding.get(pfl);
            if (set == null) {
                set = new HashSet<>();
                unitsForPathfinding.put(pfl, set);
            }
            set.add(wunit);
            SpacewarStructure sws = (SpacewarStructure) wunit;
            if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                sws.setPathingMethod(getFighterPathfinding(sws));
            } else if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS || sws.item.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
                sws.setPathingMethod(getCapShipPathfinding(sws));
            } else if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                // Stations have no exclusion zone, they are just big and occupy a 3x3 space.
                for (Location neighbor : sws.location().getListOfNeighbors()) {
                    set = unitsForPathfinding.get(neighbor);
                    if (set == null) {
                        set = new HashSet<>();
                        unitsForPathfinding.put(neighbor, set);
                    }
                    set.add(sws);
                }
            }
        }
    }

    @Override
    public void removeUnit(WarUnit unit) {
        super.removeUnit(unit);
        if (((SpacewarStructure) unit).item.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
            for (Location locToRemove : unit.location().getListOfNeighbors()) {
                Set<WarUnit> set = unitsForPathfinding.get(locToRemove);
                if (set != null) {
                    if (!set.remove(unit)) {
                        Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", locToRemove.x, locToRemove.y)));
                    }
                }
            }
        }
    }
    @Override
    boolean reserveCellFor(WarUnit unit) {
        SpacewarStructure sws = (SpacewarStructure) unit;
        if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
            return super.reserveCellFor(unit);
        } else {
            return reserveCellForCapitalShip(unit);
        }
    }
    @Override
    boolean isCellReserved(Location loc, WarUnit unit) {
        SpacewarStructure sws = (SpacewarStructure) unit;
        if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
            return super.isCellReserved(loc, unit);
        } else {
            return isCellReservedForCapitalShip(loc, unit);
        }
    }
    /**
     * Reserve the next movement cell for the capital ship.
     * @param unit the unit
     * @return true if the next cell is successfully reserved
     */
    boolean reserveCellForCapitalShip(WarUnit unit) {
        if (unitsForPathfinding.get(unit.getNextMove()) != null) {
            for (WarUnit wunit : unitsForPathfinding.get(unit.getNextMove())) {
                if (wunit != unit) {
                    return false;
                }
            }
        } else if (isCellReserved(unit.getNextMove(), unit)) {
            return false;
        }
        //Check the surrounding cells ignoring fighters
        ArrayList<Location> locationsToCheck = unit.getNextMove().getListOfNeighbors();
        locationsToCheck.remove(unit.location());
        locationsToCheck.removeAll(unit.location().getListOfNeighbors());
        for (Location loc : locationsToCheck) {
            if (unitsForPathfinding.get(loc) != null) {
                for (WarUnit wunit : unitsForPathfinding.get(loc)) {
                    if (wunit != unit && ((SpacewarStructure) wunit).item.type.category != ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                        return false;
                    }
                }
            }
        }
        if (isCellReservedForCapitalShip(unit.getNextMove(), unit)) {
            return false;
        }

        reservedCells.put(unit.getNextMove(), unit);
        return true;
    }
    /**
     * Check if a cell is reserved for a capital ship.
     * @param loc the location to check
     * @param unit the unit
     * @return true if the cell is already reserved
     */
    boolean isCellReservedForCapitalShip(Location loc, WarUnit unit) {
        //Check the surrounding cells ignoring fighters
        SpacewarStructure sws = (SpacewarStructure) unit;
        if (reservedCells.get(loc) != null && reservedCells.get(loc) != unit) {
            return true;
        }
        ArrayList<Location> locationsToCheck = loc.getListOfNeighbors();
        // Do not check currently occupied locations.
        locationsToCheck.remove(unit.location());
        locationsToCheck.removeAll(unit.location().getListOfNeighbors());
        for (Location locToCheck : locationsToCheck) {
            if (reservedCells.get(locToCheck) != null && reservedCells.get(locToCheck) != unit && ((SpacewarStructure)reservedCells.get(locToCheck)).item.type.category != ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean isPassable(Location loc, WarUnit unit) {
        SpacewarStructure sws = (SpacewarStructure) unit;
        if (sws.item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
            return isPassableForFighter(loc, unit);
        } else {
            boolean ip = isPassableForCapitalShip(loc, unit);
            return ip;
        }
    }

    /**
     * Returns a pathfinding used by fighter typed ships.
     * @param unit the unit doing the pathfinding
     * @return the pathfinding object
     */
    Pathfinding getFighterPathfinding(final WarUnit unit) {
        Pathfinding pathfinding = new Pathfinding();
        pathfinding.isPassable = new Func1<Location, Boolean>() {
            @Override
            public Boolean invoke(Location value) {
                return isPassableForFighter(value, unit);
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

    boolean isPassableForFighter(Location loc, WarUnit unit) {
        return super.isPassable(loc, unit);
    }

    /**
     * Returns a pathfinding used by capital ships(battleships cruisers destroyers etc.).
     * @param unit the unit doing the pathfinding
     * @return the pathfinding object
     */
    Pathfinding getCapShipPathfinding(final WarUnit unit) {
        Pathfinding pathfinding = new Pathfinding();
        pathfinding.isPassable = new Func1<Location, Boolean>() {
            @Override
            public Boolean invoke(Location value) {
                return isPassableForCapitalShip(value, unit);
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

    boolean isPassableForCapitalShip(Location loc, WarUnit unit) {
        boolean ip = super.isPassable(loc, unit);
        if (!ip) {
            return false;
        }
        for (Location neighbor : loc.getListOfNeighbors()) {
            Set<WarUnit> wunits = unitsForPathfinding.get(neighbor);
            if (wunits == null) {
                continue;
            }
            if (wunits.isEmpty()) {
                continue;
            }
            boolean isFighter = false;
            for (WarUnit u : wunits) {
                isFighter = ((SpacewarStructure)u).item.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS;
            }
            if (isFighter) {
                continue;
            }
            if (!super.isPassable(neighbor, unit)) {
                return false;
            }
        }
        return ip;
    }
}
