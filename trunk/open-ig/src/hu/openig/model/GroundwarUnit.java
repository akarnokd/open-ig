/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Location;
import hu.openig.utils.U;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The ground war unit.
 * @author akarnokd, 2011.09.02.
 */
public class GroundwarUnit extends GroundwarObject implements HasLocation, Owned {
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
	/** Unit target if non null. */
	public GroundwarUnit attackUnit;
	/** Building target if non null. */
	public Building attackBuilding;
	/** The target of the attack-move if non null. */
	public Location attackMove;
	/** The weapon cooldown counter. */
	public int cooldown;
	/** The current movement path to the target. */
	public final List<Location> path = new ArrayList<>();
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
	/** The save of current target after firing one shot, then advancing to a closer cell. */
	public GroundwarUnit advanceOnUnit;
	/** The save of current target after firing one shot, then advancing to a closer cell. */
	public Building advanceOnBuilding;
	/** Is the unit in guard mode? */
	public boolean guard = true;
	/** Is the unit selected? */
	public boolean selected;
	/**
	 * Orders the units based on damage level.
	 */
	public static final Comparator<GroundwarUnit> MOST_DAMAGED = new Comparator<GroundwarUnit>() {
		@Override
		public int compare(GroundwarUnit o1, GroundwarUnit o2) {
			return java.lang.Double.compare(o1.hp / o1.model.hp, o2.hp / o2.model.hp);
		}
	};
	/** @return is this unit destroyed? */
	public boolean isDestroyed() {
		return hp <= 0;
	}
	/**
	 * Apply damage to this unit.
	 * @param points the points of damage
	 */
	public void damage(double points) {
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
	/**
	 * Determines the location which should be used for path calculation.
	 * @return the location
	 */
	public Location pathFindingLocation() {
		if ((Math.abs(x - (int)x) < 1E-9 && Math.abs(y - (int)y) < 1E-9) || path.isEmpty()) {
			return Location.of((int)x, (int)y);
		}
		return path.get(0);
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
	@Override
	public Player owner() {
		return owner;
	}
	/**
	 * @return the damage provided by this unit
	 */
	public double damage() {
		return model.damage(owner);
	}
	/**
	 * Check if the given other unit is within the range of this unit.
	 * @param other the other unit
	 * @param range the maximum range
	 * @return true if within that range
	 */
	public boolean inRange(GroundwarUnit other, double range) {
		return Math.hypot(other.x - x, other.y - y) <= range;
	}
	/**
	 * Check if the given building is within the given range of
	 * this unit.
	 * @param b the building to check
	 * @param range the maximum range
	 * @return true if within the range
	 */
	public boolean inRange(Building b, double range) {
		int bx = b.location.x;
		int bx2 = bx + b.width() - 1;
		int by = b.location.y;
		int by2 = by - b.height() + 1;
		
		if (Math.hypot(x - bx, y - by) <= range) {
			return true;
		} else
		if (Math.hypot(x - bx2, y - by) <= range) {
			return true;
		} else
		if (Math.hypot(x - bx, y - by2) <= range) {
			return true;
		} else
		if (Math.hypot(x - bx2, y - by2) <= range) {
			return true;
		} else
		if (x >= bx && x <= bx2 && (U.within(y - by, 0, range) || U.within(by2 - y, 0, range))) {
			return true;
		} else
		if (y <= by && y >= by2 && (U.within(bx - x, 0, range) || U.within(x - bx2, 0, range))) {
			return true;
		}
		return false; 
	}
	/**
	 * Returns the bounding rectangle in screen coordinates (without offset).
	 * @return the rectangle with screen coordinates
	 */
	public Rectangle rectangle() {
		int px = (int)Tile.toScreenX(x, y);
		int py = (int)Tile.toScreenY(x, y) + 27 - model.height;
		return new Rectangle(px, py, model.width, model.height);
	}
	/**
	 * Returns the top-left point of the bounding rectangle
	 * in screen coordinates (without offset).
	 * @return the top-left point in screen coordinates
	 */
	public Point position() {
		int px = (int)Tile.toScreenX(x, y);
		int py = (int)Tile.toScreenY(x, y) + 27 - model.height;
		return new Point(px, py);
	}
	/**
	 * Returns the center of the bounding rectangle in screen coordinates (without offset).
	 * @return the center point with screen coordinates
	 */
	public Point center() {
		int px = (int)Tile.toScreenX(x, y);
		int py = (int)Tile.toScreenY(x, y) + 27 - model.height;
		return new Point(px + model.width / 2, py + model.height / 2);
	}
	/**
	 * Merges the current path with the new skipping
	 * overlapping parts to avoid unnecessary rotations. 
	 * @param newPath the new path to follow
	 */
	public void mergePath(List<Location> newPath) {
		// path.addAll(newPath);
		if (!newPath.isEmpty()) {
			
			if (newPath.size() >= 2) {
				Location p0 = newPath.get(0);
			
				double vx = p0.x - x;
				double vy = p0.y - y;
				double v = Math.hypot(vx, vy);

				if (v >= 1E-9) {
					Location p1 = newPath.get(1);
	
					double ax = p1.x - p0.x;
					double ay = p1.y - p0.y;
					double a = Math.hypot(ax, ay);
					
					double angle = Math.acos((vx * ax + vy * ay) / v / a);
					if (angle >= Math.PI - 1E-6) {
						path.clear();
						path.addAll(newPath.subList(1, newPath.size()));
						nextMove = p1;
						nextRotate = p1;
						return;
					}
				}		
			}
			path.addAll(newPath);
		}
	}
}
