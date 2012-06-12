/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.core.Pred1;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIResult;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.ExplorationMap;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.Planet;
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
	/** Indicator to allow actions that spendmoney. */
	boolean maySpendMoney;
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
		maySpendMoney = (world.money >= 100000 || world.global.planetCount > 2);
		
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
		if (maySpendMoney) {
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
		}
		if (candidatesGetMorePlanets.size() > 0 && conquerMorePlanets()) {
			return;
		}
		return;
	}
	/**
	 * Ckeck if more planets can be conquered.
	 * @return true if action taken
	 */
	boolean conquerMorePlanets() {
		if (world.mayConquer 
				&& (world.colonizationLimit < 0 
				|| world.colonizationLimit > world.statistics.planetsColonized)) {
			// TODO this is more complicated
			planConquest();
			return true;
		}
		return false;
	}
	/**
	 * Checki if the colonizers have actually reached their planet.
	 * @return true if action taken
	 */
	boolean checkColonizersReachedPlanet() {
		List<AIFleet> colonizers = findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip);
		for (AIFleet fleet : colonizers) {
				if (!fleet.isMoving()
							&& fleet.statistics.planet != null) {
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
			world.addInventoryCount(csi.second, -1);
			add(new Action0() {
				@Override
				public void invoke() {
					if (p.inventoryCount(csi.second) > 0) {
						Fleet f = controls.actionCreateFleet(label(p.id + ".colonizer_fleet"), spaceport.planet);
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
			if (maySpendMoney) {
				buildMilitarySpaceport();
			}
			return;
		}
		if (spaceport.second != null) {
			if (deployInventoryColonyShip(spaceport.second)) {
				return;
			}
			if (maySpendMoney) {
				if (checkOrbitalFactory()) {
					return;
				}
				final ResearchType cs = world.isAvailable("ColonyShip");
				if (cs != null) {
					placeProductionOrder(cs, 1);
					return;
				}
			}
		}
	}
	/**
	 * Plan how the labs will be reconstructed to allow the next research.
	 * @param rebuildCount the number of new buildings needed for each research
	 * @param candidatesReconstruct the candidates for the research
	 */
	void planReconstruction(
			final Map<ResearchType, Integer> rebuildCount,
			List<ResearchType> candidatesReconstruct) {
		// find the research that requires the fewest lab rebuilds
		Collections.sort(candidatesReconstruct, new CompareFromMap(rebuildCount));

		final ResearchType rt = candidatesReconstruct.get(0);

		boolean noroom = false;
		// find an empty planet
		for (AIPlanet planet : world.ownPlanets) {
			if (planet.statistics.labCount() == 0) {
				AIResult r = buildOneLabFor(rt, planet);
				if (r == AIResult.SUCCESS || r == AIResult.NO_MONEY) {
					return;
				} else
				if (r == AIResult.NO_ROOM) {
					noroom = true;
				} else
				if (r == AIResult.NO_AVAIL) {
					noroom = false;
				}
			}
		}
		// find a planet with excess labs.
		for (AIPlanet planet : world.ownPlanets) {
			if (!planet.statistics.constructing) {
				if (demolishOneLabFor(rt, planet)) {
					return;
				}
			}
		}
		// if at least one empty planet failed to build the required lab
		// conquer more planets
		boolean enoughLabs = rt.hasEnoughLabs(world.global);
		if (noroom && !enoughLabs) {
			if (conquerMorePlanets()) {
				return;
			}
		}
		
		return;
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
	 * @return the construction result
	 */
	AIResult buildOneLabFor(final ResearchType rt, final AIPlanet planet) {
		int noroom = 0;
		AIResult r = buildOneLabIf(rt.aiLab, world.global.aiLab, planet.statistics.aiLab, planet, "ai");
		if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
			return r;
		}
		if (r == AIResult.NO_ROOM) {
			noroom++;
		}
		r = buildOneLabIf(rt.civilLab, world.global.civilLab, planet.statistics.civilLab, planet, "civil");
		if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
			return r;
		}
		if (r == AIResult.NO_ROOM) {
			noroom++;
		}
		r = buildOneLabIf(rt.compLab, world.global.compLab, planet.statistics.compLab, planet, "computer");
		if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
			return r;
		}
		if (r == AIResult.NO_ROOM) {
			noroom++;
		}
		r = buildOneLabIf(rt.mechLab, world.global.mechLab, planet.statistics.mechLab, planet, "mechanical");
		if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
			return r;
		}
		if (r == AIResult.NO_ROOM) {
			noroom++;
		}
		r = buildOneLabIf(rt.milLab, world.global.milLab, planet.statistics.milLab, planet, "military");
		if (r != AIResult.NO_ROOM && r != AIResult.CONTINUE) {
			return r;
		}
		if (r == AIResult.NO_ROOM) {
			noroom++;
		}
		if (noroom > 0) {
			return AIResult.NO_ROOM;
		}
		return AIResult.CONTINUE;
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
	AIResult buildOneLabIf(int required, int available, int local, final AIPlanet planet, final String resource) {
		if (required > available && local == 0) {
			final Planet planet0 = planet.planet;
			if (!planet.statistics.canBuildAnything()) {
				return AIResult.NO_AVAIL;
			}
			final BuildingType bt = findBuildingType(resource);
			if (bt == null) {
				new AssertionError("Can't find building for resource " + resource).printStackTrace();
				return AIResult.NO_AVAIL;
			}
			if (bt.cost <= world.money) {
				Point pt = planet.placement.findLocation(planet.planet.getPlacementDimensions(bt));
				final AssertionError e = new AssertionError("Warning: " + planet0.id + ", " + bt.id);
				if (pt != null) {
					applyActions.add(new Action0() {
						@Override
						public void invoke() {
							AIResult r = controls.actionPlaceBuilding(planet0, bt);
							if (r == AIResult.NO_AVAIL) {
								e.printStackTrace();
							}
						}
					});
					return AIResult.SUCCESS;
				} else {
					return AIResult.NO_ROOM;
				}
			} else {
				return AIResult.NO_MONEY;
			}
		}
		return AIResult.CONTINUE;
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
