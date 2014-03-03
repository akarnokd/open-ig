/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for a fleet status.
 * @author akarnokd, 2013.04.27.
 */
public class FleetStatus implements MessageObjectIO, MessageArrayItemFactory<FleetStatus> {
	/** Array name. */
	public static final String ARRAY_NAME = "FLEET_STATUSES";
	/** The object name. */
	public static final String OBJECT_NAME = "FLEET";
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
	public final List<Point2D.Double> waypoints = new ArrayList<>();
	/** If the fleet should follow the other fleet. */
	public Integer targetFleet;
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
	/** The default fleet formation. */
	public int formation;
	/** The source of the infection of this fleet if not null. */
	public String infectedBy;
	/** The inventory. */
	public final List<InventoryItemStatus> inventory = new ArrayList<>();
	@Override
	public void fromMessage(MessageObject mo) {
		id = mo.getInt("id");
		knowledge = mo.getEnum("knowledge", FleetKnowledge.values());
		owner = mo.getString("owner");
		x = mo.getDouble("x");
		y = mo.getDouble("y");
		name = mo.getString("name");
		
		for (MessageObject wp : mo.getArray("waypoints").objects()) {
			waypoints.add(new Point2D.Double(wp.getDouble("x"), wp.getDouble("y")));
		}
		
		targetFleet = mo.getIntObject("targetFleet");
		targetPlanet = mo.getStringObject("targetPlanet");
		arrivedAt = mo.getStringObject("arrivedAt");
		mode = mo.getEnumObject("mode", FleetMode.values());
		task = mo.getEnum("task", FleetTask.values());
		refillOnce = mo.getBoolean("refillOnce");
		formation = mo.getInt("formation");
		infectedBy = mo.getStringObject("infectedBy");
		
		for (MessageObject mio : mo.getArray("inventory").objects()) {
			InventoryItemStatus iis = new InventoryItemStatus();
			iis.fromMessage(mio);
			inventory.add(iis);
		}
	}
	@Override
	public MessageObject toMessage() {
		MessageObject result = new MessageObject(objectName());
		
		result.set("id", id);
		result.set("knowledge", knowledge);
		result.set("owner", owner);
		result.set("x", x);
		result.set("y", y);
		result.set("name", name);
		
		MessageArray ma = new MessageArray(null);
		result.set("waypoints", ma);
		for (Point2D.Double pt : waypoints) {
			ma.add(new MessageObject("POINT").set("x", pt.x).set("y", pt.y));
		}
		result.set("targetFleet", targetFleet);
		result.set("targetPlanet", targetPlanet);
		result.set("arrivedAt", arrivedAt);
		result.set("mode", mode);
		result.set("task", task);
		result.set("refillOnce", refillOnce);
		result.set("formation", formation);
		result.set("infectedBy", infectedBy);
		
		MessageArray ia = new MessageArray(null);
		result.set("inventory", ia);
		for (InventoryItemStatus iis : inventory) {
			ia.add(iis.toMessage());
		}
		
		return result;
	}
	@Override
	public FleetStatus invoke() {
		return new FleetStatus();
	}
	@Override
	public String objectName() {
		return OBJECT_NAME;
	}
	@Override
	public String arrayName() {
		return ARRAY_NAME;
	}
	/**
	 * Convert the list of fleet statuses into a message array.
	 * @param list the list
	 * @return the message array
	 */
	public static MessageArray toArray(Iterable<? extends FleetStatus> list) {
		MessageArray ma = new MessageArray(ARRAY_NAME);
		for (FleetStatus fs : list) {
			ma.add(fs.toMessage());
		}
		return ma;
	}
}
