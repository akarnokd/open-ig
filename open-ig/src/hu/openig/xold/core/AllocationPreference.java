/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * Enumeration used to select resource allocation strategy.
 * @author karnokd
 */
public enum AllocationPreference {
	/** Distribute resources equally among the buildings. */
	UNIFORM("Uniform"),
	/** Prefer living conditions enhancing buildings. */
	LIVING_CONDITIONS("LivingConditions"),
	/** Prefer production buildings. */
	PRODUCTION("Production"),
	/** Prefer scientific buildings. */
	SCIENCE("Science"),
	/** Prefer economic buildings. */
	ECONOMIC("Economic"),
	/** Prefer military buildings. */
	MILITARY("Military"),
	/** Maximize building operational percentage. */
	OPERATIONAL("Operational")
	;
	/** The identifier used for labels. */
	public final String id;
	/**
	 * Constructor.
	 * @param id the label id
	 */
	AllocationPreference(String id) {
		this.id = id;
	}
}
