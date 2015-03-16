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
 * The global world statistics.
 * @author akarnokd, Apr 9, 2011
 */
public class WorldStatistics {
	/** The seconds spent in game. */
	public LongField playTime;
	/** The seconds spent running the game simulation. */ 
	public LongField simulationTime;
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
	/** The map of fields. */
	public final Map<String, LongField> fields;
	/**
	 * Constructor, initializes the mapping and the fields.
	 */
	public WorldStatistics() {
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
	 * Clears all the statistics.
	 */
	public void clear() {
		for (LongField f : fields.values()) {
			f.value = 0;
		}
	}
	/** @return creates a copy of this object */
	public WorldStatistics copy() {
		WorldStatistics result = new WorldStatistics();

		result.assign(this);
		
		return result;
	}
	/**
	 * Assign the values from another world statistics.
	 * @param other the other world statistics
	 */
	public void assign(WorldStatistics other) {
		for (Map.Entry<String, LongField> e : fields.entrySet()) {
			e.getValue().value = other.fields.get(e.getKey()).value;
		}
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
