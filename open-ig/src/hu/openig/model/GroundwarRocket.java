/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/**
 * The rocket flying in the ground war.
 * @author akarnokd, 2011.09.08.
 */
public class GroundwarRocket extends GroundwarObject {
	/** The current cell coordinate. */
	public double x;
	/** The current cell coordinate. */
	public double y;
	/** The target cell point. */
	public double targetX;
	/** The target cell point. */
	public double targetY;
	/** The damage to inflict. */
	public double damage;
	/** The damage area. */
	public int area;
	/** How fast the rocket flies. */
	public int movementSpeed;
	/** 
	 * Constructor.
	 * @param matrix The rocket matrix. 
	 */
	public GroundwarRocket(BufferedImage[][] matrix) {
		super(matrix);
		computeAngles2();
	}
	/** 
	 * Compute the rotation side angles. 
	 */
	public void computeAngles2() {
		double[] vx = { 28, -30, -28,  30, 28};
		double[] vy = { 15,  12, -15, -12, 15};
		
		double[] array = new double[angles.length - 1];
		computeSection(2, vx[0], vy[0], vx[1], vy[1], 5, array);
		computeSection(7, vx[1], vy[1], vx[2], vy[2], 3, array);
		computeSection(10, vx[2], vy[2], vx[3], vy[3], 5, array);
		computeSection(15, vx[3], vy[3], vx[4], vy[4], 3, array);
		
		System.arraycopy(array, 0, angles, 0, array.length);
		// wrap around
		angles[angles.length - 1] = angles[0];
	}
	/**
	 * Compute the N equal segment angles and store it in the given offset.
	 * @param offset the offset within the {@code a} array
	 * @param vx1 the first vector x
	 * @param vy1 the first vector y
	 * @param vx2 the second vector x
	 * @param vy2 the second vector y
	 * @param n the number of segments
	 * @param array the output array
	 */
	void computeSection(int offset, double vx1, double vy1, double vx2, double vy2, 
			int n, double[] array) {
		double wx = vx2 - vx1;
		double wy = vy2 - vy1;
		for (int j = 0; j < n; j++) {
			double px = vx1 + wx * j / n;
			double py = vy1 + wy * j / n;
			
			double a = Math.atan2(py, px) / Math.PI / 2;
			if (a < 0) {
				a = 1 + a; // 0..1
			}
			array[(offset + j) % array.length] = a;
		}
	}
	
}
