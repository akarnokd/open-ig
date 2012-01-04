/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Location;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.EquipmentSlot;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.utils.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The starmap exploration and satellite planner.
 * @author akarnokd, 2011.12.27.
 */
public class ExplorationPlanner extends Planner {
	/** The exploration map. */
	final ExplorationMap exploration;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 * @param exploration the exploration map
	 */
	public ExplorationPlanner(AIWorld world, AIControls controls, ExplorationMap exploration) {
		super(world, controls);
		this.exploration = exploration;
	}
	@Override
	public void plan() {
		// find a fleet which has at least a decent radar range
		// and is among the fastest available
		if (exploration.allowedMap(world.explorationInnerLimit, world.explorationOuterLimit).size() > 0) {
			// check our current exploration fleets are idle
			List<AIFleet> fs = findFleetsWithTask(FleetTask.EXPLORE, null);
			for (AIFleet f : fs) {
				if (!f.isMoving()) {
					// move it
					setExplorationTarget(f);
					return;
				}
			}
			// try recruiting a radar fleet
			AIFleet bestFleet = null;
			for (AIFleet f : findFleetsFor(FleetTask.EXPLORE, null)) {
				if (f.radar >= w.params().fleetRadarUnitSize()) {
					if (bestFleet == null 
							|| (bestFleet.statistics.speed < f.statistics.speed && bestFleet.radar == f.radar)
							|| bestFleet.radar < f.radar) {
						bestFleet = f;
					}
				}
			}
			if (bestFleet != null) {
				setExplorationTarget(bestFleet);
				return;
			} else {
				// at least find a fleet and move it around
				for (AIFleet f : findFleetsFor(FleetTask.EXPLORE, null)) {
					setExplorationTarget(f);
					return;
				}
			}
			if (checkExplorerLevel(fs)) {
				if (planDiscoveryFleet()) {
					return;
				}
			}
		} else {
			// yield more when exploration is finished
			if (w.random.get().nextDouble() < 0.90) {
				return;
			}
			// exploration complete, set explorers to idle
			for (final AIFleet f : findFleetsWithTask(FleetTask.EXPLORE, null)) {
				add(new Action0() {
					@Override
					public void invoke() {
						f.fleet.task = FleetTask.IDLE;
					}
				});
				return;
			}
			for (final AIFleet bf : findFleetsWithTask(FleetTask.PATROL, null)) {
				if (!bf.isMoving()) {
					if (world.ownPlanets.size() > 0) {
						setPatrolTarget(bf);
						return;
					}
				}
			}
			// if we explored everything, let's patrol
			for (final AIFleet bf : findFleetsFor(FleetTask.PATROL, null)) {
				if (world.ownPlanets.size() > 0) {
					setPatrolTarget(bf);
					return;
				}
			}
		}
		
		List<AIPlanet> survey = new ArrayList<AIPlanet>(world.unknownPlanets);
		survey.addAll(world.enemyPlanets);
		Collections.shuffle(survey, w.random.get());
		// traverse all known planet and deploy satellites
		outer:
		for (final AIPlanet planet : survey) {
			AIInventoryItem currentSatellite = null;
			for (AIInventoryItem ii : planet.inventory) {
				if (ii.owner == p && ii.type.has("detector") 
						&& ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
					currentSatellite = ii;
				}
			}
			// find the most advanced satellite
			ResearchType sat = null;
			for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
				ResearchType rt = e.getKey();
				int radar = rt.getInt("detector", 0);
				if (e.getValue() > 0 
						&& rt.category == ResearchSubCategory.SPACESHIPS_SATELLITES
						&& radar > 0) {
					if (sat == null || sat.getInt("detector") < radar) {
						sat = rt;
					}
				}
			}
			if (sat != null) {
				// if we couldn't find a better satellite
				if (currentSatellite != null 
						&& currentSatellite.type.getInt("detector") >= sat.getInt("detector")) {
					continue outer;
				}
				final ResearchType sat0 = sat;
				final Planet planet0 = planet.planet;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionDeploySatellite(planet0, sat0);
					}
				});
				return;
			} else {
				// find the best available detector
				for (ResearchType rt : p.available().keySet()) {
					if (rt.has("detector")) {
						if (sat == null || sat.getInt("detector") < rt.getInt("detector")) {
							sat = rt;
						}
					}
				}
				if (sat != null) {
					placeProductionOrder(sat, 10);
					return;
				}
			}
		}
	}
	
	/**
	 * Find the best radar and check if we could build
	 * a fleet with even better radar.
	 * @param fleets the list of current explorers
	 * @return true if better radar available
	 */
	boolean checkExplorerLevel(List<AIFleet> fleets) {
		int maxRadar = 0;
		for (AIFleet f : fleets) {
			int rl = f.radarLevel();
			if (rl > maxRadar) {
				maxRadar = rl;
			}
		}
		for (ResearchType rt : world.availableResearch) {
			if (rt.has("radar") 
					&& rt.category == ResearchSubCategory.EQUIPMENT_RADARS) {
				if (rt.getInt("radar") > maxRadar) {
					return true;
				}
			}
			if (fitForExploration(rt)) {
				// check fixed slot ships
				for (EquipmentSlot es : rt.slots.values()) {
					if (es.fixed) {
						for (ResearchType rt1 : es.items) {
							if (rt1.has("radar") && rt1.getInt("radar") > maxRadar) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * Set a patrol target for the given fleet.
	 * @param bf the fleet
	 */
	void setPatrolTarget(final AIFleet bf) {
		AIPlanet p = w.random(world.ownPlanets);
		final int x = p.planet.x;
		final int y = p.planet.y;
		add(new Action0() {
			@Override
			public void invoke() {
				bf.fleet.task = FleetTask.PATROL;
				controls.actionMoveFleet(bf.fleet, x, y);
			}
		});
	}
	/**
	 * Set a new exploration target and task for the given fleet.
	 * @param bestFleet the fleet
	 */
	void setExplorationTarget(AIFleet bestFleet) {
		final AIFleet bf = bestFleet;
		final int ec = exploration.cellSize;
		Comparator<Location> distance = new Comparator<Location>() {
			@Override
			public int compare(Location o1, Location o2) {
				double d1 = Math.hypot(bf.x - (o1.x + 0.5) * ec, bf.y - (o1.y + 0.5) * ec);
				double d2 = Math.hypot(bf.x - (o2.x + 0.5) * ec, bf.y - (o2.y + 0.5) * ec);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		};
		Location loc = null;
		final int rl = bf.radarLevel();
		if (rl > 0 && w.random.get().nextDouble() < 0.95) {
			loc = Collections.min(exploration.allowedMap(world.explorationInnerLimit, world.explorationOuterLimit), distance);
		} else {
			List<Location> ls = new ArrayList<Location>(exploration.allowedMap(world.explorationInnerLimit, world.explorationOuterLimit));
			Collections.sort(ls, distance);
			if (ls.size() > 20) {
				loc = w.random(ls.subList(0, 20));
			} else {
				loc = w.random(ls);
			}
		}
		// move to center or edge of the cell?
		double bias = 0.5;
		if (rl > 0 && rl % 2 == 0) {
			bias = 0;
		}
		final double fbias = bias;
		final Location floc = loc;
		add(new Action0() {
			@Override
			public void invoke() {
				bf.fleet.task = FleetTask.EXPLORE;
				controls.actionMoveFleet(bf.fleet, (floc.x + fbias) * ec, (floc.y + fbias) * ec);
			}
		});
	}
	/**
	 * Plan for discovery fleet creation.
	 * @return if action taken
	 */
	boolean planDiscoveryFleet() {
		if (checkEquipment()) {
			return true;
		}
		if (checkMilitarySpaceport()) {
			return true;
		}
		if (checkDeploy()) {
			return true;
		}
		return false;
	}
	/**
	 * Find the best available radar and ship in inventory, and deploy it.
	 * @return true if action taken
	 */
	boolean checkDeploy() {
		List<AIPlanet> mss = JavaUtils.newArrayList();
		for (final AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.hasMilitarySpaceport) {
				mss.add(planet);
			}
		}
		if (mss.isEmpty()) {
			return false;
		}
		final AIPlanet deploy = w.random(mss);
		ResearchType what = findBestFixed();
		if (what == null) {
			what = findBestNormal();
		}
		if (what != null) {
			if (what.has("needsOrbitalFactory")) {
				// check the existence of orbital factory
				if (checkOrbitalFactory()) {
					return true;
				}
			}
			final ResearchType fwhat = what;
			add(new Action0() {
				@Override
				public void invoke() {
					if (deploy.planet.owner.inventoryCount(fwhat) > 0) {
						Fleet f = controls.actionCreateFleet(w.env.labels().get("discovery_fleet"), deploy.planet);
						f.addInventory(fwhat, 1);
						f.upgradeAll();
						f.owner.changeInventoryCount(fwhat, -1);
					}
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Find the best regular ship which can be equipped with the best radar.
	 * @return the ship choosen
	 */
	ResearchType findBestNormal() {
		ResearchType best = null;
		int bestRadar = 0;
		for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
			if (e.getValue() == 0) {
				continue;
			}
			ResearchType rt = e.getKey();
			if (!fitForExploration(rt)) {
				continue;
			}
			for (EquipmentSlot es : rt.slots.values()) {
				for (ResearchType rt0 : es.items) {
					if (rt0.has("radar") && world.inventoryCount(rt0) > 0) {
						if (bestRadar < rt0.getInt("radar")) {
							bestRadar = rt0.getInt("radar");
							best = rt;
						}
					}
				}
			}
		}
		return best;
	}
	/**
	 * Find the best available, fixed radar ship from the inventory.
	 * @return the best found
	 */
	ResearchType findBestFixed() {
		// check if we have a ship which has a fixed radar in inventory
		ResearchType bestFixed = null;
		int bestFixedRadar = 0;
		outer0:
		for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
			ResearchType rt = e.getKey();
			if (fitForExploration(rt) && e.getValue() > 0) {
				for (EquipmentSlot es : rt.slots.values()) {
					if (es.fixed) {
						for (ResearchType rt0 : es.items) {
							if (rt0.has("radar")) {
								if (bestFixed == null || bestFixedRadar < rt0.getInt("radar")) {
									bestFixed = rt;
									bestFixedRadar = rt0.getInt("radar");
								}
								continue outer0;
							}
						}
					}
				}
			}
		}
		return bestFixed;
	}
	/**
	 * Check if the given technology should be considered for exploration ship construction.
	 * @param rt the technology
	 * @return true if applicable
	 */
	boolean fitForExploration(ResearchType rt) {
		return rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS
				|| rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS;
	}
	/**
	 * Check if we have radar in inventory.
	 * @return if action taken
	 */
	boolean checkEquipment() {
		ResearchType bestFixed = findBestFixed();
		if (bestFixed != null) {
			return false;
		}
		// check if we can construct a ship with a fixed radar
		bestFixed = null;
		int bestFixedRadar = 0;
		outer1:
		for (ResearchType rt : world.availableResearch) {
			if (fitForExploration(rt)) {
				for (EquipmentSlot es : rt.slots.values()) {
					if (es.fixed) {
						for (ResearchType rt0 : es.items) {
							if (rt0.has("radar")) {
								if (bestFixed == null || bestFixedRadar < rt0.getInt("radar")) {
									bestFixed = rt;
									bestFixedRadar = rt0.getInt("radar");
								}
								continue outer1;
							}
						}
					}
				}
			}
		}
		if (bestFixed != null) {
			placeProductionOrder(bestFixed, 1);
			return true;
		}
		// list available radars
		List<ResearchType> radars = new ArrayList<ResearchType>();
		for (ResearchType rt : world.availableResearch) {
			if (rt.has("radar") && rt.category == ResearchSubCategory.EQUIPMENT_RADARS) {
				radars.add(rt);
			}
		}
		// list ships capable of carrying radar
		List<ResearchType> ships = new ArrayList<ResearchType>();
		outer4:
		for (ResearchType rt1 : world.availableResearch) {
			if (fitForExploration(rt1)) {
				for (EquipmentSlot es : rt1.slots.values()) {
					if (!es.fixed) {
						for (ResearchType rt2 : es.items) {
							if (rt2.has("radar") && rt2.category == ResearchSubCategory.EQUIPMENT_RADARS) {
								ships.add(rt1);
								continue outer4;
							}
						}
					}
				}
			}
		}
		ResearchType bestRadar = null;
		
		// find the best radar/ship combination
		for (ResearchType radar : radars) {
			for (ResearchType ship : ships) {
				EquipmentSlot es = ship.supports(radar);
				if (es != null && !es.fixed) {
					if (bestRadar == null || bestRadar.getInt("radar") < radar.getInt("radar")) {
						bestRadar = radar;
					}
				}
			}
		}
		
		if (bestRadar != null) {
			if (world.inventoryCount(bestRadar) > 0) {
				return checkShip(bestRadar);
			}
			placeProductionOrder(bestRadar, 5);
			return true;
		}
		return false;
	}
	/**
	 * Check if we have a ship we could deploy a radar to.
	 * @param radar the radar to apply
	 * @return true if action taken
	 */
	boolean checkShip(ResearchType radar) {
		ResearchType bestShip = null;
		for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
			if (e.getValue() == 0) {
				continue;
			}
			ResearchType rt = e.getKey();
			if (!fitForExploration(rt)) {
				continue;
			}
			EquipmentSlot es = rt.supports(radar);
			if (es != null && !es.fixed) {
				// find the cheapest
				if (bestShip == null || bestShip.productionCost > rt.productionCost) {
					bestShip = rt;
					break;
				}
			}
		}
		// there is one such
		if (bestShip != null) {
			return false;
		}
		// find the cheapest and produce it
		for (ResearchType rt : world.availableResearch) {
			if (!fitForExploration(rt)) {
				continue;
			}
			// check if supports the given radar
			for (EquipmentSlot es : rt.slots.values()) {
				if (es.items.contains(radar)) {
					if (bestShip == null || bestShip.productionCost > rt.productionCost) {
						bestShip = rt;
						break;
					}
				}
			}
		}
		if (bestShip != null) {
			placeProductionOrder(bestShip, 1);
		}
		return true;
	}
	/**
	 * Create a military spaceport if necessary.
	 * @return true if action taken
	 */
	boolean checkMilitarySpaceport() {
		// if there is at least one operational we are done
		if (world.global.hasMilitarySpaceport) {
			return false;
		}
		// do not build below this money
		if (world.money < 150000) {
			return true;
		}
		// check if there is a spaceport which we could get operational
		for (final AIPlanet planet : world.ownPlanets) {
			for (final AIBuilding b : planet.buildings) {
				if (b.type.id.equals("MilitarySpaceport")) {
					if (b.isDamaged() && !b.repairing) {
						add(new Action0() {
							@Override
							public void invoke() {
								controls.actionRepairBuilding(planet.planet, b.building, true);
							}
						});
					}
					// found and wait for it to become available
					return true;
				}
			}
		}
		// build one somewhere
		final BuildingType ms = findBuilding("MilitarySpaceport");
		// check if we can afford it
		if (ms.cost <= world.money) {
			List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
			Collections.shuffle(planets);
			// try building one somewhere randomly
			for (final AIPlanet planet : planets) {
				if (planet.findLocation(ms) != null) {
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet.planet, ms);
						}
					});
					return true;
				}
			}
			// there was no room, so demolish a trader's spaceport somewhere
			for (final AIPlanet planet : planets) {
				for (final AIBuilding b : planet.buildings) {
					if (b.type.id.equals("TradersSpaceport")) {
						add(new Action0() {
							@Override
							public void invoke() {
								controls.actionDemolishBuilding(planet.planet, b.building);
							}
						});
						return true;
					}
				}
			}			
		}
		// can't seem to do much now
		return true;
	}
}
