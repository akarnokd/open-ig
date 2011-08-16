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
 * A projectile representing a rocket or bomb, 
 * capable of changing directions in flight.
 * They don't have any animation phase in general.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarProjectile extends SpacewarObject {
	/** The beam speed per simulation tick. */
	public double speed;
	/** The angle images of the projectile. */
	public BufferedImage[] angles;
	/** ECM distraction limit 1..2 .*/
	public int ecmLimit;
	/** The damage to inflict. */
	public int damage;
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	@Override
	public BufferedImage get() {
		double a = angle / 2 / Math.PI; // angle to percentage
		return angles[((int)Math.round(angles.length * a)) % angles.length];
	}
	/** Move the beam to the next location. */
	public void move() {
		x += speed * Math.cos(angle);
		y += speed * Math.sin(angle);
	}
}
