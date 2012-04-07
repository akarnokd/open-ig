/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Pred0;
import hu.openig.core.Tile;
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.VehiclePlan;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Planner for building ground defenses and space stations.
 * @author akarnokd, 2012.01.04.
 */
public class StaticDefensePlanner extends Planner {
	/** The new construction money limit. */
	protected static final int MONEY_LIMIT = 150000;
	/**
	 * Constructor. Initializes the fields.
	 * @param world the world object
	 * @param controls the controls
	 */
	public StaticDefensePlanner(AIWorld world, AIControls controls) {
		super(world, controls);
	}

	@Override
	protected void plan() {
		List<AIPlanet> planets = new ArrayList<AIPlanet>(world.ownPlanets);
		Collections.sort(planets, BEST_PLANET);
		for (AIPlanet planet : planets) {
			if (managePlanet(planet)) {
				return;
			}
		}
	}
	/**
	 * Manage a planet. Used by the AutoBuilder.
	 * @param planet the target planet
	 */
	public void managePlanet(Planet planet) {
		for (AIPlanet p : world.ownPlanets) {
			if (p.planet == planet) {
				managePlanet(p);
				return;
			}
		}
	}
	/**
	 * Manage a concrete planet.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	public boolean managePlanet(final AIPlanet planet) {
		if (planet.statistics.constructing) {
			return false;
		}
		
		List<Pred0> actions = new ArrayList<Pred0>();
		
		int defenseLimit = 3;
		if (world.difficulty == Difficulty.NORMAL) {
			defenseLimit = 4;
		} else
		if (world.difficulty == Difficulty.HARD) {
			defenseLimit = 5;
		}
		final int fdefenseLimit = defenseLimit;

		
		if (world.money >= 150000 && planet.population >= planet.statistics.workerDemand * 1.1) {
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					if (checkBuildingKind(planet, "Gun", fdefenseLimit)) {
						return true;
					}
					return false;
				}
			});
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					// find the best available shield technology
					if (checkBuildingKind(planet, "Shield", Integer.MAX_VALUE)) {
						return true;
					}
					return false;
				}
			});
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					// find bunker
					if (checkBuildingKind(planet, "Bunker", Integer.MAX_VALUE)) {
						return true;
					}
					return false;
				}
			});
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					// find barracks..strongholds
					if (checkBuildingKind(planet, "Defensive", fdefenseLimit)) {
						return true;
					}
					return false;
				}
			});
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					// check for military spaceport
					if (checkMilitarySpaceport(planet)) {
						return true;
					}
					return false;
				}
			});
			if (world.level > 1) {
				actions.add(new Pred0() {
					@Override
					public Boolean invoke() {
						// find the space stations
						if (checkStations(planet)) {
							return true;
						}
						return false;
					}
				});
			}
		}

		if (world.level > 1) {
			actions.add(new Pred0() {
				@Override
				public Boolean invoke() {
					// check for military spaceport
					if (checkTanks(planet)) {
						return true;
					}
					return false;
				}
			});
		}
		
		Collections.shuffle(actions);
		
		for (Pred0 p : actions) {
			if (p.invoke()) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Check the tanks.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkTanks(final AIPlanet planet) {
		final VehiclePlan plan = new VehiclePlan();
		plan.calculate(world.availableResearch, w.battle, 
				planet.statistics.vehicleMax, 
				p == world.mainPlayer ? Difficulty.HARD : world.difficulty);
		
		if (planet.owner.money >= MONEY_LIMIT) {
			// issue production order for the difference
			for (Map.Entry<ResearchType, Integer> prod : plan.demand.entrySet()) {
				ResearchType rt = prod.getKey();
				int count = prod.getValue();
				int inventoryGlobal = world.inventoryCount(rt);
				int inventoryLocal = planet.inventoryCount(rt);
				
				int localDemand = count - inventoryLocal;
				if (localDemand > 0 && localDemand > inventoryGlobal) {
					// if in production wait
					if (world.productionCount(rt) > 0) {
						return false;
					}
					placeProductionOrder(rt, localDemand - inventoryGlobal);
					return true;
				}
			}
		
			// undeploy old technology
			for (final AIInventoryItem ii : planet.inventory) {
				if (!plan.demand.containsKey(ii.type) 
						&& (plan.tanks.contains(ii.type) || plan.sleds.contains(ii.type))) {
					add(new Action0() {
						@Override
						public void invoke() {
							int cnt = planet.planet.inventoryCount(ii.type, ii.owner);
							planet.planet.changeInventory(ii.type, planet.owner, -cnt);
							planet.owner.changeInventoryCount(ii.type, cnt);
							log("Undeploy, Planet = %s, Type = %s, Count = %s", planet.planet.id, ii.type, cnt);
						}
					});
					return true;
				}
			}
		}
		// deploy new equipment
		for (Map.Entry<ResearchType, Integer> e : plan.demand.entrySet()) {
			final ResearchType rt = e.getKey();
			final int count = e.getValue();
			final int inventoryLocal = planet.inventoryCount(rt);
			if (inventoryLocal < count) {
				add(new Action0() {
					@Override
					public void invoke() {
						int cnt = count - inventoryLocal;
						if (p.inventoryCount(rt) >= cnt) {
							planet.planet.changeInventory(rt, planet.owner, cnt);
							p.changeInventoryCount(rt, -cnt);
							log("DeployTanks, Planet = %s, Type = %s, Count = %s", planet.planet.id, rt, cnt);
						}
						
					}
				});
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Try constructing planets / 2 + 1 spaceports.
	 * @param planet the target planet
	 * @return true if action taken
	 */
	boolean checkMilitarySpaceport(final AIPlanet planet) {
		if (world.money > 100000) {
			if (planet.statistics.militarySpaceportCount == 0 
					&& world.global.planetCount / 2 + 1 > world.global.militarySpaceportCount) {
				final BuildingType bt = findBuilding("MilitarySpaceport");
				Point pt = planet.findLocation(bt);
				if (pt != null) {
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet.planet, bt);
						}
					});
					return true;
				} else {
					// if no room, make it by demolishing a traders spaceport
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
		}
		return false;
	}
	/**
	 * Check if stations can be placed/replaced.
	 * @param planet the target planet
	 * @return if action taken
	 */
	boolean checkStations(final AIPlanet planet) {
		// find best station
		ResearchType station = null;
		for (ResearchType rt : world.availableResearch) {
			if (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS && !rt.id.equals("OrbitalFactory")) {
				if (station == null || station.productionCost < rt.productionCost) {
					station = rt;
				}
			}
		}
		
		if (station != null) {
			// count stations and find cheapest one
			int stationCount = 0;
			AIInventoryItem cheapest = null;
			for (AIInventoryItem ii : planet.inventory) {
				if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					stationCount++;
					if (!ii.type.id.equals("OrbitalFactory")) {
						if (cheapest == null || cheapest.type.productionCost > ii.type.productionCost) {
							cheapest = ii;
						}
					}
				}
			}
			// if not enough, place one
			if (stationCount < 3) {
				// if not available in inventory, construct one
				if (world.inventoryCount(station) == 0) {
					placeProductionOrder(station, 1);
					return true;
				}
				//deploy satellite
				final ResearchType fstation = station;
				add(new Action0() {
					@Override
					public void invoke() {
						controls.actionDeploySatellite(planet.planet, fstation);
					}
				});
				return true;
			} else {
				// if the cheapest is cheaper than the best station, sell it
				if (cheapest != null && cheapest.type.productionCost < station.productionCost) {
					final AIInventoryItem fcheapest = cheapest;
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionSellSatellite(planet.planet, fcheapest.type, 1);
						}
					});
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Check the availability of the given building kinds and replace
	 * cheaper ones.
	 * @param planet the target planet
	 * @param kind the building kind
	 * @param limit constrain the building count even further
	 * @return true if action taken
	 */
	boolean checkBuildingKind(final AIPlanet planet, String kind, int limit) {
		// find the best available gun technology
		BuildingType bt = null;
		for (BuildingType bt0 : p.world.buildingModel.buildings.values()) {
			if (planet.canBuild(bt0) && bt0.kind.equals(kind)) {
				if (bt == null || bt.cost < bt0.cost) {
					bt = bt0;
				}
			}
		}
		if (bt != null) {
			int gunCount = 0;
			// count guns
			for (AIBuilding b : planet.buildings) {
				if (b.type.kind.equals(kind)) {
					gunCount++;
				}
			}
			// if room, build one
			boolean hasRoom = planet.findLocation(bt) != null;
			if (gunCount < Math.abs(bt.limit) && gunCount < limit) {
				if (hasRoom) {
					final BuildingType fbt = bt;
					add(new Action0() {
						@Override
						public void invoke() {
							controls.actionPlaceBuilding(planet.planet, fbt);
						}
					});
					return true;
				}
			} else {
				
				// the current defense level and demolish lesser guns
				for (final AIBuilding b : planet.buildings) {
					if (b.type.kind.equals(kind) && b.type.cost < bt.cost) {
						
						// check if there would be room for the upgraded version
						Tile bts = bt.tileset.get(planet.planet.race).normal;
						Tile bs = b.tileset.normal;
						
						if (hasRoom || (bs.width >= bts.width && bs.height >= bts.height)) {
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
		}
		return false;
	}
	/** 
	 * Returns the actions to perform.
	 * @return the actions to perform
	 */
	public List<Action0> actions() {
		return applyActions;
	}
}
