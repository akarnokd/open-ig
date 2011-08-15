/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * An animation for a space explosion.
 * @author akarnokd, 2011.08.15.
 */
public class SpaceExplosion extends SpaceObject {
	/** The phase counter for the animation if any. */
	public int phase;
	/** The phase image of the beam (e.g., the rotating meson bubble). */
	public BufferedImage[] phases;
	@Override
	public void draw(Graphics2D g2, int x, int y) {
		BufferedImage img = phases[phase];
		g2.drawImage(img, x - img.getWidth() / 2, y - img.getHeight() / 2, null);
	}
}
