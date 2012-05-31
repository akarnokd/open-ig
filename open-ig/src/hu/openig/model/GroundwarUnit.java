/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.utils.U;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The ground war unit.
 * @author akarnokd, 2011.09.02.
 */
public class GroundwarUnit extends GroundwarObject implements HasLocation {
	/** The model entity. */
	public BattleGroundVehicle model;
	/** The position with fractional precision in surface coordinates. */
	public double x;
	/** The position with fractional precision in surface coordinates. */
	public double y;
	/** The available hitpoints. */
	public double hp;
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
	/** The current movement path to the target. */
	public final List<Location> path = U.newArrayList();
	/** The next move rotation. */
	public Location nextRotate;
	/** The next move location. */
	public Location nextMove;
	/** Is the unit paralized? */
	public GroundwarUnit paralized;
	/** The remaining duration for paralization. */
	public int paralizedTTL;
	/** The countdown for yielding. */
	public int yieldTTL;
	/** Indicate that this unit is in motion. For path planning and yielding purposes. */
	public boolean inMotionPlanning;
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
	/**
	 * Constructor.
	 * @param matrix the unit matrix
	 */
	public GroundwarUnit(BufferedImage[][] matrix) {
		super(matrix);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Location location() {
		return Location.of((int)x, (int)y);
	}
	@Override
	public Double exactLocation() {
		return new Point2D.Double(x, y);
	}
	@Override
	public String toString() {
		return item.type.id + " [hp = " + hp + "]";
	}
	/**
	 * @return true if the unit has a target which is still operational
	 */
	public boolean hasValidTarget() {
		if (attackBuilding == null && attackUnit == null) {
			return false;
		}
		if (attackBuilding != null && attackBuilding.isDestroyed()) {
			return false;
		}
		if (attackUnit != null && attackUnit.isDestroyed()) {
			return false;
		}
		return true;
	}
	/**
	 * Returns the distance from the other unit.
	 * @param u2 the other unit
	 * @return the distance
	 */
	public double distance(HasLocation u2) {
		Point2D.Double p2 = u2.exactLocation();
		return Math.hypot(x - p2.x, y - p2.y);
	}
	/**
	 * Returns the distance from the map location.
	 * @param loc the other unit
	 * @return the distance
	 */
	public double distance(Location loc) {
		return Math.hypot(x - loc.x, y - loc.y);
	}
	/** @return true if the unit is moving. */
	public boolean isMoving() {
		return nextMove != null || !path.isEmpty();
	}
	/**
	 * @return the target cell of movement or null if not moving
	 */
	public Location target() {
		if (path.isEmpty()) {
			return nextMove;
		}
		return path.get(path.size() - 1);
	}
	/**
	 * Check if the target is in range.
	 * @param u2 the other unit
	 * @return true if in range
	 */
	public boolean inRange(HasLocation u2) {
		double dist = distance(u2);
		return model.minRange <= dist && dist <= model.maxRange;
	}
}
