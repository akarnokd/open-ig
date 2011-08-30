/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * Defines the battle parties.
 * @author akarnokd, 2011.08.16.
 */
public class BattleInfo {
	/** The attacker fleet. */
	public Fleet attacker;
	/** The targeted fleet if non null. */
	public Fleet targetFleet;
	/** The targeted planet if non null. */
	public Planet targetPlanet;
	/** The potential helper fleet if a planet is attacked. */
	public Fleet helperFleet;
	/** The potential helper planet if a fleet is attacked. */
	public Planet helperPlanet;
	/** Count the attacker's losses. */
	public int attackerLosses;
	/** Count the defender's losses. */
	public int defenderLosses;
	/** Ground unit count of the attacker. */
	public int attackerGroundUnits;
	/** Ground unit count of the defender. */
	public int defenderGroundUnits;
	/** @return the helper planet if any. */
	public Planet getPlanet() {
		return targetPlanet != null ? targetPlanet : helperPlanet;
	}
	/** @return the helper fleet if any. */
	public Fleet getFleet() {
		return targetFleet != null ? targetFleet : helperFleet;
	}
}
