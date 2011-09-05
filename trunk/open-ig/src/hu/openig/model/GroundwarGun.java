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
 * A ground war gun.
 * @author akarnokd, 2011.09.05.
 */
public class GroundwarGun {
	/** The turret model. */
	public BattleGroundTurret model;
	/** The rendering cell position of the gun. */
	public int rx;
	/** The rendering cell position of the gun. */
	public int ry;
	/** The facing angle. */
	public double angle;
	/** The fire animation phase. */
	public int phase;
	/** The attached building. */
	public Building building;
	/** The owner planet. */
	public Planet planet;
	/** The owner. */
	public Player owner;
	/** The target unit. */
	public GroundwarUnit attack;
	/** Is the specific gun selected? */
	public boolean selected;
	/** The weapon cooldown counter. */
	public int cooldown;
	/** @return Get the image for the current rotation and phase. */
	public BufferedImage get() {
		// -0.5 .. +0.5
		double a = normalizedAngle() / Math.PI / 2;
		if (a < 0) {
			a = 1 + a; 
		}
		int phaseIndex = phase % model.matrix.length;
		BufferedImage[] imageAngle = model.matrix[phaseIndex];
		int angleIndex = ((int)Math.round(imageAngle.length * a)) % imageAngle.length;
		return imageAngle[angleIndex];
	}
	/**
	 * @return the normalized angle between -PI and +PI.
	 */
	public double normalizedAngle() {
		return Math.atan2(Math.sin(angle), Math.cos(angle));
	}
	/**
	 * @return the maximum phase count
	 */
	public int maxPhase() {
		return model.matrix.length;
	}
	/**
	 * @return the angle between the rotation phases (in radians)
	 */
	public double angleDelta() {
		return Math.PI * 2 / model.matrix[0].length;
	}
}
