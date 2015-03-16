/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The resource allocation strategy enumeration.
 * @author akarnokd, 2010.09.15.
 */
public enum ResourceAllocationStrategy {
	/** Zero strategy: set every allocation to zero. */
	ZERO,
	/** Default strategy: the original game's strategy, allocates resources uniformly. */
	DEFAULT,
	/** 
	 * Damage aware default strategy: 
	 * it considers the building's damage level when assigning resources uniformly.
	 * Damaged buildings are computed with reduced energy/worker demands, giving room for
	 * the rest of the buildings.
	 */
	DAMAGE_AWARE,
	/**
	 * Battle strategy, transfer energy to defenses.
	 */
	BATTLE
}
