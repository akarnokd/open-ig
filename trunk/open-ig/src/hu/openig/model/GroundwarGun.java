/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * A ground war gun.
 * @author akarnokd, 2011.09.05.
 */
public class GroundwarGun extends GroundwarObject implements Owned {
	/**
	 * Constructor.
	 * @param model the turret's model. 
	 */
	public GroundwarGun(BattleGroundTurret model) {
		super(model.matrix);
		this.model = model;
	}
	/** The turret model. */
	public final BattleGroundTurret model;
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
	/** The index within the building's guns. */
	public int index;
	/** The total number of guns. */
	public int count;
	/** Is the unit selected? */
	public boolean selected;
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
	/**
	 * @return the damage provided by this unit
	 */
	public double damage() {
		return model.damage(owner);
	}
	/**
	 * Returns the screen rectangle of this gun (no screen offset).
	 * @return the rectangle in screen coordinates
	 */
	public Rectangle rectangle() {
		int px = (Tile.toScreenX(rx, ry));
		int py = (Tile.toScreenY(rx, ry));
		BufferedImage img = get();
		
		int ux = px + (54 - img.getWidth()) / 2 + model.px;
		int uy = py + (28 - img.getHeight()) / 2 + model.py;
		
		return new Rectangle(ux, uy, img.getWidth(), img.getHeight());
	}
	/**
	 * Returns the center screen coordinates of this gun (no screen offset).
	 * @return the point in screen coordinates
	 */
	public Point center() {
		int px = (Tile.toScreenX(rx, ry));
		int py = (Tile.toScreenY(rx, ry));
		BufferedImage img = get();
		
		int ux = px + (54 - img.getWidth()) / 2 + model.px;
		int uy = py + (28 - img.getHeight()) / 2 + model.py;

		return new Point(ux + img.getWidth() / 2, uy + img.getHeight() / 2);
	}
}
