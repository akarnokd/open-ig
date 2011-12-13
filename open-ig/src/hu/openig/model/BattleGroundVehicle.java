/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;


import java.awt.image.BufferedImage;

/**
 * The rotation + fire phase images of ground entities.
 * @author akarnokd, Jul 31, 2011
 */
public class BattleGroundVehicle {
	/** The normal [rotation][fire-phase] of the entity. */
	public BufferedImage[][] normal;
	/** The alternative [rotation][fire-phase] of the entity. */
	public BufferedImage[][] alternative;
	/** The destruction sound. */
	public SoundType destroy;
	/** The fire sound if non null. */
	public SoundType fire;
	/** The explosion to play. */
	public ExplosionType explosion;
	/** The hitpoints. */
	public int hp;
	/** The inflicted damage. */
	public int damage;
	/** The minimum range. */
	public double minRange;
	/** The maximum range. */
	public double maxRange;
	/** The damage area. */
	public int area;
	/** The unit behavior type. */
	public GroundwarUnitType type;
	/** The rotation time per angle-segment. */
	public int rotationTime;
	/** The movement pixels per simulation step. */
	public int movementSpeed;
	/** Delay between firing. */
	public int delay;
	/** Number of milliseconds for a full HP repair. */
	public int selfRepairTime;
	/** The technology name. */
	public String id;
	/** The original image width. */
	public int width;
	/** The original image height. */
	public int height;
}
