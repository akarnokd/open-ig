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
 * Gameplay messages.
 * @author karnokd
 *
 */
public enum Messages {
	/** Welcome text. */
	IMPERIUM_GALACTICA(1, 0),
	/** The research %1 has stopped. */
	RESEARCH_STOPPED(2, 1),
	/** The research %1 has completed. */
	RESEARCH_COMPLETED(3, 1),
	/** %1 lost at planet %2. */
	OBJECT_LOST_AT_PLANET(4, 2),
	/** Not enough money for building repairs. */
	NO_MONEY_FOR_REPAIR(5, 0),
	/** Hospital needed on planet %1. */
	HOSPITAL_NEEDED(6, 1),
	/** Stadion needed on planet %1. */
	STADION_NEEDED(7, 1),
	/** Fire department needed on planet %1. */
	FIRE_DEPT_NEEDED(8, 1),
	/** Water vaporator needed on planet %1. */
	WATER_NEEDED(9, 1),
	/** Starvation on planet %1. */
	STARVATION(10, 1),
	/** Living space needed on planet %1. */
	LIVING_SPACE_NEEDED(11, 1),
	/** Unknown ship detected. */
	UNKNOWN_SHIP_DETECTED(12, 0),
	/** Inhabitants are rioting on planet %1. */
	RIOTING(13, 1),
	/** Inhabitants have revolted on planet %1. */
	REVOLTED(14, 1),
	/** Colony has been destroyed on planet %1. */
	COLONY_DESTROYED(15, 1),
	/** New fleet detected. */
	NEW_FLEET_DETECTED(16, 0),
	/** Production of %1 completed. */
	PRODUCTION_COMPLETED(17, 1),
	/** No colony hub on planet %1. */
	COLONY_HUB_NEEDED(18, 1),
	/** Not enough money for research %1. */
	NO_MONEY_FOR_RESEARCH(19, 1),
	/** Enemy fleet detected. */
	ENEMY_SHIP_DETECTED(20, 0),
	/** There are not enough inhabitans living on planet %1. */
	TOO_FEW_INHABITANTS(21, 1),
	/** Alien technology plans found on planet %1. */
	PLANS_FOUND(22, 1),
	/** Planet %1 has been discovered. */
	PLANET_DISCOVERED(23, 1),
	/** Message sent by %1 available in diplomacy. */
	MESSAGE_ARRIVED(24, 1),
	/** A nuclear plant exploded on planet %1. */
	NUCLEAR_PLANT_EXPLODED(25, 1),
	/** Enemy fleet near planet %1. */
	ENEMY_FLEET_AT_PLANET(26, 1),
	/** Virus infection on planet %1. */
	VIRUS_INFECTION(27, 1)
	;
	/** The message index. */
	public final int index;
	/** The parameter count in this message. */
	public final int paramCount;
	/**
	 * Constructor. Sets the field values. 
	 * @param index the message index
	 * @param paramCount the message parameter count
	 */
	Messages(int index, int paramCount) {
		this.index = index;
		this.paramCount = paramCount;
	}
	/** Map from message index to message enumeration. */
	public static final Map<Integer, Messages> map;
	
	static {
		Map<Integer, Messages> m = new HashMap<Integer, Messages>();
		for (Messages msg : values()) {
			m.put(msg.index, msg);
		}
		map = Collections.unmodifiableMap(m);
	}
	/**
	 * Converts the patameterized string to a correct String.format() format specification.
	 * @param s the string to convert
	 * @return the converted string
	 */
	public static String toFormat(String s) {
		return s.replaceAll("%1", "%1$s").replaceAll("%2", "%2$s");
	}
}
