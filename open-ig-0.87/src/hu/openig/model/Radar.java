/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Act;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

/**
 * The algorithm that adjusts the knowledge level of planets and fleets for all players.
 * @author akarnokd, 2011.04.05.
 */
public class Radar {
	/** The timer to perform the periodic computation. */
	protected Timer timer;
	/** The world to compute. */
	protected World world;
	/**
	 * Constructor.
	 * @param delay the between delay
	 * @param world the world to compute
	 */
	public Radar(int delay, World world) {
		this.world = world;
		this.timer = new Timer(delay, new Act() {
			@Override
			public void act() {
				compute();
			}
		});
		this.timer.setInitialDelay(0);
	}
	/**
	 * Compute for all players.
	 */
	public void compute() {
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
						for (Planet q : findPlanetsInRange(f.x, f.y, ri * 35)) {
							if (ri == 1) {
								updateKnowledge(player.planets, q, PlanetKnowledge.VISIBLE);
							} else
							if (ri == 2) {
								updateKnowledge(player.planets, q, PlanetKnowledge.OWNER);
							} else
							if (ri == 3) {
								updateKnowledge(player.planets, q, PlanetKnowledge.BUILDING);
							}
						}
						for (Fleet f1 : findFleetsInRange(f.x, f.y, ri * 35)) {
							if (ri == 1) {
								updateKnowledge(player.fleets, f1, FleetKnowledge.VISIBLE);
							} else
							if (ri == 2) {
								updateKnowledge(player.fleets, f1, FleetKnowledge.COMPOSITION);
							} else
							if (ri == 3) {
								updateKnowledge(player.fleets, f1, FleetKnowledge.FULL);
							}
						}
					}
				}
			}
		}
		// traverse each planet
		for (Planet p : world.planets) {
			if (p.owner != null) {
				updateKnowledge(p.owner.planets, p, PlanetKnowledge.BUILDING);
			}
			for (PlanetInventoryItem pii : p.inventory) {
				String radar = pii.type.get("radar");
				if (radar != null) {
					if ("1".equals(radar)) {
						updateKnowledge(pii.owner.planets, p, PlanetKnowledge.NAME);
					}
					if ("2".equals(radar)) {
						updateKnowledge(pii.owner.planets, p, PlanetKnowledge.OWNER);
					}
					if ("3".equals(radar)) {
						updateKnowledge(pii.owner.planets, p, PlanetKnowledge.BUILDING);
					}
					if ("4".equals(radar)) {
						for (Planet q : findPlanetsInRange(p.x, p.y, 4 * 35)) {
							updateKnowledge(pii.owner.planets, q, PlanetKnowledge.BUILDING);
						}
						for (Fleet f : findFleetsInRange(p.x, p.y, 4 * 35)) {
							updateKnowledge(pii.owner.fleets, f, FleetKnowledge.FULL);
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
				for (Planet q : findPlanetsInRange(p.x, p.y, radar * 35)) {
					if (radar == 3) {
						updateKnowledge(p.owner.planets, q, PlanetKnowledge.BUILDING);
					} else
					if (radar == 2) {
						updateKnowledge(p.owner.planets, q, PlanetKnowledge.OWNER);
					} else
					if (radar == 1) {
						updateKnowledge(p.owner.planets, q, PlanetKnowledge.VISIBLE);
					}
				}
				for (Fleet f1 : findFleetsInRange(p.x, p.y, radar * 35)) {
					if (radar == 1) {
						updateKnowledge(p.owner.fleets, f1, FleetKnowledge.VISIBLE);
					} else
					if (radar == 2) {
						updateKnowledge(p.owner.fleets, f1, FleetKnowledge.COMPOSITION);
					} else
					if (radar == 3) {
						updateKnowledge(p.owner.fleets, f1, FleetKnowledge.FULL);
					}
				}
			}
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param map the target knowledge map
	 * @param p the planet
	 * @param k the new knowledge
	 */
	void updateKnowledge(Map<Planet, PlanetKnowledge> map, Planet p, PlanetKnowledge k) {
		PlanetKnowledge k0 = map.get(p);
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			map.put(p, k);
		}
	}
	/**
	 * Update the planet knowledge by only increasing it in the given mapping.
	 * @param map the target knowledge map
	 * @param p the planet
	 * @param k the new knowledge
	 */
	void updateKnowledge(Map<Fleet, FleetKnowledge> map, Fleet p, FleetKnowledge k) {
		FleetKnowledge k0 = map.get(p);
		if (k0 == null || k0.ordinal() < k.ordinal()) {
			map.put(p, k);
		}
	}
	/**
	 * Locate planets within the given circle.
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param range the range
	 * @return the list of planets in range
	 */
	public List<Planet> findPlanetsInRange(int x, int y, int range) {
		List<Planet> result = new ArrayList<Planet>();
		for (Planet p : world.planets) {
			if ((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) < range * range) {
				result.add(p);
			}
		}
		
		return result;
	}
	/**
	 * Locate fleets within the given circle.
	 * @param x the center coordinate
	 * @param y the center coordinate
	 * @param range the range
	 * @return the list of planets in range
	 */
	public List<Fleet> findFleetsInRange(int x, int y, int range) {
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
	/** Stop the timer. */
	public void stop() {
		timer.stop();
	}
	/** Start the timer. */
	public void start() {
		timer.start();
	}
}
