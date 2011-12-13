/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.World;

/**
 * Simulation algorithms for automatic space and surface battles.
 * @author akarnokd, 2011.08.25.
 *
 */
public final class BattleSimulator {
	/** The world object. */
	protected final World world;
	/** The battle configuration. */
	protected final BattleInfo battle;
	/**
	 * Constructor.
	 * @param world the world object
	 * @param battle the battle configuration
	 */
	public BattleSimulator(World world, BattleInfo battle) {
		this.world = world;
		this.battle = battle;
	}
	/**
	 * Run the given battle automatically.
	 */
	public void autoBattle() {
	}
	/** Simulate the ground battle. */
	void autoGroundBattle() {
		
	}
	/**
	 * Find helper fleet or planet for the battle.
	 * @param battle the battle configuration
	 * @param world the world object
	 */
	public static void findHelpers(BattleInfo battle, World world) {
		final int minDistance = 20;
		if (battle.targetFleet != null) {
			// locate the nearest planet
			double dmin = Double.MAX_VALUE;
			Planet pmin = null;
			for (Planet p : world.planets.values()) {
				if (p.owner == battle.attacker.owner || p.owner == battle.targetFleet.owner) {
					double d = World.dist(battle.targetFleet.x, battle.targetFleet.y, p.x, p.y);
					if (d < dmin && d <= minDistance) {
						dmin = d;
						pmin = p;
					}
				}
				
			}
			battle.helperPlanet = pmin;
		} else 
		if (battle.targetPlanet != null) {
			// locate the nearest fleet with the same owner
			double dmin = Double.MAX_VALUE;
			Fleet fmin = null;
			for (Fleet f : battle.targetPlanet.owner.fleets.keySet()) {
				if (f.owner == battle.targetPlanet.owner) {
					double d = World.dist(f.x, f.y, battle.targetPlanet.x, battle.targetPlanet.y);
					if (d < dmin && d <= minDistance) {
						dmin = d;
						fmin = f;
					}
				}
			}
			battle.helperFleet = fmin;
		} else {
			throw new AssertionError("No target in battle settings.");
		}
	}
	/**
	 * @param planet the target planet 
	 * @return true if there are troops or structures on the surface which need to be destroyed. 
	 */
	public static boolean groundBattleNeeded(Planet planet) {
		int vehicles = planet.inventoryCount(ResearchSubCategory.WEAPONS_TANKS, planet.owner)
				+ planet.inventoryCount(ResearchSubCategory.WEAPONS_VEHICLES, planet.owner);
		if (vehicles > 0) {
			return true;
		}
		for (Building b : planet.surface.buildings) {
			if (b.type.kind.equals("Defensive")) {
				return true;
			}
		}
		return false;
	}
}
