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
 * The ground war unit.
 * @author akarnokd, 2011.09.02.
 */
public class GroundwarUnit {
	/** The model entity. */
	public BattleGroundVehicle model;
	/** The position with fractional precision in surface coordinates. */
	public double x;
	/** The position with fractional precision in surface coordinates. */
	public double y;
	/** The facing angle. */
	public double angle;
	/** The fire animation phase. */
	public int phase;
	/** The render matrix. */
	public BufferedImage[][] matrix;
	/** The available hitpoints. */
	public int hp;
	/** The original inventory item. */
	public InventoryItem item;
	/** The unit owner. */
	public Player owner;
	/** The owner planet if non-null. */
	public Planet planet;
	/** The owner fleet if non-null. */
	public Fleet fleet;
	/** Unit target if non null. */
	public GroundwarUnit attackUnit;
	/** Building target if non null. */
	public Building attackBuilding;
	/** Is this unit selected? */
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
	/**
	 * @return the maximum phase count
	 */
	public int maxPhase() {
		return matrix.length;
	}
	/**
	 * @return the angle between the rotation phases (in radians)
	 */
	public double angleDelta() {
		return Math.PI * 2 / matrix[0].length;
	}
}
