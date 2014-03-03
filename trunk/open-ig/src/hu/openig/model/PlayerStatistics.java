/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.LongField;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The per player statistics.
 * @author akarnokd, Apr 9, 2011
 */
public class PlayerStatistics {
	/** Total positive money income. */
	public LongField moneyIncome;
	/** The total trade income. */
	public LongField moneyTradeIncome;
	/** The total tax income. */
	public LongField moneyTaxIncome;
	/** The total demolish income. */
	public LongField moneyDemolishIncome;
	/** The income from selling equipment. */
	public LongField moneySellIncome;
	/** Total money spent. */
	public LongField moneySpent;
	/** The money spent on building. */
	public LongField moneyBuilding;
	/** The money spent on repairing. */
	public LongField moneyRepair;
	/** The money spent on research. */
	public LongField moneyResearch;
	/** The money spent on production. */
	public LongField moneyProduction;
	/** The money spent on upgrading. */
	public LongField moneyUpgrade;
	/** The total build count. */
	public LongField buildCount;
	/** The demolish count. */
	public LongField demolishCount;
	/** The sell count. */
	public LongField sellCount;
	/** The research count. */
	public LongField researchCount;
	/** The production count. */
	public LongField productionCount;
	/** The upgrade count. */
	public LongField upgradeCount;
	/** Number of planets owned. */
	public LongField planetsOwned;
	/** Number of planets discovered. */
	public LongField planetsDiscovered;
	/** Number of planets conquered. */
	public LongField planetsConquered;
	/** Number of planets colonized. */
	public LongField planetsColonized;
	/** Number of planets lost to the enemy. */
	public LongField planetsLost;
	/** Planets lost to aliens (non pirates). */
	public LongField planetsLostAlien;
	/** Number of planets lost due revolts. */
	public LongField planetsRevolted;
	/** Number of planets lost due it died out. */
	public LongField planetsDied;
	/** Number of fought space battles. */
	public LongField spaceBattles;
	/** Number of fought ground battles. */
	public LongField groundBattles;
	/** Number of space wins. */
	public LongField spaceWins;
	/** Number of ground wins. */
	public LongField groundWins;
	/** Number of space loses. */
	public LongField spaceLoses;
	/** Number of ground loses. */
	public LongField groundLoses;
	/** Number of space retreats. */
	public LongField spaceRetreats;
	/** Fleets created. */
	public LongField fleetsCreated;
	/** Fleets lost. */
	public LongField fleetsLost;
	/** Number of buildings destroyed during battle. TODO gather */
	public LongField buildingsDestroyed;
	/** The cost of buildings destroyed. TODO gather  */
	public LongField buildingsDestroyedCost;
	/** The count of lost buildings. TODO gather  */
	public LongField buildingsLost;
	/** The cost of lost buildings. TODO gather */
	public LongField buildingsLostCost;
	/** Number of ships destroyed. */
	public LongField shipsDestroyed;
	/** Const of ships destroyed. */
	public LongField shipsDestroyedCost;
	/** Number of ships lost. */
	public LongField shipsLost;
	/** Cost of ships lost. */
	public LongField shipsLostCost;
	/** Number of vehicles destroyed. TODO gather */
	public LongField vehiclesDestroyed;
	/** Cost of vehicles destroyed. *TODO gather */
	public LongField vehiclesDestroyedCost;
	/** Number of vehicles lost. *TODO gather */
	public LongField vehiclesLost;
	/** Cost of vehicles. TODO gather */
	public LongField vehiclesLostCost;
	/** Total buildings. */
	public LongField totalBuilding;
	/** Total working buildings. */
	public LongField totalAvailableBuilding;
	/** The total population. */
	public LongField totalPopulation;
	/** Total available house. */
	public LongField totalAvailableHouse;
	/** Total energy demand. */
	public LongField totalEnergyDemand;
	/** Total worker demand. */
	public LongField totalWorkerDemand;
	/** Total available energy. */
	public LongField totalAvailableEnergy;
	/** Total available food. */
	public LongField totalAvailableFood;
	/** Total available hospital. */
	public LongField totalAvailableHospital;
	/** Total available police. */
	public LongField totalAvailablePolice;
	/** The number of mission chats used. */
	public LongField chats;
	/** The map of fields. */
	public final Map<String, LongField> fields;
	/**
	 * Constructor, initializes the mapping and the fields.
	 */
	public PlayerStatistics() {
		Map<String, LongField> fields = new LinkedHashMap<>();
		for (Field f : getClass().getFields()) {
			if (LongField.class.isAssignableFrom(f.getType())) {
				try {
					LongField lf = new LongField();
					fields.put(f.getName(), lf);
					f.set(this, lf);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Exceptions.add(e);
				}
			}
		}
		this.fields = Collections.unmodifiableMap(fields);
	}
	/**
	 * Assign the values from another player statistics.
	 * @param ps the other player statistics
	 */
	public void assign(PlayerStatistics ps) {
		for (Map.Entry<String, LongField> e : ps.fields.entrySet()) {
			fields.get(e.getKey()).value = e.getValue().value;
		}
		
	}
	/** @return creates a copy of this object */
	public PlayerStatistics copy() {
		PlayerStatistics result = new PlayerStatistics();

		result.assign(this);
		
		return result;
	}
	/**
	 * Save the statistics.
	 * @param target the target XElement
	 */
	public void save(XElement target) {
		for (Map.Entry<String, LongField> e : fields.entrySet()) {
			target.set(e.getKey(), e.getValue().value);
		}
	}
	/**
	 * Load the statistics.
	 * @param source the source
	 */
	public void load(XElement source) {
		for (Map.Entry<String, LongField> e : fields.entrySet()) {
			String s = source.get(e.getKey(), null);
			e.getValue().value = 0;
			if (s != null) {
				try {
					e.getValue().value = Long.parseLong(s);
				} catch (NumberFormatException ex) {
					Exceptions.add(ex);
				}
			}
		}
	}
}
