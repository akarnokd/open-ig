/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * @author akarnokd, 2011.09.07.
 *
 */
public class GroundwarExplosion {
	/** The X surface coordinate, center of the explosion. */
	public double x;
	/** The Y surface coordinate, center of the explosion. */
	public double y;
	/** The explosion phase. */
	public int phase;
	/** The available phases. */
	public BufferedImage[] phases;
	/** The target of the explosion. */
	public GroundwarUnit target;
	/** @return the image of the current phase. */
	public BufferedImage get() {
		return phases[phase];
	}
	/** @return move to the next phase and return true if more phases are available. */
	public boolean next() {
		return ++phase < phases.length;
	}
	/** @return Is the current phase the half? */
	public boolean half() {
		return phase == phases.length / 2;
	}
}
