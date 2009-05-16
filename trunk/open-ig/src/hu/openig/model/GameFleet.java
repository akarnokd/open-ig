/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

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
	/** The fleet's radar radius. Zero means no radar. */
	public int radarRadius;
	/** The fleet's movement speed. */
	public int speed;
	/** The user given name of the fleet. */
	public String name;
	/** The fleet's X coordinate on the starmap. Used as the central position of the icon. */
	public int x;
	/** The fleet's Y coordinate on the starmap. Used as the central position of the icon. */
	public int y;
	/** The visibility of the fleet. Can be used to hide fleets. */
	public boolean visible = true;
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
}
