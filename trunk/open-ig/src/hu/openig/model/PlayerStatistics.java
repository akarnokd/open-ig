/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.lang.reflect.Field;

/**
 * The per player statistics.
 * @author akarnokd, Apr 9, 2011
 */
public class PlayerStatistics {
	/** Total positive money income. */
	public long moneyIncome;
	/** The total trade income. */
	public long moneyTradeIncome;
	/** The total tax income. */
	public long moneyTaxIncome;
	/** The total demolish income. */
	public long moneyDemolishIncome;
	/** The income from selling equipment. */
	public long moneySellIncome;
	/** Total money spent. */
	public long moneySpent;
	/** The money spent on building. */
	public long moneyBuilding;
	/** The money spent on repairing. */
	public long moneyRepair;
	/** The money spent on research. */
	public long moneyResearch;
	/** The money spent on production. */
	public long moneyProduction;
	/** The money spent on upgrading. */
	public long moneyUpgrade;
	/** The total build count. */
	public long buildCount;
	/** The demolish count. */
	public long demolishCount;
	/** The sell count. */
	public long sellCount;
	/** The research count. */
	public long researchCount;
	/** The production count. */
	public long productionCount;
	/** The upgrade count. */
	public long upgradeCount;
	/** Number of planets owned. */
	public long planetsOwned;
	/** Number of planets discovered. */
	public long planetsDiscovered;
	/** Number of planets conquered. */
	public long planetsConquered;
	/** Number of planets colonized. */
	public long planetsColonized;
	/** Number of planets lost to the enemy. */
	public long planetsLost;
	/** Planets lost to aliens (non pirates). */
	public long planetsLostAlien;
	/** Number of planets lost due revolts. */
	public long planetsRevolted;
	/** Number of planets lost due it died out. */
	public long planetsDied;
	/** Number of fought space battles. */
	public long spaceBattles;
	/** Number of fought ground battles. */
	public long groundBattles;
	/** Number of space wins. */
	public long spaceWins;
	/** Number of ground wins. */
	public long groundWins;
	/** Number of space loses. */
	public long spaceLoses;
	/** Number of ground loses. */
	public long groundLoses;
	/** Number of space retreats. */
	public long spaceRetreats;
	/** Fleets created. */
	public long fleetsCreated;
	/** Fleets lost. */
	public long fleetsLost;
	/** Number of buildings destroyed during battle. TODO gather */
	public long buildingsDestroyed;
	/** The cost of buildings destroyed. TODO gather  */
	public long buildingsDestroyedCost;
	/** The count of lost buildings. TODO gather  */
	public long buildingsLost;
	/** The cost of lost buildings. TODO gather */
	public long buildingsLostCost;
	/** Number of ships destroyed. */
	public long shipsDestroyed;
	/** Const of ships destroyed. */
	public long shipsDestroyedCost;
	/** Number of ships lost. */
	public long shipsLost;
	/** Cost of ships lost. */
	public long shipsLostCost;
	/** Number of vehicles destroyed. TODO gather */
	public long vehiclesDestroyed;
	/** Cost of vehicles destroyed. *TODO gather */
	public long vehiclesDestroyedCost;
	/** Number of vehicles lost. *TODO gather */
	public long vehiclesLost;
	/** Cost of vehicles. TODO gather */
	public long vehiclesLostCost;
	/** Total buildings. */
	public long totalBuilding;
	/** Total working buildings. */
	public long totalAvailableBuilding;
	/** The total population. */
	public long totalPopulation;
	/** Total available house. */
	public long totalAvailableHouse;
	/** Total energy demand. */
	public long totalEnergyDemand;
	/** Total worker demand. */
	public long totalWorkerDemand;
	/** Total available energy. */
	public long totalAvailableEnergy;
	/** Total available food. */
	public long totalAvailableFood;
	/** Total available hospital. */
	public long totalAvailableHospital;
	/** Total available police. */
	public long totalAvailablePolice;
	/** The number of mission chats used. */
	public long chats;
	/** @return creates a copy of this object */
	public PlayerStatistics copy() {
		PlayerStatistics result = new PlayerStatistics();
		result.moneyIncome = moneyIncome;
		result.moneyTradeIncome = moneyTradeIncome;
		result.moneyTaxIncome = moneyTaxIncome;
		result.moneyDemolishIncome = moneyDemolishIncome;
		result.moneySellIncome = moneySellIncome;
		result.moneySpent = moneySpent;
		result.moneyBuilding = moneyBuilding;
		result.moneyRepair = moneyRepair;
		result.moneyResearch = moneyResearch;
		result.moneyProduction = moneyProduction;
		result.moneyUpgrade = moneyUpgrade;
		result.buildCount = buildCount;
		result.demolishCount = demolishCount;
		result.sellCount = sellCount;
		result.researchCount = researchCount;
		result.productionCount = productionCount;
		result.upgradeCount = upgradeCount;
		result.planetsOwned = planetsOwned;
		result.planetsDiscovered = planetsDiscovered;
		result.planetsConquered = planetsConquered;
		result.planetsColonized = planetsColonized;
		result.planetsLost = planetsLost;
		result.planetsLostAlien = planetsLostAlien;
		result.planetsRevolted = planetsRevolted;
		result.planetsDied = planetsDied;
		result.spaceBattles = spaceBattles;
		result.groundBattles = groundBattles;
		result.spaceWins = spaceWins;
		result.groundWins = groundWins;
		result.spaceLoses = spaceLoses;
		result.groundLoses = groundLoses;
		result.spaceRetreats = spaceRetreats;
		result.fleetsCreated = fleetsCreated;
		result.fleetsLost = fleetsLost;
		result.buildingsDestroyed = buildingsDestroyed;
		result.shipsDestroyed = shipsDestroyed;
		result.shipsLost = shipsLost;
		result.totalBuilding = totalBuilding;
		result.totalAvailableBuilding = totalAvailableBuilding;
		result.totalPopulation = totalPopulation;
		result.totalAvailableHouse = totalAvailableHouse;
		result.totalEnergyDemand = totalEnergyDemand;
		result.totalWorkerDemand = totalWorkerDemand;
		result.totalAvailableEnergy = totalAvailableEnergy;
		result.totalAvailableFood = totalAvailableFood;
		result.totalAvailableHospital = totalAvailableHospital;
		result.totalAvailablePolice = totalAvailablePolice;
		result.chats = chats;
		
		result.buildingsDestroyed = buildingsDestroyed;
		result.buildingsDestroyedCost = buildingsDestroyedCost;
		result.buildingsLost = buildingsLost;
		result.buildingsLostCost = buildingsLostCost;
		result.shipsDestroyed = shipsDestroyed;
		result.shipsDestroyedCost = shipsDestroyedCost;
		result.shipsLost = shipsLost;
		result.shipsLostCost = shipsLostCost;
		result.vehiclesDestroyed = vehiclesDestroyed;
		result.vehiclesDestroyedCost = vehiclesDestroyedCost;
		result.vehiclesLost = vehiclesLost;
		result.vehiclesLostCost = vehiclesLostCost;
		
		return result;
	}
	/**
	 * Save the statistics.
	 * @param target the target XElement
	 */
	public void save(XElement target) {
		for (Field f : getClass().getFields()) {
			try {
				target.set(f.getName(), f.get(this));
			} catch (IllegalArgumentException e) {
				Exceptions.add(e);
			} catch (IllegalAccessException e) {
				Exceptions.add(e);
			}
		}
	}
	/**
	 * Load the statistics.
	 * @param source the source
	 */
	public void load(XElement source) {
		for (Field f : getClass().getFields()) {
			try {
				String s = source.get(f.getName(), null);
				if (f.getType() == Integer.TYPE) {
					if (s != null) {
						f.set(this, Integer.parseInt(s));
					} else {
						f.set(this, 0);
					}
				} else
				if (f.getType() == Long.TYPE) {
					if (s != null) {
						f.set(this, Long.parseLong(s));
					} else {
						f.set(this, 0L);
					}
				} else
				if (f.getType() == Float.TYPE) {
					if (s != null) {
						f.set(this, Float.parseFloat(s));
					} else {
						f.set(this, 0f);
					}
				} else
				if (f.getType() == Double.TYPE) {
					if (s != null) {
						f.set(this, Double.parseDouble(s));
					} else {
						f.set(this, 0d);
					}
				} else
				if (f.getType() == Boolean.TYPE) {
					if (s != null) {
						f.set(this, "true".equals(s));
					} else {
						f.set(this, false);
					}
				}
			} catch (IllegalArgumentException e) {
				Exceptions.add(e);
			} catch (IllegalAccessException e) {
				Exceptions.add(e);
			}
		}
	}
}
