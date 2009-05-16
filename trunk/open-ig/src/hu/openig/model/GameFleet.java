/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.FleetStatus;

import java.awt.Point;
import java.util.Comparator;

/**
 * Object to represent a space fleet.
 * @author karnokd, 2009.05.15.
 * @version $Revision 1.0$
 */
public class GameFleet {
	/** The owner player. */
	public GamePlayer owner;
	/** The user given name of the fleet. */
	public String name;
	/** The fleet's X coordinate on the starmap. Used as the central position of the icon. */
	public int x;
	/** The fleet's Y coordinate on the starmap. Used as the central position of the icon. */
	public int y;
	/** The visibility of the fleet. Can be used to hide fleets. */
	public boolean visible = true;
	/** The fleet status. */
	public FleetStatus status = FleetStatus.STOP;
	/** Non-null value indicates a movement to that logical point. */
	public Point moveTarget;
	/** If attack mode, contains the target planet. */
	public GamePlanet attackPlanet;
	/** If attack mode, contains the target fleet. */
	public GameFleet attackFleet;
	/** 
	 * Returns the planet's logical coordinates as point.
	 * @return the logical location as point
	 */
	public Point getPoint() {
		return new Point(x, y);
	}
	/** Fleet comparator by name ascending. */
	public static final Comparator<GameFleet> BY_NAME_ASC = new Comparator<GameFleet>() {
		@Override
		public int compare(GameFleet o1, GameFleet o2) {
			return o1.name.compareTo(o2.name);
		}
	};
	/** Fleet comparator by name descending. */
	public static final Comparator<GameFleet> BY_NAME_DESC = new Comparator<GameFleet>() {
		@Override
		public int compare(GameFleet o1, GameFleet o2) {
			return o2.name.compareTo(o1.name);
		}
	};
	/**
	 * @return the number of battleships in this fleet
	 */
	public int getBattleshipCount() {
		// TODO evaluate fleet contents correctly
		return 0;
	}
	/**
	 * @return the number of destroyers in this fleet
	 */
	public int getDestroyerCount() {
		// TODO evaluate fleet contents correctly
		return 0;
	}
	/**
	 * @return the number of fighters in this fleet
	 */
	public int getFighterCount() {
		// TODO evaluate fleet contents correctly
		return 0;
	}
	/**
	 * @return the number of tanks in this fleet
	 */
	public int getTankCount() {
		// TODO evaluate fleet contents correctly
		return 0;
	}
	/**
	 * @return the fleet's maximum speed
	 */
	public int getSpeed() {
		// TODO evaluate fleet contents correctly
		return 6;
	}
	/**
	 * @return the fleet's radar radius
	 */
	public int getRadarRadius() {
		// TODO evaluate fleet contents correctly
		return 50;
	}
	/**
	 * @return the fleet's current firepower
	 */
	public int getFirepower() {
		return 1;
	}
}
