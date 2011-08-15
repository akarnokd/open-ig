/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A record representing a spaceship, a spacestation or space defense.
 * @author akarnokd, 2011.08.15.
 */
public class SpaceShipStationDefense extends SpaceObject {
	/** The speed per simulation tick. */
	public double speed;
	/** The angle images of the spaceship. */
	public BufferedImage[] angles;
	/** Available hitpoints, single object. */
	public int hp;
	/** The maximum hitpoints. */
	public int hpMax;
	/** Available shields single object. */
	public int shield;
	/** The maximum shield points. */
	public int shieldMax;
	/** Number of batched fighters. */
	public int count;
	/** The technology identifier, e.g., "Fighter1". */
	public String techId;
	/** Is the ship selected? */
	public boolean selected;
	/** The movement instruction is an attack instruction. */
	public boolean attacking;
	/** The movement path elements. */
	public final List<Point2D.Double> path = new ArrayList<Point2D.Double>();
	/** The target object. */
	public SpaceShipStationDefense target;
	/** The ECM level of this ship, 0 means none. */
	public int ecmLevel;
	/** The object should not move? */
	public boolean stationary;
	/** The ship should rotate to the target angle to be able to fire. Used by fighters and ground defenses. */
	public boolean rotateToFire;
	/** The available weapon ports. */
	public final List<SpaceWeaponPort> ports = new ArrayList<SpaceWeaponPort>();
	@Override
	public void draw(Graphics2D g2, int x, int y) {
		double a = angle / 2 / Math.PI; // angle to percentage
		BufferedImage img = angles[((int)Math.round(angles.length * a)) % angles.length];
		g2.drawImage(img, x - img.getWidth() / 2, y - img.getHeight() / 2, null);
	}
	/** Move the beam to the next location. */
	public void move() {
		x += speed * Math.cos(angle);
		y += speed * Math.sin(angle);
	}
}
