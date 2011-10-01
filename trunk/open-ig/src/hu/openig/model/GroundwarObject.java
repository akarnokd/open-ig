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
	protected final double[] angles;
	/** @return the matrix [phase][angle] */
	protected final BufferedImage[][] matrix;
	/**
	 * Initializes the object with the given rotation matrix.
	 * @param matrix the rotation matrix
	 */
	public GroundwarObject(BufferedImage[][] matrix) {
		this.matrix = matrix;
		this.angles = computeAngles(matrix[0].length);
	}
	/** @return Get the image for the current rotation and phase. */
	public BufferedImage get() {
		BufferedImage[] rotation = matrix[phase % matrix.length];

		if (cachedAngle != angle) {
			double a = normalizedAngle() / 2 / Math.PI;
			if (a < 0) {
				a = 1 + a;
			}
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
		return matrix.length - 1;
	}
	/** @return the number of angles of the matrix. */
	public int angleCount() {
		return matrix[0].length;
	}
	/** 
	 * Compute the rotation side angles. 
	 * @param count the number of elements within the full circle
	 * @return the angles
	 */
	public static double[] computeAngles(int count) {
		double[] vx = { 28, -30, -28,  30, 28};
		double[] vy = { 15,  12, -15, -12, 15};
		
		double[] angles = new double[count + 1];
		int n = (angles.length - 1) / 4;
		
		for (int i = 0; i < 4; i++) {
			double wx = vx[i + 1] - vx[i];
			double wy = vy[i + 1] - vy[i];
			for (int j = 0; j < n; j++) {
				double px = vx[i] + wx * j / n;
				double py = vy[i] + wy * j / n;
				
				double a = Math.atan2(py, px) / Math.PI / 2;
				if (a < 0) {
					a = 1 + a; // 0..1
				}
				angles[i * n + j] = a;
			}

		}
		// wrap around
		angles[angles.length - 1] = angles[0];
		return angles;
	}
	/** @return the unit matrix. */
	public BufferedImage[][] matrix() {
		return matrix;
	}
}
