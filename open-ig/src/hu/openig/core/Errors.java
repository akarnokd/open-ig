/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for error conditions
 * @author karnokd
 *
 */
public enum Errors {
	/** Not enough money. */
	NOT_ENOUGH_MONEY(1),
	/** A planet can hold only one research center. */
	RESEARCH_CENTER_LIMIT(2),
	/** The building cannot be built on this kind of planet. */
	NOT_BUILDABLE_ON_PLANET(3),
	/** Only one colony hub can be built on the planet. */
	COLONY_HUB_LIMIT(4),
	/** No colony hub on planet. */
	NO_COLONY_HUB(5),
	/** Colony hub cannot be deactivated. */
	NO_TURN_OFF_COLONY_HUB(6),
	/** Colony hub cannot be demolished. */
	NO_DEMOLISH_COLONY_HUB(7),
	/** Empty fleet cannot be controlled. */
	EMPTY_FLEET_NOT_CONTROLLABLE(8),
	/** Not your fleet. */
	NOT_YOUR_FLEET(9),
	/** This satellite cannot be deployed to an alien colony. */
	SATELLITE_NOT_DEPLOYABLE_HERE(10),
	/** Cannot attack without surface vehicles. */
	NO_ATTACK_WITHOUT_VEHICLES(11),
	/** Attack enemy planet would result more. */
	ATTACK_ENEMY_PLANET_INSTEAD(12),
	/** Cannot attack the planet. */
	CANNOT_ATTACK_PLANET(13),
	/** Cannot attack own fleet. */
	CANNOT_ATTACK_OWN_FLEET(14),
	/** Try to control your own fleet. */
	TRY_YOUR_OWN_FLEET(15),
	/** Cannot build this item without a space factory. */
	SPACE_FACTORY_NEEDED(16),
	/** You need an equipment factory to produce this item. */
	EQUIPMENT_FACTORY_NEEDED(17),
	/** You need a weapons factory to produce this item. */
	WEAPONS_FACTORY_NEEDED(18),
	/** The planet can hold only one space factory. */
	SPACE_FACTORY_LIMIT(19),
	/** The planet can hold only one equipment factory. */
	EQUIPMENT_FACTORY_LIMIT(20),
	/** The planet can hold only one weapons factory. */
	WEAPONS_FACTORY_LIMIT(21),
	/** The planet can hold up to five planetary guns. */
	PLANETARY_GUN_LIMIT(22),
	/** The planet can hold only one shield array. */
	SHIELD_LIMIT(23),
	/** The planet can hold only one from this building. */
	BUILDING_LIMIT(24)
	;
	/** The error code for reference. */
	public final int index;
	/**
	 * Constructor. Sets the error index.
	 * @param index the error index
	 */
	Errors(int index) {
		this.index = index;
	}
	/**
	 * Maps the error index value to the error enumeration.
	 */
	public static final Map<Integer, Errors> map;
	static {
		Map<Integer, Errors> m = new HashMap<Integer, Errors>();
		for (Errors e : values()) {
			m.put(e.index, e);
		}
		map = Collections.unmodifiableMap(m);
	}
}
