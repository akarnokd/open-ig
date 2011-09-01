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
	public int movementSpeed;
	/** The rotation time per angle segment. */
	public int rotationTime;
	/** The angle images of the projectile. */
	public BufferedImage[][] matrix;
	/** The animation phase with an angle. */
	public int phase;
	/** ECM distraction limit 1..2 .*/
	public int ecmLimit;
	/** The damage to inflict. */
	public int damage;
	/** The beam angle in an X-Y screen directed coordinate system, 0..2*PI. */
	public double angle;
	/** The targeted structure. */
	public SpacewarStructure target;
	/** The impact sound. */
	public SoundType impactSound;
	@Override
	public BufferedImage get() {
		// -0.5 .. +0.5
		double a = normalizedAngle() / Math.PI / 2;
		if (a < 0) {
			a = 1 + a; 
		}
		BufferedImage[] imageAngle = matrix[((int)Math.round(matrix.length * a)) % matrix.length]; 
		return imageAngle[phase % imageAngle.length];
	}
	/**
	 * @return the normalized angle between -PI and +PI.
	 */
	public double normalizedAngle() {
		return Math.atan2(Math.sin(angle), Math.cos(angle));
	}
}
