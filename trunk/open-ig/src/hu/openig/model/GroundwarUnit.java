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
	/** The unit type. */
	public enum UnitType {
		/** A fixed defensive gun. */
		GUN,
		/** A regular tank. */
		TANK,
		/** A rocket sled. */
		ROCKET_SLED,
		/** A radar car. */
		RADAR,
		/** A radar jammer. */
		RADAR_JAMMER,
		/** A paralizer. */
		PARALIZER,
		/** A rocket jammer. */
		ROCKET_JAMMER,
		/** A minelayer. */
		MINELAYER,
		/** A mine. */
		MINE,
		/** An artillery. */
		ARTILLERY,
		/** A kamikaze tank. */
		KAMIKAZE,
		/** A self repair tank. */
		SELF_REPAIR_TANK
	}
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
	/** The maximum hitpoints. */
	public int hpMax;
	/** The rotation speed: millisecond time per angle element. */
	public int rotationTime;
	/** The movement speed: Milliseconds per one pixel. */
	public int movementSpeed;
	/** The firing range maximum. */
	public int maxRange;
	/** The firing range minimum. */
	public int minRange;
	/** The effect area. */
	public double area;
	/** The damage inflicted. */
	public int damage;
	/** The sound to play when destroyed. */
	public SoundType destroy;
	/** The sound to play when firing. */
	public SoundType fire;
	/** The original inventory item. */
	public InventoryItem item;
	/** The unit owner. */
	public Player owner;
	/** The owner planet if non-null. */
	public Planet planet;
	/** The owner fleet if non-null. */
	public Fleet fleet;
	/** Unit type. */
	public UnitType type;
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
}
