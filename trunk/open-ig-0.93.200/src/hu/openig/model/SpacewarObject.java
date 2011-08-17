/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * A base class for space objects (beams, projectiles, ships, ground defenses).
 * @author akarnokd, 2011.08.15.
 */
public abstract class SpacewarObject {
	/** The current location. */
	public double x;
	/** The current location. */
	public double y;
	/** The owner player. */
	public Player owner;
	/**
	 * Get the image of this space object.
	 * @return the image
	 */
	public abstract BufferedImage get();
}
