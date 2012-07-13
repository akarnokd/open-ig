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
 * A projectile representing a laser, cannon, rocket or bomb.
 * They don't have any animation phase in general.
 * @author akarnokd, 2011.08.15.
 */
public class SpacewarProjectile extends SpacewarObject {
	/** The beam speed per simulation tick. */
	public int movementSpeed;
	/** The rotation time per angle segment. */
	public double rotationTime;
	/** The projectile's model. */
	public BattleProjectile model;
	/** The angle images of the projectile. */
	public BufferedImage[][] matrix;
	/** The animation phase with an angle. */
	public int phase;
	/** ECM distraction limit 0..2 .*/
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
		int phaseIndex = phase % matrix.length;
		BufferedImage[] imageAngle = matrix[phaseIndex];
		int angleIndex = ((int)Math.round(imageAngle.length * a)) % imageAngle.length;
		return imageAngle[angleIndex];
	}
	/**
	 * @return the normalized angle between -PI and +PI.
	 */
	public double normalizedAngle() {
		return Math.atan2(Math.sin(angle), Math.cos(angle));
	}
}
