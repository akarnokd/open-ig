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
import hu.openig.model.AIBuilding;
import hu.openig.model.AIControls;
import hu.openig.model.AIInventoryItem;
import hu.openig.model.AIPlanet;
import hu.openig.model.AIWorld;
import hu.openig.model.BuildingType;
import hu.openig.model.EquipmentSlot;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Tile;
import hu.openig.model.VehiclePlan;
import hu.openig.utils.U;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
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
				if (world.mainPlayer == p && world.money < world.autoBuildLimit) {
					return;
				}
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
//		if (planet.planet.id.equals("Centronom")) {
//			System.out.println();
//		}
		
		if (world.autobuildEconomyFirst && p == world.mainPlayer && !isEconomyBuilt(planet)) {
			return false;
		}
		
		List<Pred0> actions = new ArrayList<Pred0>();
		
		// FIXME how many barracks to build per difficulty
		int defenseLimit = 1;
		if (world.difficulty == Difficulty.NORMAL) {
			defenseLimit = 3;
		}
		if (world.difficulty == Difficulty.HARD || p == world.mainPlayer) {
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
		}
		if (world.money >= 150000) {
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
				actions.add(new Pred0() {
					@Override
					public Boolean invoke() {
						// find the space stations
						if (checkRockets(planet)) {
							return true;
						}
						return false;
					}
				});
			}
			if (world.level > 2) {
				actions.add(new Pred0() {
					@Override
					public Boolean invoke() {
						// find the space stations
						if (checkFighters(planet)) {
							return true;
						}
						return false;
					}
				});
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
		}
		
		Collections.shuffle(actions);
		
		boolean result = false;
		for (Pred0 p : actions) {
			if (p.invoke()) {
				if (world.mainPlayer != this.p || world.money < world.autoBuildLimit) {
					return true;
				} else {
					result = true;
				}
			}
		}
		
		return result;
	}
	/**
	 * Check if all economic buildings have been built.
	 * @param planet the target planet
	 * @return true if all economic buildings built
	 */
	boolean isEconomyBuilt(AIPlanet planet) {
		boolean hasMultiply = false;
		boolean hasCredit = false;
		boolean hasTrade = world.isAvailable("TradeCenter") == null;
		boolean hasRadar = false;
		boolean hasSocial = false;
		boolean hasPolice = planet.population < 5000;
		boolean hasHospital = planet.population < 5000;
		
		for (AIBuilding b : planet.buildings) {
			if (b.isComplete()) {
				hasMultiply |= b.hasResource("multiply");
				hasCredit |= b.hasResource("credit");
				hasRadar |= b.hasResource("radar");
				hasSocial |= b.hasResource("morale");
				hasPolice |= b.hasResource("police");
				hasHospital |= b.hasResource("hospital");
				hasTrade |= b.type.id.equals("TradeCenter");
			}
		}
		return hasMultiply && hasCredit
				&& planet.statistics.hasTradersSpaceport
				&& planet.statistics.weaponsActive > 0
				&& planet.statistics.equipmentActive > 0
				&& planet.statistics.spaceshipActive > 0
				&& hasRadar
				&& hasSocial
				&& hasPolice
				&& hasHospital
				&& hasTrade;
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
					int cnt1 = planet.inventoryCount(ii.type, p);
					world.addInventoryCount(ii.type, cnt1);
					planet.addInventoryCount(ii.type, p, -cnt1);
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
			final int cnt = count - inventoryLocal;
			if (inventoryLocal < count && world.inventoryCount(rt) >= cnt) {
				world.addInventoryCount(rt, -cnt);
				add(new Action0() {
					@Override
					public void invoke() {
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
		if (world.money > 100000 && world.money >= world.autoBuildLimit) {
			if (planet.statistics.militarySpaceportCount == 0 
					&& world.global.planetCount / 2 + 1 > world.global.militarySpaceportCount) {
				final BuildingType bt = findBuilding("MilitarySpaceport");
				Point pt = planet.findLocation(bt);
				if (pt != null) {
					world.money -= bt.cost;
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
			if (stationCount < world.stationLimit) {
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
			if (planet.canBuildReplacement(bt0) && bt0.kind.equals(kind)) {
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
					world.money -= fbt.cost;
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
	/**
	 * Check if space station rockets are equipped.
	 * @param planet the target planet
	 * @return true if action performed
	 */
	boolean checkRockets(AIPlanet planet) {
		Map<ResearchType, Integer> demand = U.newHashMap();
		boolean result = false;
		// collect rocket demands
		for (final AIInventoryItem ii : planet.inventory) {
			if (ii.owner == p && ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				for (InventorySlot is : ii.slots) {
					// find best available technology
					ResearchType rt1 = null;
					for (ResearchType rt0 : is.slot.items) {
						if (world.isAvailable(rt0)) {
							rt1 = rt0;
						}
					}
					if (rt1 != null && rt1.category == ResearchSubCategory.WEAPONS_PROJECTILES) {
						int d = is.slot.max;
						if (is.type == rt1) {
							d = is.slot.max - is.count;
						}
						
						// check if inventory is available
						if (d <= world.inventoryCount(rt1)) {
							world.addInventoryCount(rt1, -d);
							final int fd = d;
							final ResearchType frt1 = rt1;
							final EquipmentSlot fes = is.slot;
							// deploy into slot
							add(new Action0() {
								@Override
								public void invoke() {
									if (p.inventoryCount(frt1) >= fd) {
										p.changeInventoryCount(frt1, -fd);
										
										if (ii.parent != null) {
											for (InventorySlot is : ii.parent.slots) {
												if (is.slot == fes) {
													is.count += fd;
													break;
												}
											}
										}
									}
								}
							});
							result = true;
						} else {
							Integer id = demand.get(rt1);
							demand.put(rt1, id != null ? id + d : d);
						}
					}
				}
			}
		}
		// place production order for the difference
		for (Map.Entry<ResearchType, Integer> de : demand.entrySet()) {
			int di = de.getValue();
			int ic = world.inventoryCount(de.getKey());
			if (di > ic) {
				if (placeProductionOrder(de.getKey(), di - ic)) {
					result = true;
				}
			}
		}
		return result;
	}
	/**
	 * Check if enough fighters are placed into orbit.
	 * @param planet the planet
	 * @return true if action performed
	 */
	boolean checkFighters(final AIPlanet planet) {
		final List<ResearchType> fighters = U.sort2(availableResearchOf(EnumSet.of(ResearchSubCategory.SPACESHIPS_FIGHTERS)), ResearchType.EXPENSIVE_FIRST);
		boolean result = false;
		for (final ResearchType rt : fighters) {
			int ic = planet.inventoryCount(rt, planet.owner);
			if (ic < world.fighterLimit) {
				int gic = world.inventoryCount(rt);
				final int needed = world.fighterLimit - ic;
				if (gic >= needed) {
					world.addInventoryCount(rt, -needed);
					add(new Action0() {
						@Override
						public void invoke() {
							DefaultAIControls.actionDeployFighters(planet.owner, planet.planet, rt, needed);
						}
					});
					result = true;
				} else {
					if (placeProductionOrder(rt, Math.max(10, needed - gic))) {
						result = true;
					}
				}
			}
		}
		
		return result;
	}
	
}
