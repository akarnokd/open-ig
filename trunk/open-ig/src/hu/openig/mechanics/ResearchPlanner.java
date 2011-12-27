/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.model.AIControls;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.Resource;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple research planner.
 * @author akarnokd, 2011.12.27.
 */
public class ResearchPlanner {
	/** The world copy. */
	final AIWorld world;
	/** The original world object. */
	final World w;
	/** The player. */
	final Player p;
	/** The actions to perform. */
	public final List<Action0> applyActions;
	/** The controls to affect the world in actions. */
	private final AIControls controls;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls to affect the world in actions
	 */
	public ResearchPlanner(AIWorld world, AIControls controls) {
		this.world = world;
		this.controls = controls;
		this.p = world.player;
		this.w = p.world;
		this.applyActions = new ArrayList<Action0>();
	}
	/**
	 * Simple next research planner.
	 * @return the list of issued actions
	 */
	public List<Action0> run() {
		if (!p.id.equals("Empire")) {
			return applyActions;
		}
		if (world.runningResearch != null) {
			return applyActions;
		}
		final Map<ResearchType, Integer> enablesCount = new HashMap<ResearchType, Integer>();
		final Map<ResearchType, Integer> rebuildCount = new HashMap<ResearchType, Integer>();
		List<ResearchType> candidatesImmediate = new ArrayList<ResearchType>();
		List<ResearchType> candidatesReconstruct = new ArrayList<ResearchType>();
		List<ResearchType> candidatesGetMorePlanets = new ArrayList<ResearchType>();
		for (ResearchType rt : world.remainingResearch) {
			if (rt.hasEnoughLabs(world.global)) {
				candidatesImmediate.add(rt);
				setResearchEnables(rt, enablesCount);
			} else
			if (rt.labCount() <= world.ownPlanets.size()) {
				candidatesReconstruct.add(rt);
				setResearchEnables(rt, enablesCount);
				rebuildCount.put(rt, rebuildCount(rt));
			} else {
				candidatesGetMorePlanets.add(rt);
				setResearchEnables(rt, enablesCount);
			}
		}
		if (candidatesImmediate.size() > 0) {
			Collections.sort(candidatesImmediate, new CompareFromMap<ResearchType>(enablesCount));
			final ResearchType rt = candidatesImmediate.get(0);
			double mf = 1.0;
			if (rt.researchCost * 5 >= world.money) {
				mf = 2.0;
			}
			final double moneyFactor = mf; // TODO decision variable
			applyActions.add(new Action0() {
				@Override
				public void invoke() {
					controls.actionStartResearch(rt, moneyFactor);
				}
			});
			return applyActions;
		}
		if (candidatesReconstruct.size() > 0) {
			// find the research that requires the fewest lab rebuilds
			Collections.sort(candidatesReconstruct, new CompareFromMap<ResearchType>(rebuildCount));

			final ResearchType rt = candidatesReconstruct.get(candidatesReconstruct.size() - 1);
			
			for (AIPlanet planet : world.ownPlanets) {
				if (planet.statistics.labCount() != planet.statistics.activeLabCount()
						&& !planet.statistics.constructing) {
					buildMorePowerPlant(planet.planet);
					return applyActions;
				}
			}
			// find an empty planet
			for (AIPlanet planet : world.ownPlanets) {
				if (planet.statistics.labCount() == 0 && !planet.statistics.constructing) {
					buildOneLabFor(rt, planet);
					return applyActions;
				}
			}
			for (AIPlanet planet : world.ownPlanets) {
				if (demolishOneLabFor(rt, planet)) {
					return applyActions;
				}
			}
			return applyActions;
		}
		if (candidatesGetMorePlanets.size() > 0) {
			Collections.sort(candidatesGetMorePlanets, new CompareFromMap<ResearchType>(rebuildCount));
			// TODO this is more complicated
		}
		return applyActions;
	}
	/**
	 * Build or upgade a power plant on the planet.
	 * @param planet the target planet
	 */
	void buildMorePowerPlant(final Planet planet) {
		applyActions.add(new Action0() {
			@Override
			public void invoke() {
				// scan for buildings
				for (Building b : planet.surface.buildings) {
					Resource r = b.type.resources.get("energy");
					if (r != null && r.amount > 0) {
						// if damaged, repair
						if (b.isDamaged()) {
							controls.actionRepairBuilding(planet, b, !b.isOperational());
							return;
						} else
						// if upgradable and can afford upgrade
						if (b.upgradeLevel < b.type.upgrades.size()) {
							int newLevel = Math.min(b.upgradeLevel + (int)(p.money / b.type.cost), b.type.upgrades.size());
							if (newLevel != b.upgradeLevel) {
								controls.actionUpgradeBuilding(planet, b, newLevel);
								return;
							}
						}
					}
				}
				// if no existing building found
				// find the most expensive still affordable building
				BuildingType target = null;
				for (BuildingType bt : w.buildingModel.buildings.values()) {
					if (bt.resources.containsKey("energy") && planet.canBuild(bt)) {
						if (target == null || (bt.cost <= p.money && bt.cost > target.cost)) {
							target = bt;
						}
					}					
				}
				if (target != null) {
					controls.actionPlaceBuilding(planet, target);
				}
			}
		});
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
		if (buildOneLabIf(rt.aiLab, world.global.aiLab, planet.planet, "ai")) {
			return;
		}
		if (buildOneLabIf(rt.civilLab, world.global.civilLab, planet.planet, "civil")) {
			return;
		}
		if (buildOneLabIf(rt.compLab, world.global.compLab, planet.planet, "computer")) {
			return;
		}
		if (buildOneLabIf(rt.mechLab, world.global.mechLab, planet.planet, "mechanical")) {
			return;
		}
		if (buildOneLabIf(rt.milLab, world.global.milLab, planet.planet, "military")) {
			return;
		}
	}
	/**
	 * Build one of the labs if the prerequisite counts match.
	 * @param required the required count of lab
	 * @param available the available count of lab
	 * @param planet the target planet
	 * @param resource the building type identification resource
	 * @return true if successful
	 */
	boolean buildOneLabIf(int required, int available, final Planet planet, String resource) {
		if (required > available) {
			final BuildingType bt = findBuildingType(resource);
			if (bt != null) {
				if (bt.cost <= world.money) {
					applyActions.add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet, bt);
						}
					});
					return true;
				}
			} else {
				new AssertionError("Can't find building for resource " + resource).printStackTrace();
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
	 * Comparator which takes an integer index from the supplied map. 
	 * @author akarnokd, 2011.12.26.
	 * @param <T> the element type
	 */
	class CompareFromMap<T> implements Comparator<T> {
		/** The backing map. */
		final Map<T, Integer> map;
		/**
		 * Constructor.
		 * @param map the backing map to use
		 */
		public CompareFromMap(Map<T, Integer> map) {
			this.map = map;
		}
		@Override
		public int compare(T o1, T o2) {
			int count1 = map.get(o1);
			int count2 = map.get(o2);
			return count1 < count2 ? 1 : (count1 > count2 ? -1 : 0);
		}
	}
	/**
	 * Count how many labs need to be built in addition to the current settings.
	 * @param rt the research type
	 * @return the total number of new buildings required
	 */
	int rebuildCount(ResearchType rt) {
		return 
				rebuildRequiredCount(rt.aiLab, world.global.aiLab)
				+ rebuildRequiredCount(rt.civilLab, world.global.civilLab)
				+ rebuildRequiredCount(rt.compLab, world.global.compLab)
				+ rebuildRequiredCount(rt.mechLab, world.global.mechLab)
				+ rebuildRequiredCount(rt.milLab, world.global.milLab)
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
		int count = 0;
		for (ResearchType rt2 : world.remainingResearch) {
			if (rt2.prerequisites.contains(rt)) {
				count++;
			}
		}
		for (ResearchType rt2 : world.furtherResearch) {
			if (rt2.prerequisites.contains(rt)) {
				count++;
			}
		}
		map.put(rt, count);
	}

}
