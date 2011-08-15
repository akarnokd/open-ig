/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Graphics2D;

/**
 * A base class for space objects (beams, projectiles, ships, ground defenses).
 * @author akarnokd, 2011.08.15.
 */
public abstract class SpaceObject {
	/** The current location. */
	public double x;
	/** The current location. */
	public double y;
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	/** The owner player. */
	public Player owner;
	/**
	 * Draw the space object to the given coordinates.
	 * @param g2 the graphics context
	 * @param x the center of the beam
	 * @param y the center of the beam
	 */
	public abstract void draw(Graphics2D g2, int x, int y);
}
