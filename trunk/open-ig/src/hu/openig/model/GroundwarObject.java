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
 * @author akarnokd, 2011.09.07.
 *
 */
public abstract class GroundwarObject {
	/** The facing angle. */
	public double angle;
	/** The fire animation phase. */
	public int phase;
	/** The owner. */
	public Player owner;
	/** Is the specific gun selected? */
	public boolean selected;
	/** The cached angle. */
	protected double cachedAngle = Double.NaN;
	/** The cached index. */
	protected int cachedIndex;
	/** @return The precalcualted angle table. */
	protected abstract double[] getAngles();
	/** @return the matrix [phase][angle] */
	protected abstract BufferedImage[][] getMatrix();
	/** @return Get the image for the current rotation and phase. */
	public BufferedImage get() {
		BufferedImage[] rotation = getMatrix()[phase];

		if (cachedAngle != angle) {
			double a = normalizedAngle() / 2 / Math.PI;
			if (a < 0) {
				a = 1 + a;
			}
			double[] angles = getAngles();
			for (int i = 0; i < angles.length - 1; i++) {
				double a0 = angles[i];
				double a1 = angles[i + 1];
				if (a0 < a1) {
					if (a >= a0 && a < a1) {
						if (a - a0 < a1 - a) {
							cachedIndex = i;
						} else {
							cachedIndex = i + 1;
						}
						break;
					}
				} else {
					if (a >= a0 || a < a1) {
						if (Math.abs(a - a0) < Math.abs(a1 - a)) {
							cachedIndex = i;
						} else {
							cachedIndex = i + 1;
						}
						break;
					}
				}
			}
			cachedAngle = angle;
		}
		return rotation[cachedIndex % rotation.length];
	}
	/**
	 * @return the normalized angle between -PI and +PI.
	 */
	public double normalizedAngle() {
		return Math.atan2(Math.sin(angle), Math.cos(angle));
	}
	/**
	 * @return the maximum phase index
	 */
	public int maxPhase() {
		return getMatrix().length - 1;
	}
}
