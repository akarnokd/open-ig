/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;


import hu.openig.model.Building;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.Production;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchType;
import hu.openig.model.TaxLevel;
import hu.openig.model.World;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
		
		Map<Planet, PlanetStatistics> planetStats = new HashMap<Planet, PlanetStatistics>();
		for (Player player : world.players.values()) {
			PlanetStatistics all = player.getPlanetStatistics(planetStats);
			
			if (day0 != day1) {
				player.yesterday.clear();
				player.yesterday.assign(player.today);
				player.today.clear();
			}
			result |= progressResearch(player, all) && player == world.player;
			result |= progressProduction(player, all) && player == world.player;
		}
		for (Planet p : world.planets.values()) {
			if (p.owner != null) {
				result |= progressPlanet(p, day0 != day1, planetStats.get(p)) && p == world.player.currentPlanet;
			}
		}
//		return result;
		if (day0 != day1) {
			return true;
		}
		return false;
	}
	/**
	 * Make progress on the buildings of the planet.
	 * @param planet the planet
	 * @param dayChange consider day change
	 * @param ps the planet statistics
	 * @return true if repaint will be needed 
	 */
	static boolean progressPlanet(Planet planet, boolean dayChange,
			PlanetStatistics ps) {
		boolean result = false;
		final int repairCost = 20;
		final int repairAmount = 50;
		int tradeIncome = 0;
		float multiply = 1.0f;
		float moraleBoost = 0;
		for (Building b : planet.surface.buildings) {
			if (b.isConstructing()) {
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
					if (planet.owner.money >= 20) {
						planet.owner.money -= repairCost; // FIXME repair cost per unit?
						planet.owner.today.repairCost += repairCost;
						b.hitpoints += repairAmount;
						b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
						result = true;
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
				newMorale += (ps.policeAvailable - planet.population) * 10f / planet.population;
			}
			
			
			newMorale = Math.max(0, Math.min(100, newMorale));
			float nextMorale = (planet.morale * 0.8f + 0.2f * newMorale);
			planet.morale = (int)nextMorale;
			
			// avoid a practically infinite population descent
			if (planet.population < 1000 && nextMorale < 50) {
				planet.population = (int)Math.max(0, planet.population + 1000 * (nextMorale - 50) / 500);
			} else {
				planet.population = (int)Math.max(0, planet.population + planet.population * (nextMorale - 50) / 500);
			}
			
			planet.tradeIncome = (int)(tradeIncome * multiply);
			planet.taxIncome = (int)(1.0f * planet.population * planet.morale * planet.tax.percent / 10000);

			planet.owner.money += planet.tradeIncome + planet.taxIncome;
			
			planet.owner.yesterday.taxIncome += planet.taxIncome;
			planet.owner.yesterday.tradeIncome += planet.tradeIncome;
			planet.owner.yesterday.taxMorale += planet.morale;
			planet.owner.yesterday.taxMoraleCount++;
			
			if (planet.population == 0) {
				planet.die();
			}
		}
		
		return result;
	}
	/**
	 * Make progress on the active research if any.
	 * @param all the total planet statistics of the player
	 * @return true if repaint will be needed 
	 * @param player the player
	 */
	static boolean progressResearch(Player player, PlanetStatistics all) {
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
				player.availableResearch.add(rs.type);
			}
			return true;
		}
		return false;
	}
	/**
	 * Perform the next step of the production process.
	 * @param player the player
	 * @param all the all planet statistics of the player
	 * @return need for repaint?
	 */
	static boolean progressProduction(Player player, PlanetStatistics all) {
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
					int currentCap = Math.min(Math.min(
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
						
						result = true;
					}
				}			
			}
		}
		
		return result;
	}
}
