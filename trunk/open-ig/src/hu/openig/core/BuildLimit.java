/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for various kinds of building limits used in the game.
 * @author karnokd
 */
public enum BuildLimit {
	/** There is no limit imposed. */
	UNLIMITED("Unlimited"),
	/** A fixed number of buildings can be built. */
	FIXED_NUMBER_PER_PLANET("Fixed"),
	/** Only a fixed number can be built from a group of similar buildings 
	 * (e.g. one kind of research center per planet). */
	FIXED_KIND_PER_PLANET("Kind")
	;
	/** The build limit id in xml. */
	public final String id;
	/**
	 * Constructor.
	 * @param id the build limit id in xml.
	 */
	BuildLimit(String id) {
		this.id = id;
	}
	/** The id to buildlimit map. */
	private static final Map<String, BuildLimit> MAP;
	/** Static initialization. */
	static {
		MAP = new HashMap<String, BuildLimit>();
		for (BuildLimit bl : values()) {
			MAP.put(bl.id, bl);
		}
	}
	/**
	 * Returns a buildlimit based on its id.
	 * @param id the identifier
	 * @return the build limit enum
	 */
	public static BuildLimit getById(String id) {
		return MAP.get(id);
	}
}
