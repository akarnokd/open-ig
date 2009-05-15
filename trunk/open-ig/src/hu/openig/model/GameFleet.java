/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

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
}
