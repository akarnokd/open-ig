/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;

import java.util.List;
import java.util.Set;

/**
 * Interface to describe the ground war state and allow issuing orders to units for AI players and Scripting.
 * @author akarnokd, 2012.01.13.
 */
public interface GroundwarWorld {
    /**
     * @return the battle object
     */
    BattleInfo battle();
    /**
     * Set of locations where the player could place its units.
     * @param player the player
     * @return the set of locations
     */
    Set<Location> placementOptions(Player player);
    /** @return the planet. */
    Planet planet();
    /** @return The list of units. */
    List<GroundwarUnit> units();
    /** @return The list of guns. */
    List<GroundwarGun> guns();
    /**
     * Stop the unit.
     * @param u the unit to stop
     */
    void stop(GroundwarUnit u);
    /**
     * Stop the gun.
     * @param g the gun to stop
     */
    void stop(GroundwarGun g);
    /**
     * Issue attack order for the target.
     * @param u the unit
     * @param target the target
     */
    void attack(GroundwarUnit u, GroundwarUnit target);
    /**
     * Issue attack order for the target.
     * @param u the unit
     * @param target the target
     */
    void attack(GroundwarUnit u, Building target);
    /**
     * Issue attack order for the target.
     * @param g the gun
     * @param target the target
     */
    void attack(GroundwarGun g, GroundwarUnit target);
    /**
     * Move unit to location.
     * @param u the unit
     * @param x the location X
     * @param y the location Y
     */
    void move(GroundwarUnit u, int x, int y);
    /**
     * Activate the special ability of the unit.
     * @param u the unit
     */
    void special(GroundwarUnit u);
    /**
     * Check if the given location already has mines.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return true
     */
    boolean hasMine(int x, int y);
    /**
     * Check if the given map coordinate is passable.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return true if passable
     */
    boolean isPassable(int x, int y);
}
