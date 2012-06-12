/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * A ground war gun.
 * @author akarnokd, 2011.09.05.
 */
public class GroundwarGun extends GroundwarObject implements Owned {
	/**
	 * Constructor.
	 * @param matrix the gun matrix
	 */
	public GroundwarGun(BufferedImage[][] matrix) {
		super(matrix);
	}
	/** The turret model. */
	public BattleGroundTurret model;
	/** The rendering cell position of the gun. */
	public int rx;
	/** The rendering cell position of the gun. */
	public int ry;
	/** The attached building. */
	public Building building;
	/** The owner planet. */
	public Planet planet;
	/** The target unit. */
	public GroundwarUnit attack;
	/** The weapon cooldown counter. */
	public int cooldown;
	@Override
	public Player owner() {
		return owner;
	}
	/**
	 * Check if the target is in range.
	 * @param u2 the other unit
	 * @return true if in range
	 */
	public boolean inRange(HasLocation u2) {
		double dist = distance(u2);
		return 0 <= dist && dist <= model.maxRange;
	}
	/**
	 * Returns the distance from the other unit.
	 * @param u2 the other unit
	 * @return the distance
	 */
	public double distance(HasLocation u2) {
		Point2D.Double p2 = u2.exactLocation();
		Point2D.Double p1 = building.exactLocation();
		return Math.hypot(p1.x - p2.x, p1.y - p2.y);
	}
}
