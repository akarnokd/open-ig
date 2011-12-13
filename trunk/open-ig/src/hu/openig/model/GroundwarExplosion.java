/*
 * Copyright 2008-2012, David Karnok 
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
	/** The explosion location (center). */
	public int x;
	/** The explosion location (center). */
	public int y;
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
	
	/**
	 * Test if the explosion is within the given rectangle.
	 * @param px the center point
	 * @param py the center point
	 * @param pw the width
	 * @param ph the height
	 * @return true if within
	 */
	public boolean within(int px, int py, int pw, int ph) {
		int s = get().getWidth();
		int ax0 = x - s / 2;
		int ax1 = ax0 + s - 1;
		int ay0 = y - s / 2;
		int ay1 = ay0 + s - 1;
		
		int bx0 = px - pw / 2;
		int bx1 = bx0 + s - 1;
		int by0 = py - ph / 2;
		int by1 = by0 + s - 1;
		
		return !(ax1 < bx0 || bx1 < ax0 || ay1 < by0 || by1 < ay0);
	}
}
