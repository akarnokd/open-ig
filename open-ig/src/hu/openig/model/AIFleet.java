/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

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
	/** The target fleet. */
	public Fleet targetFleet;
	/** The target planet. */
	public Planet targetPlanet;
	/** The target planet. */
	public Planet arrivedAt;
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
	public final List<AIInventoryItem> inventory = new ArrayList<AIInventoryItem>();
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
		targetFleet = fleet.targetFleet;
		targetPlanet = fleet.targetPlanet();
		arrivedAt = fleet.arrivedAt;
		if (fleet.waypoints.size() > 0) {
			targetPoint = fleet.waypoints.get(0);
		}
		mode = fleet.mode;
		task = fleet.task;
		x = fleet.x;
		y = fleet.y;
		for (InventoryItem ii : fleet.inventory) {
			inventory.add(new AIInventoryItem(ii));
		}
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
}
