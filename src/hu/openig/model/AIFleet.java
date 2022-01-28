/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a fleet for an AI player.
 * @author akarnokd, 2011.12.08.
 */
public class AIFleet {
    /** The original fleet object. */
    public Fleet fleet;
    /** The current fleet knowledge. */
    public FleetKnowledge knowledge;
    /** The current fleet statistics. */
    public FleetStatistics statistics;
    /** The radar range. */
    public int radar;
    /** The target planet. */
    public Planet targetPlanet;
    /** The target point. */
    public Point2D.Double targetPoint;
    /** The fleet mode. */
    public FleetMode mode;
    /** The current location. */
    public double x;
    /** The current location. */
    public double y;
    /** The current task. */
    public FleetTask task;
    /** The inventory. */
    public final List<AIInventoryItem> inventory = new ArrayList<>();
    /** The inventory counts. */
    public final Map<ResearchType, Integer> inventoryCounts = new HashMap<>();
    /**
     * Assign the necessary properties from a fleet.
     * @param fleet the target fleet
     * @param world the world object
     */
    public void assign(Fleet fleet, AIWorld world) {
        this.fleet = fleet;
        knowledge = world.knowledge(fleet);
        this.statistics = world.getStatistics(fleet);
        this.radar = fleet.radar;
        targetPlanet = fleet.targetPlanet();
        if (fleet.waypoints.size() > 0) {
            targetPoint = fleet.waypoints.get(0);
        }
        mode = fleet.mode;
        task = fleet.task;
        x = fleet.x;
        y = fleet.y;
        for (InventoryItem ii : fleet.inventory.iterable()) {
            inventory.add(new AIInventoryItem(ii));
            Integer v = inventoryCounts.get(ii.type);
            inventoryCounts.put(ii.type, v != null ? v + ii.count : ii.count);
            for (InventorySlot is : ii.slots.values()) {
                if (is.type != null && !is.slot.fixed) {
                    v = inventoryCounts.get(is.type);
                    inventoryCounts.put(is.type, v != null ? v + is.count * ii.count : is.count * ii.count);
                }
            }
        }
    }
    /**
     * Returns the inventory count if the specified ship, equipment or tank.
     * @param rt the technology
     * @return the count
     */
    public int inventoryCount(ResearchType rt) {
        Integer v = inventoryCounts.get(rt);
        return v != null ? v : 0;
    }
    /**
     * Check if a specific technology is in the inventory.
     * @param rt the technology
     * @return true if present
     */
    public boolean hasInventory(ResearchType rt) {
        for (AIInventoryItem ii : inventory) {
            if (ii.type == rt) {
                return true;
            }
        }
        return false;
    }
    /**
     * Check if a specific technology is in the inventory.
     * @param id the technology id
     * @return true if present
     */
    public boolean hasInventory(String id) {
        for (AIInventoryItem ii : inventory) {
            if (ii.type.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Check if a specific technology is in the inventory.
     * @param cat the technology category
     * @return true if present
     */
    public boolean hasInventory(ResearchSubCategory cat) {
        for (AIInventoryItem ii : inventory) {
            if (ii.type.category == cat) {
                return true;
            }
        }
        return false;
    }
    /**
     * @return true if moving
     */
    public boolean isMoving() {
        return mode == FleetMode.MOVE;
    }
    /** @return true if attacking. */
    public boolean isAttacking() {
        return mode == FleetMode.ATTACK;
    }
    /**
     * Returns the current radar level (0..3).
     * @return the radar level
     */
    public int radarLevel() {
        int n = fleet.owner.world.params().fleetRadarUnitSize();
        if (radar < n) {
            return 0;
        }
        return radar / n;
    }
}
