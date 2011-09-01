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
 * An animation for a space explosion.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarExplosion extends SpacewarObject {
	/** The phase counter for the animation if any. */
	public int phase;
	/** The phase image of the beam (e.g., the rotating meson bubble). */
	public BufferedImage[] phases;
	/** The structure to remove when the explosion is at the middle. */
	public SpacewarStructure target;
	@Override
	public BufferedImage get() {
		return phases[phase % phases.length];
	}
	/** @return true if the animation is at the middle. */
	public boolean isMiddle() {
		return (phase % phases.length) == phases.length / 2;
	}
}
