/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.Player;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The algorithm that adjusts the knowledge level of planets and fleets for all players.
 * @author akarnokd, 2011.04.05.
 */
public final class Radar {
	/** Utility class. */
	private Radar() {
	}
	/**
	 * Compute for all players.
	 * @param world the world
	 */
	public static void compute(World world) {
		final int rrg = world.env.params().groundRadarUnitSize();
		final int rrf = world.env.params().fleetRadarUnitSize();
		// clear knowledge
		for (Player player : world.players.values()) {
			// reset known planets to discovered state
			for (Map.Entry<Planet, PlanetKnowledge> p : player.planets.entrySet()) {
				if (p.getKey().owner != player) {
					if (p.getValue().ordinal() > PlanetKnowledge.NAME.ordinal()) {
						p.setValue(PlanetKnowledge.NAME);
					}
				}
			}
			// cleanup fleets
			Iterator<Map.Entry<Fleet, FleetKnowledge>> itf = player.fleets.entrySet().iterator();
			while (itf.hasNext()) {
				Map.Entry<Fleet, FleetKnowledge> f = itf.next();
				if (f.getKey().owner != player) {
					itf.remove();
				} else {
					f.setValue(FleetKnowledge.FULL);
				}
			}
			// run fleet-radar detection
			for (Fleet f : new ArrayList<Fleet>(player.fleets.keySet())) {
				if (f.owner == player) {
					// find the max radar
					float radar = 0.3f;
					for (InventoryItem fi : f.inventory) {
						for (InventorySlot fis : fi.slots) {
							if (fis.type != null) {
								radar = Math.max(radar, fis.type.getInt("radar", 0));
							}
						}
					}
					if (radar > 0) {
						for (Planet q : findPlanetsInRange(world, f.x, f.y, radar * rrf)) {
							if (radar < 1f) {
								updateKnowledge(world, player, q, PlanetKnowledge.VISIBLE);
							} else
							if (radar < 1.1f) {
								updateKnowledge(world, player, q, PlanetKnowledge.NAME);
							} else
							if (radar < 2.1f) {
								updateKnowledge(world, player, q, PlanetKnowledge.NAME);
							} else
							if (radar < 3.1f) {
								updateKnowledge(world, player, q, PlanetKnowledge.OWNER);
							}
						}
						for (Fleet f1 : findFleetsInRange(world, f.x, f.y, radar * rrf)) {
							if (radar < 1f) {
								updateKnowledge(world, player, f1, FleetKnowledge.VISIBLE);
							} else
							if (radar < 1.1) {
								updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
							} else
							if (radar < 2.1) {
								updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
							} else
							if (radar < 3.1) {
								updateKnowledge(world, player, f1, FleetKnowledge.COMPOSITION);
							}
						}
					}
				}
			}
		}
		// traverse each planet
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				updateKnowledge(world, p.owner, p, PlanetKnowledge.BUILDING);
			}
			int radar = 0;
			for (InventoryItem pii : p.inventory) {
				int detectorType = pii.type.getInt("detector", 0);
				if (detectorType == 1) {
					updateKnowledge(world, pii.owner, p, PlanetKnowledge.OWNER);
				}
				if (detectorType == 2) {
					updateKnowledge(world, pii.owner, p, PlanetKnowledge.STATIONS);
				}
				if (detectorType == 3) {
					updateKnowledge(world, pii.owner, p, PlanetKnowledge.BUILDING);
				}
				if (pii.type.getInt("radar", 0) == 4) {
					radar = 4;
					for (Planet q : findPlanetsInRange(world, p.x, p.y, 4 * rrg)) {
						updateKnowledge(world, pii.owner, q, PlanetKnowledge.NAME);
					}
					for (Fleet f : findFleetsInRange(world, p.x, p.y, 4 * rrg)) {
						updateKnowledge(world, pii.owner, f, FleetKnowledge.COMPOSITION);
					}
				}
			}
			if (radar == 0) {
				for (Building b : p.surface.buildings) {
					if (b.isOperational()) {
						if (b.hasResource("radar")) {
							radar = Math.max(radar, (int)b.getResource("radar"));
						}
					}
				}
				if (radar > 0) {
					for (Planet q : findPlanetsInRange(world, p.x, p.y, radar * rrg)) {
						updateKnowledge(world, p.owner, q, PlanetKnowledge.NAME);
					}
					for (Fleet f1 : findFleetsInRange(world, p.x, p.y, radar * rrg)) {
						updateKnowledge(world, p.owner, f1, FleetKnowledge.COMPOSITION);
					}
				}
			}
		}
		// notify players about the radar sweep completed
		for (Player p : world.players.values()) {
			p.ai.onRadar();
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param world the world
	 * @param player the player object
	 * @param planet the planet
	 * @param k the new knowledge
	 */
	static void updateKnowledge(World world, Player player, Planet planet, PlanetKnowledge k) {
		PlanetKnowledge k0 = player.planets.get(planet);
		if (k0 == null && planet.owner != player) {
			player.statistics.planetsDiscovered++;

			player.ai.onDiscoverPlanet(planet);
			world.env.events().onDiscovered(player, planet);
		}
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			player.planets.put(planet, k);
		}
		if (planet.owner != null && planet.owner != player && !player.knows(planet.owner)) {
			player.setStance(planet.owner, player.initialStance);
			if (!world.player.race.equals(planet.owner.race)) {
				player.ai.onDiscoverPlayer(planet.owner);
				world.env.events().onDiscovered(player, planet.owner);
			}
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param world the world
	 * @param player the player
	 * @param fleet the fleet
	 * @param k the new knowledge
	 */
	static void updateKnowledge(World world, Player player, Fleet fleet, FleetKnowledge k) {
		FleetKnowledge k0 = player.fleets.get(fleet);
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			player.fleets.put(fleet, k);
			player.ai.onDiscoverFleet(fleet);
			world.env.events().onDiscovered(player, fleet);
		}
		if (fleet.owner != null && fleet.owner != player && !player.knows(fleet.owner)) {
			player.setStance(fleet.owner, player.initialStance);
			player.ai.onDiscoverPlayer(fleet.owner);
			world.env.events().onDiscovered(player, fleet.owner);
		}
	}
	/**
	 * Locate planets within the given circle.
	 * @param world the game world
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param range the range
	 * @return the list of planets in range
	 */
	static List<Planet> findPlanetsInRange(World world, double x, double y, double range) {
		List<Planet> result = new ArrayList<Planet>();
		for (Planet p : world.planets.values()) {
			if ((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) < range * range) {
				result.add(p);
			}
		}
		
		return result;
	}
	/**
	 * Locate fleets within the given circle.
	 * @param world the game world
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param range the range
	 * @return the list of planets in range
	 */
	static List<Fleet> findFleetsInRange(World world, double x, double y, double range) {
		List<Fleet> result = new ArrayList<Fleet>();
		for (Player p : world.players.values()) {
			for (Fleet f : p.fleets.keySet()) {
				if ((x - f.x) * (x - f.x) + (y - f.y) * (y - f.y) < range * range) {
					result.add(f);
				}
			}
		}
		return result;
	}
}
