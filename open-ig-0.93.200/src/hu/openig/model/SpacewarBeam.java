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
 * A straight travelling beam (laser, etc.).
 * Once created, beams travel with the same graphics angle.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarBeam extends SpacewarObject {
	/** The beam speed per simulation tick. */
	public double speed;
	/** The phase counter for the animation if any. */
	public int phase;
	/** The phase image of the beam (e.g., the rotating meson bubble). */
	public BufferedImage[] phases;
	/** The damage to inflict. */
	public int damage;
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	@Override
	public BufferedImage get() {
		return phases[phase % phases.length];
	}
	/** Move the beam to the next location. */
	public void move() {
		x += speed * Math.cos(angle);
		y += speed * Math.sin(angle);
		phase = (phase + 1) % phases.length;
	}
}
