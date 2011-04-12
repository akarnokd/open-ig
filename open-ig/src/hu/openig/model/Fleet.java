/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A fleet.
 * @author akarnokd, 2010.01.07.
 */
public class Fleet implements Named, Owned {
	/** The unique fleet identifier. */
	public int id;
	/** The owner of the fleet. */
	public Player owner;
	/** The X coordinate. */
	public int x;
	/** The Y coordinate. */
	public int y;
	/** The associated ship icon. */
	public BufferedImage shipIcon;
	/** The radar radius. */
	public int radar;
	/** The fleet name. */
	public String name;
	/** The fleet inventory: ships and tanks. */
	public final List<FleetInventoryItem> inventory = new ArrayList<FleetInventoryItem>();
	/** The current list of movement waypoints. */
	public final List<Point> waypoints = new ArrayList<Point>();
	/** If the fleet should follow the other fleet. */
	public Fleet targetFleet;
	/** If the fleet should move to the planet. */
	public Planet targetPlanet;
	/** The movement mode. */
	public enum FleetMode {
		/** Simply move there. */
		MOVE,
		/** Attack the target. */
		ATTACK
	}
	/** The fleet movement mode. */
	public FleetMode mode;
	/** @return calculate the fleet statistics. */
	public FleetStatistics getStatistics() {
		FleetStatistics result = new FleetStatistics();
		
		return result;
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Player owner() {
		// TODO Auto-generated method stub
		return owner;
	}
	/**
	 * Returns the number of items of the give research type.
	 * @param rt the research type to count
	 * @return the count
	 */
	public int inventoryCount(ResearchType rt) {
		int count = 0;
		for (FleetInventoryItem pii : inventory) {
			if (pii.type == rt) {
				count += pii.count;
			}
		}
		return count;
	}
}
