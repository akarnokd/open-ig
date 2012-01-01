/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple research planner.
 * @author akarnokd, 2011.12.27.
 */
public class ResearchPlanner extends Planner {
	/** The set of resource names. */
	private static final Set<String> LAB_RESOURCE_NAMES = 
			new HashSet<String>(Arrays.asList("ai", "civil", "computer", "mechanical", "military"));
	/** Check colony ship function. */
	final Pred1<AIFleet> hasColonyShip = new Pred1<AIFleet>() {
		@Override
		public Boolean invoke(AIFleet value) {
			return value.hasInventory("ColonyShip");
		}
	};

	/** The exploration map. */
	final ExplorationMap exploration;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 * @param exploration the exploration map
	 */
	public ResearchPlanner(AIWorld world, AIControls controls, ExplorationMap exploration) {
		super(world, controls);
		this.exploration = exploration;
	}
	@Override
	public void plan() {
		if (world.runningResearch != null) {
			// if not enough labs, stop research and let the other management tasks apply
			if (!world.runningResearch.hasEnoughLabs(world.global)) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionStopResearch(world.runningResearch);
					}
				});
			}
			return;
		}
		
		//if low on money and planets, plan for conquest
		if (world.money < 100000 && world.global.planetCount < 2) {
			return;
		}
		
		final Map<ResearchType, Integer> enablesCount = new HashMap<ResearchType, Integer>();
		final Map<ResearchType, Integer> rebuildCount = new HashMap<ResearchType, Integer>();
		List<ResearchType> candidatesImmediate = new ArrayList<ResearchType>();
		List<ResearchType> candidatesReconstruct = new ArrayList<ResearchType>();
		List<ResearchType> candidatesGetMorePlanets = new ArrayList<ResearchType>();
		
		// prepare lab costs
		Map<String, Integer> labCosts = new HashMap<String, Integer>();
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			for (String s : LAB_RESOURCE_NAMES) {
				if (bt.resources.containsKey(s)) {
					labCosts.put(s, bt.cost);
					break;
				}
			}
		}
		for (ResearchType rt : world.remainingResearch) {
			if (rt.hasEnoughLabs(world.global)) {
				candidatesImmediate.add(rt);
				setResearchEnables(rt, enablesCount);
			} else
			if (rt.labCount() <= world.ownPlanets.size()) {
				candidatesReconstruct.add(rt);
				setResearchEnables(rt, enablesCount);
				rebuildCount.put(rt, rebuildCost(rt, labCosts));
			} else {
				candidatesGetMorePlanets.add(rt);
				setResearchEnables(rt, enablesCount);
			}
		}
		// yield if one planet available and not enough money
		if (candidatesImmediate.size() > 0) {
			Collections.sort(candidatesImmediate, new CompareFromMap(enablesCount));
			final ResearchType rt = candidatesImmediate.get(0);
			double mf = 2.0;
			final double moneyFactor = mf; // TODO decision variable
			applyActions.add(new Action0() {
				@Override
				public void invoke() {
					controls.actionStartResearch(rt, moneyFactor);
				}
			});
			return;
		}
		if (candidatesReconstruct.size() > 0) {
			planReconstruction(rebuildCount, candidatesReconstruct);
			return;
		}
		if (candidatesGetMorePlanets.size() > 0 
				&& world.mayConquer 
				&& (world.colonizationLimit < 0 
				|| world.colonizationLimit < world.statistics.planetsColonized)) {
			// TODO this is more complicated
			planConquest();
			return;
		}
		return;
	}
	/**
	 * Checki if the colonizers have actually reached their planet.
	 * @return true if action taken
	 */
	boolean checkColonizersReachedPlanet() {
		List<AIFleet> colonizers = findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip);
		for (AIFleet fleet : colonizers) {
				if (!fleet.isMoving()
							&& fleet.statistics.planet != null
							&& world.planetMap.get(fleet.statistics.planet).owner == null) {
				final Fleet f0 = fleet.fleet;
				final Planet p0 = fleet.statistics.planet;
				add(new Action0() {
					@Override
					public void invoke() {
						if (p0.owner == null) {
							controls.actionColonizePlanet(f0, p0);
						}
						f0.task = FleetTask.IDLE;
					}
				});
				return true;
			}
		}
		// if our colonizers are under way
		if (colonizers.size() > 0) {
			return true;
		}
		return false;
	}
	/**
	 * @return list of colonizable planets not already targeted
	 */
	List<AIPlanet> findColonizablePlanets() {
		// locate knownly colonizable planets
		List<AIPlanet> ps = new ArrayList<AIPlanet>();
		outer1:
		for (AIPlanet p : world.enemyPlanets) {
			if (p.owner == null && world.withinLimits(p.planet.x, p.planet.y)) {
				// check if no one targets this planet already
				for (AIFleet f : world.ownFleets) {
					if (f.targetPlanet == p.planet) {
						continue outer1;
					}
				}
				ps.add(p);
			}
		}
		return ps;
	}
	/**
	 * Assign available fleets to colonization task.
	 * @param ps the target planet
	 * @return true if action taken
	 */
	boolean assignFleetsToColonization(List<AIPlanet> ps) {
		// bring one fleet to the target planet
		for (final AIFleet fleet : findFleetsFor(FleetTask.COLONIZE, hasColonyShip)) {
			final AIPlanet p0 = Collections.min(ps, new Comparator<AIPlanet>() {
				@Override
				public int compare(AIPlanet o1, AIPlanet o2) {
					double d1 = Math.hypot(fleet.x - o1.planet.x, fleet.y - o1.planet.y);
					double d2 = Math.hypot(fleet.x - o2.planet.x, fleet.y - o2.planet.y);
					return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
				}
			});
			add(new Action0() {
				@Override
				public void invoke() {
					fleet.fleet.task = FleetTask.COLONIZE;
					controls.actionMoveFleet(fleet.fleet, p0.planet);
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Find a military spaceport.
	 * @return (false, null) if a military spaceport is being constructed,
	 * (true, null) no military spaceport found, (true, X) found at planet X
	 */
	Pair<Boolean, AIPlanet> findMilitarySpaceport() {
		AIPlanet sp = null;
		for (AIPlanet pl : world.ownPlanets) {
			if (pl.statistics.hasMilitarySpaceport) {
				sp = pl;
				break;
			} else {
				// if constructing here, return
				for (AIBuilding b : pl.buildings) {
					if (b.type.id.equals("MilitarySpaceport") && pl.statistics.constructing) {
						return Pair.of(false, null);
					}
				}
			}
		}
		return Pair.of(true, sp);
	}
	/**
	 * Build a military spaceport at the best planet.
	 */
	void buildMilitarySpaceport() {
		final BuildingType bt = findBuilding("MilitarySpaceport");
		planCategory(new Pred1<AIPlanet>() {
			@Override
			public Boolean invoke(AIPlanet value) {
				return true;
			}
		}, BEST_PLANET, new BuildingSelector() {
			@Override
			public boolean accept(AIPlanet planet, AIBuilding building) {
				return false;
			}
			@Override
			public boolean accept(AIPlanet planet, BuildingType buildingType) {
				return buildingType == bt && count(planet, bt) < 1;
			}
		}, costOrderReverse, false);
	}
	/**
	 * Deploy a colony ship from inventory.
	 * @param spaceport the target planet
	 * @return true if action taken
	 */
	boolean deployInventoryColonyShip(final AIPlanet spaceport) {
		// check if we have colony ships in the inventory
		final Pair<Integer, ResearchType> csi = world.inventoryCount("ColonyShip");
		if (csi.first > 0) {
			add(new Action0() {
				@Override
				public void invoke() {
					if (spaceport.owner.inventoryCount(csi.second) > 0) {
						Fleet f = controls.actionCreateFleet(label("colonizer_fleet_name"), spaceport.planet);
						f.addInventory(csi.second, 1);
						spaceport.owner.changeInventoryCount(csi.second, -1);
					}
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Plan for conquest.
	 */
	void planConquest() {
		if (checkColonizersReachedPlanet()) {
			return;
		}
		List<AIPlanet> ps = findColonizablePlanets();
		// if none, exit
		if (ps.size() == 0) {
			return;
		}
		
		if (assignFleetsToColonization(ps)) {
			return;
		}

		final Pair<Boolean, AIPlanet> spaceport = findMilitarySpaceport();
		// if no planet has military spaceport, build one somewhere
		if (!spaceport.first) {
			buildMilitarySpaceport();
			return;
		}
		if (deployInventoryColonyShip(spaceport.second)) {
			return;
		}
		if (checkOrbitalFactory()) {
			return;
		}
		final ResearchType cs = world.isAvailable("ColonyShip");
		if (cs != null) {
			placeProductionOrder(cs, 1);
			return;
		}
	}
	/**
	 * Check for the orbital factory.
	 * @return true
	 */
	boolean checkOrbitalFactory() {
		if (world.global.orbitalFactory == 0) {
			// check if we have orbital factory in inventory, deploy it
			final Pair<Integer, ResearchType> orbital = world.inventoryCount("OrbitalFactory");
			if (orbital.first > 0) {
				List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
				Collections.sort(planets, BEST_PLANET);
				
				if (deployOrbitalFactory(orbital.second, planets)) {
					return true;
				}
				// if no room, make
				if (sellStations(planets)) {
					return true;
				}
			}
			
			// if researched, build one
			if (produceOrbitalFactory()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Sell the cheapest deployed station.
	 * @param planets the list of planets
	 * @return true if action taken
	 */
	boolean sellStations(List<AIPlanet> planets) {
		Pair<AIPlanet, ResearchType> toSell = null;
		for (AIPlanet p : planets) {
			for (AIInventoryItem ii : p.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					if (toSell == null || toSell.second.productionCost > ii.type.productionCost) {
						toSell = Pair.of(p, ii.type);
					}
				}
			}
		}
		if (toSell != null) {
			final Pair<AIPlanet, ResearchType> fsell = toSell;
			add(new Action0() {
				@Override
				public void invoke() {
					controls.actionSellSatellite(fsell.first.planet, fsell.second, 1);
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Deploy orbital factory.
	 * @param rt the factory tech
	 * @param planets the planets to check
	 * @return true if action taken
	 */
	boolean deployOrbitalFactory(final ResearchType rt, List<AIPlanet> planets) {
		for (final AIPlanet p2 : planets) {
			int sats = count(p2.inventory, new Pred1<AIInventoryItem>() {
				@Override
				public Boolean invoke(AIInventoryItem value) {
					return value.type.category == ResearchSubCategory.SPACESHIPS_STATIONS;
				}
			}, new Func1<AIInventoryItem, Integer>() {
				@Override
				public Integer invoke(AIInventoryItem value) {
					return value.count;
				}
			});
			if (sats < 3) {
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionDeploySatellite(p2.planet, rt);
					}
				});
				return true;
			}
		}
		return false;
	}
	/**
	 * Produce an orbital factory.
	 * @return action taken
	 */
	boolean produceOrbitalFactory() {
		final ResearchType of = world.isAvailable("OrbitalFactory");
		if (of != null) {
			placeProductionOrder(of, 1);
			return true;
		}
		return false;
	}
	/**
	 * Plan how the labs will be reconstructed to allow the next research.
	 * @param rebuildCount the number of new buildings needed for each research
	 * @param candidatesReconstruct the candidates for the research
	 * @return the list of actions
	 */
	List<Action0> planReconstruction(
			final Map<ResearchType, Integer> rebuildCount,
			List<ResearchType> candidatesReconstruct) {
		// find the research that requires the fewest lab rebuilds
		Collections.sort(candidatesReconstruct, new CompareFromMap(rebuildCount));

		final ResearchType rt = candidatesReconstruct.get(0);
		
		// find an empty planet
		for (AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.activeLabCount() == 0 
					&& !planet.statistics.constructing) {
				buildOneLabFor(rt, planet);
				return applyActions;
			}
		}
		// find a planet with excess labs.
		for (AIPlanet planet : world.ownPlanets) {
			if (demolishOneLabFor(rt, planet)) {
				return applyActions;
			}
		}
		return applyActions;
	}
	/**
	 * Demolish one of the excess labs on the planet to make room.
	 * @param rt the research type
	 * @param planet the target planet
	 * @return true if demolish added
	 */
	boolean demolishOneLabFor(ResearchType rt, AIPlanet planet) {
		if (demolishOneLabIf(rt.aiLab, world.global.aiLab, planet.statistics.aiLab, planet.planet, "ai")) {
			return true;
		}
		if (demolishOneLabIf(rt.civilLab, world.global.civilLab, planet.statistics.civilLab, planet.planet, "civil")) {
			return true;
		}
		if (demolishOneLabIf(rt.compLab, world.global.compLab, planet.statistics.compLab, planet.planet, "computer")) {
			return true;
		}
		if (demolishOneLabIf(rt.mechLab, world.global.mechLab, planet.statistics.mechLab, planet.planet, "mechanical")) {
			return true;
		}
		if (demolishOneLabIf(rt.milLab, world.global.milLab, planet.statistics.milLab, planet.planet, "military")) {
			return true;
		}
		return false;
	}
	/**
	 * Demolish one lab of the given resource.
	 * @param lab the required lab count
	 * @param global the global lab count
	 * @param local the local lab count
	 * @param planet the planet
	 * @param resource the lab resource name
	 * @return true if action added
	 */
	boolean demolishOneLabIf(int lab, int global, int local, final Planet planet, final String resource) {
		if (lab < global && local > 0) {
			applyActions.add(new Action0() {
				@Override
				public void invoke() {
					for (Building b : planet.surface.buildings) {
						if (b.type.resources.containsKey(resource)) {
							controls.actionDemolishBuilding(planet, b);
							return;
						}
					}
				}
			});
			return true;
		}
		return false;
	}
	/**
	 * Build one of the required labs.
	 * @param rt the research type
	 * @param planet the target planet
	 */
	void buildOneLabFor(final ResearchType rt, final AIPlanet planet) {
		if (buildOneLabIf(rt.aiLab, world.global.aiLab, planet.statistics.aiLab, planet, "ai")) {
			return;
		}
		if (buildOneLabIf(rt.civilLab, world.global.civilLab, planet.statistics.civilLab, planet, "civil")) {
			return;
		}
		if (buildOneLabIf(rt.compLab, world.global.compLab, planet.statistics.compLab, planet, "computer")) {
			return;
		}
		if (buildOneLabIf(rt.mechLab, world.global.mechLab, planet.statistics.mechLab, planet, "mechanical")) {
			return;
		}
		if (buildOneLabIf(rt.milLab, world.global.milLab, planet.statistics.milLab, planet, "military")) {
			return;
		}
	}
	/**
	 * Build one of the labs if the prerequisite counts match.
	 * @param required the required count of lab
	 * @param available the available count of lab
	 * @param local the locally built count
	 * @param planet the target planet
	 * @param resource the building type identification resource
	 * @return true if successful
	 */
	boolean buildOneLabIf(int required, int available, int local, final AIPlanet planet, final String resource) {
		if (required > available && local == 0) {
			final Planet planet0 = planet.planet;
			if (!planet.statistics.canBuildAnything()) {
				return false;
			}
			final BuildingType bt = findBuildingType(resource);
			if (bt == null) {
				new AssertionError("Can't find building for resource " + resource).printStackTrace();
				return false;
			}
			if (bt.cost <= world.money) {
				Point pt = planet.placement.findLocation(planet.planet.getPlacementDimensions(bt));
				if (pt != null) {
					applyActions.add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet0, bt);
						}
					});
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Find the first building who provides the given resource.
	 * @param resource the resource name
	 * @return the building type or null
	 */
	BuildingType findBuildingType(String resource) {
		for (BuildingType bt : w.buildingModel.buildings.values()) {
			if (bt.resources.containsKey(resource)) {
				return bt;
			}
		}
		return null;
	}
	/**
	 * Comparator which takes an value from the supplied map for comparison. 
	 * @author akarnokd, 2011.12.26.
	 * @param <T> the element type
	 */
	class CompareFromMap implements Comparator<ResearchType> {
		/** The backing map. */
		final Map<ResearchType, Integer> map;
		/**
		 * Constructor.
		 * @param map the backing map to use
		 */
		public CompareFromMap(Map<ResearchType, Integer> map) {
			this.map = map;
		}
		@Override
		public int compare(ResearchType o1, ResearchType o2) {
			Integer count1 = map.get(o1);
			Integer count2 = map.get(o2);
			int c = count1.compareTo(count2);
			if (c == 0) {
				c = o1.researchCost - o2.researchCost;
			}
			if (c == 0) {
				c = o1.level - o2.level;
			}
			if (c == 0) {
				c = o1.productionCost - o2.productionCost;
			}
			return c;
		}
	}
	/**
	 * Count how many labs need to be built in addition to the current settings.
	 * @param rt the research type
	 * @param labCosts the cost of various labs
	 * @return the total number of new buildings required
	 */
	int rebuildCost(ResearchType rt, Map<String, Integer> labCosts) {
		return 
				rebuildRequiredCount(rt.aiLab, world.global.aiLab) * labCosts.get("ai")
				+ rebuildRequiredCount(rt.civilLab, world.global.civilLab) * labCosts.get("civil")
				+ rebuildRequiredCount(rt.compLab, world.global.compLab) * labCosts.get("computer")
				+ rebuildRequiredCount(rt.mechLab, world.global.mechLab) * labCosts.get("mechanical")
				+ rebuildRequiredCount(rt.milLab, world.global.milLab) * labCosts.get("military")
		;
	}
	/**
	 * If the lab count is greater than the active count, return the difference.
	 * @param lab the research required lab count
	 * @param active the active research counts
	 * @return zero or the difference
	 */
	int rebuildRequiredCount(int lab, int active) {
		if (lab > active) {
			return lab - active;
		}
		return 0;
	}
	/**
	 * Counts how many further research becomes available when the research is completed.
	 * @param rt the current research
	 * @param map the map for research to count
	 */
	void setResearchEnables(ResearchType rt, Map<ResearchType, Integer> map) {
//		int count = 0;
//		for (ResearchType rt2 : world.remainingResearch) {
//			if (rt2.prerequisites.contains(rt)) {
//				count++;
//			}
//		}
//		for (ResearchType rt2 : world.furtherResearch) {
//			if (rt2.prerequisites.contains(rt)) {
//				count++;
//			}
//		}
		map.put(rt, rt.researchCost);
	}

}
