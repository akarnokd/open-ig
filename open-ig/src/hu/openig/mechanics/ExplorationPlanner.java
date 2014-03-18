/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
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
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.utils.U;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The starmap exploration and satellite planner.
 * @author akarnokd, 2011.12.27.
 */
public class ExplorationPlanner extends Planner {
	/** The exploration map. */
	final ExplorationMap exploration;
	/** Set the last satellite deploy date. */
	final Action1<Date> setLastSatelliteDeploy;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 * @param exploration the exploration map
	 * @param setLastSatelliteDeploy set the last satellite deploy date
	 */
	public ExplorationPlanner(AIWorld world, AIControls controls, ExplorationMap exploration,
			Action1<Date> setLastSatelliteDeploy) {
		super(world, controls);
		this.exploration = exploration;
		this.setLastSatelliteDeploy = setLastSatelliteDeploy;
	}
	@Override
	public void plan() {
		deploySatellites();
		// find a fleet which has at least a decent radar range
		// and is among the fastest available
		if (!exploration.allowedMap(world.explorationInnerLimit, world.explorationOuterLimit).isEmpty()) {
			chechRadarUsage();
			// check our current exploration fleets are idle
			List<AIFleet> fs = findFleetsWithTask(FleetTask.EXPLORE, null);
			for (AIFleet f : fs) {
				if (!f.isMoving()) {
					// move it
					if (setExplorationTarget(f)) {
						return;
					}
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
				if (setExplorationTarget(bestFleet)) {
					return;
				}
			} else {
				// at least find a fleet and move it around
				for (AIFleet f : findFleetsFor(FleetTask.EXPLORE, null)) {
					if (setExplorationTarget(f)) {
						return;
					}
				}
			}
			if (checkExplorerLevel(world.ownFleets)) {
				planDiscoveryFleet();
			}
		} else {
			// yield more when exploration is finished
			if (ModelUtils.random() < 0.10) {
				// exploration complete, set explorers to idle
				for (final AIFleet f : findFleetsWithTask(FleetTask.EXPLORE, null)) {
					f.task = FleetTask.IDLE;
					add(new Action0() {
						@Override
						public void invoke() {
							f.fleet.task = FleetTask.IDLE;
						}
					});
				}
				for (final AIFleet bf : findFleetsWithTask(FleetTask.PATROL, null)) {
					if (!bf.isMoving()) {
						if (world.ownPlanets.size() > 0) {
							setPatrolTarget(bf);
						}
					}
				}
				// if we explored everything, let's patrol
				for (final AIFleet bf : findFleetsFor(FleetTask.PATROL, null)) {
					if (world.ownPlanets.size() > 0) {
						setPatrolTarget(bf);
					}
				}
			}
		}
		
	}
	/**
	 * Produce/Deploy satellites.
	 * @return action taken?
	 */
	public boolean deploySatellites() {
		if (world.lastSatelliteDeploy != null) {
			if (world.now.getTime() - world.lastSatelliteDeploy.getTime() < 24L * 60 * 60 * 1000) {
				return true;
			}
		}
		List<AIPlanet> survey = new ArrayList<>(world.unknownPlanets);
		survey.addAll(world.enemyPlanets);
		
		final Point2D.Double center = world.center();
		
		Collections.sort(survey, new Comparator<AIPlanet>() {
			@Override
			public int compare(AIPlanet o1, AIPlanet o2) {
				if (o1.hasSatelliteOfAI && !o2.hasSatelliteOfAI) {
					return 1;
				} else
				if (!o1.hasSatelliteOfAI && o2.hasSatelliteOfAI) {
					return -1;
				}
				double d1 = Math.hypot(o1.planet.x - center.x, o1.planet.y - center.y);
				double d2 = Math.hypot(o2.planet.x - center.x, o2.planet.y - center.y);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		});
		
		// traverse all known planet and deploy satellites
		outer:
		for (final AIPlanet planet : survey) {
			AIInventoryItem currentSatellite = null;
			for (AIInventoryItem ii : planet.inventory) {
				if (ii.owner == p && ii.type.has(ResearchType.PARAMETER_DETECTOR) 
						&& ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
					currentSatellite = ii;
				}
			}
			// find the most advanced satellite
			ResearchType sat = null;
			for (Map.Entry<ResearchType, Integer> e : world.inventory.entrySet()) {
				ResearchType rt = e.getKey();
				int radar = rt.getInt(ResearchType.PARAMETER_DETECTOR, 0);
				if (e.getValue() > 0 
						&& rt.category == ResearchSubCategory.SPACESHIPS_SATELLITES
						&& radar > 0) {
					if (sat == null || sat.getInt(ResearchType.PARAMETER_DETECTOR) < radar) {
						sat = rt;
					}
				}
			}
			if (sat != null) {
				// if we couldn't find a better satellite
				if (currentSatellite != null 
						&& currentSatellite.type.getInt(ResearchType.PARAMETER_DETECTOR) >= sat.getInt(ResearchType.PARAMETER_DETECTOR)) {
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
				world.lastSatelliteDeploy = world.now;
				setLastSatelliteDeploy.invoke(world.now);
				return true;
			}
			// find the best available detector
			for (ResearchType rt : world.availableResearch) {
				if (rt.has(ResearchType.PARAMETER_DETECTOR)) {
					if (sat == null || sat.getInt(ResearchType.PARAMETER_DETECTOR) < rt.getInt(ResearchType.PARAMETER_DETECTOR)) {
						sat = rt;
					}
				}
			}
			if (sat != null) {
				if (buildFactoryFor(sat)) {
					placeProductionOrder(sat, 1);
				}
				return true;
			}
		}
		return false;
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
		AIPlanet p = ModelUtils.random(world.ownPlanets);
		final int x = p.planet.x;
		final int y = p.planet.y;
		bf.task = FleetTask.PATROL;
		add(new Action0() {
			@Override
			public void invoke() {
				if (bf.fleet.task != FleetTask.SCRIPT) {
					controls.actionMoveFleet(bf.fleet, x, y);
					bf.fleet.task = FleetTask.PATROL;
				}
			}
		});
	}
	/**
	 * Set a new exploration target and task for the given fleet.
	 * @param bf the fleet
	 * @return true if action taken
	 */
	boolean setExplorationTarget(final AIFleet bf) {
		final Point2D.Double center = world.center();
		final Point2D.Double fa = new Point2D.Double(bf.x, bf.y);
		Set<Location> allowed = exploration.allowedMap(world.explorationInnerLimit, world.explorationOuterLimit);
		Comparator<Location> distance = new Comparator<Location>() {
			/** Returns the location's value. */
			double locationValue(Location o) {
				double dc = center.distance(exploration.toMapCenter(o));
				double df = center.distance(fa);
				return -df / bf.statistics.speed / dc;
			}
			@Override
			public int compare(Location o1, Location o2) {
				double v1 = locationValue(o1);
				double v2 = locationValue(o2);
				return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
			}
		};
		Location loc = null;
		final int rl = bf.radarLevel();
		// move to center or edge of the cell?
		double bias = 0.5;
		if (rl > 0 && rl % 2 == 0) {
			bias = 0;
			
			// remove top segments to avoid leaving the map
			Iterator<Location> it = allowed.iterator();
			while (it.hasNext()) {
				Location loc0 = it.next();
				if (loc0.y == 0 || loc0.x == 0) {
					it.remove();
				}
			}
			if (allowed.isEmpty()) {
				return false;
			}
		}
		final double fbias = bias;
		if (rl > 0/* && w.random.get().nextDouble() < 0.99*/) {
			outer:
			for (Location loc0 : U.sort(allowed, distance)) {
				for (AIFleet f : world.ownFleets) {
					if (f.targetPoint != null && f.task == FleetTask.EXPLORE) {
						double dc = Math.hypot(f.targetPoint.x - (loc0.x + fbias) * exploration.cellWidth, 
								f.targetPoint.y - (loc0.y + fbias) * exploration.cellHeight);
						if (dc < 1) {
							continue outer;
						}
					}
				}
				// noone targeted this
				loc = loc0;
				break;
			}
			if (loc == null) {
				loc = Collections.min(allowed, distance);
			}
		} else {
			List<Location> ls = U.sort(allowed, distance);
			if (ls.size() > 20) {
				loc = ModelUtils.random(ls.subList(0, 20));
			} else {
				loc = ModelUtils.random(ls);
			}
		}
		final Location floc = loc;
		bf.task = FleetTask.EXPLORE;
		add(new Action0() {
			@Override
			public void invoke() {
				if (bf.fleet.task != FleetTask.SCRIPT) {
					controls.actionMoveFleet(bf.fleet, (floc.x + fbias) * exploration.cellWidth, (floc.y + fbias) * exploration.cellHeight);
					bf.fleet.task = FleetTask.EXPLORE;
				}
			}
		});
		return true;
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
        return checkDeploy();
    }
	/**
	 * Find the best available radar and ship in inventory, and deploy it.
	 * @return true if action taken
	 */
	boolean checkDeploy() {
		List<AIPlanet> mss = new ArrayList<>();
		for (final AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.hasMilitarySpaceport) {
				mss.add(planet);
			}
		}
		if (mss.isEmpty()) {
			return false;
		}
		final AIPlanet deploy = ModelUtils.random(mss);
		ResearchType what = findBestFixed();
		if (what == null) {
			what = findBestNormal();
		}
		if (what != null) {
			final ResearchType fwhat = what;
			world.addInventoryCount(fwhat, -1);
			add(new Action0() {
				@Override
				public void invoke() {
					if (deploy.planet.owner.inventoryCount(fwhat) > 0) {
						Fleet f = controls.actionCreateFleet(format("explorer_fleet", p.shortName), deploy.planet);
						f.deployItem(fwhat, p, 1);
						f.upgradeAll();
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
	 * Find the best available, fixed radar ship from the technology tree.
	 * @return the best found
	 */
	ResearchType findBestFixedTech() {
		// check if we have a ship which has a fixed radar in inventory
		ResearchType bestFixed = null;
		int bestFixedRadar = 0;
		outer0:
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
		if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
			return true;
		}
		if (world.isAvailable("OrbitalFactory") != null) {
			return rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS;
		}
		return false;
	}
	/**
	 * Find the best available ship capable of carrying a radar.
	 * @return the best technology
	 */
	ResearchType findBestTech() {
		ResearchType best = null;
		int bestFixedRadar = 0;
		outer1:
		for (ResearchType rt : world.availableResearch) {
			if (fitForExploration(rt)) {
				for (EquipmentSlot es : rt.slots.values()) {
					if (es.fixed) {
						for (ResearchType rt0 : es.items) {
							if (rt0.has("radar") && world.isAvailable(rt0)) {
								if (best == null || bestFixedRadar < rt0.getInt("radar")) {
									best = rt;
									bestFixedRadar = rt0.getInt("radar");
								}
								continue outer1;
							}
						}
					}
				}
			}
		}
		return best;
	}
	/**
	 * Check if we have radar in inventory.
	 * @return if action taken
	 */
	boolean checkEquipment() {
		ResearchType best = findBestFixedTech();
		if (best != null) {
			if (world.inventoryCount(best) > 0) {
				return false;
			}
		} else {
			best = findBestTech();
			if (best != null) {
				if (world.inventoryCount(best) > 0) {
					return false;
				}
			}
		}
		if (best != null) {
			if (best.has("needsOrbitalFactory")) {
				// check the existence of orbital factory
				if (checkOrbitalFactory()) {
					return true;
				}
			}
			placeProductionOrder(best, 1);
			return true;
		}
		// list available radars
		List<ResearchType> radars = new ArrayList<>();
		for (ResearchType rt : world.availableResearch) {
			if (rt.has("radar") && rt.category == ResearchSubCategory.EQUIPMENT_RADARS) {
				radars.add(rt);
			}
		}
		// list ships capable of carrying radar
		List<ResearchType> ships = new ArrayList<>();
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
			if (bestShip.has("needsOrbitalFactory")) {
				// check the existence of orbital factory
				if (checkOrbitalFactory()) {
					return true;
				}
			}
			placeProductionOrder(bestShip, 1);
		}
		return true;
	}
	/**
	 * See if the available fleets lack radar and we can't even produce radars due lack of factory.
	 */
	void chechRadarUsage() {
			// check if radar technology is available
			ResearchType radarType = null;
			for (ResearchType rt : world.availableResearch) {
				if (rt.has(ResearchType.PARAMETER_RADAR)) {
					if (radarType == null || radarType.productionCost < rt.productionCost) {
						radarType = rt;
					}
				}
			}
			if (radarType != null) {
				buildFactoryFor(radarType);
			}
//		}
	}
	/**
	 * Build a factory for the given technology if not already built.
	 * @param tech the technology
	 * @return true if the factory is available and operational
	 */
	boolean buildFactoryFor(ResearchType tech) {
		boolean hasFactory = false;
		boolean workingFactory = false;
		List<AIPlanet> ps = new ArrayList<>(world.ownPlanets);
		Collections.sort(ps, BEST_PLANET);
		if (!checkPlanetPreparedness()) {
			return false;
		}
		for (AIPlanet p : ps) {
			for (AIBuilding b : p.buildings) {
				if (b.hasResource(tech.factory)) {
					hasFactory = true;
					if (b.isOperational()) {
						workingFactory = true;
						break;
					}
				}
			}
		}
		if (!hasFactory) {
			BuildingType factory = null;
			for (BuildingType bt : p.world.buildingModel.buildings.values()) {
				if (bt.hasResource(tech.factory)) {
					if (factory == null || factory.cost > bt.cost) {
						factory = bt;
					}
				}
			}
			if (factory != null && world.money >= factory.cost) {
				for (final AIPlanet p : ps) {
					if (p.canBuild(factory)) {
						Point pt = p.findLocation(factory);
						if (pt != null) {
							world.money -= factory.cost;
							final BuildingType ffactory = factory;
							add(new Action0() {
								@Override
								public void invoke() {
									controls.actionPlaceBuilding(p.planet, ffactory);
								}
							});
							return false;
						}
					}
				}
			}
		}
		return workingFactory;
	}
}
