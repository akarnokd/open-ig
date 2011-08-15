/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/** The enumeration for space effect types. */
public enum SpaceEffectsType {
	/** Acknowledge command. */
	ACKNOWLEDGE("spacewar/acknowledge_1"),
	/** Click response. */
	CLICK("spacewar/click_1"),
	/** Long explosion. */
	EXPLOSION_LONG("spacewar/explosion_long"),
	/** Medium explosion. */
	EXPLOSION_MEDIUM("spacewar/explosion_medium"),
	/** Short explosion. */
	EXPLOSION_SHORT("spacewar/explosion_short"),
	/** Fire type 1. */
	FIRE_1("spacewar/fire_1"),
	/** Fire type 2. */
	FIRE_2("spacewar/fire_2"),
	/** Fire type 3. */
	FIRE_3("spacewar/fire_3"),
	/** Fire lasers. */
	FIRE_LASER("spacewar/fire_laser"),
	/** Fire meson cannon. */
	FIRE_MESON("spacewar/fire_meson"),
	/** Fire particle cannon. */
	FIRE_PARTICLE("spacewar/fire_particle"),
	/** Fire rocket v1. */
	FIRE_ROCKET_1("spacewar/fire_rocket_1"),
	/** Fire rocket v2. */
	FIRE_ROCKET_2("spacewar/fire_rocket_2"),
	/** Rocket hit? */
	HIT("spacewar/hit")
	;
	/** The associated resource. */
	public final String resource;
	/**
	 * Constructor.
	 * @param resource the associated resource.
	 */
	SpaceEffectsType(String resource) {
		this.resource = resource;
	}
}
