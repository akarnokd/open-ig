/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;


import hu.openig.core.Difficulty;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.Fleet;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetStatistics;
import hu.openig.model.FleetTask;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Message;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Profile;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.TaxLevel;
import hu.openig.model.World;
import hu.openig.utils.U;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
		world.time.add(GregorianCalendar.MINUTE, world.params().speed());
		int day1 = world.time.get(GregorianCalendar.DATE);

		boolean result = false;
		
		// Prepare global statistics
		// -------------------------
		prepareGlobalStatistics(world);
		
		// -------------------------
		Map<Planet, PlanetStatistics> planetStats = U.newHashMap();
		
		for (Player player : world.players.values()) {
			PlanetStatistics all = player.getPlanetStatistics(planetStats);
			
			// -------------------------
			// Prepare player statistics
			
			preparePlayerStatistics(world, player, all);
			
			// -------------------------
			
			if (day0 != day1) {
				player.yesterday.clear();
				player.yesterday.assign(player.today);
				player.today.clear();
			}
			// result |= player == world.player
			progressResearch(world, player, all);
			// result |= player == world.player
			progressProduction(world, player, all);
		}
		for (Planet p : world.planets.values()) {
			if (p.owner != null && !p.owner.id.equals("Traders")) {
				progressPlanet(world, p, day0 != day1, planetStats.get(p));
			}
		}
		
		
		if (day0 != day1) {
			
			for (Player p : world.players.values()) {
				taxCompensation(p);
				p.ai.onNewDay();
			}
			
			result = true;
		}
		
		checkAchievements(world, day0 != day1);
		
		if (!world.pendingBattles.isEmpty()) {
			world.env.startBattle();
		}
		world.scripting.onTime();
		return result;
	}
	/**
	 * Prepare global statistics.
	 * <p>Typically accumulative values.</p>
	 * @param world the world object
	 */
	static void prepareGlobalStatistics(World world) {
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
	}
	/**
	 * Prepare the world and player statistics.
	 * @param world the world
	 * @param player the player
	 * @param all the all planet statistics of the player
	 */
	static void preparePlayerStatistics(World world, Player player,
			PlanetStatistics all) {
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
		int tradeIncome = 0;
		float multiply = 1.0f;
		float moraleBoost = 0;
		int radar = 0;
		long eqPlaytime = 6L * 60 * 60 * 1000;
		double populationGrowthModifier = 1.0;
		double planetTypeModifier = world.galaxyModel.getGrowth(planet.type.type, planet.race);
		
		final int repairCost = world.params().repairCost();
		final int repairAmount = world.params().repairSpeed();
//		final int buildCost = world.params().constructionCost();
		final int buildAmount = world.params().constructionSpeed();
		
		if (world.statistics.playTime >= eqPlaytime && (planet.type.type.equals("earth") || planet.type.type.equals("rocky"))) {
			if (planet.earthQuakeTTL <= 0) {
				// cause earthquake once in every 12, 6 or 3 months
				int eqDelta = 36 * 24 * 60;
				if (world.difficulty == Difficulty.NORMAL) {
					eqDelta /= 2;
				} else
				if (world.difficulty == Difficulty.HARD) {
					eqDelta /= 4;
				}
				if (world.random().nextInt(eqDelta) < 1) {
					planet.earthQuakeTTL = 6; // 1 hour
					
					Message msg = world.newMessage("message.earthquake");
					msg.priority = 25;
					msg.targetPlanet = planet;
					planet.owner.messageQueue.add(msg);
					
				}
			} else {
				planet.earthQuakeTTL--;
			}
		}

		// progress quarantine if any
		if (planet.quarantineTTL > 0) {
			planet.quarantineTTL--;
			if (planet.quarantineTTL <= 0) {
				planet.quarantineTTL = 0;

				world.cureFleets(planet);
				world.scripting.onPlanetCured(planet);
			}
		}
		boolean rebuildroads = false;
		
		for (Building b : new ArrayList<Building>(planet.surface.buildings)) {
			
			planet.owner.statistics.totalBuilding++;
			world.statistics.totalBuilding++;
			
			float eff = b.getEfficiency();
			
			if (Building.isOperational(eff)) {
				planet.owner.statistics.totalAvailableBuilding++;
				world.statistics.totalAvailableBuilding++;
			}
			
			if (b.isConstructing()) {
				b.buildProgress += buildAmount;
				b.buildProgress = Math.min(b.type.hitpoints, b.buildProgress);
				b.hitpoints += buildAmount;
				b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
				
				if (b.hitpoints == b.type.hitpoints) {
					planet.owner.ai.onBuildingComplete(planet, b);
					world.scripting.onBuildingComplete(planet, b);
				}
				
				result = true;
			} else
			// repair an unit if autorepair or explicitly requested
			if (b.repairing || (planet.owner == world.player && world.config.autoRepair && b.isDamaged())) {
				if (b.hitpoints * 100 / b.type.hitpoints < ps.freeRepair) {
					b.hitpoints += repairAmount * ps.freeRepairEff;
					b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
					result = true;
				} else {
					if (planet.owner.money >= repairCost 
							&& (b.repairing || planet.owner.money >= world.config.autoRepairLimit)) {
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
			// free repair buildings 
			if (b.isDamaged() && (b.hitpoints * 100 / b.type.hitpoints < ps.freeRepair)) {
				b.hitpoints += repairAmount * ps.freeRepairEff;
				b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
				result = true;
			}
			// turn of repairing when hitpoints are reached
			if (b.repairing && b.hitpoints == b.type.hitpoints) {
				// FIXME planet.owner.ai.onRepairComplete(planet, b);
				world.scripting.onRepairComplete(planet, b);
				b.repairing = false;
				result = true;
			}
			if (Building.isOperational(eff)) {
				if (b.hasResource("credit")) {
					tradeIncome += b.getResource("credit") * eff;
				}
				if (b.hasResource("multiply")) {
					multiply = b.getResource("multiply") * eff;
				}
				if (b.hasResource("morale")) {
					moraleBoost += b.getResource("morale") * eff;
				}
				if (b.hasResource("radar")) {
					radar = Math.max(radar, (int)b.getResource("radar"));
				}
				if (b.hasResource("population-growth")) {
					populationGrowthModifier = 1 + b.getResource("population-growth") / 100;
				}
			}
			if (planet.earthQuakeTTL > 0) {
				if (b.type.kind.equals("Factory")) {
					b.hitpoints -= b.type.hitpoints * 15 / 600;
				} else
				if (b.getEnergy() <= 0) {
					// reduce regular building health by 25% of its total hitpoints during the earthquake duration
					b.hitpoints -= b.type.hitpoints * 10 / 600;
				} else {
					// reduce energy building health by 55% of its total hitpoints during the earthquake duration
					b.hitpoints -= b.type.hitpoints * 20 / 600;
				}
				if (b.hitpoints <= 0) {
					planet.surface.removeBuilding(b);
					rebuildroads = true;
				}
			}
		}
		if (rebuildroads) {
			planet.rebuildRoads();
		}
		// search for radar capable inventory
		for (InventoryItem pii : planet.inventory) {
			if (pii.owner == planet.owner) {
				radar = Math.max(radar, pii.type.getInt("radar", 0));
			}
		}		
		if (radar != 0) {
			for (Map.Entry<InventoryItem, Integer> ittl : new ArrayList<Map.Entry<InventoryItem, Integer>>(planet.timeToLive.entrySet())) {
				if (ittl.getKey().owner != planet.owner) {
					Integer cttl = ittl.getValue();
					int cttl2 = cttl.intValue() - radar;
					if (cttl2 <= 0) {
						planet.timeToLive.remove(ittl.getKey());
						planet.inventory.remove(ittl.getKey());
	
						ittl.getKey().owner.ai.onSatelliteDestroyed(planet, ittl.getKey());
						world.scripting.onInventoryRemove(planet, ittl.getKey());
					} else {
						ittl.setValue(cttl2);
					}
				}
			}
		}
		if (planet.autoBuild != AutoBuild.OFF 
				&& (planet.owner == world.player && planet.owner.money >= world.config.autoBuildLimit)) {
			AutoBuilder.performAutoBuild(world, planet, ps);
		}
		
		// reequip station bombs and rockets
		if (planet.owner == world.player && world.env.config().reequipBombs) {
			for (InventoryItem ii : planet.inventory) {
				if (ii.owner == planet.owner && ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
					for (InventorySlot is : ii.slots) {
						refillSlot(planet.owner, is);
					}
				}
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
				newMorale += (ps.hospitalAvailable - planet.population) * 50f / planet.population;
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
					newMorale += (ps.policeAvailable - planet.population) * 5f / planet.population;
				}
			}
			
			
			newMorale = Math.max(0, Math.min(100, newMorale));
			float nextMorale = (planet.morale * 0.8f + 0.2f * newMorale);
			planet.morale = (int)nextMorale;
			
			float nextPopulation = 0;
			if (nextMorale < 20) {
				nextPopulation = Math.max(0, planet.population + 1000 * (nextMorale - 50) / 100);
			} else
			if (nextMorale < 30) {
				nextPopulation = Math.max(0, planet.population + 1000 * (nextMorale - 50) / 150);
			} else
			if (nextMorale < 40) {
				nextPopulation = Math.max(0, planet.population + 1000 * (nextMorale - 50) / 200);
			} else
			if (nextMorale < 50) {
				nextPopulation = Math.max(0, planet.population + 1000 * (nextMorale - 50) / 250);
			} else {
				nextPopulation = Math.max(0, planet.population + 1000 * (nextMorale - 50) / 500);
				nextPopulation += ((nextPopulation - planet.population) * populationGrowthModifier * planetTypeModifier);
			}
			
			planet.population = (int)nextPopulation;
			
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
				planet.owner.ai.onPlanetDied(planet);
				world.scripting.onLost(planet);
				planet.owner.statistics.planetsDied++;
				planet.die();
			} else {
				if (planet.morale <= 15) {
					planet.owner.ai.onPlanetRevolt(planet);
					if (planet.lastMorale > 15) {
						planet.owner.statistics.planetsRevolted++;
					}
					if (planet.morale < 10 && planet.lastMorale < 10) {
						revoltPlanet(world, planet);
					}
				}
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
	 * Switch sides for the revolting planet.
	 * @param world the world
	 * @param planet the planet
	 */
	static void revoltPlanet(World world, Planet planet) {
		
		// find the closest known alien planet
		double d = -1;
		Player newOwner = null;
		for (Planet p : world.planets.values()) {
			if (p.owner != null && p.owner != planet.owner && planet.owner.knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				double d2 = Math.hypot(planet.x - p.x, planet.y - p.y);
				if (d2 < 100) {
					if (d < 0 || d > d2) {
						d = d2;
						newOwner = p.owner;
					}
				}
			}
		}
		if (d < 0) {
			newOwner = world.players.get("Pirates");
		}
		
		
		if (newOwner != null) {
			planet.takeover(newOwner);
			planet.autoBuild = AutoBuild.OFF;
			planet.morale = 50;
		} else {
			planet.owner.ai.onPlanetDied(planet);
			world.scripting.onLost(planet);
			planet.owner.statistics.planetsDied++;
			planet.die();
		}
	}
	/**
	 * Make progress on the active research if any.
	 * @param world the world
	 * @param player the player
	 * @param all the total planet statistics of the player
	 * @return true if repaint will be needed 
	 */
	static boolean progressResearch(World world, Player player, PlanetStatistics all) {
		if (player.runningResearch() != null) {
			Research rs = player.research.get(player.runningResearch());
			int maxpc = rs.getResearchMaxPercent(all);
			// test for money
			// test for max percentage
			if (rs.remainingMoney > 0) {
				if (rs.getPercent() < maxpc) {
					float rel = 1.0f * rs.assignedMoney / rs.remainingMoney;
					int dmoney = (int)(rel * world.params().researchSpeed());
					if (dmoney < player.money) {
						rs.remainingMoney = Math.max(0, rs.remainingMoney - dmoney);
						rs.assignedMoney = Math.min((int)(rs.remainingMoney * rel) + 1, rs.remainingMoney);
						player.today.researchCost += dmoney;
						player.money -= dmoney;
						
						player.statistics.moneyResearch += dmoney;
						player.statistics.moneySpent += dmoney;

						world.statistics.moneyResearch += dmoney;
						world.statistics.moneySpent += dmoney;

						ResearchState last = rs.state;
						rs.state = ResearchState.RUNNING;
						if (last != rs.state) {
							player.ai.onResearchStateChange(rs.type, rs.state);
						}
					} else {
						rs.state = ResearchState.MONEY;
						player.ai.onResearchStateChange(rs.type, rs.state);
					}
				} else {
					rs.state = ResearchState.LAB;
					player.ai.onResearchStateChange(rs.type, rs.state);
				}
			}
			// test for completedness
			if (rs.remainingMoney == 0) {
				rs.state = ResearchState.COMPLETE;
				player.runningResearch(null);
				player.research.remove(rs.type);
				player.setAvailable(rs.type);
				
				player.statistics.researchCount++;
				
				player.ai.onResearchStateChange(rs.type, rs.state);
				world.scripting.onResearched(player, rs.type);
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
					int targetCap = (capacity * pr.priority / prioritySum) / world.params().productionUnit();
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
						
						if (pr.count == 0) {
							player.ai.onProductionComplete(pr.type);
							world.scripting.onProduced(player, pr.type);
						}
					}
				}			
			}
		}
		
		return result;
	}
	/**
	 * Run the tests for achievements.
	 * @param world the world to test for achievements
	 * @param dayChange was there a day change?
	 */
	static void checkAchievements(World world, boolean dayChange) {
		Profile p = world.env.profile();

		for (String a : AchievementManager.achievements()) {
			if (!p.hasAchievement(a)) {
				if (AchievementManager.get(a).invoke(world, world.player)) {
					world.env.achievementQueue().add(a);
					p.grantAchievement(a);
				}
			}
		}
		
	}
	/**
	 * Move all fleets.
	 * @param world the world
	 * @return true if repaint will be needed
	 */
	public static boolean moveFleets(World world) {
		boolean radar = false;
		for (Player p : world.players.values()) {
			radar |= moveFleets(p.ownFleets(), world);
		}
		if (radar) {
			Radar.compute(world);
		}
		return radar;
	}
	/**
	 * Move fleets.
	 * @param playerFleets the list of fleets
	 * @param world the world object to indicate battle scenarios
	 * @return true if a fleet was moved and the radar needs to be recalculated
	 */
	static boolean moveFleets(List<Fleet> playerFleets, World world) {
		boolean invokeRadar = false;
		double stepMultiplier = 0.025 * world.params().speed();
		
		for (Fleet f : playerFleets) {
			// regenerate shields
			regenerateFleet(f);
			
			// move fleet
			Point2D.Double target = null;
			double targetSpeed = 0.0;
			boolean removeWp = false;
			if (f.targetFleet != null) {
				target = new Point2D.Double(f.targetFleet.x, f.targetFleet.y);
				f.waypoints.clear();
				// if not in radar range any more just move to its last position and stop
				if (!f.owner.fleets.containsKey(f.targetFleet)
						|| !f.owner.withinLimits(f.targetFleet.x, f.targetFleet.y, 1)) {
					
					f.owner.ai.onLostTarget(f, f.targetFleet);
					
					f.stop();
				} else {
					targetSpeed = getSpeed(f.targetFleet) * stepMultiplier;
				}
			} else
			if (f.targetPlanet() != null) {
				target = new Point2D.Double(f.targetPlanet().x, f.targetPlanet().y);
				f.waypoints.clear();
			} else
			if (f.waypoints.size() > 0) {
				target = f.waypoints.get(0);
				removeWp = true;
			}
			if (target != null) {
				double dist = Math.sqrt((f.x - target.x) * (f.x - target.x) + (f.y - target.y) * (f.y - target.y));
				double dx = getSpeed(f) * stepMultiplier;
				// if the target has roughly the same speed as our fleet, give a small boost
				if (targetSpeed > 0 && Math.abs(dx - targetSpeed) < 0.5) {
					dx += 0.5 / 4 * stepMultiplier;
				}
				
				if (dx >= dist) {
					f.x = target.x;
					f.y = target.y;
					if (removeWp) {
						f.waypoints.remove(0);
					}
					if (f.waypoints.size() == 0) {
						boolean clearPlanet = false;
						boolean clearFleet = false;
						if (f.targetPlanet() != null) {
							f.owner.ai.onFleetArrivedAtPlanet(f, f.targetPlanet());
							world.scripting.onFleetAt(f, f.targetPlanet());
							clearPlanet = true;
						} else
						if (f.targetFleet != null) {
							f.owner.ai.onFleetArrivedAtFleet(f, f.targetFleet);
							world.scripting.onFleetAt(f, f.targetFleet);
							clearFleet = f.mode == FleetMode.ATTACK;
						} else {
							f.owner.ai.onFleetArrivedAtPoint(f, f.x, f.y);
							world.scripting.onFleetAt(f, f.x, f.y);
						}
						if (f.mode == FleetMode.ATTACK) {
							BattleInfo bi = new BattleInfo();
							bi.attacker = f;
							bi.targetFleet = f.targetFleet;
							bi.targetPlanet = f.targetPlanet();
							f.task = FleetTask.IDLE;

							world.pendingBattles.add(bi);
						}
						f.mode = null;
						if (clearFleet) {
							f.targetFleet = null;
						}
						if (clearPlanet) {
							f.targetPlanet(null);
						}
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
	 * Regenerate the shields and/or health.
	 * @param f the fleet
	 */
	static void regenerateFleet(Fleet f) {
		Planet np = f.nearbyPlanet();
		boolean spaceport = f.task != FleetTask.SCRIPT 
				&& np != null && np.owner == f.owner && np.hasMilitarySpaceport();
		for (InventoryItem ii : new ArrayList<InventoryItem>(f.inventory)) {
			if (spaceport || ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
				int hpMax = ii.owner.world.getHitpoints(ii.type);
				if (ii.hp < hpMax) {
					ii.hp = Math.min(hpMax, (ii.hp * 100 + hpMax) / 100);
					// regenerate slots
					for (InventorySlot is : ii.slots) {
						if (is.type != null) {
							int m = ii.owner.world.getHitpoints(is.type);
							is.hp = Math.min(m, (is.hp * 100 + m) / 100);
						}
					}
				}
			}
			int sm = ii.shieldMax();
			if (sm > 0 && ii.shield < sm) {
				ii.shield = Math.min(sm, (ii.shield * 100 + sm) / 100);
			}
		}
		if (spaceport) {
			checkRefill(f);
		}
	}
	/**
	 * Check if the auto-refill of tanks and equipment is on.
	 * @param f the target fleet
	 */
	static void checkRefill(Fleet f) {
		if (f.owner == f.owner.world.player) {
			for (InventoryItem ii : f.inventory) {
				if (f.owner.world.env.config().reequipBombs) {
					for (InventorySlot is : ii.slots) {
						if (is.getCategory() == ResearchSubCategory.WEAPONS_PROJECTILES 
								&& !is.isFilled()) {
							refillSlot(f.owner, is);
						}
					}
				}
			}
			if (f.owner.world.env.config().reequipTanks) {
				FleetStatistics fs = f.getStatistics();
				if (fs.vehicleCount < fs.vehicleMax) {
					f.stripVehicles();
					f.upgradeVehicles(fs.vehicleMax);
				}
			}
		}
	}
	/**
	 * Refill a particular inventory slot.
	 * @param owner the owner
	 * @param is the slot
	 */
	static void refillSlot(Player owner, InventorySlot is) {
		if (is.type != null) {
			int demand = is.slot.max - is.count;
			int inv = owner.inventoryCount(is.type);
			int add = Math.min(inv, demand);
			is.count += add;
			owner.changeInventoryCount(is.type, -add);
		} else {
			for (int i = is.slot.items.size() - 1; i >= 0; i--) {
				ResearchType rt = is.slot.items.get(i);
				if (owner.isAvailable(rt)) {
					int demand = is.slot.max;
					int inv = owner.inventoryCount(rt);
					int add = Math.min(inv, demand);
					if (add > 0) {
						is.type = rt;
						is.count = add;
						owner.changeInventoryCount(rt, -add);
						break;
					}
				}
			}
		}
	}
	/**
	 * Calculates the fleet speed per simulation step.
	 * @param f the fleet
	 * @return the speed
	 */
	static double getSpeed(Fleet f) {
		return f.getSpeed() / 4d;
	}
	/**
	 * Calculate a compensated tax based on the current number of planets.
	 * @param p the player
	 */
	static void taxCompensation(Player p) {
		long n = p.statistics.planetsOwned;
		if (n > 0) {
			long diff = 0;
			double k = 3 / Math.sqrt(n - 2 + p.world.difficulty.ordinal());
			for (Planet pl : p.planets.keySet()) {
				if (pl.owner == p) {
					int ti = pl.taxIncome;
					pl.taxIncome = (int)(pl.taxIncome * k);
					diff += pl.taxIncome - ti;
				}
			}
			p.yesterday.taxIncome += diff;
			p.statistics.moneyIncome += diff;
			p.statistics.moneyTaxIncome += diff;
			p.world.statistics.moneyIncome += diff;
			p.world.statistics.moneyTaxIncome += diff;
		}
	}
}
