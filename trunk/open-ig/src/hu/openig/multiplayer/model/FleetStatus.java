/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer.model;

import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetTask;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for a fleet status.
 * @author akarnokd, 2013.04.27.
 */
public class FleetStatus {
	/** The fleet's unique identifier. */
	public int id;
	/** The knowledge about the fleet. */
	public FleetKnowledge knowledge;
	/** The fleet's owner. */
	public String owner;
	/** The current location. */
	public double x;
	/** The current location. */
	public double y;
	/** The fleet name. */
	public String name;
	/** The current list of movement waypoints. */
	public final List<Point2D.Double> waypoints = new ArrayList<Point2D.Double>();
	/** If the fleet should follow the other fleet. */
	public int targetFleet;
	/** If the fleet should move to the planet. */
	public String targetPlanet;
	/** If the fleet was moved to a planet. */
	public String arrivedAt;
	/** The fleet movement mode. */
	public FleetMode mode;
	/** The current task. */
	public FleetTask task;
	/** Refill once. */
	public boolean refillOnce;
	/**
	 * The inventory.
	 */
	public final List<InventoryItemStatus> inventory = new ArrayList<InventoryItemStatus>();
}
