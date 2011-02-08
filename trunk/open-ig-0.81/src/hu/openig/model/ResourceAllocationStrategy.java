/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The resource allocation strategy enumeration.
 * @author karnok, 2010.09.15.
 * @version $Revision 1.0$
 */
public enum ResourceAllocationStrategy {
	/** Zero strategy: set every allocation to zero. */
	ZERO_STRATEGY,
	/** Default strategy: the original game's strategy, allocates resources uniformly. */
	DEFAULT_STRATEGY,
	/** 
	 * Damage aware default strategy: 
	 * it considers the building's damage level when assigning resources uniformly.
	 * Damaged buildings are computed with reduced energy/worker demands, giving room for
	 * the rest of the buildings.
	 */
	DAMAGE_AWARE_DEFAULT_STRATEGY,
	/** The maximum efficiency heuristics. */
	MAX_EFFICIENCY
}
