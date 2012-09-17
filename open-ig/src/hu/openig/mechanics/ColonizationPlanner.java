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
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetTask;
import hu.openig.model.Planet;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.utils.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Colonization planner.
 * @author akarnokd, 2012.08.20.
 */
public class ColonizationPlanner extends Planner {
	/** Indicator to allow actions that spendmoney. */
	boolean maySpendMoney;
	/** Check colony ship function. */
	final Pred1<AIFleet> hasColonyShip = new Pred1<AIFleet>() {
		@Override
		public Boolean invoke(AIFleet value) {
			return value.hasInventory("ColonyShip");
		}
	};
	/**
	 * Constructs the planner.
	 * @param world the world
	 * @param controls the controls
	 */
	public ColonizationPlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}
	@Override
	protected void plan() {
		//if low on money and planets, plan for conquest
		maySpendMoney = (world.money >= 100000 || world.global.planetCount > 2);
		
		boolean rq = world.researchRequiresColonization;
		boolean exp = checkEnemyExpansion();
		boolean col = checkColonizersReady();
		if (rq || exp || col) { 
			conquerMorePlanets();
		}
	}
	/**
	 * Check if we have colonizers in inventory or in fleet.
	 * @return true if colonizers can be activated
	 */
	boolean checkColonizersReady() {
		return world.inventoryCount("ColonyShip").first > 0
				|| !findFleetsFor(FleetTask.COLONIZE, hasColonyShip).isEmpty()
				|| !findFleetsWithTask(FleetTask.COLONIZE, hasColonyShip).isEmpty();
	}
	/**
	 * Check if enemies have more planets then we have.
	 * @return true if expansion is needed
	 */
	boolean checkEnemyExpansion() {
		Map<Player, Integer> counts = U.newHashMap();
		for (AIPlanet p : world.enemyPlanets) {
			Integer i = counts.get(p.owner);
			counts.put(p.owner, i != null ? i.intValue() + 1 : 1);
		}
		if (!counts.isEmpty()) {
			int max = Collections.max(counts.values());
			return max > world.ownPlanets.size() && !world.unknownPlanets.isEmpty();
		}
		return false;
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

}
