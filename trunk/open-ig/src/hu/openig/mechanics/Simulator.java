/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;


import hu.openig.core.Func1;
import hu.openig.core.Location;
import hu.openig.model.AutoBuild;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.Resource;
import hu.openig.model.TaxLevel;
import hu.openig.model.TileSet;
import hu.openig.model.World;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of algorithms which update the world
 * state as time progresses: progress on buildings, repair
 * research, production, fleet movement, etc.
 * @author akarnokd, Apr 5, 2011
 */
public final class Simulator {
	/** Utility class. */
	private Simulator() {
	}
	/** 
	 * The main computation. 
	 * @param world the world to compute
	 * @return true if the a quicksave is needed 
	 */
	public static boolean compute(World world) {
		int day0 = world.time.get(GregorianCalendar.DATE);
		world.time.add(GregorianCalendar.MINUTE, 10);
		int day1 = world.time.get(GregorianCalendar.DATE);

		boolean result = false;
		
		// Prepare global statistics
		// -------------------------
		world.statistics.totalPopulation = 0;
		world.statistics.totalBuilding = 0;
		world.statistics.totalAvailableBuilding = 0;
		
		world.statistics.totalWorkerDemand = 0;
		world.statistics.totalEnergyDemand = 0;
		world.statistics.totalAvailableEnergy = 0;
		world.statistics.totalAvailableHouse = 0;
		world.statistics.totalAvailableFood = 0;
		world.statistics.totalAvailableHospital = 0;
		world.statistics.totalAvailablePolice = 0;
		
		// -------------------------
		boolean invokeRadar = false;
		
		Map<Planet, PlanetStatistics> planetStats = new HashMap<Planet, PlanetStatistics>();
		for (Player player : world.players.values()) {
			PlanetStatistics all = player.getPlanetStatistics(planetStats);
			
			// -------------------------
			// Prepare player statistics
			
			player.statistics.totalPopulation = 0;
			player.statistics.totalBuilding = 0;
			player.statistics.totalAvailableBuilding = 0;
			
			player.statistics.totalWorkerDemand = all.workerDemand;
			player.statistics.totalEnergyDemand = all.energyDemand;
			player.statistics.totalAvailableEnergy = all.energyAvailable;
			player.statistics.totalAvailableHouse = all.houseAvailable;
			player.statistics.totalAvailableFood = all.foodAvailable;
			player.statistics.totalAvailableHospital = all.hospitalAvailable;
			player.statistics.totalAvailablePolice = all.policeAvailable;

			world.statistics.totalWorkerDemand += all.workerDemand;
			world.statistics.totalEnergyDemand += all.energyDemand;
			world.statistics.totalAvailableEnergy += all.energyAvailable;
			world.statistics.totalAvailableHouse += all.houseAvailable;
			world.statistics.totalAvailableFood += all.foodAvailable;
			world.statistics.totalAvailableHospital += all.hospitalAvailable;
			world.statistics.totalAvailablePolice += all.policeAvailable;
			
			player.statistics.planetsOwned = 0;
			
			// -------------------------
			
			if (day0 != day1) {
				player.yesterday.clear();
				player.yesterday.assign(player.today);
				player.today.clear();
			}
			result |= progressResearch(world, player, all) && player == world.player;
			result |= progressProduction(world, player, all) && player == world.player;
			invokeRadar |= moveFleets(player.ownFleets());
		}
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				result |= progressPlanet(world, p, day0 != day1, planetStats.get(p)) && p == world.player.currentPlanet;
			}
		}
		
		testAchievements(world);

		if (invokeRadar) {
			Radar.compute(world);
		}
		
		verify(world);
		
		if (day0 != day1) {
			return true;
		}
		return false;
	}
	/**
	 * Make progress on the buildings of the planet.
	 * @param world the world
	 * @param planet the planet
	 * @param dayChange consider day change
	 * @param ps the planet statistics
	 * @return true if repaint will be needed 
	 */
	static boolean progressPlanet(World world, Planet planet, boolean dayChange,
			PlanetStatistics ps) {
		boolean result = false;
		final int repairCost = 20;
		final int repairAmount = 50;
		int tradeIncome = 0;
		float multiply = 1.0f;
		float moraleBoost = 0;
		boolean buildInProgress = false;
		
		for (Building b : planet.surface.buildings) {
			
			planet.owner.statistics.totalBuilding++;
			world.statistics.totalBuilding++;
			if (b.getEfficiency() >= 0.5f) {
				planet.owner.statistics.totalAvailableBuilding++;
				world.statistics.totalAvailableBuilding++;
			}
			
			if (b.isConstructing()) {
				buildInProgress = true;
				b.buildProgress += 200;
				b.buildProgress = Math.min(b.type.hitpoints, b.buildProgress);
				b.hitpoints += 200;
				b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
				result = true;
			} else
			if (b.repairing) {
				if (b.hitpoints * 100 / b.type.hitpoints < ps.freeRepair) {
					b.hitpoints += repairAmount * ps.freeRepairEff;
					b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
					result = true;
				} else {
					if (planet.owner.money >= repairCost) {
						planet.owner.money -= repairCost; // FIXME repair cost per unit?
						planet.owner.today.repairCost += repairCost;
						b.hitpoints += repairAmount;
						b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
						result = true;
						
						planet.owner.statistics.moneyRepair += repairCost;
						planet.owner.statistics.moneySpent += repairCost;

						world.statistics.moneyRepair += repairCost;
						world.statistics.moneySpent += repairCost;
					}
				}
			} else
			if (b.isDamaged()) {
				if (b.hitpoints * 100f / b.type.hitpoints < ps.freeRepair) {
					b.hitpoints += repairAmount * ps.freeRepairEff;
					b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
					result = true;
				}
			}
			if (b.repairing && b.hitpoints == b.type.hitpoints) {
				b.repairing = false;
				result = true;
			}
			if (b.getEfficiency() >= 0.5) {
				if (b.hasResource("credit")) {
					tradeIncome += b.getResource("credit");
				}
				if (b.hasResource("multiply")) {
					multiply = b.getResource("multiply");
				}
				if (b.hasResource("morale")) {
					moraleBoost += b.getResource("morale") * b.getEfficiency();
				}
			}
			// there is one step when the building is ready but not yet allocated
			if (b.enabled 
					&& (b.assignedWorker == 0 || (ps.energyAvailable > 0 && b.getEnergy() < 0 && b.assignedEnergy == 0))) {
				buildInProgress = true;
			}
		}
		
		if (planet.autoBuild != AutoBuild.OFF && planet.owner.money >= world.getAutoBuildLimit.invoke(null)) {
			if (!buildInProgress 
					&& !ps.hasWarning(PlanetProblems.COLONY_HUB)
					&& !ps.hasProblem(PlanetProblems.COLONY_HUB)
			) {
				performAutoBuild(world, planet, ps);
			}
		}
		
		if (dayChange) {

			planet.lastMorale = planet.morale;
			planet.lastPopulation = planet.population;
			
			// FIXME morale computation
			float newMorale = 50 + moraleBoost;
			if (planet.tax == TaxLevel.NONE) {
				newMorale += 5;
			} else
			if (planet.tax.ordinal() <= TaxLevel.MODERATE.ordinal()) {
				newMorale -= planet.tax.percent / 6f;
			} else {
				newMorale -= planet.tax.percent / 3f;
			}
			if (ps.houseAvailable < planet.population) {
				newMorale += (ps.houseAvailable - planet.population) * 75f / planet.population;
			} else {
				newMorale += (ps.houseAvailable - planet.population) * 2f / planet.population;
			}
			if (ps.hospitalAvailable < planet.population) {
				newMorale += (ps.hospitalAvailable - planet.population) * 75f / planet.population;
			}
			if (ps.foodAvailable < planet.population) {
				newMorale += (ps.foodAvailable - planet.population) * 75f / planet.population;
			}
			if (ps.policeAvailable < planet.population) {
				newMorale += (ps.policeAvailable - planet.population) * 50f / planet.population;
			} else {
				if (!planet.owner.race.equals(planet.race)) {
					newMorale += (ps.policeAvailable - planet.population) * 25f / planet.population;
				} else {
					newMorale += (ps.policeAvailable - planet.population) * 10f / planet.population;
				}
			}
			
			
			newMorale = Math.max(0, Math.min(100, newMorale));
			float nextMorale = (planet.morale * 0.8f + 0.2f * newMorale);
			planet.morale = (int)nextMorale;
			
			// avoid a practically infinite population descent
			if (planet.population < 1000 && nextMorale < 50) {
				planet.population = (int)Math.max(0, planet.population + 1000 * (nextMorale - 50) / 250);
			} else 
			if (nextMorale < 50) {
				// lower morale should decrease population more rapidly
				planet.population = (int)Math.max(0, planet.population + planet.population * (nextMorale - 50) / 250);
			} else {
				planet.population = (int)Math.max(0, planet.population + planet.population * (nextMorale - 50) / 500);
			}
			
			planet.tradeIncome = (int)(tradeIncome * multiply);
			planet.taxIncome = (int)(1.0f * planet.population * planet.morale * planet.tax.percent / 10000);

			planet.owner.money += planet.tradeIncome + planet.taxIncome;
			
			planet.owner.statistics.moneyIncome += planet.tradeIncome + planet.taxIncome;
			planet.owner.statistics.moneyTaxIncome += planet.taxIncome;
			planet.owner.statistics.moneyTradeIncome += planet.tradeIncome;

			world.statistics.moneyIncome += planet.tradeIncome + planet.taxIncome;
			world.statistics.moneyTaxIncome += planet.taxIncome;
			world.statistics.moneyTradeIncome += planet.tradeIncome;
			
			planet.owner.yesterday.taxIncome += planet.taxIncome;
			planet.owner.yesterday.tradeIncome += planet.tradeIncome;
			planet.owner.yesterday.taxMorale += planet.morale;
			planet.owner.yesterday.taxMoraleCount++;
			
			if (planet.population == 0) {
				planet.die();
				// FIXME send planet died message
			}
		}
		
		if (planet.owner != null) {
			planet.owner.statistics.totalPopulation += planet.population;
			world.statistics.totalPopulation += planet.population;
			planet.owner.statistics.planetsOwned++;
		}
		
		return result;
	}
	/**
	 * Make progress on the active research if any.
	 * @param world the world
	 * @param player the player
	 * @param all the total planet statistics of the player
	 * @return true if repaint will be needed 
	 */
	static boolean progressResearch(World world, Player player, PlanetStatistics all) {
		if (player.runningResearch != null) {
			Research rs = player.research.get(player.runningResearch);
			int maxpc = rs.getResearchMaxPercent(all);
			// test for money
			// test for max percentage
			if (rs.remainingMoney > 0) {
				if (rs.getPercent() < maxpc) {
					float rel = 1.0f * rs.assignedMoney / rs.remainingMoney;
					int dmoney = (int)(rel * 40);
					if (dmoney < player.money) {
						rs.remainingMoney = Math.max(0, rs.remainingMoney - dmoney);
						rs.assignedMoney = Math.min((int)(rs.remainingMoney * rel) + 1, rs.remainingMoney);
						player.today.researchCost += dmoney;
						player.money -= dmoney;
						
						player.statistics.moneyResearch += dmoney;
						player.statistics.moneySpent += dmoney;

						world.statistics.moneyResearch += dmoney;
						world.statistics.moneySpent += dmoney;
						
						rs.state = ResearchState.RUNNING;
					} else {
						rs.state = ResearchState.MONEY;
					}
				} else {
					rs.state = ResearchState.LAB;
				}
			}
			// test for completedness
			if (rs.remainingMoney == 0) {
				player.runningResearch = null;
				player.research.remove(rs.type);
				player.setAvailable(rs.type);
				
				player.statistics.researchCount++;
				
				// FIXME send research complete message
			}
			return true;
		}
		return false;
	}
	/**
	 * Perform the next step of the production process.
	 * @param world the world
	 * @param player the player
	 * @param all the all planet statistics of the player
	 * @return need for repaint?
	 */
	static boolean progressProduction(World world, Player player, PlanetStatistics all) {
		boolean result = false;
		for (Map.Entry<ResearchMainCategory, Map<ResearchType, Production>> prs : player.production.entrySet()) {
			int capacity = 0;
			if (prs.getKey() == ResearchMainCategory.SPACESHIPS) {
				capacity = all.spaceshipActive;
			} else
			if (prs.getKey() == ResearchMainCategory.WEAPONS) {
				capacity = all.weaponsActive;
			} else
			if (prs.getKey() == ResearchMainCategory.EQUIPMENT) {
				capacity = all.equipmentActive;
			}
			int prioritySum = 0;
			for (Production pr : prs.getValue().values()) {
				if (pr.count > 0) {
					prioritySum += pr.priority;
				}
			}
			if (prioritySum > 0) {
				for (Production pr : new ArrayList<Production>(prs.getValue().values())) {
					int targetCap = (capacity * pr.priority / prioritySum) / 50;
					if (pr.count == 0) {
						targetCap = 0;
					}
					int currentCap = (int)Math.min(Math.min(
							player.money, 
							targetCap
							),
							pr.count * pr.type.productionCost - pr.progress
					);
					if (currentCap > 0) {
						player.money -= currentCap;
						player.today.productionCost += currentCap;
						
						int progress = pr.progress + currentCap;
						
						int countDelta = progress / pr.type.productionCost;
						int progressDelta = progress % pr.type.productionCost;
						
						int count0 = pr.count;
						pr.count = Math.max(0, pr.count - countDelta);
						pr.progress = progressDelta;
						
						int intoInventory = count0 - pr.count;
						Integer invCount = player.inventory.get(pr.type);
						player.inventory.put(pr.type, invCount != null ? invCount + intoInventory : intoInventory);
						
						player.statistics.moneyProduction += currentCap;
						player.statistics.moneySpent += currentCap;
						
						world.statistics.moneyProduction += currentCap;
						world.statistics.moneySpent += currentCap;
						
						player.statistics.productionCount += intoInventory;
						world.statistics.productionCount += intoInventory;
						
						result = true;
						
						// FIXME send production complete message
					}
				}			
			}
		}
		
		return result;
	}
	/**
	 * Run the tests for achievements.
	 * @param world the world to test for achievements
	 */
	static void testAchievements(World world) {
		// FIXME implement
	}
	/**
	 * Move fleets.
	 * @param playerFleets the list of fleets
	 * @return true if a fleet was moved and the radar needs to be recalculated
	 */
	static boolean moveFleets(List<Fleet> playerFleets) {
		boolean invokeRadar = false;
		
		for (Fleet f : playerFleets) {
			Point2D.Float target = null;
			boolean removeWp = false;
			if (f.targetFleet != null) {
				target = new Point2D.Float(f.targetFleet.x, f.targetFleet.y);
				f.waypoints.clear();
			} else
			if (f.targetPlanet != null) {
				target = new Point2D.Float(f.targetPlanet.x, f.targetPlanet.y);
				f.waypoints.clear();
			} else
			if (f.waypoints.size() > 0) {
				target = f.waypoints.get(0);
				removeWp = true;
			}
			if (target != null) {
				double dist = Math.sqrt((f.x - target.x) * (f.x - target.x) + (f.y - target.y) * (f.y - target.y));
				double dx = f.getStatistics().speed / 4;
				
				if (dx >= dist) {
					f.x = target.x;
					f.y = target.y;
					if (removeWp) {
						f.waypoints.remove(0);
					}
					if (f.waypoints.size() == 0) {
						f.mode = null;
						f.targetFleet = null;
						f.targetPlanet = null;
					}
				} else {
					double angle = Math.atan2(target.y - f.y, target.x - f.x);
					f.x = (float)(f.x + Math.cos(angle) * dx);
					f.y = (float)(f.y + Math.sin(angle) * dx);
				}
				invokeRadar = true;
			} else {
				f.mode = null;
			}
		}
		
		return invokeRadar;
	}
	/**
	 * Perform the auto-build if necessary.
	 * @param world the world
	 * @param planet the planet
	 * @param ps the planet statistics
	 */
	static void performAutoBuild(final World world, final Planet planet, final PlanetStatistics ps) {
		// if there is a worker shortage, it may be the root clause
		if (ps.workerDemand > planet.population) {
			return;
		}
		// if energy shortage
		if (ps.energyAvailable < ps.energyDemand) {
			findOptions(world, planet, 
			new Func1<Building, Boolean>() {
				@Override
				public Boolean invoke(Building b) {
					return b.getEnergy() > 0 && planet.owner.money >= b.type.cost;
				}
			},
			new Func1<BuildingType, Boolean>() {
				@Override
				public Boolean invoke(BuildingType value) {
					Resource res = value.resources.get("energy");
					return res != null && res.amount > 0;
				}
			}
			);
			return;
		}
		if (planet.autoBuild == AutoBuild.CIVIL) {
			// if living space shortage
			if (ps.houseAvailable < planet.population || (planet.population <= 5000 && ps.houseAvailable <= 5000)) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("house");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("house");
						return res != null;
					}
				}
				);
			}
			// if food shortage
			if (ps.foodAvailable < planet.population) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("food");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("food");
						return res != null;
					}
				}
				);
			}
			// if hospital shortage
			if (ps.hospitalAvailable < planet.population) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("hospital");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("hospital");
						return res != null;
					}
				}
				);
			}
			// if living space shortage
			if (ps.policeAvailable < planet.population) {
				findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.hasResource("police");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						Resource res = value.resources.get("police");
						return res != null;
					}
				}
				);
			}
			if (ps.hasProblem(PlanetProblems.STADIUM) || ps.hasWarning(PlanetProblems.STADIUM)) {
				findOptions(world, planet, 
					new Func1<Building, Boolean>() {
						@Override
						public Boolean invoke(Building b) {
							return b.type.id.equals("Stadium");
						}
					},
					new Func1<BuildingType, Boolean>() {
						@Override
						public Boolean invoke(BuildingType value) {
							return value.id.equals("Stadium");
						}
					}
					);
			}
		} else
		if (planet.autoBuild == AutoBuild.ECONOMIC) {
			findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.type.kind.equals("Economic");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						return value.kind.equals("Economic");
					}
				}
			);
		} else
		if (planet.autoBuild == AutoBuild.FACTORY) {
			findOptions(world, planet, 
				new Func1<Building, Boolean>() {
					@Override
					public Boolean invoke(Building b) {
						return b.type.kind.equals("Factory");
					}
				},
				new Func1<BuildingType, Boolean>() {
					@Override
					public Boolean invoke(BuildingType value) {
						return value.kind.equals("Factory");
					}
				}
			);
		}
	}
	/**
	 * Find and invoke options for the given world and planet and use
	 * the building upgrade and build selectors to find suitable buildings.
	 * @param world the world
	 * @param planet the planet
	 * @param upgradeSelector the selector to find upgradable buildings
	 * @param buildSelector the selector to find building types to construct
	 */
	static void findOptions(World world, Planet planet, Func1<Building, Boolean> upgradeSelector, Func1<BuildingType, Boolean> buildSelector) {
		List<Building> upgr = findUpgradables(planet, upgradeSelector);
		if (upgr.size() > 0) {
			doUpgrade(world, planet, upgr.get(0));
			return;
		}
		// look for energy producing buildings in the model
		List<BuildingType> bts = findBuildables(world, planet, buildSelector);
		// build the most costly building if it can be placed
		for (BuildingType bt : bts) {
			TileSet ts = bt.tileset.get(planet.race);
			Point pt = planet.surface.findLocation(ts.normal.width + 2, ts.normal.height + 2);
			if (pt != null) {
				doConstruct(world, planet, bt, pt);
				return;
			}
		}
		// no room at all, turn off autobuild and let the user do it
		// FIXME notify user
		if (bts.size() > 0) {
			planet.autoBuild = AutoBuild.OFF;
		}

	}
	/**
	 * Locate buildings which satisfy a given filter and have available upgrade levels.
	 * @param planet the target planet
	 * @param filter the building filter
	 * @return the list of potential buildings
	 */
	static List<Building> findUpgradables(Planet planet, Func1<Building, Boolean> filter) {
		List<Building> result = new ArrayList<Building>();
		for (Building b : planet.surface.buildings) {
			if (b.upgradeLevel < b.type.upgrades.size()
				&& b.type.cost <= planet.owner.money
				&& filter.invoke(b)
			) {
				result.add(b);
			}
		}
		return result;
	}
	/**
	 * Locate building types which satisfy a given filter.
	 * @param world the world
	 * @param planet the target planet
	 * @param filter the building filter
	 * @return the list of potential building types
	 */
	static List<BuildingType> findBuildables(World world, Planet planet, Func1<BuildingType, Boolean> filter) {
		List<BuildingType> result = new ArrayList<BuildingType>();
		for (BuildingType bt : world.buildingModel.buildings.values()) {
			if (
					bt.tileset.containsKey(planet.race)
					&& (bt.research == null || planet.owner.isAvailable(bt.research))
					&& planet.owner.money >= bt.cost
					&& planet.canBuild(bt)
					&& filter.invoke(bt)
			) {
				result.add(bt);
			}
		}
		Collections.sort(result, new Comparator<BuildingType>() {
			@Override
			public int compare(BuildingType o1, BuildingType o2) {
				return o2.cost - o1.cost;
			}
		});
		return result;
	}
	/**
	 * Construct a building on the given planet.
	 * @param world the world for the model
	 * @param planet the target planet
	 * @param bt the building type to build
	 * @param pt the place to build (the top-left corner of the roaded building base rectangle).
	 */
	static void doConstruct(World world, Planet planet, BuildingType bt,
			Point pt) {
		Building b = new Building(bt, planet.race);
		b.location = Location.of(pt.x + 1, pt.y - 1);

		planet.surface.placeBuilding(b.tileset.normal, b.location.x, b.location.y, b);
		planet.surface.placeRoads(planet.race, world.buildingModel);

		planet.owner.money -= bt.cost;
		planet.owner.today.buildCost += bt.cost;
		
		planet.owner.statistics.buildCount++;
		planet.owner.statistics.moneyBuilding += bt.cost;
		planet.owner.statistics.moneySpent += bt.cost;
		
		world.statistics.buildCount++;
		world.statistics.moneyBuilding += bt.cost;
		world.statistics.moneySpent += bt.cost;
	}
	/**
	 * Increase the level of the given building by one.
	 * @param world the world for the models
	 * @param planet the target planet
	 * @param b the building to upgrade
	 */
	static void doUpgrade(World world, Planet planet, Building b) {
		do {
			b.setLevel(b.upgradeLevel + 1);
			b.buildProgress = b.type.hitpoints * 1 / 4;
			b.hitpoints = b.buildProgress;
			
			planet.owner.today.buildCost += b.type.cost;
			
			planet.owner.money -= b.type.cost;
			planet.owner.statistics.upgradeCount++;
			planet.owner.statistics.moneySpent += b.type.cost;
			planet.owner.statistics.moneyUpgrade += b.type.cost;
			
			world.statistics.upgradeCount++;
			world.statistics.moneySpent += b.type.cost;
			world.statistics.moneyUpgrade += b.type.cost;
			// maximize upgrade level if the player has enough money relative to the building's cost
		} while (b.upgradeLevel < b.type.upgrades.size() && planet.owner.money >= 30 * b.type.cost);
	}
	/**
	 * A world diagnostic to check for mechanics consistency errors.
	 * @param world the world to test
	 */
	static void verify(World world) {
		// Owner knows about its planet
		for (Planet p : world.planets.values()) {
			if (p.owner != null && p.owner.planets.get(p) != PlanetKnowledge.BUILDING) {
				System.err.printf("Planet knowledge error | Player: %s, Planet: %s, Knowledge: %s%n", p.owner.id, p.id, p.owner.planets.get(p));
			}
			for (Building b : p.surface.buildings) {
				if (b.upgradeLevel > b.type.upgrades.size()) {
					System.err.printf("Upgrade limit error | Player: %s, Planet: %s, Building: %s, Location: %d;%d, Level: %d, Limit: %d%n", 
							p.owner.id, p.id, b.type.id, b.location.x, b.location.y, b.upgradeLevel, b.type.upgrades.size());
				}
			}
		}
		for (Player p : world.players.values()) {
			if (p.money < 0) {
				System.err.printf("Negative money | Player %s, Money %d%n", p.id, p.money);
			}
		}
	}
}
