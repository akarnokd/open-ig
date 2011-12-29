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
import hu.openig.model.AIControls;
import hu.openig.model.AIFleet;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.AutoBuild;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
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
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 */
	public ResearchPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
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
		if (candidatesGetMorePlanets.size() > 0) {
			// TODO this is more complicated
			planConquest();
			return;
		}
		return;
	}
	/**
	 * Plan for conquest.
	 */
	void planConquest() {
		// if colonization ship underway, exit
		for (AIFleet fleet : world.ownFleets) {
			if (fleet.isMoving() && fleet.hasInventory("ColonyShip") && fleet.targetPlanet != null) {
				return;
			}
		}
		// if a fleet with colony ship is in position, colonize the planet
		for (AIFleet fleet : world.ownFleets) {
			if (!fleet.isMoving() && fleet.hasInventory("ColonyShip")) {
				if (fleet.statistics.planet != null) {
					for (AIPlanet planet : world.enemyPlanets) {
						if (planet.planet == fleet.statistics.planet && planet.owner == null) {
							final Fleet f0 = fleet.fleet;
							final Planet p0 = fleet.statistics.planet;
							add(new Action0() {
								@Override
								public void invoke() {
									if (p0.owner == null) {
										controls.actionColonizePlanet(f0, p0);
										p0.autoBuild = AutoBuild.CIVIL; // FIXME to avoid further problems
									}
								}
							});
							return;
						}
					}
				}
			}
		}
		// locate knownly colonizable planets
		List<AIPlanet> ps = new ArrayList<AIPlanet>();
		outer1:
		for (AIPlanet p : world.enemyPlanets) {
			if (p.owner == null) {
				// check if no one targets this planet already
				for (AIFleet f : world.ownFleets) {
					if (f.targetPlanet == p.planet) {
						continue outer1;
					}
				}
				ps.add(p);
			}
		}
		// if none exit
		if (ps.size() == 0) {
			return;
		}
		// bring one fleet to the target planet
		for (final AIFleet fleet : world.ownFleets) {
			if (!fleet.isMoving() && fleet.hasInventory("ColonyShip")) {
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
						controls.actionMoveFleet(fleet.fleet, p0.planet);
					}
				});
			}
		}
		AIPlanet sp = null;
		for (AIPlanet pl : world.ownPlanets) {
			if (pl.statistics.hasMilitarySpaceport) {
				sp = pl;
				break;
			}
		}
		// if no planet has military spaceport, build one somewhere
		if (sp == null) {
			sp = w.random(world.ownPlanets);
			final BuildingType bt = findBuilding("MilitarySpaceport");
			if (bt == null) {
				System.err.println("Military spaceport not buildable for player " + p.id);
			} else {
				if (bt.cost <= world.money) {
					final Planet spaceport = sp.planet; 
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(spaceport, bt);
						}
					});
				}
			}
			return;
		}
		final Planet spaceport = sp.planet; 
		// check if we have colony ships in the inventory
		final Pair<Integer, ResearchType> csi = world.inventoryCount("ColonyShip");
		if (csi.first > 0) {
			add(new Action0() {
				@Override
				public void invoke() {
					Fleet f = controls.actionCreateFleet(label("colonizer_fleet_name"), spaceport);
					f.addInventory(csi.second, 1);
				}
			});
			return;
		}
		if (world.global.orbitalFactory == 0) {
			// check if we have orbital factory in inventory, deploy it
			final Pair<Integer, ResearchType> orbital = world.inventoryCount("OrbitalFactory");
			if (orbital.first > 0) {
				List<AIPlanet> planets = shuffle(world.ownPlanets);
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
								controls.actionDeploySatellite(p2.planet, orbital.second);
							}
						});
						return;
					}
				}
				// if no room, make
				Pair<AIPlanet, ResearchType> toSell = null;
				for (AIPlanet p : planets) {
					for (AIInventoryItem ii : p.inventory) {
						if (ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
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
					return;
				}
			}
			
			// if researched, build one
			final ResearchType of = world.isAvailable("OrbitalFactory");
			if (of != null) {
				placeProductionOrder(of, 1);
				return;
			}
		} else {
			final ResearchType cs = world.isAvailable("ColonyShip");
			if (cs != null) {
				placeProductionOrder(cs, 1);
				return;
			}
		}
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
