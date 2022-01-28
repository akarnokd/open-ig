/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.utils.Exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The record for per planet persistent deployed units
 * and turrets.
 * @author akarnokd, 2013.06.01.
 */
public class PlanetGround {
    /** The ground war units. */
    public final List<GroundwarUnit> units = new ArrayList<>();
    /** The guns. */
    public final List<GroundwarGun> guns = new ArrayList<>();
    /** The current animating explosions. */
    public final Set<GroundwarExplosion> explosions = new HashSet<>();
    /** The active rockets. */
    public final Set<GroundwarRocket> rockets = new HashSet<>();
    /** The helper map to list ground units to be rendered at a specific location. */
    public final Map<Location, Set<GroundwarUnit>> unitsAtLocation = new HashMap<>();
    /** The mine locations. */
    public final Map<Location, Mine> mines = new HashMap<>();
    /** Set of minelayers currently placing a mine. */
    public final Set<GroundwarUnit> minelayers = new HashSet<>();
    /** The current player's selected units. */
    public final Set<GroundwarUnit> selectedUnits = new HashSet<>();
    /** The current player's selected guns. */
    public final Set<GroundwarGun> selectedGuns = new HashSet<>();
    /** The map for pathfinding passability check. */
    public final Map<Location, Set<GroundwarUnit>> unitsForPathfinding = new HashMap<>();
    /** The grouping of structures. */
    public final Map<Object, Integer> groups = new HashMap<>();
    /**
     * Remove a destroyed unit from the location helper.
     * @param u the unit to remove
     */
    public void removeUnitLocation(GroundwarUnit u) {
        Location loc = unitLocation(u);
        Set<GroundwarUnit> set = unitsAtLocation.get(loc);
        if (set != null) {
            if (!set.remove(u)) {
                Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", loc.x, loc.y)));
            }
        }
        // remove from pathfinding helper
        Location pfl = u.location();
        set = unitsForPathfinding.get(pfl);
        if (set != null) {
            if (!set.remove(u)) {
                Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", pfl.x, pfl.y)));
            }
        }
    }
    /**
     * Returns the given unit's rendering location in cell coordinates.
     * @param u the unit
     * @return the the rendering location
     */
    public Location unitLocation(GroundwarUnit u) {
        return Location.of((int)Math.floor(u.x - 1), (int)Math.floor(u.y - 1));
    }
    /**
     * Update the location of the specified unit by the given amount.
     * @param u the unit to move
     * @param dx the delta X to move
     * @param dy the delta Y to move
     * @param relative is this a relative move
     */
    public void updateUnitLocation(GroundwarUnit u, double dx, double dy, boolean relative) {
        Location current = unitLocation(u);
        Location pfl = u.location();

        if (relative) {
            u.x += dx;
            u.y += dy;
        } else {
            u.x = dx;
            u.y = dy;
        }
        Location next = unitLocation(u);
        Location pfl2 = u.location();

        if (!current.equals(next)) {
            Set<GroundwarUnit> set = unitsAtLocation.get(current);
            if (set != null) {
                if (!set.remove(u)) {
                    Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", current.x, current.y)));
                }
                if (unitsAtLocation.isEmpty()) {
                    unitsAtLocation.remove(current);
                }
            }

        }
        if (!pfl.equals(pfl2)) {
            // remove from pathfinding helper
            Set<GroundwarUnit> set = unitsForPathfinding.get(pfl);
            if (set != null) {
                if (!set.remove(u)) {
                    Exceptions.add(new AssertionError(String.format("Unit was not found at location %s, %s%n", pfl.x, pfl.y)));
                }
                if (set.isEmpty()) {
                    unitsForPathfinding.remove(pfl);
                }
            }
        }

        Set<GroundwarUnit> set = unitsAtLocation.get(next);
        if (set == null) {
            set = new HashSet<>();
            unitsAtLocation.put(next, set);
        }
        set.add(u);

        set = unitsForPathfinding.get(pfl2);
        if (set == null) {
            set = new HashSet<>();
            unitsForPathfinding.put(pfl2, set);
        }
        set.add(u);
    }
    /**
     * Add a unit with its current location to the mapping.
     * @param u the unit to move
     */
    public void addUnitLocation(GroundwarUnit u) {
        Location current = unitLocation(u);
        Set<GroundwarUnit> set = unitsAtLocation.get(current);
        if (set == null) {
            set = new HashSet<>();
            unitsAtLocation.put(current, set);
        }
        set.add(u);

        Location pfl = u.location();
        set = unitsForPathfinding.get(pfl);
        if (set == null) {
            set = new HashSet<>();
            unitsForPathfinding.put(pfl, set);
        }
        set.add(u);
    }
    /**
     * Remove the given gun from this container.
     * @param g the gun to remove
     */
    public void remove(GroundwarGun g) {
        guns.remove(g);
        selectedGuns.remove(g);
    }
    /**
     * Removes an unit from this container.
     * @param u the unit to remove
     */
    public void remove(GroundwarUnit u) {
        units.remove(u);
        selectedUnits.remove(u);
        removeUnitLocation(u);
    }
    /**
     * Returns an unit at the specified location or null.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the ground war unit or null if no unit is there
     */
    public GroundwarUnit unitAt(int x, int y) {
        for (GroundwarUnit u1 : units) {
            if ((int)u1.x == x && (int)u1.y == y) {
                return u1;
            }
        }
        return null;
    }
    /**
     * Assign the selected units to a group.
     * @param groupNo the group number
     * @param player the target player
     */
    public void assignGroup(int groupNo, Player player) {
        List<Owned> selected = new ArrayList<>();
        boolean own = false;
        boolean enemy = false;
        for (GroundwarUnit u : selectedUnits) {
            own |= u.owner() == player;
            enemy |= u.owner() != player;
            selected.add(u);
        }
        for (GroundwarGun g : selectedGuns) {
            own |= g.owner() == player;
            enemy |= g.owner() != player;
            selected.add(g);
        }

        removeGroup(groupNo);

        if (own && !enemy) {
            for (Object o : selected) {
                groups.put(o, groupNo);
            }
        }
    }
    /**
     * Reselect the units of the saved group.
     * @param groupNo the group number
     */
    public void recallGroup(int groupNo) {
        selectedUnits.clear();
        for (GroundwarUnit u : units) {
            Integer gr = groups.get(u);
            if (gr != null && gr == groupNo) {
                selectedUnits.add(u);
            }
        }
        for (GroundwarGun g : guns) {
            Integer gr = groups.get(g);
            if (gr != null && gr == groupNo) {
                selectedGuns.add(g);
            }
        }
    }
    /**
     * Remove units from group.
     * @param i the group index
     */
    public void removeGroup(int i) {
        Iterator<Map.Entry<Object, Integer>> it = groups.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Integer> e = it.next();
            if (e.getValue() == i) {
                it.remove();
            }
        }
    }
    /**

     * Deselect all units.

     */
    public void deselectAll() {
        selectedGuns.clear();
        selectedUnits.clear();
    }
    /**

     * Select all of the player's units.
     * @param player the target player

     */
    public void selectAll(Player player) {
        deselectAll();
        for (GroundwarUnit u : units) {
            if (u.owner == player) {
                selectedUnits.add(u);
            }
        }
        for (GroundwarGun g : guns) {
            if (g.owner == player) {
                selectedGuns.add(g);
            }
        }
    }
    /**
     * Deselect the given unit.
     * @param u the unit to select
     */
    public void deselect(GroundwarUnit u) {
        selectedUnits.remove(u);
    }
    /**
     * Deselect the given gun.
     * @param g the gun to select
     */
    public void deselect(GroundwarGun g) {
        selectedGuns.remove(g);
    }
    /**
     * Select the given unit.
     * @param u the unit to select
     */
    public void select(GroundwarUnit u) {
        selectedUnits.add(u);
    }
    /**
     * Select the given gun.
     * @param g the gun to select
     */
    public void select(GroundwarGun g) {
        selectedGuns.add(g);
    }
    /**
     * Check if the given unit is selected.
     * @param u the unit to test
     * @return true if selected
     */
    public boolean isSelected(GroundwarUnit u) {
        return selectedUnits.contains(u);
    }
    /**
     * Check if the given gun is selected.
     * @param g the gun to test
     * @return true if selected
     */
    public boolean isSelected(GroundwarGun g) {
        return selectedGuns.contains(g);
    }
    /**
     * Select the given unit.
     * @param u the unit to select
     * @param state the new selection state
     */
    public void select(GroundwarUnit u, boolean state) {
        if (state) {
            selectedUnits.add(u);
        } else {
            selectedUnits.remove(u);
        }
    }
    /**
     * Select the given gun.
     * @param g the gun to select
     * @param state the new selection state
     */
    public void select(GroundwarGun g, boolean state) {
        if (state) {
            selectedGuns.add(g);
        } else {
            selectedGuns.remove(g);
        }
    }
    /**
     * Add a ground war unit.
     * @param u the unit
     */
    public void add(GroundwarUnit u) {
        units.add(u);
        addUnitLocation(u);
    }
    /**
     * Counts the units of the given owner.
     * @param owner the owner
     * @return the count
     */
    public int countUnits(Player owner) {
        int i = 0;
        for (GroundwarUnit u : units) {
            if (u.owner == owner) {
                i++;
            }
        }
        return i;
    }
    /**
     * Remove the gun from the specified index.
     * @param index the index
     */
    public void removeGun(int index) {
        GroundwarGun g = guns.remove(index);
        selectedGuns.remove(g);
    }
}
