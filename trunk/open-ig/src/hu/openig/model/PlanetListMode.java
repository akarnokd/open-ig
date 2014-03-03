/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The last column type in the information / planets screen.
 * @author akarnokd, 2013.09.04.
 *
 */
public enum PlanetListMode {
	/** Problems. */
	PROBLEMS,
	/** Labs. */
	LABS,
	/** Show trader's spaceport. */
	TRADERS_SPACEPORT,
	/** Bank. */
	BANK,
	/** Trade center. */
	TRADE_CENTER,
	/** Spaceship factory. */
	SPACESHIP_FACTORY,
	/** Equipment factory. */
	EQUIPMENT_FACTORY,
	/** Weapons factory. */
	WEAPONS_FACTORY,
	/** Barracks. */
	BARRACKS,
	/** Guns. */
	GUNS,
}
