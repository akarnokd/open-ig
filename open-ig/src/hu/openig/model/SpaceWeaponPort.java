/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * A weapon port description.
 * @author akarnokd, 2011.08.15.
 */
public class SpaceWeaponPort {
	/** The weapon port type. */
	public enum WeaponPortType {
		/** This is an auto-fire beam weapon. */
		BEAM,
		/** This is a bomb port, fired manually from GUI. */
		BOMB,
		/** This is a rocket port, fired manually from GUI. */
		ROCKET
	}
	/** The technology ID of the weapon port. */
	public String techId;
	/** The total weapon port damage (e.g., depends on how many lasers are there). */
	public int damage;
	/** The weapon port type. */
	public WeaponPortType type;
	/** The fire range. */
	public double range;
}
