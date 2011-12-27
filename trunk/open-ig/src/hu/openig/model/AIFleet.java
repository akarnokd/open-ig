/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;

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
	/** The target point. */
	public Point2D.Double targetPoint;
	/** The fleet mode. */
	public FleetMode mode;
	/** The current location. */
	public double x;
	/** The current location. */
	public double y;
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
		if (fleet.waypoints.size() > 0) {
			targetPoint = fleet.waypoints.get(0);
		}
		mode = fleet.mode;
		x = fleet.x;
		y = fleet.y;
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
