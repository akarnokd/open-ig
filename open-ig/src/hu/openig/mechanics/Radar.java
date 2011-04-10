/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetInventoryItem;
import hu.openig.model.FleetInventorySlot;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Planet;
import hu.openig.model.PlanetInventoryItem;
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
		// clear knowledge
		for (Player player : world.players.values()) {
			// reset known planets to discovered state
			for (Map.Entry<Planet, PlanetKnowledge> p : player.planets.entrySet()) {
				if (p.getKey().owner != player) {
					p.setValue(PlanetKnowledge.VISIBLE);
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
					String radar = "0";
					for (FleetInventoryItem fi : f.inventory) {
						for (FleetInventorySlot fis : fi.slots) {
							if (fis.type != null) {
								String r = fis.type.get("radar");
								if (r != null && radar.compareTo(r) < 0) {
									radar = r;
								}
							}
						}
					}
					int ri = Integer.parseInt(radar);
					if (ri > 0) {
						for (Planet q : findPlanetsInRange(world, f.x, f.y, ri * 35)) {
							if (ri == 1) {
								updateKnowledge(player, q, PlanetKnowledge.VISIBLE);
							} else
							if (ri == 2) {
								updateKnowledge(player, q, PlanetKnowledge.OWNER);
							} else
							if (ri == 3) {
								updateKnowledge(player, q, PlanetKnowledge.BUILDING);
							}
						}
						for (Fleet f1 : findFleetsInRange(world, f.x, f.y, ri * 35)) {
							if (ri == 1) {
								updateKnowledge(player, f1, FleetKnowledge.VISIBLE);
							} else
							if (ri == 2) {
								updateKnowledge(player, f1, FleetKnowledge.COMPOSITION);
							} else
							if (ri == 3) {
								updateKnowledge(player, f1, FleetKnowledge.FULL);
							}
						}
					}
				}
			}
		}
		// traverse each planet
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				updateKnowledge(p.owner, p, PlanetKnowledge.BUILDING);
			}
			for (PlanetInventoryItem pii : p.inventory) {
				String radar = pii.type.get("radar");
				if (radar != null) {
					if ("1".equals(radar)) {
						updateKnowledge(pii.owner, p, PlanetKnowledge.NAME);
					}
					if ("2".equals(radar)) {
						updateKnowledge(pii.owner, p, PlanetKnowledge.OWNER);
					}
					if ("3".equals(radar)) {
						updateKnowledge(pii.owner, p, PlanetKnowledge.BUILDING);
					}
					if ("4".equals(radar)) {
						for (Planet q : findPlanetsInRange(world, p.x, p.y, 4 * 35)) {
							updateKnowledge(pii.owner, q, PlanetKnowledge.BUILDING);
						}
						for (Fleet f : findFleetsInRange(world, p.x, p.y, 4 * 35)) {
							updateKnowledge(pii.owner, f, FleetKnowledge.FULL);
						}
					}
				}
			}
			int radar = 0;
			for (Building b : p.surface.buildings) {
				if (b.getEfficiency() >= 0.5) {
					if (b.hasResource("radar")) {
						radar = Math.max(radar, (int)b.getResource("radar"));
					}
				}
			}
			if (radar > 0) {
				for (Planet q : findPlanetsInRange(world, p.x, p.y, radar * 35)) {
					if (radar == 3) {
						updateKnowledge(p.owner, q, PlanetKnowledge.BUILDING);
					} else
					if (radar == 2) {
						updateKnowledge(p.owner, q, PlanetKnowledge.OWNER);
					} else
					if (radar == 1) {
						updateKnowledge(p.owner, q, PlanetKnowledge.VISIBLE);
					}
				}
				for (Fleet f1 : findFleetsInRange(world, p.x, p.y, radar * 35)) {
					if (radar == 1) {
						updateKnowledge(p.owner, f1, FleetKnowledge.VISIBLE);
					} else
					if (radar == 2) {
						updateKnowledge(p.owner, f1, FleetKnowledge.COMPOSITION);
					} else
					if (radar == 3) {
						updateKnowledge(p.owner, f1, FleetKnowledge.FULL);
					}
				}
			}
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param player the player object
	 * @param planet the planet
	 * @param k the new knowledge
	 */
	static void updateKnowledge(Player player, Planet planet, PlanetKnowledge k) {
		PlanetKnowledge k0 = player.planets.get(planet);
		if (k0 == null && planet.owner != player) {
			player.statistics.planetsDiscovered++;
		}
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			player.planets.put(planet, k);
		}
		if (planet.owner != null && planet.owner != player && !player.knows(planet.owner)) {
			player.setStance(planet.owner, player.initialStance);
			// FIXME send discover message
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param player the player
	 * @param fleet the fleet
	 * @param k the new knowledge
	 */
	static void updateKnowledge(Player player, Fleet fleet, FleetKnowledge k) {
		FleetKnowledge k0 = player.fleets.get(fleet);
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			player.fleets.put(fleet, k);
		}
		if (fleet.owner != null && fleet.owner != player && !player.knows(fleet.owner)) {
			player.setStance(fleet.owner, player.initialStance);
			// FIXME send discover message
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
	static List<Planet> findPlanetsInRange(World world, int x, int y, int range) {
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
	static List<Fleet> findFleetsInRange(World world, int x, int y, int range) {
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
