/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Act;
import hu.openig.screens.GameControls;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.swing.Timer;

/**
 * Collection of algorithms which update the world
 * state as time progresses: progress on buildings, repair
 * research, production, fleet movement, etc.
 * @author akarnokd, Apr 5, 2011
 */
public class Simulator {
	/** The timer to periodically change things. */
	protected final Timer timer;
	/** The world object. */
	protected final World world;
	/** The general game world controls. */
	protected final GameControls controls;
	/**
	 * Construct the simulator.
	 * @param delay the real time delay between calculations
	 * @param world the world object
	 * @param controls the game world controls
	 */
	public Simulator(int delay, World world, GameControls controls) {
		this.controls = controls;
		this.world = world;
		timer = new Timer(delay, new Act() {
			@Override
			public void act() {
				if (compute()) {
					Simulator.this.controls.repaintInner();
				}
			}
		});
		timer.setCoalesce(false);
		timer.setInitialDelay(0);
	}
	/** 
	 * The main computation. 
	 * @return true if repaint will be needed 
	 */
	public boolean compute() {
		int day0 = world.time.get(GregorianCalendar.DATE);
		world.time.add(GregorianCalendar.MINUTE, 10);
		int day1 = world.time.get(GregorianCalendar.DATE);

		boolean result = false;
		
		for (Player player : world.players.values()) {
			if (day0 != day1) {
				player.yesterday.clear();
				player.yesterday.assign(player.today);
				player.today.clear();
			}
			result |= progressResearch(player) && player == world.player;
		}
		for (Player player : world.players.values()) {
			result |= progressProduction(player) && player == world.player;
		}
		for (Planet p : world.planets) {
			if (p.owner != null) {
				result |= progressPlanet(p, day0 != day1) && p == world.player.currentPlanet;
			}
		}
//		return result;
		return true;
	}
	/**
	 * Make progress on the buildings of the planet.
	 * @param planet the planet
	 * @param dayChange consider day change
	 * @return true if repaint will be needed 
	 */
	public boolean progressPlanet(Planet planet, boolean dayChange) {
		boolean result = false;
		float freeRepair = 0;
		for (Building b : planet.surface.buildings) {
			if (b.getEfficiency() >= 0.5 && b.hasResource("repair")) {
				freeRepair = Math.max(b.getResource("repair"), freeRepair);
			}
		}
		final int repairCost = 20;
		final int repairAmount = 50;
		int tradeIncome = 0;
		float multiply = 1.0f;
		float moraleBoost = 0;
		PlanetStatistics ps = planet.getStatistics();
		for (Building b : planet.surface.buildings) {
			if (b.isConstructing()) {
				b.buildProgress += 200;
				b.buildProgress = Math.min(b.type.hitpoints, b.buildProgress);
				b.hitpoints += 200;
				b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
				result = true;
			} else
			if (b.repairing) {
				if (b.hitpoints * 100 / b.type.hitpoints < freeRepair) {
					b.hitpoints += repairAmount;
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
				if (b.hitpoints * 100 / b.type.hitpoints < freeRepair) {
					b.hitpoints += repairAmount;
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
					moraleBoost += b.getResource("morale");
				}
			}
		}
		
		if (dayChange) {

			planet.lastMorale = planet.morale;
			planet.lastPopulation = planet.population;
			
			// FIXME morale computation
			int problemcount = ps.problems.size();
			float newMorale = planet.morale + moraleBoost - 8 * problemcount - planet.tax.percent / 3;
			if (ps.houseAvailable < planet.population) {
				newMorale += (ps.houseAvailable - planet.population) * 50 / planet.population;
			}
			if (ps.hospitalAvailable < planet.population) {
				newMorale += (ps.hospitalAvailable - planet.population) * 50 / planet.population;
			}
			if (ps.foodAvailable < planet.population) {
				newMorale += (ps.foodAvailable - planet.population) * 50 / planet.population;
			}
			if (ps.policeAvailable < planet.population) {
				newMorale += (ps.policeAvailable - planet.population) * 50 / planet.population;
			}
			
			
			newMorale = Math.max(0, Math.min(100, newMorale));

			if (planet.population < 5000) {
				planet.population = (int)Math.max(0, planet.population + 10000 * (newMorale - 50) / 1000);
			} else {
				planet.population = (int)Math.max(0, planet.population + 4 * planet.population * (newMorale - 50) / 1000);
			}
			
			planet.morale = (int)(planet.morale * 0.8f + 0.2f * newMorale);
			
			planet.tradeIncome = (int)(tradeIncome * multiply);
			planet.taxIncome = planet.population * planet.morale * planet.tax.percent / 10000;

			planet.owner.money += planet.tradeIncome + planet.taxIncome;
			
			planet.owner.yesterday.taxIncome += planet.taxIncome;
			planet.owner.yesterday.tradeIncome += planet.tradeIncome;
			planet.owner.yesterday.taxMorale += planet.morale;
			planet.owner.yesterday.taxMoraleCount++;
			
			if (planet.population == 0) {
				planet.race = null;
				planet.owner = null;
				planet.surface.buildingmap.clear();
				planet.surface.buildings.clear();
			}
		}
		
		return result;
	}
	/**
	 * Make progress on the active research if any.
	 * @return true if repaint will be needed 
	 * @param player the player
	 */
	public boolean progressResearch(Player player) {
		if (player.runningResearch != null) {
			Research rs = player.research.get(player.runningResearch);
			int maxpc = rs.getResearchMaxPercent(player.getPlanetStatistics());
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
	/** Start the timer. */
	public void start() {
		timer.start();
	}
	/** Stop the timer. */
	public void stop() {
		timer.stop();
	}
	/**
	 * Set the time delay between calculations.
	 * @param delay the delay in milliseconds
	 */
	public void setDelay(int delay) {
		timer.setDelay(delay);
	}
	/** @return Is the timer running? */
	public boolean isRunning() {
		return timer.isRunning();
	}
	/**
	 * Perform the next step of the production process.
	 * @param player the player
	 * @return need for repaint?
	 */
	public boolean progressProduction(Player player) {
		boolean result = false;
		PlanetStatistics ps = player.getPlanetStatistics();
		for (Map.Entry<ResearchMainCategory, Map<ResearchType, Production>> prs : player.production.entrySet()) {
			int capacity = 0;
			if (prs.getKey() == ResearchMainCategory.SPACESHIPS) {
				capacity = ps.spaceshipActive;
			} else
			if (prs.getKey() == ResearchMainCategory.WEAPONS) {
				capacity = ps.weaponsActive;
			} else
			if (prs.getKey() == ResearchMainCategory.EQUIPMENT) {
				capacity = ps.equipmentActive;
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
	/** @return the current timer delay. */
	public int getDelay() {
		return timer.getDelay();
	}
}
