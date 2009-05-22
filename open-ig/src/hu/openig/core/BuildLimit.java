/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

/**
 * Enumeration for various kinds of building limits used in the game.
 * @author karnokd
 */
public enum BuildLimit {
	/** There is no limit imposed. */
	UNLIMITED,
	/** A fixed number of buildings can be built. */
	FIXED_NUMBER_PER_PLANET,
	/** Only one can be built from a group of similar buildings (e.g. one kind of research center per planet). */
	ONE_KIND_PER_PLANET
}
