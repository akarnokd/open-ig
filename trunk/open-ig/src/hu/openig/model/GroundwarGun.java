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
	/** The rendering cell position of the gun. */
	public int rx;
	/** The rendering cell position of the gun. */
	public int ry;
	/** The pixel offset of the rendering. */
	public int px;
	/** The pixel offset of the rendering. */
	public int py;
	/** The facing angle. */
	public double angle;
	/** The fire animation phase. */
	public int phase;
	/** The render matrix. */
	public BufferedImage[][] matrix;
	/** The attached building. */
	public Building building;
	/** The owner planet. */
	public Planet planet;
	/** The owner. */
	public Player owner;
	/** The rotation speed: millisecond time per angle element. */
	public int rotationTime;
	/** The damage inflicted. */
	public int damage;
	/** The firing range maximum. */
	public int maxRange;
	/** The sound to play when firing. */
	public SoundType fire;
	/** The target unit. */
	public GroundwarUnit attack;
	/** @return Get the image for the current rotation and phase. */
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
