/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * A space entity representing a ship, a projectile or a ground defense.
 * @author akarnokd, Jul 31, 2011
 */
public class SpaceEntity {
	/** The X coordinate. */
	public double x;
	/** The Y coordinate. */
	public double y;
	/** The rotation angle. */
	public double angle;
	/** The image to use for rotation. */
	public BufferedImage[] rotation;
	/** The selection indicator. */
	public boolean selected;
	/** The shield percentage. */
	public double shieldPercent;
	/** The hit-points percentage. */
	public double hpPercent;
	/** The referenced inventory item if this entity represents a ship or station, null if this is a laser or cannon projectile or a building. */
	public InventoryItem item;
	/** The projectile or building represented by this entity, or null if it is a ship or station. */
	public ResearchType research;
	/** The target rotation angle. */
	public double targetAngle;
	/** The target X coordinate for movement. */
	public double targetX;
	/** The target Y coordinate for movement. */
	public double targetY;
	/** The target coordinates and angle represent a target to attack. */
	public boolean attacking;
	/** The movement path. */
	public final List<Point2D> path = new LinkedList<Point2D>();
}
