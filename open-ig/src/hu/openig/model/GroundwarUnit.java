/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.utils.JavaUtils;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The ground war unit.
 * @author akarnokd, 2011.09.02.
 */
public class GroundwarUnit extends GroundwarObject {
	/** The model entity. */
	public BattleGroundVehicle model;
	/** The position with fractional precision in surface coordinates. */
	public double x;
	/** The position with fractional precision in surface coordinates. */
	public double y;
	/** The available hitpoints. */
	public int hp;
	/** The original inventory item. */
	public InventoryItem item;
	/** The owner planet if non-null. */
	public Planet planet;
	/** The owner fleet if non-null. */
	public Fleet fleet;
	/** Unit target if non null. */
	public GroundwarUnit attackUnit;
	/** Building target if non null. */
	public Building attackBuilding;
	/** The weapon cooldown counter. */
	public int cooldown;
	/** The unit matrix [phase][angle]. */
	public BufferedImage[][] matrix;
	/** The current movement path to the target. */
	public final List<Location> path = JavaUtils.newArrayList();
	/** The next move rotation. */
	public Location nextRotate;
	/** The next move location. */
	public Location nextMove;
	/** @return is this unit destroyed? */
	public boolean isDestroyed() {
		return hp <= 0;
	}
	/**
	 * Apply damage to this unit.
	 * @param points the points of damage
	 */
	public void damage(int points) {
		hp = Math.max(0, hp - points);
	}
	@Override
	protected double[] getAngles() {
		return model.angles;
	}
	@Override
	protected BufferedImage[][] getMatrix() {
		return matrix;
	}
}
